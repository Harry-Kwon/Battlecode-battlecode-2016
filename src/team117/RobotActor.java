package team117;

import java.util.ArrayList;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;

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
    int allyTTMNum;
    int allyScoutsNum;
    int allySoldiersNum;
    int allyGuardsNum;
    int allyVipersNum;

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

        enemiesInfo = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, myTeam.opponent());
        enemiesNum = enemiesInfo.length;
        enemiesPos = new MapLocation[enemiesNum];
        for(int i=0; i<enemiesNum; i++) {
            enemiesPos[i] = enemiesInfo[i].location;
        }

        zombiesInfo = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, Team.ZOMBIE);
        zombiesNum = zombiesInfo.length;
        zombiesPos = new MapLocation[zombiesNum];
        for(int i=0; i<zombiesNum; i++) {
            zombiesPos[i] = zombiesInfo[i].location;
        }

        alliesInfo = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, myTeam);
        alliesNum = alliesInfo.length;
        alliesPos = new MapLocation[alliesNum];

        allyTurretsNum = 0;
        allyTTMNum=0;
        allyScoutsNum = 0;
        allySoldiersNum = 0;
        allyGuardsNum = 0;
        allyVipersNum = 0;
        for(int i=0; i<alliesNum; i++) {
            alliesPos[i] = alliesInfo[i].location;
            switch(alliesInfo[i].type) {
                case SCOUT:
                    allyScoutsNum++;
                    break;
                case TURRET:
                    allyTurretsNum++;
                    break;
                case TTM:
                	allyTTMNum++;
                	break;
                case GUARD:
                	allyGuardsNum++;
                	break;
                case VIPER:
                	allyVipersNum++;
                	break;
                case SOLDIER:
                	allySoldiersNum++;
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
    
    MapLocation nearestGuardPos;
    int nearestGuardDist;

    public void findNearestGuard() {
    	nearestGuardDist=9999999;
        nearestGuardPos=null;
        for(RobotInfo info : alliesInfo) {
            if(info.type == RobotType.GUARD) {
                int dist = myLocation.distanceSquaredTo(info.location);
                if(dist < nearestTurretDist) {
                	nearestGuardDist = dist;
                	nearestGuardPos = new MapLocation(info.location.x, info.location.y);
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
        if(lastDirection==null){
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
    
    public void moveFromLocationClearIfStuckBack(MapLocation target) throws GameActionException {
        Direction dir = myLocation.directionTo(target).opposite();

        if(dir == Direction.OMNI) {
            dir = directionBias;
        }

        moveInDirectionClearIfStuckBack(dir);
    }

    public void moveToLocationClearIfStuckBack(MapLocation target) throws GameActionException {
        Direction dir = myLocation.directionTo(target);

        if(dir == Direction.OMNI) {
            dir = directionBias;
        }

        moveInDirectionClearIfStuckBack(dir);
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
            
            if(thisDir.opposite()==lastDirection) {
            	continue;
            }
            
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
            	} else if(rc.senseRubble(loc) < 300){
            		rc.clearRubble(thisDir);
            		return;
            	}
                
            }
        }
    }
    
    public void moveInDirectionClearIfStuckBack(Direction d) throws GameActionException {

        if(!rc.isCoreReady()) {
            return;
        }

        boolean moved = false;

        //not sure if Direction is passed by reference
        Direction dir = d;
        for(int i=0; i<8;i++) {
            Direction thisDir = nextDir(dir, i);
            
            if(thisDir.opposite()==lastDirection) {
            	continue;
            }
            
            if(!rc.onTheMap(myLocation.add(thisDir))) {
        		directionBias = directionBias.opposite();
        	}
            
            if(rc.canMove(thisDir)) {
            	try{
            		rc.move(thisDir);
            		moved = true;
            		lastDirection = thisDir;
            	}
            	catch(Exception e){e.printStackTrace();}
                return;
            }
        }

        if(!moved) {
            for(int i=0; i<8; i++) {
            	Direction thisDir = nextDir(dir, i);
                if(rc.senseRobotAtLocation(myLocation.add(thisDir))==null && rc.senseRubble(myLocation.add(thisDir)) > 50.0 && rc.senseRubble(myLocation.add(thisDir)) < 300) {
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
            
            if(thisDir.opposite()==lastDirection) {
            	continue;
            }
            
            if(!rc.onTheMap(myLocation.add(thisDir))) {
        		directionBias = directionBias.opposite();
        	}
            
            if(rc.canMove(thisDir)) {
            	try{
            		rc.move(thisDir);
            		moved = true;
            		lastDirection = thisDir;
            	}
            	catch(Exception e){e.printStackTrace();}
                return;
            }
        }

        if(!moved) {
            for(int i=0; i<5; i++) {
            	Direction thisDir = nextDir(dir, i);
                if(rc.senseRobotAtLocation(myLocation.add(thisDir))==null && rc.senseRubble(myLocation.add(thisDir)) > 50.0  && rc.senseRubble(myLocation.add(thisDir)) < 300) {
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
            
            if(thisDir.opposite()==lastDirection) {
            	continue;
            }
            
            if(!rc.onTheMap(myLocation.add(thisDir))) {
        		directionBias = directionBias.opposite();
        	}

            if(rc.canMove(thisDir)) {
            	try{
            		rc.move(thisDir);
            		lastDirection = thisDir;
            	}
            	catch(Exception e){e.printStackTrace();}
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
    
    public void tryBFSMoveClear(MapLocation target) throws GameActionException {
    	Direction moveDir = getBFSDirectionTo(target);
    	if(moveDir==Direction.NONE) {
    		moveToLocationClear(target);
    	} else {
    		moveInDirectionClearIfStuck(moveDir);
    	}
    }
    
    public void tryBFSMove(MapLocation target) throws GameActionException {
    	Direction moveDir = getBFSDirectionTo(target);
    	if(moveDir == Direction.NONE) {
    		moveToLocation(target);
    	} else {
    		moveInDirection(moveDir);
    	}
    }
    
    public void tryBFSMoveClearIfStuckBack(MapLocation target) throws GameActionException {
    	Direction moveDir = getBFSDirectionTo(target);
    	if(moveDir == Direction.NONE) {
    		moveToLocationClearIfStuckBack(target);
    	} else {
    		moveInDirectionClearIfStuckBack(moveDir);
    	}
    }

    
    public void tryBFSMoveClearIfStuck(MapLocation target) throws GameActionException {
    	Direction moveDir = getBFSDirectionTo(target);
    	if(moveDir == Direction.NONE) {
    		moveToLocationClearIfStuck(target);
    	} else {
    		moveInDirectionClearIfStuck(moveDir);
    	}
    }
    
    public Direction getBFSDirectionTo(MapLocation target) throws GameActionException {
    	if(!rc.canSense(target)) {
    		MapLocation newTarget = new MapLocation(myLocation.x, myLocation.y);
    		Direction dir= myLocation.directionTo(target);
    		
    		while(rc.canSense(newTarget.add(dir))) {
    			newTarget = newTarget.add(dir);
    		}
    		
    		return(getBFSDirectionTo(newTarget));
    	}
    	
    	Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
                Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
    	
    	int myX = myLocation.x;
    	int myY = myLocation.y;
    	boolean[][] traveled = new boolean[21][21];
    	ArrayList<MapLocation> border = new ArrayList<MapLocation>(0);
    	
    	border.add(target);
    	traveled[target.x-myX+10][target.y-myY+10] =true;
    	
    	while(border.size()>0) {
    		MapLocation tile = border.get(0);
    		border.remove(0);
    		
    		for(Direction dir : directions) {
    			MapLocation loc = tile.add(dir);
    			
    			if(loc.equals(myLocation)) {
    				return(dir.opposite());
    			} else {
    				int relX = loc.x-myX+10;
    				int relY = loc.y-myY+10;
    				if(!traveled[relX][relY] && rc.canSense(loc) && rc.onTheMap(loc) && !rc.isLocationOccupied(loc) && rc.senseRubble(loc) <50.0) {
    					border.add(new MapLocation(loc.x, loc.y));
    					traveled[relX][relY]=true;
    				}
    			}
    		}
    		
    	}
    	
    	return(Direction.NONE);
    }
    
}