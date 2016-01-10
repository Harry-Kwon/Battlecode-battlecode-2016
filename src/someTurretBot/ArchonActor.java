package someTurretBot;

import battlecode.common.*;

public class ArchonActor extends RobotActor {


    MapLocation central = myLocation;
    boolean reachedCentral = false;

    double lastHealth;

    RobotType typeToSpawn;

    int state = 0;

    public ArchonActor(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void setInitialVars() {
        myLocation = rc.getLocation();
        myTeam = rc.getTeam();
        lastHealth = rc.getHealth();
    }

    public void broadcastInitialPosition() throws GameActionException {
        rc.broadcastMessageSignal(-2, myLocation.x+1000*myLocation.y, 20000);
    }

    public void findCentral() throws GameActionException {
        int archons = 1;
        MapLocation[] archonPositions = new MapLocation[6];
        archonPositions[0] = myLocation;

        Signal[] signals = rc.emptySignalQueue();
        for(Signal s : signals) {
            int[] msg = s.getMessage();
            if(s.getTeam() != myTeam || msg==null||msg[0]!=-2) {
                continue;
            }
            int x = msg[1]%1000;
            int y = msg[1]/1000;
            archonPositions[archons++] = new MapLocation(x, y);
        }

        int bestArchon = 0;
        int bestScore = 9999999;
        for(int i=0; i<archons; i++) {
            int score = 0;
            for(int j=0; j<archons; j++) {
                score += archonPositions[i].distanceSquaredTo(archonPositions[j]);
            }

            if(score <= bestScore) {
                if(score==bestScore) {
                    if(archonPositions[i].x+1000*archonPositions[i].y > archonPositions[bestArchon].x+1000*archonPositions[bestArchon].y) {
                        continue;
                    }
                }
                bestScore = score;
                bestArchon = i;
            }
        }

        central = archonPositions[bestArchon];
    }

    public void updateRoundVars() throws GameActionException {
        myLocation = rc.getLocation();
        countNearbyRobots();
        findNearestHostilePos();
        findAverageAlliesPos();

        //updateState();

        determineSpawnType();
    }

    public void updateState() throws GameActionException {
        /*if(allyTurretsNum>40) {
            state = 1;
        } else {
            state = 0;
        }*/
        state = 0;
    }

    public void determineSpawnType() {
        //set type to spawn

        if(allyScoutsNum==0) {
            typeToSpawn = RobotType.SCOUT;
        } else {
            typeToSpawn = RobotType.TURRET;
        }
       
    }

    public void act() throws GameActionException {
        setInitialVars();
        broadcastInitialPosition();

        Clock.yield();
        findCentral();

        while(true) {

            updateRoundVars();
            
            if(!reachedCentral && myLocation.distanceSquaredTo(central) <= 3) {
                reachedCentral=true;
            }

            if(hostilesNearby() && rc.getHealth() <=500) {
                //Direction dir = nearestHostilePos.directionTo(central);
                //MapLocation target = central.add(dir, 6);
                moveToLocationClearIfStuck(central);
            }

            if(!reachedCentral) {
                moveToLocationClearIfStuck(central);
            } else {
                //checkHealth();
                repairAdjacentRobots();
                findNearestTurret();
                
                if(rc.hasBuildRequirements(typeToSpawn)) {
                    if(nearestTurretDist <=3) {
                        buildActions();
                    } else {
                        if(nearestTurretPos!=null) {
                            moveToLocationClearIfStuck(nearestTurretPos);
                        } else {
                            buildActions();
                        }
                    }
                    
                } else {
                    repairAllies();
                }
                
            }

            Clock.yield();
        }
    }

    public void checkHealth() throws GameActionException {

        double health = rc.getHealth();
        if(health < lastHealth) {
            rc.setIndicatorString(1, "GET BACK"+lastDirection);
            moveInOppLastDirection();
        } 
        lastHealth = health;
    }

    public void repairAllies() throws GameActionException {
        MapLocation target = null;
        int bestDist = 9999999;
        for(RobotInfo r : alliesInfo) {
            if(r.health < r.maxHealth && r.type!=RobotType.ARCHON) {
                int dist = myLocation.distanceSquaredTo(r.location);

                if(central.distanceSquaredTo(r.location)>144) {
                //    continue;
                }

                if(dist < bestDist) {
                    target = new MapLocation(r.location.x, r.location.y);
                    bestDist = dist;
                }
            }
        }

        if(target!=null ) {
            rc.setIndicatorString(0, "REPAIRING"+target.x+", "+target.y);
            moveToLocationClearIfStuck(target);
        } else {
            moveToLocationClearIfStuck(central);
        }
    }

    public void repairAdjacentRobots() throws GameActionException {
        Direction dir = Direction.NORTH;

        for(int i=0; i<8; i++) {
            MapLocation loc = myLocation.add(dir);
            RobotInfo info = rc.senseRobotAtLocation(loc);
            if(info != null && info.team==myTeam && info.type!=RobotType.ARCHON) {
                if(info.health < info.maxHealth) {
                    rc.repair(loc);
                    break;
                }
            }
            dir=dir.rotateRight();
        }
    }

    public boolean hostilesNearby() {
        if(enemiesNum+zombiesNum>0 && nearestHostileDist<=9) {
            return true;
        }
        return false;
    }

    public void buildActions() throws GameActionException {

        //check requirements
        boolean hasRequirements = rc.hasBuildRequirements(typeToSpawn);

        boolean hasSpawnableUnoccupiedTiles = false;
        boolean spawned = false;

        if(hasRequirements) {

            Direction dir = Direction.NORTH_EAST;
            if((myLocation.x+myLocation.y)%2==0) {
                dir = Direction.NORTH;
            }
 
            for(int i=0; i<4; i++) {
                if(rc.senseRobotAtLocation(myLocation.add(dir))==null && rc.onTheMap(myLocation.add(dir))) {
                    hasSpawnableUnoccupiedTiles=true;
                }

                if(spawnUnit(typeToSpawn, dir)) {
                    spawned = true;
                    break;
                }
                
                dir=dir.rotateRight().rotateRight();
            }
        }

        if(!rc.isCoreReady()) {
            return;
        }
        
        if(!spawned) {
            if(hasSpawnableUnoccupiedTiles) {
                //System.out.println("attempting to clear");
                //clear rubble from unoccupied tiles
                Direction dir = Direction.NORTH_EAST;
                if((myLocation.x+myLocation.y)%2==0) {
                    dir = Direction.NORTH;
                }
                for(int i=0; i<4; i++) {
                    MapLocation loc = myLocation.add(dir);
                    if(rc.senseRobotAtLocation(loc)==null && rc.senseRubble(loc) >50 && rc.onTheMap(loc)) {
                        rc.clearRubble(dir);
                        break;
                    }
                    dir=dir.rotateRight().rotateRight();
                }
            } else {
                MapLocation target = closestSpawnableTile();
                moveToLocationClearIfStuck(target);
            }
        }
    }

    //odd tiles only
    public MapLocation closestSpawnableTile() throws GameActionException {
        MapLocation bestLoc = myLocation;
        int bestDist = 999999999;

        for(int x=-6; x<=6; x++) {
            for(int y=-6; y<=6; y++) {
                
                MapLocation loc = new MapLocation(myLocation.x+x, myLocation.y+y);

                if(rc.canSenseLocation(loc) && rc.onTheMap(loc) && rc.senseRobotAtLocation(loc)==null && rc.senseRubble(loc)<=50.0 && (loc.x+loc.y)%2==1) {
                    int dist = averageAlliesPos.distanceSquaredTo(loc);
                    if(dist < bestDist) {
                        bestDist=dist;
                        bestLoc=loc;
                    }
                }

            }
        }

        //if(!bestLoc.equals(myLocation)) {
        //    System.out.println("  "+bestLoc.x+","+bestLoc.y+" | "+((bestLoc.x+bestLoc.y)%2==1));
        //}

        return(bestLoc);

    }

    public boolean spawnUnit(RobotType type, Direction dir) throws GameActionException {
        if(!rc.isCoreReady()) {
            return false;
        }

        if(rc.hasBuildRequirements(type)) {
            if(rc.canBuild(dir, type)) {
                rc.build(dir, type);
                return true;
            }
        }
        return false;
    }

    public boolean spawnUnit(RobotType type) throws GameActionException {
        if(rc.hasBuildRequirements(type)) {
            Direction dir = Direction.NORTH;
            for(int i=0; i<8; i++) {
                if(rc.canBuild(dir, type)) {
                    rc.build(dir, type);
                    return true;
                }
                dir = dir.rotateRight();
            }
        }
        return false;
    }
}