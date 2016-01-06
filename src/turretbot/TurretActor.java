package turretbot;

import battlecode.common.*;

public class TurretActor extends RobotActor {


    public TurretActor(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void act() throws GameActionException {
        rc.setIndicatorString(0, "TURRET ACTOR");
        myTeam = rc.getTeam();

        while(true) {
            myLocation = rc.getLocation();

            if(rc.isCoreReady()) {

                countNearbyRobots();

                if(enemiesNum+zombiesNum > 0) {
                    findNearestHostilePos();
                    attack(nearestHostilePos);
                }
            }

            Clock.yield();
        }
    }

}