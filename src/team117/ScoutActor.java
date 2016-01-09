package team117;

import battlecode.common.*;

public class ScoutActor extends RobotActor {

    boolean[] bcHash = new boolean[1000000];
    int[] hashedValues = new int[1000000];
    int hashedNum = 0;

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

    public ScoutActor(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void act() throws GameActionException {
        myTeam = rc.getTeam();
        myID = rc.getID();


        while(true) {
            /*if(rc.getRoundNum()%50==0) {
                for(int i=0; i<hashedNum; i++) {
                    bcHash[hashedValues[i]]=false;
                }
                hashedValues = new int[1000000];
                hashedNum=0;
            }*/

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
                        range = 10000;
                        break;
                    }
                }

                if(type==RobotType.TURRET) {
                    header = 1;
                    range = 10000;
                }
                rc.broadcastMessageSignal(header, msg, range);
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
            if(msg==null || bcHash[msg[1]]) {
                continue;
            }
            bcHash[msg[1]]=true;
            hashedValues[hashedNum++]=msg[1];
            rc.broadcastMessageSignal(s.getRobotID(), msg[1], 106);
        }

        if(target != null) {
            attack(target);
        }
    }

    public void move() throws GameActionException {
        findNearestScout(myLocation);

        if(nearestHostileDist <=36) {
            moveFromLocationClearIfStuck(nearestHostilePos);
            return;
        }

        moveFromLocationClearIfStuck(averageAlliesPos);

        // if(nearestScoutPos!= null) {
        //     moveFromLocationClearIfStuck(nearestScoutPos);
        // } else {
        //     moveInLastDirection();
        // }
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
        nearestScoutPos = null;
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