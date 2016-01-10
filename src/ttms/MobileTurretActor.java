package ttms;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Signal;

public class MobileTurretActor extends RobotActor {
	
	public MobileTurretActor(RobotController rc) throws GameActionException {
		super(rc);
	}
	
	MapLocation nearestBroadcastEnemy;
	MapLocation nearestBroadcastAlly;
	RobotType myType;
	
	MapLocation nearestAttackableEnemy;
	
	Signal[] signals;
	
	public void act() throws GameActionException {
        myTeam = rc.getTeam();

        while(true) {
        	myType = rc.getType();
        	myLocation = rc.getLocation();
        	signals = rc.emptySignalQueue();
        	
            countNearbyRobots();
            findAverageAlliesPos();
            findNearestHostilePos();
            readBroadcasts();
            findNearestAttackableEnemy();
            findAverageAlliesNoScouts();
            
            if(nearestAttackableEnemy!=null) {
            	rc.setIndicatorString(0, "TARGET"+ nearestAttackableEnemy.x+", "+nearestAttackableEnemy.y);
            }
            
            rc.setIndicatorString(1, "DELAYS CORE, ATTACK: "+rc.getCoreDelay()+", "+rc.getWeaponDelay() + " | WEAPON READY: "+rc.isWeaponReady());
            
            if(nearestAttackableEnemy!=null && myType==RobotType.TURRET) {
            	attack(nearestAttackableEnemy);
            }

            /*find nearby*/
            countNearbyRobots();

            /*act*/
            switch(myType) {
            case TURRET:
            	moveTurret();
            	break;
            case TTM:
            	moveTTM();
            	break;
			default:
				break;
            }

            Clock.yield();
        }
    }
	
	public void findNearestAttackableEnemy() throws GameActionException {
		nearestAttackableEnemy = null;
		int bestDist = 2000000;
		
		for(RobotInfo info : enemiesInfo) {
			MapLocation loc = info.location;
			int dist = myLocation.distanceSquaredTo(loc);
			if(dist >5 && dist < bestDist) {
				nearestAttackableEnemy = new MapLocation(loc.x, loc.y);
				bestDist = dist;
			}
		}
		
		for(Signal s: signals) {
			int[] msg = s.getMessage();
			if(s.getTeam() != myTeam || msg==null) {
				continue;
			}
			
			MapLocation loc = new MapLocation(msg[1]%1000, msg[1]/1000);
			int dist = myLocation.distanceSquaredTo(loc);
			if(dist >5 && dist<=48 && dist < bestDist) {
				nearestAttackableEnemy = new MapLocation(loc.x, loc.y);
				bestDist = dist;
			}
		}
	}
	
	public void readBroadcasts() throws GameActionException {
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
	
	public void moveTurret() throws GameActionException {
		if(enemiesNum+zombiesNum>0) {
			rc.broadcastSignal(myType.sensorRadiusSquared*2);
			if(allyGuardsNum+allyTurretsNum >= enemiesNum+zombiesNum) {
				return;
			} else {
				rc.pack();
			}
		} else if(nearestAttackableEnemy!=null) {
			return;
		} else {
			
			//if(allyGuardsNum == 0) {
				rc.pack();
			//}
		}
		
	}
	
    public void moveTTM() throws GameActionException {


        if(enemiesNum+zombiesNum>0) {
        	rc.broadcastSignal(myType.sensorRadiusSquared*2);
        	if(allyGuardsNum+allyTurretsNum>=enemiesNum+zombiesNum) {
        		rc.unpack();
        	} else {
        		moveFromLocation(nearestHostilePos);
        	}
        	
        } else {
        	if(nearestBroadcastEnemy!=null) {
        		if(nearestAttackableEnemy!=null && myLocation.distanceSquaredTo(nearestAttackableEnemy)<=36) {
        			rc.unpack();
        		} else {
        			moveToLocation(nearestBroadcastEnemy);
        		}
        	} else if(nearestBroadcastAlly!=null) {
        		moveToLocation(nearestBroadcastAlly);
        	} else {
        		moveToLocation(averageAlliesNoScouts);
        	}
        }
    }
}
