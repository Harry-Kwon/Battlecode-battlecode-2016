package autobots;

import battlecode.common.*;

public class ArchonActor extends RobotActor {


    public ArchonActor(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void act() throws GameActionException {
        rc.setIndicatorString(0, "ARCHON ACTOR");
        myTeam = rc.getTeam();

        while(true) {

            myLocation = rc.getLocation();

            if(rc.isCoreReady()) {

                countNearbyRobots();

                if(enemiesNum+zombiesNum >0) {

                    if(alliesNum > 3) {
                        spawnUnit(RobotType.SOLDIER);
                    }
                    findNearestHostilePos();
                    moveFromLocationClearIfStuck(nearestHostilePos);
                } else  {
                    spawnUnit(RobotType.SOLDIER);
                    //findAverageAlliesPos();
                    //moveToLocationClearIfStuck(averageAlliesPos);
                }

                //findAverageAlliesPos();
                //moveToLocation(averageAlliesPos);

                //moveInSomeDirection();

            }

            Clock.yield();
        }
    }

    public boolean spawnUnit(RobotType type) throws GameActionException {
        if(rc.hasBuildRequirements(type)) {
            Direction dir = Direction.NORTH;
            for(int i=0; i<8; i++) {
                if(rc.canBuild(dir, type)) {
                    rc.build(dir, type);
                    return true;
                }
                dir = dir.rotateRight();
            }
        }
        return false;
    }
}