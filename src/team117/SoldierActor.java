package team117;

import battlecode.common.*;

public class SoldierActor extends RobotActor {



    MapLocation spawnLocation;


    public SoldierActor(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void act() throws GameActionException {
        rc.setIndicatorString(0, "SOLDIER ACTOR");
        myTeam = rc.getTeam();
        spawnLocation = rc.getLocation();

        while(true) {
            myLocation = rc.getLocation();

            /*find nearby*/
            countNearbyRobots();
            rc.setIndicatorString(1, "E,Z,A"+enemiesNum+","+zombiesNum+","+alliesNum);

            /*act*/
            move();

            Clock.yield();
        }
    }

    public void move() throws GameActionException {


        if(enemiesNum+zombiesNum==0) {
            findAverageAlliesPos();
            if(alliesNum < 15) {
                moveToLocationClearIfStuck(averageAlliesPos);
            } else {
                moveFromLocationClearIfStuck(averageAlliesPos);
            }
        } else {
            findNearestHostilePos();

            //turret rush
            if(rc.senseRobotAtLocation(nearestHostilePos).type==RobotType.TURRET) {
                if(nearestHostileDist >2) {
                    moveToLocationClearIfStuck(nearestHostilePos);
                    return;
                }
            }

            if(enemiesNum+zombiesNum>alliesNum) {
                //move away from closest enemy
                moveFromLocationClearIfStuck(nearestHostilePos);
            } else {
                //attack
                if(nearestHostileDist > 13) {
                    moveToLocationClearIfStuck(nearestHostilePos);
                } else {
                    attack(nearestHostilePos);
                    moveFromLocationClearIfStuck(nearestHostilePos);
                }
            }
        }
    }


}