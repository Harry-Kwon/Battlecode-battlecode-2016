package ttms;

import battlecode.common.*;

public class ScoutActor extends RobotActor {

    MapLocation nearestArchonPos;
    int nearestArchonDist;

    MapLocation spawnLocation = null;
    RobotType myType;
    
    MapLocation bestIdleSignal;
	int bestIdleCount;

    public ScoutActor(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void act() throws GameActionException {
        myTeam = rc.getTeam();
        myType = rc.getType();

        spawnLocation = rc.getLocation();

        while(true) {
            myLocation = rc.getLocation();

            countNearbyRobots();
            findNearestHostilePos();
            findAverageAlliesPos();
            findAverageAlliesNoScouts();

            /*if(rc.getHealth() < 70) {
                findNearestArchon(myLocation);
                moveToLocationClearIfStuck(nearestArchonPos);
            }*/

            if(enemiesNum+zombiesNum > 0) {
                rc.broadcastMessageSignal(0, nearestHostilePos.x+1000*nearestHostilePos.y, myType.sensorRadiusSquared*33);
                /*for(RobotInfo info : enemiesInfo) {
                    rc.broadcastMessageSignal(0, info.location.x+1000*info.location.y, 106);
                }*/
            } else {
            	if(rc.getRoundNum()%2==0) {
            		rc.broadcastMessageSignal(alliesNum*10+1, averageAlliesNoScouts.x+1000*averageAlliesNoScouts.y, myType.sensorRadiusSquared*33);
            	} else {
//            		getBestIdleSignal();
//            		if(bestIdleSignal!=null) {
//            			rc.broadcastMessageSignal(1, bestIdleSignal.x+1000*bestIdleSignal.y, myType.sensorRadiusSquared*33);
//            		}
            		
            	}
            	
            }

            move();

            lastLocation = new MapLocation(myLocation.x, myLocation.y);

            Clock.yield();
        }
    }
    
    public void getBestIdleSignal() {
    	bestIdleSignal = null;
    	bestIdleCount = 0;
    	
    	
    }

    public void move() throws GameActionException {
        findNearestTurret();
        findNearestScout();

        if(myLocation.distanceSquaredTo(averageAlliesNoScouts)>16) {
            moveToLocationClearIfStuck(averageAlliesNoScouts);
        } else {
            if(nearestScoutPos!=null) {
                moveFromLocationClearIfStuck(nearestScoutPos);
            } else {
                moveFromLocationClearIfStuck(averageAlliesNoScouts);
            }
        }
    }

}