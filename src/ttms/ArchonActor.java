package ttms;

import battlecode.common.*;

public class ArchonActor extends RobotActor {


    MapLocation central = myLocation;
    boolean reachedCentral = false;
    
    MapLocation nearestBroadcastEnemy;
	MapLocation nearestBroadcastAlly;

    double lastHealth;

    RobotType typeToSpawn;

    int state = 0;
    
    Signal[] signals;
    RobotType myType;

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
    	myType = rc.getType();
    	myLocation = rc.getLocation();
    	signals = rc.emptySignalQueue();
    	
        countNearbyRobots();
        findAverageAlliesPos();
        findNearestHostilePos();
        readBroadcasts();

        //updateState();

        determineSpawnType();
    }
    
    public void readBroadcasts() throws GameActionException {
        nearestBroadcastEnemy = null;
        nearestBroadcastAlly = null;

        int bestEnemyDist = 2000000;
        int bestAllyDist = 2000000;
        
        for(Signal s : signals) {
            if(s.getTeam() != myTeam) {
                continue;
            }
            
            int[] msg = s.getMessage();
            if(msg==null) {
            	MapLocation loc = s.getLocation();
                
                int dist = myLocation.distanceSquaredTo(loc);
                if(dist < bestAllyDist) {
                    bestAllyDist = dist;
                    nearestBroadcastEnemy = new MapLocation(loc.x, loc.y);
                }
            	 
            } else {
            	MapLocation loc = new MapLocation(msg[1]%1000, msg[1]/1000);
                
                int dist = myLocation.distanceSquaredTo(loc);
                if(dist < bestEnemyDist) {
                    bestEnemyDist = dist;
                    nearestBroadcastEnemy = new MapLocation(loc.x, loc.y);
                }
            }
            
            
        }
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
        	if(allyGuardsNum < allyTurretsNum*2) {
        		typeToSpawn = RobotType.GUARD;
        	} else {
        		typeToSpawn = RobotType.TURRET;
        	}
        }
       
    }

    public void act() throws GameActionException {
        setInitialVars();
        broadcastInitialPosition();

        Clock.yield();
        findCentral();

        while(true) {

            updateRoundVars();
            
            if(!reachedCentral && myLocation.distanceSquaredTo(central) <= 16) {
                reachedCentral=true;
            }
            
            if(!reachedCentral) {
                moveToLocationClearIfStuck(central);
            } else {
            	
            	move();
                
            }

            Clock.yield();
        }
    }
    
    public void move() throws GameActionException {
        repairAdjacentRobots();
        
        if(hostilesNearby() /*&& rc.getHealth() <=500*/) {
        	Direction dir = myLocation.directionTo(nearestHostilePos);
        	int bestDist = myLocation.add(dir).distanceSquaredTo(averageAlliesPos);
        	
        	int dist = myLocation.add(dir.rotateRight()).distanceSquaredTo(averageAlliesPos);
        	if(dist > bestDist) {
        		dir = dir.rotateRight();
        		bestDist = dist;
        	}
        	dist = myLocation.add(dir.rotateLeft().rotateLeft()).distanceSquaredTo(averageAlliesPos);
        	if(dist > bestDist) {
        		dir = dir.rotateLeft().rotateLeft();
        		bestDist = dist;
        	}
            moveFromLocationClearIfStuck(nearestHostilePos);
            return;
        }
        
        if(rc.hasBuildRequirements(typeToSpawn)) {
            buildActions();
        } else if(!repairAllies()){
            if(nearestBroadcastEnemy!=null) {
            	moveToLocationClearIfStuck(nearestBroadcastEnemy);
            } else if(nearestBroadcastAlly!=null) {
            	moveToLocationClearIfStuck(nearestBroadcastAlly);
            } else {
            	moveToLocationClearIfStuck(averageAlliesPos);
            }
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

    public boolean repairAllies() throws GameActionException {
    	
        MapLocation target = null;
        int bestDist = 9999999;
        for(RobotInfo r : alliesInfo) {
            if(r.health < r.maxHealth && r.type!=RobotType.ARCHON) {
                int dist = myLocation.distanceSquaredTo(r.location);

                if(dist < bestDist) {
                    target = new MapLocation(r.location.x, r.location.y);
                    bestDist = dist;
                }
            }
        }

        if(target!=null ) {
            rc.setIndicatorString(0, "REPAIRING"+target.x+", "+target.y);
            moveToLocationClearIfStuck(target);
            return(true);
        } else {
            return(false);
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

        boolean spawned = false;

        MapLocation spawnLocation = closestSpawnableTile();
        Direction spawnDir = myLocation.directionTo(spawnLocation);
        if(spawnLocation==null) {
            return;
        }

        if(hasRequirements && myLocation.add(spawnDir).equals(spawnLocation)) {

            if(spawnUnit(typeToSpawn, spawnDir)) {
                spawned = true;
            }
        }

        if(!rc.isCoreReady()) {
            return;
        }
        
        if(!spawned) {
            moveToLocationClearIfStuck(spawnLocation);
        }
    }

    //odd tiles only
    public MapLocation closestSpawnableTile() throws GameActionException {
        MapLocation bestLoc = null;
        int bestDist = 999999999;

        for(int x=-6; x<=6; x++) {
            for(int y=-6; y<=6; y++) {
                
                MapLocation loc = new MapLocation(myLocation.x+x, myLocation.y+y);

                if(rc.canSenseLocation(loc) && rc.onTheMap(loc) && rc.senseRobotAtLocation(loc)==null && rc.senseRubble(loc)<=50.0) {
//                    if( (loc.y/2+loc.x)%3==0 && loc.y%2==0) {
                        //int dist = averageAlliesPos.distanceSquaredTo(loc);
                        int dist = myLocation.distanceSquaredTo(loc);
                		if(dist < bestDist) {
                            bestDist=dist;
                            bestLoc=new MapLocation(loc.x, loc.y);
                        }
//                    }
                    
                }

            }
        }

        //if(!bestLoc.equals(myLocation)) {
        //    System.out.println("  "+bestLoc.x+","+bestLoc.y+" | "+((bestLoc.x+bestLoc.y)%2==1));
        //}

        if(bestLoc!=null) {
            rc.setIndicatorString(1, ""+((bestLoc.y+bestLoc.x)%3==1));
        }

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