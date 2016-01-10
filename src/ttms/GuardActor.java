package ttms;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.Signal;

public class GuardActor extends RobotActor {
	
	MapLocation nearestBroadcastEnemy;
	MapLocation nearestBroadcastAlly;
	RobotType myType;
	
	public GuardActor(RobotController rc) throws GameActionException {
        super(rc);
    }

	
	public void act() throws GameActionException {
        rc.setIndicatorString(0, "SOLDIER ACTOR");
        myTeam = rc.getTeam();
        myType = rc.getType();

        while(true) {
        	myLocation = rc.getLocation();
            countNearbyRobots();
            findAverageAlliesPos();
            findNearestHostilePos();
            
            readBroadcasts();
            
            if(nearestHostileDist <= 3) {
            	attack(nearestHostilePos);
            }

            /*find nearby*/
            countNearbyRobots();
            rc.setIndicatorString(1, "E,Z,A"+enemiesNum+","+zombiesNum+","+alliesNum);

            /*act*/
            move();

            Clock.yield();
        }
    }
	
	public void readBroadcasts() throws GameActionException {
        Signal[] signals = rc.emptySignalQueue();
        nearestBroadcastEnemy = null;
        nearestBroadcastAlly = null;

        int bestEnemyDist = 2000000;
        int bestAllyDist = 2000000;
        
        for(Signal s : signals) {
            if(s.getTeam() != myTeam) {
                continue;
            }
            
            int[] msg = s.getMessage();
            if(msg==null) {
            	MapLocation loc = s.getLocation();
                
                int dist = myLocation.distanceSquaredTo(loc);
                if(dist < bestAllyDist) {
                    bestAllyDist = dist;
                    nearestBroadcastEnemy = new MapLocation(loc.x, loc.y);
                }
            	 
            } else {
            	MapLocation loc = new MapLocation(msg[1]%1000, msg[1]/1000);
                
                int dist = myLocation.distanceSquaredTo(loc);
                if(dist < bestEnemyDist) {
                    bestEnemyDist = dist;
                    nearestBroadcastEnemy = new MapLocation(loc.x, loc.y);
                }
            }
            
            
        }
    }

    public void move() throws GameActionException {


        if(enemiesNum+zombiesNum>0) {
        	rc.broadcastSignal(myType.sensorRadiusSquared*2);
        	if(alliesNum >= enemiesNum+zombiesNum || allyTurretsNum>0) {
        		moveToLocationClearIfStuck(nearestHostilePos);
        	} else {
        		moveFromLocationClearIfStuck(nearestHostilePos);
        		
        	}
        	
        } else {
        	if(nearestBroadcastEnemy!=null) {
        		moveToLocationClearIfStuck(nearestBroadcastEnemy);
        	} else if(nearestBroadcastAlly!=null) {
        		moveToLocationClearIfStuck(nearestBroadcastAlly);
        	} else {
        		if(alliesNum >= 20) {
        			moveFromLocationClear(averageAlliesPos);
        		} else {
        			moveToLocationClearIfStuck(averageAlliesPos);
        		}
        		
        	}
        }
    }
}
