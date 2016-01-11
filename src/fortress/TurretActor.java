package fortress;

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

            countNearbyRobots();

            if(enemiesNum+zombiesNum > 0) {
                findNearestHostilePos();
                attack(nearestHostilePos);
                rc.emptySignalQueue();
            } else {
                Signal[] signals = rc.emptySignalQueue();
                MapLocation target = null;

                for(Signal s : signals) {
                    if(s.getTeam() != myTeam) {
                        continue;
                    }

                    int[] msg = s.getMessage();
                    if(msg==null) {
                        continue;
                    }
                    MapLocation loc = new MapLocation(msg[1]%1000, msg[1]/1000);
                    if(rc.canAttackLocation(loc)) {
                        target = new MapLocation(loc.x, loc.y);
                    }
                }

                if(target != null) {
                    attack(target);
                }
                
            }

            lastLocation = new MapLocation(myLocation.x, myLocation.y);

            Clock.yield();
        }
    }

}