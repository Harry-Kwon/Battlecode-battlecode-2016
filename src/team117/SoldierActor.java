package team117;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.Signal;

public class SoldierActor extends RobotActor {
	
	MapLocation nearestBroadcastEnemy;
	MapLocation nearestBroadcastAlly;
	MapLocation nearestBroadcastDen;
	MapLocation nearestBroadcastRally;
	
	MapLocation savedRally;
	
	RobotType myType;
	
	String debugString = "";
	
	public SoldierActor(RobotController rc) throws GameActionException {
        super(rc);
    }
	
	public void act() throws GameActionException {
        myTeam = rc.getTeam();
        myType = rc.getType();

        while(true) {
        	myLocation = rc.getLocation();
        	debugString += " | "+Clock.getBytecodeNum();
            countNearbyRobots();
            debugString += " | "+Clock.getBytecodeNum();
            findAverageAlliesPos();
            findNearestHostilePos();
            findAverageAlliesNoScouts();
            
            debugString += " | "+Clock.getBytecodeNum();
            
            readBroadcasts();
            
            debugString += " | "+Clock.getBytecodeNum();
            
            attack();
            
            debugString += " | "+Clock.getBytecodeNum();

            /*act*/
            move();
            
            debugString += " | "+Clock.getBytecodeNum();
            
            rc.setIndicatorString(0, debugString);
            debugString = "";

            Clock.yield();
        }
    }
	
	public void readBroadcasts() throws GameActionException {
        Signal[] signals = rc.emptySignalQueue();
        nearestBroadcastEnemy = null;
        nearestBroadcastAlly = null;
        nearestBroadcastDen = null;
        nearestBroadcastRally = null;

        int bestEnemyDist = 2000000;
        int bestAllyDist = 2000000;
        int bestDenDist = 2000000;
        int bestRallyDist = 2000000;
        
        for(Signal s : signals) {
            if(s.getTeam() != myTeam) {
                continue;
            }
            
            int[] msg = s.getMessage();
            if(msg==null) {
            	MapLocation loc = s.getLocation();
                
                int dist = myLocation.distanceSquaredTo(loc);
                if(dist < bestAllyDist && dist>myType.sensorRadiusSquared) {
                    bestAllyDist = dist;
                    nearestBroadcastAlly = new MapLocation(loc.x, loc.y);
                }
            	 
            } else if(msg[0]==0){
            	MapLocation loc = new MapLocation(msg[1]%1000, msg[1]/1000);
                
                int dist = myLocation.distanceSquaredTo(loc);
                if(dist < bestEnemyDist) {
                    bestEnemyDist = dist;
                    nearestBroadcastEnemy = new MapLocation(loc.x, loc.y);
                }
            } else if(msg[0]==1) {
            	MapLocation loc = new MapLocation(msg[1]%1000, msg[1]/1000);
            	
            	int dist = myLocation.distanceSquaredTo((loc));
            	if(dist < bestDenDist) {
            		bestDenDist = dist;	
            		nearestBroadcastDen = new MapLocation(loc.x, loc.y);
            	}
            } else if(msg[0]==3) {
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
	
	public void attack() throws GameActionException {
		if(nearestHostilePos != null) {
			if(nearestHostileDist <= myType.attackRadiusSquared) {
	        	attack(nearestHostilePos);
	        }
		} else {
			if(nearestDenPos != null) {
				if(nearestDenDist <= myType.attackRadiusSquared) {
					attack(nearestDenPos);
				}
			}
		}
		
	}
	
    public void move() throws GameActionException {


        if(nearestHostilePos != null) {
        	rc.broadcastSignal(myType.sensorRadiusSquared*2);
        	if(nearestTurretPos!=null) {
        		if(nearestTurretDist < 13 || !(allyGuardsNum>=15)) {
        			tryBFSMoveClearIfStuck(nearestHostilePos);
        			//BFSMoveToNearestEnemy();
        		} else {
        			if(nearestTurretPos!=null) {
        				tryBFSMoveClearIfStuck(nearestTurretPos);
        			} else if(savedRally!=null){
        				moveToLocationClearIfStuck(savedRally);
        			}
        			
        		} 	
        	} else {
        		if(savedRally!=null) {
        			tryBFSMoveClearIfStuck(savedRally);
        		} else {
        			tryBFSMoveClearIfStuck(nearestHostilePos);
        		}
        		
        	}
        	
        } else {
        	findNearestTurret();
        	if(nearestTurretDist>=13 && nearestTurretPos != null) {
        		moveToLocationClearIfStuck(nearestTurretPos);
        	} else if(nearestBroadcastEnemy!=null) {
        		moveToLocationClearIfStuck(nearestBroadcastEnemy);
        	} else if(nearestBroadcastAlly!=null) {
        		moveToLocationClearIfStuck(nearestBroadcastAlly);
        	} 
//        	else if(nearestDenPos!=null) { 
//        		moveToLocationClearIfStuck(nearestDenPos);
//        	} 
        	else if(nearestBroadcastDen!=null && myLocation.distanceSquaredTo(nearestBroadcastDen)>myType.sensorRadiusSquared) {
        		moveToLocationClearIfStuck(nearestBroadcastDen);
        	} else if(savedRally!=null && myLocation.distanceSquaredTo(savedRally)>53) {
        		moveToLocationClearIfStuck(savedRally);
        	} else {
        		
        		if(alliesNum >= 20) {
        			moveFromLocationClear(averageAlliesNoScouts);
        		} else {
        			moveToLocationClear(averageAlliesNoScouts);
        		}
        		
        	}
        }
    }
}
