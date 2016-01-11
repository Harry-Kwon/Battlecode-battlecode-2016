package team117;

import battlecode.common.*;

public class ArchonActor extends RobotActor {


    MapLocation central = myLocation;
    boolean reachedCentral = false;
    
    MapLocation nearestBroadcastEnemy;
	MapLocation nearestBroadcastAlly;
	MapLocation nearestBroadcastDen;
	MapLocation nearestBroadcastRally;
	MapLocation savedRally;
    double lastHealth;

    RobotType typeToSpawn;

    int state = 0;
    
    Signal[] signals;
    RobotType myType;
    
    boolean isCentral = false;
    int sent = 0;

    public ArchonActor(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void setInitialVars() {
        myLocation = rc.getLocation();
        myTeam = rc.getTeam();
        lastHealth = rc.getHealth();
    }

    public void broadcastInitialPosition() throws GameActionException {
        rc.broadcastMessageSignal(-2, myLocation.x+1000*myLocation.y, 20000);
    }

    public void findCentral() throws GameActionException {
        int archons = 1;
        MapLocation[] archonPositions = new MapLocation[6];
        archonPositions[0] = myLocation;

        Signal[] signals = rc.emptySignalQueue();
        for(Signal s : signals) {
            int[] msg = s.getMessage();
            if(s.getTeam() != myTeam || msg==null||msg[0]!=-2) {
                continue;
            }
            int x = msg[1]%1000;
            int y = msg[1]/1000;
            archonPositions[archons++] = new MapLocation(x, y);
        }

        int bestArchon = 0;
        int bestScore = 9999999;
        for(int i=0; i<archons; i++) {
            int score = 0;
            for(int j=0; j<archons; j++) {
                score += archonPositions[i].distanceSquaredTo(archonPositions[j]);
            }

            if(score <= bestScore) {
                if(score==bestScore) {
                    if(archonPositions[i].x+1000*archonPositions[i].y > archonPositions[bestArchon].x+1000*archonPositions[bestArchon].y) {
                        continue;
                    }
                }
                bestScore = score;
                bestArchon = i;
            }
        }

        central = archonPositions[bestArchon];
        
        if(myLocation.equals(central)) {
        	isCentral = true;
        }
    }

    public void updateRoundVars() throws GameActionException {
    	myType = rc.getType();
    	myLocation = rc.getLocation();
    	signals = rc.emptySignalQueue();
    	
        countNearbyRobots();
        findAverageAlliesPos();
        findNearestHostilePos();
        readBroadcasts();

        //updateState();

        determineSpawnType();
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
            	 
            } else if(msg[0]==0) {
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

    public void updateState() throws GameActionException {
        /*if(allyTurretsNum>40) {
            state = 1;
        } else {
            state = 0;
        }*/
        state = 0;
    }

    public void determineSpawnType() {
        //set type to spawn

        if(allyScoutsNum==0 && (allyGuardsNum+allyTurretsNum)>0) {
            typeToSpawn = RobotType.SCOUT;
        } else {
        	if(allyGuardsNum < (allyTurretsNum+allyTTMNum)*2) {
        		typeToSpawn = RobotType.GUARD;
        	} else {
        		typeToSpawn = RobotType.TURRET;
        	}
        }
       
    }
    
    public void act() throws GameActionException {
        setInitialVars();
        broadcastInitialPosition();

        Clock.yield();
        findCentral();

        while(true) {

            updateRoundVars();
            findAverageAlliesNoScouts();
            
            if(!reachedCentral && myLocation.distanceSquaredTo(central) <= 36) {
                reachedCentral=true;
            }
            
            broadcast();
            
            if(!reachedCentral) {
                moveToLocationClear(central);
            } else {
            	
            	move();
                
            }

            Clock.yield();
        }
    }
    
    public void broadcast() throws GameActionException {
    	sent = 0;
    	if(isCentral) {
    		if(rc.getRoundNum()%100==30) {
    			rc.broadcastMessageSignal(3, averageAlliesNoScouts.x+1000*averageAlliesNoScouts.y, 20000);
    			sent = 20;
    		} else {
    			rc.broadcastMessageSignal(3, averageAlliesNoScouts.x+1000*averageAlliesNoScouts.y, myType.sensorRadiusSquared*2);
    			sent++;
    		}
        }
        
        broadcastEnemies();
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
    	}
    }
    
    public void move() throws GameActionException {
        repairAdjacentRobots();
        
        if(hostilesNearby()) {
        	if(isCentral) {
                moveFromLocationClearIfStuck(nearestHostilePos);
                return;
        	} else if(savedRally!=null) {
        		moveToLocationClearIfStuck(savedRally);
        	} else {
        		moveFromLocationClearIfStuck(nearestHostilePos);
        	}
        	return;
        }
        
        if(rc.hasBuildRequirements(typeToSpawn)) {
            buildActions();
        } else {
        	findNearestTurret();
        	if(nearestTurretDist>=13 && nearestTurretPos != null) {
				moveToLocationClearIfStuck(nearestTurretPos);
        	} else {
	            findNearestPartCache();
	            findNearestNeutral();
	            if(nearestPartCache!=null) {
	            	moveToLocationClearIfStuck(nearestPartCache);
	            } else if(nearestNeutralPos!=null) {
	            	if(myLocation.distanceSquaredTo(nearestNeutralPos) <= 3) {
	            		if(rc.isCoreReady()) {
	            			rc.activate(nearestNeutralPos);
	            		}
	            		return;
	            	} else {
	            		moveToLocationClearIfStuck(nearestNeutralPos);
	            	}
	            } else if(!repairAllies()){
	            	if(savedRally!=null) {
	            		moveToLocationClearIfStuck(savedRally);
	            	} else {
	            		moveToLocationClearIfStuck(averageAlliesNoScouts);
	            	}
	            }
	        }
        }
        	
    }
    
    MapLocation nearestPartCache;
    int nearestPartCacheDist;
    
    public void findNearestPartCache() throws GameActionException {
    	nearestPartCache = null;
    	nearestPartCacheDist = 2000000;
    	
    	for(int x=-5; x<=5; x++) {
			for(int y=-5; y<=5; y++) {
				MapLocation loc = new MapLocation(myLocation.x+x, myLocation.y+y);
				if(rc.canSense(loc) && rc.senseParts(loc)>0.0) {
					int dist = x*x+y*y;
					if(dist < nearestPartCacheDist) {
						nearestPartCacheDist = dist;
						nearestPartCache = new MapLocation(loc.x, loc.y);
					}
				}
			}
    	}
    	
    }
    
    MapLocation nearestNeutralPos;
    int nearestNeutralDist;
    
    public void findNearestNeutral() throws GameActionException {
    	nearestNeutralPos = null;
    	int nearestNeutralDist = 2000000;
    	
    	RobotInfo[]neutrals = rc.senseNearbyRobots(myType.sensorRadiusSquared, Team.NEUTRAL);
    	
    	for(RobotInfo info : neutrals) {
    		int dist = myLocation.distanceSquaredTo(info.location);
    		if(dist < nearestNeutralDist) {
    			nearestNeutralDist = dist;
    			nearestNeutralPos = new MapLocation(info.location.x, info.location.y);
    		}
    	}
    }
    
    public boolean repairAllies() throws GameActionException {
    	
        MapLocation target = null;
        int bestDist = 9999999;
        for(RobotInfo r : alliesInfo) {
            if(r.health < r.maxHealth && r.type!=RobotType.ARCHON) {
                int dist = myLocation.distanceSquaredTo(r.location);

                if(dist < bestDist) {
                    target = new MapLocation(r.location.x, r.location.y);
                    bestDist = dist;
                }
            }
        }

        if(target!=null ) {
            rc.setIndicatorString(0, "REPAIRING"+target.x+", "+target.y);
            moveToLocationClearIfStuck(target);
            return(true);
        } else {
            return(false);
        }
    }

    public void repairAdjacentRobots() throws GameActionException {
        Direction dir = Direction.NORTH;

        for(int i=0; i<8; i++) {
            MapLocation loc = myLocation.add(dir);
            RobotInfo info = rc.senseRobotAtLocation(loc);
            if(info != null && info.team==myTeam && info.type!=RobotType.ARCHON) {
                if(info.health < info.maxHealth) {
                    rc.repair(loc);
                    break;
                }
            }
            dir=dir.rotateRight();
        }
    }

    public boolean hostilesNearby() {
        if(nearestHostilePos!=null && nearestHostileDist<=9) {
            return true;
        }
        return false;
    }

    public void buildActions() throws GameActionException {

        //check requirements
        boolean hasRequirements = rc.hasBuildRequirements(typeToSpawn);

        boolean spawned = false;

        MapLocation spawnLocation = closestSpawnableTile();
        Direction spawnDir = myLocation.directionTo(spawnLocation);
        if(spawnLocation==null) {
            return;
        }

        if(hasRequirements && myLocation.add(spawnDir).equals(spawnLocation)) {

            if(spawnUnit(typeToSpawn, spawnDir)) {
                spawned = true;
            }
        }

        if(!rc.isCoreReady()) {
            return;
        }
        
        if(!spawned) {
            moveToLocationClearIfStuck(spawnLocation);
        }
    }

    //odd tiles only
    public MapLocation closestSpawnableTile() throws GameActionException {
        MapLocation bestLoc = null;
        int bestDist = 999999999;

        for(int x=-6; x<=6; x++) {
            for(int y=-6; y<=6; y++) {
                
                MapLocation loc = new MapLocation(myLocation.x+x, myLocation.y+y);

                if(rc.canSenseLocation(loc) && rc.onTheMap(loc) && rc.senseRobotAtLocation(loc)==null && rc.senseRubble(loc)<=50.0) {
//                    if( (loc.y/2+loc.x)%3==0 && loc.y%2==0) {
                        //int dist = averageAlliesPos.distanceSquaredTo(loc);
                        int dist = myLocation.distanceSquaredTo(loc);
                		if(dist < bestDist) {
                            bestDist=dist;
                            bestLoc=new MapLocation(loc.x, loc.y);
                        }
//                    }
                    
                }

            }
        }

        //if(!bestLoc.equals(myLocation)) {
        //    System.out.println("  "+bestLoc.x+","+bestLoc.y+" | "+((bestLoc.x+bestLoc.y)%2==1));
        //}

        if(bestLoc!=null) {
            rc.setIndicatorString(1, ""+((bestLoc.y+bestLoc.x)%3==1));
        }

        return(bestLoc);

    }

    public boolean spawnUnit(RobotType type, Direction dir) throws GameActionException {
        if(!rc.isCoreReady()) {
            return false;
        }

        if(rc.hasBuildRequirements(type)) {
            if(rc.canBuild(dir, type)) {
                rc.build(dir, type);
                return true;
            }
        }
        return false;
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