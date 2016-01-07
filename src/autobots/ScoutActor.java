package autobots;

import battlecode.common.*;

public class ScoutActor extends RobotActor {

    MapLocation nearestTurretPos;
    int nearestTurretDist;

    MapLocation nearestScoutPos;
    int nearestScoutDist;

    MapLocation nearestArchonPos;
    int nearestArchonDist;

    int myID;

    int header=-1;
    int msg;
    int range;

    int broadcastRepeat = 0;

    public ScoutActor(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void act() throws GameActionException {
        myTeam = rc.getTeam();
        myID = rc.getID();


        while(true) {
            myLocation = rc.getLocation();

            countNearbyRobots();
            findNearestHostilePos();
            findAverageAlliesPos();

            if(enemiesNum+zombiesNum > 0) {
                RobotType type = rc.senseRobotAtLocation(nearestHostilePos).type;
                header = 0;
                msg = nearestHostilePos.x+1000*nearestHostilePos.y;
                range = 106;

                for(RobotInfo info : enemiesInfo) {
                    if(info.type==RobotType.TURRET) {
                        msg = info.location.x+1000*info.location.y;
                        range = 2000000;
                        break;
                    }
                }

                broadcastRepeat = 0;
                if(type==RobotType.TURRET) {
                    header = 1;
                    range = 2000000;
                }
                rc.broadcastMessageSignal(header, msg, range);
            } else if(header!=-1 && broadcastRepeat<50) {
                rc.broadcastMessageSignal(header, msg, range);
                broadcastRepeat++;
            }

            move();

            //extendBroadcastSignals();

            lastLocation = new MapLocation(myLocation.x, myLocation.y);

            Clock.yield();
        }
    }

    public void extendBroadcastSignals() throws GameActionException {
        Signal[] signals = rc.emptySignalQueue();
        rc.setIndicatorString(0, "SIGNALS: "+signals.length);
        MapLocation target = null;

        for(Signal s : signals) {
            if(s.getTeam() != myTeam) {
                continue;
            }
            int[] msg = s.getMessage();
            if(msg==null || msg[0]==myID) {
                continue;
            }
            rc.broadcastMessageSignal(s.getRobotID(), msg[1], 106);
        }

        if(target != null) {
            attack(target);
        }
    }

    public void move() throws GameActionException {
        if(nearestHostileDist <=36) {
            moveFromLocationClearIfStuck(nearestHostilePos);
            return;
        }

        findNearestScout(myLocation);
        if(nearestScoutPos!= null) {
            if(nearestScoutDist >45) {
                moveToLocationClearIfStuck(nearestScoutPos);
            } else {
                moveFromLocationClearIfStuck(nearestScoutPos);
            }
            
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

}