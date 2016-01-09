package ttms;

import battlecode.common.*;

public class ScoutActor extends RobotActor {

    MapLocation nearestTurretPos;
    int nearestTurretDist;

    MapLocation nearestScoutPos;
    int nearestScoutDist;

    MapLocation nearestArchonPos;
    int nearestArchonDist;

    MapLocation spawnLocation = null;

    public ScoutActor(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void act() throws GameActionException {
        myTeam = rc.getTeam();

        spawnLocation = rc.getLocation();

        while(true) {
            myLocation = rc.getLocation();

            countNearbyRobots();
            findNearestHostilePos();
            findAverageAlliesPos();

            /*if(rc.getHealth() < 70) {
                findNearestArchon(myLocation);
                moveToLocationClearIfStuck(nearestArchonPos);
            }*/

            if(enemiesNum+zombiesNum > 0) {
                rc.broadcastMessageSignal(0, nearestHostilePos.x+1000*nearestHostilePos.y, 106);
            } 

            /*if(nearestHostileDist <= 3) {
                moveFromLocationClearIfStuck(nearestHostilePos);
            } else if(nearestHostileDist >8) {
                if(!myLocation.equals(spawnLocation)) {
                    moveToLocationClearIfStuck(spawnLocation);
                }
            }*/

            //moveToBorder();

            lastLocation = new MapLocation(myLocation.x, myLocation.y);

            Clock.yield();
        }
    }

    public void findNearestArchon(MapLocation check) throws GameActionException {
        nearestArchonPos = myLocation;
        nearestArchonDist = 9999999;
        for(RobotInfo info:alliesInfo) {
            if(info.type==RobotType.ARCHON) {
                int dist = check.distanceSquaredTo(info.location);
                if(dist < nearestArchonDist) {
                    nearestArchonDist = dist;
                    nearestArchonPos = new MapLocation(info.location.x, info.location.y);
                }
            }
        }
    }

    public void moveToBorder() throws GameActionException {

        findNearestTurret(myLocation);
        findNearestScout(myLocation);

        

        if(nearestTurretDist <=3) {
            if(nearestTurretPos!=null) {
                moveFromLocationClearIfStuck(averageAlliesPos);
            }
        } else {
            if(nearestTurretDist>5) {
                if(nearestTurretPos!=null) {
                    moveToLocationClearIfStuck(nearestTurretPos);
                }
            } else {
                if(nearestScoutDist <=9) {
                    moveFromLocationClearIfStuck(nearestScoutPos);
                } else {
                    moveToLocationClearIfStuck(nearestTurretPos);
                }
            }
        }
    }

    public void findNearestScout(MapLocation check) throws GameActionException {
        nearestScoutPos = myLocation;
        nearestScoutDist = 9999999;
        for(RobotInfo info:alliesInfo) {
            if(info.type==RobotType.SCOUT) {
                int dist = check.distanceSquaredTo(info.location);
                if(dist < nearestScoutDist) {
                    nearestScoutDist = dist;
                    nearestScoutPos = new MapLocation(info.location.x, info.location.y);
                }
            }
        }
    }

    public void findNearestTurret(MapLocation check) throws GameActionException {
        nearestTurretDist = 9999999;
        nearestTurretPos = null;
        for(RobotInfo info:alliesInfo) {
            if(info.type==RobotType.TURRET) {
                int dist = check.distanceSquaredTo(info.location);
                if(dist < nearestTurretDist) {
                    nearestTurretDist = dist;
                    nearestTurretPos = new MapLocation(info.location.x, info.location.y);
                }
            }
        }
    }

    public boolean isInsideBorder(MapLocation check) throws GameActionException {
        boolean insideBorder = true;

        Direction dir = Direction.NORTH_EAST;
        if((check.x+check.y)%2==0) {
            dir = Direction.NORTH;
        }

        for(int i=0; i<4; i++) {
            MapLocation loc = check.add(dir);
            if(rc.onTheMap(loc)) {
                RobotInfo info = rc.senseRobotAtLocation(loc);
                if(info==null || !info.team.equals(myTeam)) {
                    insideBorder = false;
                    break;
                }
            }

            dir = dir.rotateRight().rotateRight();
        }

        return(insideBorder);
    }

}