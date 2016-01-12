package team117;

import battlecode.common.*;

public class ScoutActor extends RobotActor {

    MapLocation nearestArchonPos;
    int nearestArchonDist;
    RobotType myType;
    
    MapLocation bestIdleSignal;
	int bestIdleCount;
	
	MapLocation nearestBroadcastRally;
	
	MapLocation savedRally;
	
	int sent;

    public ScoutActor(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void setInitialVars() throws GameActionException {    	
    	myTeam = rc.getTeam();
    	myType = rc.getType();
    }
    
    public void updateRoundVars() throws GameActionException {
    	myLocation = rc.getLocation();
    	sent = 0;
    	
    	countNearbyRobots();
    	findNearestHostilePos();
    	findAverageAlliesPos();
    	findAverageAlliesNoScouts();
    	readBroadcasts();
    	
    }
    
    public void act() throws GameActionException {
    	setInitialVars();
        while(true) {
            updateRoundVars();
        	broadcast();
            move();

            Clock.yield();
        }
    }
    
    public void broadcast() throws GameActionException {    	
    	broadcastEnemies();
    	broadcastPartCaches();
    }
    
    public void broadcastEnemies() throws GameActionException {
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
    		
    		if(sent>=20) {
    			return;
    		}
    		
    		if(info.type==RobotType.BIGZOMBIE) {
    			rc.broadcastMessageSignal(2, info.location.x+1000*info.location.y, myType.sensorRadiusSquared*2);
    			sent++;
    		}
    		
    		sent++;
    		
    	}
    }
    
    public void broadcastPartCaches() throws GameActionException {
    	for(int x=-5; x<=5; x++) {
			for(int y=-5; y<=5; y++) {
				if(sent>=20) {
					return;
				}
				
				MapLocation loc = new MapLocation(myLocation.x+x, myLocation.y+y);
				if(rc.canSense(loc) && rc.senseParts(loc)>0.0) {
					rc.broadcastMessageSignal(4,  loc.x+1000*loc.y, myType.sensorRadiusSquared*2);
					sent++;
				}
			}
    	}
    }
    
    public void readBroadcasts() throws GameActionException {
        Signal[] signals = rc.emptySignalQueue();
        nearestBroadcastRally = null;

        int bestRallyDist = 2000000;
        
        for(Signal s : signals) {
            if(s.getTeam() != myTeam) {
                continue;
            }
            
            int[] msg = s.getMessage();
            if(msg!=null && msg[0]==3) {
            	MapLocation loc = new MapLocation(msg[1]%1000, msg[1]/1000);
            	
            	int dist = myLocation.distanceSquaredTo((loc));
            	if(dist < bestRallyDist) {
					bestRallyDist = dist;
					nearestBroadcastRally = new MapLocation(loc.x, loc.y);
            	}
            }
        }
        if(nearestBroadcastRally!=null) {
        	savedRally = new MapLocation(nearestBroadcastRally.x, nearestBroadcastRally.y);
        }
    }
    
    public void move() throws GameActionException {
        findNearestTurret();
        findNearestScout();
        
        if(nearestHostilePos!=null && myLocation.distanceSquaredTo(nearestHostilePos)<9) {
        	moveFromLocationClearIfStuck(nearestHostilePos);
        } if(alliesNum==0 && savedRally!=null) {
        	moveToLocationClearIfStuck(savedRally);
        } else if(myLocation.distanceSquaredTo(averageAlliesNoScouts)>16/*8*/) {
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