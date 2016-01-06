package team117;

import battlecode.common.*;

public class RobotActor {

    RobotController rc;
    MapLocation myLocation;
    Team myTeam;

    public RobotActor(RobotController rc) throws GameActionException {
        this.rc = rc;
    }

    public void act() throws GameActionException {
        while(true) {
            rc.setIndicatorString(0, "NO ACTOR");
        }
    }

    public void attack(MapLocation loc) throws GameActionException {
        if(!rc.isWeaponReady()) {
            return;
        }
        if(rc.canAttackLocation(loc)) {
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

    int nearestHostileDist;
    MapLocation nearestHostilePos;

    MapLocation averageAlliesPos;

    public void findAverageAlliesPos() throws GameActionException {
        int cX=0;
        int cY=0;

        for(MapLocation loc : alliesPos) {
            cX+=loc.x;
            cY+=loc.y;
        }

        averageAlliesPos = new MapLocation(cX, cY);
    }

    public void findNearestHostilePos() throws GameActionException {
        nearestHostileDist = 999999999;
        nearestHostilePos = myLocation;
        for(MapLocation loc : enemiesPos) {
            int dist = myLocation.distanceSquaredTo(loc);
            if(dist < nearestHostileDist) {
                nearestHostileDist = dist;
                nearestHostilePos = new MapLocation(loc.x, loc.y);
            }
        }

        for(MapLocation loc : zombiesPos) {
            int dist = myLocation.distanceSquaredTo(loc);
            if(dist < nearestHostileDist) {
                nearestHostileDist = dist;
                nearestHostilePos = new MapLocation(loc.x, loc.y);
            }
        }
    }

    public void countNearbyRobots() throws GameActionException {
        RobotInfo[] enemiesInfo = rc.senseNearbyRobots(24, myTeam.opponent());
        enemiesNum = enemiesInfo.length;
        enemiesPos = new MapLocation[enemiesNum];
        for(int i=0; i<enemiesNum; i++) {
            enemiesPos[i] = enemiesInfo[i].location;
        }

        RobotInfo[] zombiesInfo = rc.senseNearbyRobots(24, Team.ZOMBIE);
        zombiesNum = zombiesInfo.length;
        zombiesPos = new MapLocation[zombiesNum];
        for(int i=0; i<zombiesNum; i++) {
            zombiesPos[i] = zombiesInfo[i].location;
        }

        RobotInfo[] alliesInfo = rc.senseNearbyRobots(24, myTeam);
        alliesNum = alliesInfo.length;
        alliesPos = new MapLocation[alliesNum];
        for(int i=0; i<alliesNum; i++) {
            alliesPos[i] = alliesInfo[i].location;
        }
    }


    /*****navigation*******/

    public void moveFromLocation(MapLocation target) throws GameActionException {
        Direction dir = myLocation.directionTo(target).opposite();

        if(dir == Direction.OMNI) {
            dir = Direction.NORTH;
        }

        moveInDirection(dir);
    }

    public void moveToLocation(MapLocation target) throws GameActionException {
        Direction dir = myLocation.directionTo(target);

        if(dir == Direction.OMNI) {
            dir = Direction.NORTH;
        }

        moveInDirection(dir);
    }

    public void moveFromLocationClearIfStuck(MapLocation target) throws GameActionException {
        Direction dir = myLocation.directionTo(target).opposite();

        if(dir == Direction.OMNI) {
            dir = Direction.NORTH;
        }

        moveInDirectionClearIfStuck(dir);
    }

    public void moveToLocationClearIfStuck(MapLocation target) throws GameActionException {
        Direction dir = myLocation.directionTo(target);

        if(dir == Direction.OMNI) {
            dir = Direction.NORTH;
        }

        moveInDirectionClearIfStuck(dir);
    }

    public void moveInDirectionClearIfStuck(Direction d) throws GameActionException {

        if(!rc.isCoreReady()) {
            return;
        }

        boolean moved = false;

        //not sure if Direction is passed by reference
        Direction dir = d;
        for(int i=0; i<8;i++) {
            if(safeToMove(dir)) {
                rc.move(dir);
                moved = true;
                return;
            }
            dir = dir.rotateRight();
        }

        if(!moved) {
            for(int i=0; i<8; i++) {
                if(rc.senseRobotAtLocation(myLocation.add(dir))==null && rc.senseRubble(myLocation.add(dir)) > 50.0) {
                    rc.clearRubble(dir);
                    return;
                }
                dir = dir.rotateRight();
            }
        }
    }

    public void moveInDirection(Direction d) throws GameActionException {
        if(!rc.isCoreReady()) {
            return;
        }

        //not sure if Direction is passed by reference
        Direction dir = d;
        for(int i=0; i<8;i++) {
            if(safeToMove(dir)) {
                rc.move(dir);
                break;
            }
            dir = dir.rotateRight();
        }
    }

    public void moveInSomeDirection() throws GameActionException {
        Direction dir = Direction.NORTH;
        for(int i=0; i<8;i++) {
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

        return true;
    }
}