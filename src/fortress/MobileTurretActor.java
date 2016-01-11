package fortress;

import battlecode.common.Clock;
import battlecode.common.Direction;
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
	MapLocation nearestBroadcastDen;
	RobotType myType;
	
	MapLocation nearestAttackablePos;
	
	Signal[] signals;
	
	public void act() throws GameActionException {
        myTeam = rc.getTeam();

        while(true) {
        	myType = rc.getType();
        	myLocation = rc.getLocation();
        	signals = rc.emptySignalQueue();
        	
            countNearbyRobots();
            findNearestHostilePos();
            findAverageAlliesPos();
            
            readBroadcasts();
    		findNearestAttackablePos();

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
	
	public void moveTTM() throws GameActionException {
		if((myLocation.x+myLocation.y)%2==1) {
			rc.unpack();
		} else {
			moveToClosestOddTile();
		}
	}
	
	public void moveToClosestOddTile() throws GameActionException {
		MapLocation target = null;
		int bestDist = 999999999;
		for(int x=-4;x<=4;x++) {
			for(int y=-4;y<=4;y++) {
				MapLocation loc = new MapLocation(myLocation.x+x, myLocation.y+y);
				if((x+y)%2==1 && rc.canSense(loc) && rc.onTheMap(loc) && rc.senseRobotAtLocation(loc)==null && rc.senseRubble(loc)<50.0) {
					int dist = x*x+y*y;
					if(dist < bestDist) {
						bestDist = dist;
						target = new MapLocation(loc.x, loc.y);
					}
				}
			}
		}
		
		if(target!=null) {
			moveToLocation(target);
		} else {
			moveFromLocation(averageAlliesPos);
		}
	}
	
	public void moveTurret() throws GameActionException {
		if((myLocation.x+myLocation.y)%2!=1) {
			rc.pack();
		} else if(nearestAttackablePos!=null) {
			attack(nearestAttackablePos);
		}
	}
	
	public void readBroadcasts() throws GameActionException {
        nearestBroadcastEnemy = null;
        nearestBroadcastDen = null;

        int bestEnemyDist = 2000000;
        int bestDenDist = 2000000;
        
        for(Signal s : signals) {
            int[] msg = s.getMessage();
            if(s.getTeam() != myTeam || msg == null) {
                continue;
            }
            
            if(msg[0]==0) {
            	MapLocation loc = new MapLocation(msg[1]%1000, msg[1]/1000);
                
                int dist = myLocation.distanceSquaredTo(loc);
                if(dist > 5 && dist < bestEnemyDist) {
                    bestEnemyDist = dist;
                    nearestBroadcastEnemy = new MapLocation(loc.x, loc.y);
                }
            } else if(msg[0]==1) {
            	MapLocation loc = new MapLocation(msg[1]%1000, msg[1]/1000);
            	
            	int dist = myLocation.distanceSquaredTo((loc));
            	if(dist > 5 && dist < bestDenDist ) {
            		bestDenDist = dist;	
            		nearestBroadcastDen = new MapLocation(loc.x, loc.y);
            	}
            }
            
            
        }
    }
	
	public void findNearestAttackablePos() throws GameActionException {
		nearestAttackablePos = null;
		int bestDist = 2000000;
		
		for(RobotInfo info : enemiesInfo) {
			MapLocation loc = info.location;
			int dist = myLocation.distanceSquaredTo(loc);
			if(dist >5 && dist < bestDist) {
				nearestAttackablePos = new MapLocation(loc.x, loc.y);
				bestDist = dist;
			}
		}
		
		if(nearestBroadcastEnemy!=null) {
			int dist = myLocation.distanceSquaredTo(nearestBroadcastEnemy);
			if(dist<=48 && dist < bestDist) {
				nearestAttackablePos = new MapLocation(nearestBroadcastEnemy.x, nearestBroadcastEnemy.y);
				bestDist = dist;
			}
		}
		
		//if no enemy units, target den
		if(nearestAttackablePos==null) {
			if(nearestDenPos != null && nearestDenDist > 5) {
				nearestAttackablePos = new MapLocation(nearestDenPos.x, nearestDenPos.y);
			} else if(nearestBroadcastDen!=null && myLocation.distanceSquaredTo(nearestBroadcastDen)<=48) {
				nearestAttackablePos = new MapLocation(nearestBroadcastDen.x, nearestBroadcastDen.y);
			}
		}
	}
	

}
