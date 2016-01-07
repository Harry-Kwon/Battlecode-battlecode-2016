package team117;

import battlecode.common.*;

public class ScoutActor extends RobotActor {


    public ScoutActor(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void act() throws GameActionException {
        rc.setIndicatorString(0, "SCOUT ACTOR");
        myTeam = rc.getTeam();

        while(true) {
            myLocation = rc.getLocation();

            countNearbyRobots();
            findNearestHostilePos();

            if(enemiesNum+zombiesNum > 0) {
                rc.broadcastMessageSignal(nearestHostilePos.x, nearestHostilePos.y, 999999);
            }

            lastLocation = new MapLocation(myLocation.x, myLocation.y);

            Clock.yield();
        }
    }

}