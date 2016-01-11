package team117;

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

            
//            findNearestTurret();
//            
//            if(nearestTurretPos!=null) {
//            	
//            }

            move();
            broadcastEnemies();

            lastLocation = new MapLocation(myLocation.x, myLocation.y);

            Clock.yield();
        }
    }
    
    public void broadcastEnemies() throws GameActionException {
    	int sent = 0;
    	for(RobotInfo info : enemiesInfo) {
    		if(sent>=20) {
    			return;
    		}
    		rc.broadcastMessageSignal(0,  info.location.x+1000*info.location.y, myType.sensorRadiusSquared*2);
    		sent++;
    	}
    	
    	for(RobotInfo info : zombiesInfo) {
    		if(sent>=20) {
    			return;
    		}
    		if(info.type==RobotType.ZOMBIEDEN) {
    			rc.broadcastMessageSignal(1, info.location.x+1000*info.location.y, myType.sensorRadiusSquared*2);
    		} else {
    			rc.broadcastMessageSignal(0,  info.location.x+1000*info.location.y, myType.sensorRadiusSquared*2);
    		}
    		
    		sent++;
    		
    	}
    }

    public void move() throws GameActionException {
        findNearestTurret();
        findNearestScout();
        
        if(myLocation.distanceSquaredTo(averageAlliesNoScouts)>8) {
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