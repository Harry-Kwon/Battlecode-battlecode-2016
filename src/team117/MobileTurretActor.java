package team117;

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
	MapLocation nearestBroadcastAlly;
	MapLocation nearestBroadcastDen;
	MapLocation nearestBroadcastRally;
	MapLocation savedRally;
	RobotType myType;
	
	MapLocation nearestAttackableEnemy;
	boolean nearestAttackableEnemyIsDen;
	
	Signal[] signals;
	
	int timer = 0;
	
	public void setInitialVars() throws GameActionException {
		myTeam = rc.getTeam();
	}
	
	public void updateRoundVars() throws GameActionException {		
		myType = rc.getType();
		myLocation = rc.getLocation();
		signals = rc.emptySignalQueue();
		
		countNearbyRobots();
		findAverageAlliesPos();
		findNearestHostilePos();
		
		readBroadcasts();
		findNearestAttackableEnemy();
		findAverageAlliesNoScouts();
	}

	public void broadcast() throws GameActionException {
		if(nearestHostilePos != null) {
			rc.broadcastSignal(myType.sensorRadiusSquared*2);
		}
	}
	
	public void act() throws GameActionException {
        setInitialVars();

        while(true) {
            updateRoundVars();
            broadcast();
            
            attack();
            
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
	
	public void attack() throws GameActionException {
		if(nearestAttackableEnemy!=null) {
			attack(nearestAttackableEnemy);
		}		
	}
	
	public void findNearestAttackableEnemy() throws GameActionException {
		nearestAttackableEnemy = null;
		int bestDist = 2000000;
		nearestAttackableEnemyIsDen = false;
		
		boolean enemyIsBigZombie = false;
		
		for(RobotInfo info : enemiesInfo) {
			MapLocation loc = info.location;
			int dist = myLocation.distanceSquaredTo(loc);

				if(dist >5 && dist < bestDist) {
					nearestAttackableEnemy = new MapLocation(loc.x, loc.y);
					bestDist = dist;
			}
		}
		if(nearestAttackableEnemy!=null) {
			return;
		}
		
		for(RobotInfo info : zombiesInfo) {
			MapLocation loc = info.location;
			int dist = myLocation.distanceSquaredTo(loc);
			if(!enemyIsBigZombie && info.type==RobotType.BIGZOMBIE && dist>5) {
				nearestAttackableEnemy = new MapLocation(loc.x, loc.y);
				bestDist = dist;
				enemyIsBigZombie=true;
			} else 
				if(dist >5 && dist < bestDist) {
				if(info.type==RobotType.BIGZOMBIE || !enemyIsBigZombie) {
					nearestAttackableEnemy = new MapLocation(loc.x, loc.y);
					bestDist = dist;
					if(info.type == RobotType.BIGZOMBIE) {
						enemyIsBigZombie=true;
					}
				}
			}
		}
		
		boolean enemyTargetFound = false;
		
		for(Signal s: signals) {
			int[] msg = s.getMessage();
			if(s.getTeam() != myTeam || msg==null || !(msg[0]==0||msg[0]==2||msg[0]==5)) {
				continue;
			}
			
			MapLocation loc = new MapLocation(msg[1]%1000, msg[1]/1000);
			int dist = myLocation.distanceSquaredTo(loc);
			
			if(enemyIsBigZombie) {
				if(msg[0]!=2) {
					continue;
				}
				
				if(dist >5 && dist<=48 && dist < bestDist) {
					nearestAttackableEnemy = new MapLocation(loc.x, loc.y);
					bestDist = dist;
				}
			} else {
				if(msg[0]==2 && dist>5 && dist<48) {
					nearestAttackableEnemy = new MapLocation(loc.x, loc.y);
					bestDist = dist;
					enemyIsBigZombie=true;
				} else {
					if(enemyTargetFound) {
						if(msg[0]==4) {
							continue;
						} else {
							if(dist >5 && dist<=48 && dist < bestDist) {
								nearestAttackableEnemy = new MapLocation(loc.x, loc.y);
								bestDist = dist;
							}
						}
					} else {
						if(msg[0]==0 && dist>5 && dist<48) {
							nearestAttackableEnemy = new MapLocation(loc.x, loc.y);
							bestDist = dist;
							enemyTargetFound=true;
						} else {
							if(dist >5 && dist<=48 && dist < bestDist) {
								nearestAttackableEnemy = new MapLocation(loc.x, loc.y);
								bestDist = dist;
							}
						}
					}
				}
			}
			
			if(!enemyIsBigZombie && msg[0]==2 && dist>5 && dist<=48) {
				nearestAttackableEnemy = new MapLocation(loc.x, loc.y);
				bestDist = dist;
				enemyIsBigZombie=true;
			} else 
				if(dist >5 && dist<=48 && dist < bestDist) {
					if(msg[0]==2||!enemyIsBigZombie) {
						nearestAttackableEnemy = new MapLocation(loc.x, loc.y);
						bestDist = dist;
						if(msg[0]==2) {
							enemyIsBigZombie = true;
						}
					}
				}
			
		}
		
		//if no enemy units, target den
		if(nearestAttackableEnemy==null) {
			if(nearestDenPos != null && nearestDenDist > 5) {
				nearestAttackableEnemyIsDen = true;
				nearestAttackableEnemy = nearestDenPos;
			} else if(nearestBroadcastDen!=null && myLocation.distanceSquaredTo(nearestBroadcastDen)<=48) {
				nearestAttackableEnemyIsDen = true;
				nearestAttackableEnemy = nearestBroadcastDen;
			}
		}
	}
	
	public void readBroadcasts() throws GameActionException {
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
            	 
            } else if(msg[0]==0 || msg[0]==5) {
            	MapLocation loc = new MapLocation(msg[1]%1000, msg[1]/1000);
                
                int dist = myLocation.distanceSquaredTo(loc);
                if(dist < bestEnemyDist) {
                    bestEnemyDist = dist;
                    nearestBroadcastEnemy = new MapLocation(loc.x, loc.y);
                }
            } else if(msg[0]==1) {
            	MapLocation loc = new MapLocation(msg[1]%1000, msg[1]/1000);
            	
            	int dist = myLocation.distanceSquaredTo((loc));
            	if(dist < bestDenDist && dist>5) {
            		bestDenDist = dist;	
            		nearestBroadcastDen = new MapLocation(loc.x, loc.y);
            	}
            }  else if(msg[0]==3) {
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
	
	public void moveTurret() throws GameActionException {
		if(nearestAttackableEnemy!=null) {			
			if(nearestAttackableEnemyIsDen) {
				if(nearestBroadcastEnemy!=null) {
					if(rc.isCoreReady()) {
						rc.pack();
					}
				} else {
					return;
				}
			} else {
				return;
			}
			timer = -1;

		} else if(rc.isCoreReady()){
			if(timer==-1) {
				timer=0;
			} else {
				timer++;
			}
			
			if(timer>=5) {
				rc.pack();
				timer = -1;
			}
		}
		
	}
	
    public void moveTTM() throws GameActionException {

        if(nearestAttackableEnemy!=null && !nearestAttackableEnemyIsDen) {        	
    		if(rc.isWeaponReady()) {
//    			if(onEvenTile()) {
    				rc.unpack();
//    			} else {
//    				moveToEvenTile();
//    			}
    		}
    		return;
        	
        } else if(nearestAttackableEnemy!=null && nearestAttackableEnemyIsDen && nearestBroadcastEnemy==null) {
        	if(rc.isWeaponReady()) {
//    			if(onEvenTile()) {
    				rc.unpack();
//    			} else {
//    				moveToEvenTile();
//    			}
    		}
        	return;
        } else if(nearestHostilePos!=null && myLocation.distanceSquaredTo(nearestHostilePos) <= 5) {
        	moveFromLocation(nearestHostilePos);
        } else if(nearestDenPos!=null && myLocation.distanceSquaredTo(nearestDenPos)<=5) {
        	moveFromLocation(nearestDenPos);
        } else {
        	if(nearestBroadcastEnemy!=null) {
        		moveToLocation(nearestBroadcastEnemy);
        	} else if(nearestBroadcastAlly!=null) {
        		moveToLocation(nearestBroadcastAlly);
        	} else if(nearestDenPos!=null) { 
        		moveToLocation(nearestDenPos);
        	} else if(nearestBroadcastDen!=null) {
        		moveToLocation(nearestBroadcastDen);
        	} else if(savedRally!=null && myLocation.distanceSquaredTo(savedRally)>25) {
        		moveToLocation(savedRally);
        	} else {
        		if(alliesNum >= 20) {
        			moveFromLocation(averageAlliesNoScouts);
        		} else {
        			moveToLocation(averageAlliesNoScouts);
        		}
        	}
        }
    }

}
