package fortress;

import battlecode.common.*;

public class ArchonActor extends RobotActor {


    MapLocation central = myLocation;
    boolean reachedCentral = false;

    RobotType typeToSpawn;
    
    int buildNum=0;

    public ArchonActor(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void setInitialVars() {
        myLocation = rc.getLocation();
        myTeam = rc.getTeam();
        myType = rc.getType();
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
    }

    public void updateRoundVars() throws GameActionException {
    	myLocation = rc.getLocation();
    	
        countNearbyRobots();
        findAverageAlliesPos();
        findNearestHostilePos();

        determineSpawnType();
    }

    public void determineSpawnType() {
        //set type to spawn
    	typeToSpawn = RobotType.TURRET;
    	
//    	if(buildNum%6==1) {
//    		typeToSpawn = RobotType.SCOUT;
//    	} else if(buildNum%3==0) {
//    		typeToSpawn = RobotType.TTM;
//    	} else if(buildNum%3==1||buildNum%3==2) {
//    		typeToSpawn = RobotType.GUARD;
//    	}
    }

    public void act() throws GameActionException {
        setInitialVars();
        broadcastInitialPosition();

        Clock.yield();
        findCentral();

        while(true) {

            updateRoundVars();
            
            if(!reachedCentral && myLocation.distanceSquaredTo(central) <= 16) {
                reachedCentral=true;
            }
            
            if(!reachedCentral) {
                moveToLocationClearIfStuck(central);
            } else {
            	if((myLocation.x+myLocation.y)%2!=0) {
            		moveToOtherTile();
            	} else {
            		buildUnit();
            	}
            }

            Clock.yield();
        }
    }

    public void buildUnit() throws GameActionException {
    	
    	if(!(rc.isCoreReady() && rc.hasBuildRequirements(typeToSpawn))) {
        	return;
        }
    	
    	
        Direction dir = myLocation.directionTo(central);
        if(dir == Direction.OMNI) {
        	dir = Direction.NORTH;
        }
        
        for(int i=0; i<8; i++) {
        	if(rc.canBuild(dir, typeToSpawn)) {
        		rc.build(dir, typeToSpawn);
        		buildNum++;
        		return;
        	}
        	dir = dir.rotateRight();
        }
    }
}