package ttms;

import battlecode.common.*;

public class RobotActor {

    RobotController rc;
    MapLocation myLocation;
    Team myTeam;
    MapLocation lastLocation;

    public RobotActor(RobotController rc) throws GameActionException {
        this.rc = rc;
    }

    public void act() throws GameActionException {
        while(true) {
            rc.setIndicatorString(0, "NO ACTOR");
        }
    }

    public void attack(MapLocation loc) throws GameActionException {
        if(rc.canAttackLocation(loc) && rc.isWeaponReady()) {
            rc.attackLocation(loc);
        }

    }

    /******sensing*********/

    int enemiesNum;
    int zombiesNum;
    int alliesNum;
    MapLocation[] enemiesPos;
    MapLocation[] zombiesPos;
    MapLocation[] alliesPos;

    int allyTurretsNum;
    int allyScoutsNum;
    int allyGuardsNum;

    int nearestHostileDist;
    MapLocation nearestHostilePos;
    
    int nearestDenDist;
    MapLocation nearestDenPos;

    MapLocation averageAlliesPos;

    public void findAverageAlliesPos() throws GameActionException {
        if(alliesNum==0) {
            averageAlliesPos = myLocation;
            return;
        }

        int cX=0;
        int cY=0;

        for(MapLocation loc : alliesPos) {
            cX+=loc.x;
            cY+=loc.y;
        }

        averageAlliesPos = new MapLocation(cX/alliesNum, cY/alliesNum);
    }
    
    MapLocation averageAlliesNoScouts;
    
    public void findAverageAlliesNoScouts() throws GameActionException {
    	int cX = myLocation.x;
    	int cY = myLocation.y;
    	int points = 1;
    	for(RobotInfo info : alliesInfo) {
    		if(info.type!=RobotType.SCOUT) {
    			cX+=info.location.x;
    			cY+=info.location.y;
    			points++;
    		}
    	}
    	
    	averageAlliesNoScouts = new MapLocation(cX/points, cY/points);
    	
    	
    }

    public void findNearestHostilePos() throws GameActionException {
        nearestHostileDist = 999999999;
        nearestHostilePos = null;
        
        nearestDenDist = 999999999;
        nearestDenPos = null;
        
        for(RobotInfo info : enemiesInfo) {
        	MapLocation loc = info.location;
        	int dist = myLocation.distanceSquaredTo(loc);
        	if(info.type != RobotType.ZOMBIEDEN) {
                if(dist < nearestHostileDist) {
                    nearestHostileDist = dist;
                    nearestHostilePos = new MapLocation(loc.x, loc.y);
                }
        	} else {
        		if(dist < nearestDenDist) {
        			nearestDenDist = dist;
        			nearestDenPos = new MapLocation(loc.x, loc.y);
        		}
        	}
        }
        
        for(RobotInfo info : zombiesInfo) {
        	MapLocation loc = info.location;
        	int dist = myLocation.distanceSquaredTo(loc);
        	if(info.type != RobotType.ZOMBIEDEN) {
                if(dist < nearestHostileDist) {
                    nearestHostileDist = dist;
                    nearestHostilePos = new MapLocation(loc.x, loc.y);
                }
        	} else {
        		if(dist < nearestDenDist) {
        			nearestDenDist = dist;
        			nearestDenPos = new MapLocation(loc.x, loc.y);
        		}
        	}
        }
    }

    RobotInfo[] alliesInfo;
    RobotInfo[] zombiesInfo;
    RobotInfo[] enemiesInfo;

    public void countNearbyRobots() throws GameActionException {

        enemiesInfo = rc.senseNearbyRobots(53, myTeam.opponent());
        enemiesNum = enemiesInfo.length;
        enemiesPos = new MapLocation[enemiesNum];
        for(int i=0; i<enemiesNum; i++) {
            enemiesPos[i] = enemiesInfo[i].location;
        }

        zombiesInfo = rc.senseNearbyRobots(53, Team.ZOMBIE);
        zombiesNum = zombiesInfo.length;
        zombiesPos = new MapLocation[zombiesNum];
        for(int i=0; i<zombiesNum; i++) {
            zombiesPos[i] = zombiesInfo[i].location;
        }

        alliesInfo = rc.senseNearbyRobots(53, myTeam);
        alliesNum = alliesInfo.length;
        alliesPos = new MapLocation[alliesNum];

        allyTurretsNum = 0;
        allyScoutsNum = 0;
        allyGuardsNum = 0;
        for(int i=0; i<alliesNum; i++) {
            alliesPos[i] = alliesInfo[i].location;
            switch(alliesInfo[i].type) {
                case SCOUT:
                    allyScoutsNum++;
                    break;
                case TURRET:
                    allyTurretsNum++;
                    break;
                case GUARD:
                	allyGuardsNum++;
                	break;
                default:
                    break;
            }
        }

        if(rc.getType()==RobotType.ARCHON) {
            allyScoutsNum=0;
            for(int i=0; i<alliesNum; i++) {
                if(alliesInfo[i].type==RobotType.SCOUT) {
                    if(myLocation.distanceSquaredTo(alliesPos[i]) <= 25) {
                        allyScoutsNum++;
                    }
                }
            }
        }
    }

    MapLocation nearestTurretPos;
    int nearestTurretDist;

    public void findNearestTurret() {
        nearestTurretDist=9999999;
        nearestTurretPos=null;
        for(RobotInfo info : alliesInfo) {
            if(info.type == RobotType.TURRET) {
                int dist = myLocation.distanceSquaredTo(info.location);
                if(dist < nearestTurretDist) {
                    nearestTurretDist = dist;
                    nearestTurretPos = new MapLocation(info.location.x, info.location.y);
                }
            }
        }
    }
    
    MapLocation farthestTurretPos;
    int farthestTurretDist;

    public void findFarthestTurret() {
    	farthestTurretDist=9999999;
    	farthestTurretPos=null;
        for(RobotInfo info : alliesInfo) {
            if(info.type == RobotType.TURRET) {
                int dist = myLocation.distanceSquaredTo(info.location);
                if(dist > farthestTurretDist) {
                	farthestTurretDist = dist;
                	farthestTurretPos = new MapLocation(info.location.x, info.location.y);
                }
            }
        }
    }

    MapLocation nearestScoutPos;
    int nearestScoutDist;

    public void findNearestScout() {
        nearestScoutDist=9999999;
        nearestScoutPos=null;
        for(RobotInfo info : alliesInfo) {
            if(info.type == RobotType.SCOUT) {
                int dist = myLocation.distanceSquaredTo(info.location);
                if(dist < nearestScoutDist) {
                    nearestScoutDist = dist;
                    nearestScoutPos = new MapLocation(info.location.x, info.location.y);
                }
            }
        }
    }
    
    MapLocation nearestAllyPos;
    int nearestAllyDist;
    
    public void findNearestAlly() {
        nearestAllyDist=9999999;
        nearestAllyPos=null;
        for(RobotInfo info : alliesInfo) {
            int dist = myLocation.distanceSquaredTo(info.location);
            if(dist < nearestAllyDist) {
                nearestAllyDist = dist;
                nearestAllyPos = new MapLocation(info.location.x, info.location.y);
            }
        }
    }

    /*****navigation*******/

    Direction lastDirection = null;
    Direction directionBias = Direction.NORTH;

    public void moveInOppLastDirection() throws GameActionException {
        if(!rc.isCoreReady()) {
            return;
        }

        Direction dir = lastDirection.opposite();

        String test = "";

        for(int i=0; i<8;i++) {
            Direction thisDir = nextDir(dir, i);
            test = test + thisDir.toString();
            rc.setIndicatorString(2, ""+test);
            
            if(rc.canMove(thisDir)) {
                rc.move(thisDir);
                lastDirection = thisDir;
                return;
            }
        }

    }


    public void moveFromLocationClear(MapLocation target) throws GameActionException {
        Direction dir = myLocation.directionTo(target).opposite();

        if(dir == Direction.OMNI) {
            dir = directionBias;
        }

        moveInDirectionClear(dir);
    }

    public void moveToLocationClear(MapLocation target) throws GameActionException {
        Direction dir = myLocation.directionTo(target);

        if(dir == Direction.OMNI) {
            dir = directionBias;
        }

        moveInDirectionClear(dir);
    }
    
    public void moveFromLocation(MapLocation target) throws GameActionException {
        Direction dir = myLocation.directionTo(target).opposite();

        if(dir == Direction.OMNI) {
            dir = directionBias;
        }

        moveInDirection(dir);
    }

    public void moveToLocation(MapLocation target) throws GameActionException {
        Direction dir = myLocation.directionTo(target);

        if(dir == Direction.OMNI) {
            dir = directionBias;
        }

        moveInDirection(dir);
    }

    public void moveFromLocationClearIfStuck(MapLocation target) throws GameActionException {
        Direction dir = myLocation.directionTo(target).opposite();

        if(dir == Direction.OMNI) {
            dir = directionBias;
        }

        moveInDirectionClearIfStuck(dir);
    }

    public void moveToLocationClearIfStuck(MapLocation target) throws GameActionException {
        Direction dir = myLocation.directionTo(target);

        if(dir == Direction.OMNI) {
            dir = directionBias;
        }

        moveInDirectionClearIfStuck(dir);
    }
    
    public void moveInDirectionClear(Direction d) throws GameActionException {
    	if(!rc.isCoreReady()) {
            return;
        }

        //not sure if Direction is passed by reference
        Direction dir = d;
        for(int i=0; i<8;i++) {
            Direction thisDir = nextDir(dir, i);
            MapLocation loc = myLocation.add(thisDir);
            
            if(!rc.onTheMap(myLocation.add(thisDir))) {
        		directionBias = directionBias.opposite();
        	}

            if(rc.senseRobotAtLocation(loc)==null && rc.onTheMap(loc)) {
            	if(rc.senseRubble((loc)) < 50) {
            		if(rc.canMove(thisDir)) {
            			rc.move(thisDir);
                        lastDirection = thisDir;
                        return;
            		}
            	} else {
            		rc.clearRubble(thisDir);
            		return;
            	}
                
            }
        }
    }
    
    public void moveInDirectionClearIfStuck(Direction d) throws GameActionException {

        if(!rc.isCoreReady()) {
            return;
        }

        boolean moved = false;

        //not sure if Direction is passed by reference
        Direction dir = d;
        for(int i=0; i<5;i++) {
            Direction thisDir = nextDir(dir, i);
            
            if(!rc.onTheMap(myLocation.add(thisDir))) {
        		directionBias = directionBias.opposite();
        	}

            if(safeToMove(thisDir)) {
                rc.move(thisDir);
                moved = true;
                lastDirection = thisDir;
                return;
            }
        }

        if(!moved) {
            for(int i=0; i<5; i++) {
            	Direction thisDir = nextDir(dir, i);
                if(rc.senseRobotAtLocation(myLocation.add(thisDir))==null && rc.senseRubble(myLocation.add(thisDir)) > 50.0) {
                    rc.clearRubble(thisDir);
                    return;
                }
            }
        }
    }

    public Direction nextDir(Direction forward, int i) {
        Direction dir;
        switch(i%8) {
            case 0:
                dir = forward;
                break;
            case 1:
                dir=forward.rotateRight();
                break;
            case 2:
                dir = forward.rotateLeft();
                break;
            case 3:
                dir = forward.rotateRight().rotateRight();
                break;
            case 4:
                dir = forward.rotateLeft().rotateLeft();
                break;
            case 5:
                dir = forward.opposite().rotateLeft();
                break;
            case 6:
                dir = forward.opposite().rotateRight();
                break;
            case 7:
                dir = forward.opposite();
                break;
            default:
                dir = null;
                break;
        }
        return(dir);
    }

    public void moveInDirection(Direction d) throws GameActionException {
        if(!rc.isCoreReady()) {
            return;
        }

        //not sure if Direction is passed by reference
        Direction dir = d;
       for(int i=0; i<8;i++) {
            Direction thisDir = nextDir(dir, i);
            
            if(!rc.onTheMap(myLocation.add(thisDir))) {
        		directionBias = directionBias.opposite();
        	}

            if(safeToMove(thisDir)) {
                rc.move(thisDir);
                return;
            }
        }
    }

    public void moveInSomeDirection() throws GameActionException {
        Direction dir = directionBias;
        for(int i=0; i<8;i++) {
        	
        	if(!rc.onTheMap(myLocation.add(dir))) {
        		directionBias = directionBias.opposite();
        	}
        	
            if(safeToMove(dir)) {
                rc.move(dir);
                break;
            }
            dir = dir.rotateRight();
        }
    }

    public boolean safeToMove(Direction dir) throws GameActionException {

        if(!rc.canMove(dir)) {
            return false;
        }

        MapLocation target = myLocation.add(dir);

        if(rc.senseRubble(target) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH) {
            return false;
        }

        if(dir.opposite() == lastDirection) {
            return false;
        }

        return true;
    }
}