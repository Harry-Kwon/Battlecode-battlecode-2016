package someTurretBot;

import battlecode.common.*;

public class TurretActor extends RobotActor {

    MapLocation nearestAttackableEnemy;
    Signal[] signals;

    MapLocation nearestBroadcastEnemy;
    MapLocation nearestBroadcastAlly;

    public TurretActor(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void act() throws GameActionException {
        rc.setIndicatorString(0, "TURRET ACTOR");
        myTeam = rc.getTeam();

        while(true) {
            myLocation = rc.getLocation();
            signals = rc.emptySignalQueue();

            countNearbyRobots();
            findAverageAlliesPos();
            findNearestHostilePos();
            readBroadcasts();
            findNearestAttackableEnemy();

            if(nearestAttackableEnemy!=null) {
                attack(nearestAttackableEnemy);
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


}