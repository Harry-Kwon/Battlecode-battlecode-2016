package turretbot;

import battlecode.common.*;

public class ArchonActor extends RobotActor {


    public ArchonActor(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void act() throws GameActionException {
        rc.setIndicatorString(0, "ARCHON ACTOR");
        myTeam = rc.getTeam();

        while(true) {

            myLocation = rc.getLocation();
            rc.setIndicatorString(1, "TEAM PARTS: " + rc.getTeamParts());

            countNearbyRobots();
            findNearestHostilePos();
            findAverageAlliesPos();

            if(rc.isCoreReady()) {
                if(enemiesNum+zombiesNum >0) {
                    moveFromLocationClearIfStuck(nearestHostilePos);
                } else  {
                    boolean hasSpawnableTiles = false;
                    boolean spawned = false;
                    Direction dir = Direction.NORTH_EAST;
                    if((myLocation.x+myLocation.y)%2==0) {
                        dir = Direction.NORTH;
                    }

                    for(int i=0; i<4; i++) {
                        if(rc.canMove(dir)) {
                            hasSpawnableTiles=true;
                        }
                        if(spawnUnit(RobotType.TURRET, dir)) {
                            spawned = true;
                            break;
                        }
                        
                        dir=dir.rotateRight().rotateRight();
                    }

                    if(!hasSpawnableTiles) {
                        MapLocation target = closestSpawnableTile();
                        //System.out.println(". "+target.x+", " +target.y);
                        rc.setIndicatorString(2, "CLOSEST SPAWNABLE TILE: "+target.x+", " +target.y);
                        moveToLocationClearIfStuck(target);
                    }
                    
                }
            }
            

            Clock.yield();
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