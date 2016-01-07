package autobots;

import battlecode.common.*;

public class GuardActor extends RobotActor {



    MapLocation spawnLocation;
    MapLocation nearestBroadcastEnemy;

    int[] blacklist;//xmin, xmax, ymin, ymax
    MapLocation blacklistCenter;
    int blacklistRadiusX, blacklistRadiusY;

    int lastRound;

    public GuardActor(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void act() throws GameActionException {
        rc.setIndicatorString(0, "SOLDIER ACTOR");
        myTeam = rc.getTeam();
        spawnLocation = rc.getLocation();

        //ZombieSpawnSchedule schedule = rc.getZombieSpawnSchedule();
        //int[] rounds = schedule.getRounds();
        //lastRound = rounds[rounds.length-1];
        lastRound = 2000;

        if(blacklist!=null) {
            rc.setIndicatorString(0, "CENTER"+blacklistCenter.x+", "+blacklistCenter.y);
        }

        while(true) {
            myLocation = rc.getLocation();
            countNearbyRobots();
            findAverageAlliesPos();
            findNearestHostilePos();

            if(!nearestHostilePos.equals(myLocation)) {
                attack(nearestHostilePos);
            }

            readBroadcasts();

            if(rc.getRoundNum() < lastRound+300) {
                move();
            } else {
                attackEnemyHQ();
            }

            Clock.yield();
        }
    }

    public void attackEnemyHQ()  throws GameActionException {
        if(enemiesNum+zombiesNum>0) {
            moveToLocationClear(nearestHostilePos);
        } else {
            if(nearestBroadcastEnemy!=null) {
                moveToLocationClear(nearestBroadcastEnemy);
            } else {
                moveToLocationClear(blacklistCenter);
            }
        }
    }

    public void readBroadcasts() throws GameActionException {
        Signal[] signals = rc.emptySignalQueue();
        nearestBroadcastEnemy = null;

        int bestDist = 2000000;

        for(Signal s : signals) {
            if(s.getTeam() != myTeam) {
                continue;
            }

            int[] msg = s.getMessage();
            if(msg==null) {
                continue;
            }
            MapLocation loc = new MapLocation(msg[1]%1000, msg[1]/1000);
            if(msg[0]==0) {
                int dist = myLocation.distanceSquaredTo(loc);
                if(dist < bestDist) {
                    bestDist = dist;
                    nearestBroadcastEnemy = new MapLocation(loc.x, loc.y);
                }
            } else if(msg[0]==1) {
                addToBlacklist(loc);
            }
            
        }
    }

    public void addToBlacklist(MapLocation loc) {
        int x=loc.x;
        int y=loc.y;
        if(blacklist==null) {
            blacklist = new int[]{x, x, y, y};
        } else {
            if(x<blacklist[0]) {
            blacklist[0]=x;
            }
            if(x>blacklist[1]) {
                blacklist[1]=x;
            }
            if(y<blacklist[2]) {
                blacklist[2]=y;
            }
            if(y>blacklist[3]) {
                blacklist[3]=y;
            }
        }
        int aveX = blacklist[0]+blacklist[1];
        aveX/=2;
        int aveY = blacklist[2]+blacklist[3];
        aveY/=2;
        blacklistCenter = new MapLocation(aveX, aveY);
        blacklistRadiusX = blacklist[1]-aveX+9;
        blacklistRadiusY = blacklist[3]-aveY+9;
    }

    public boolean isInsideBlacklist() {
        if(blacklist==null) {
            return false;
        }

        int xDist = myLocation.x - blacklistCenter.x;
        xDist *= xDist;

        int yDist = myLocation.y - blacklistCenter.y;
        yDist *= yDist;

        int xRadSquared = blacklistRadiusX*blacklistRadiusX;
        int yRadSquared = blacklistRadiusY*blacklistRadiusY;

        if(xDist/xRadSquared+yDist/yRadSquared<=1) {
            return true;
        }
        return false;


    }

    public void move() throws GameActionException {

        if(isInsideBlacklist()) {
            moveFromLocationClearIfStuck(blacklistCenter);
            return;
        } 

        if(enemiesNum+zombiesNum==0) {

            if(nearestBroadcastEnemy != null) {
                moveToLocationClearIfStuck(nearestBroadcastEnemy);
            } else {
                if(alliesNum < 15) {
                    moveToLocationClear(averageAlliesPos);
                } else {
                    moveFromLocationClear(averageAlliesPos);
                }
            }            

        } else {
            if(enemiesNum+zombiesNum>alliesNum) {
                //move away from closest enemy
                moveFromLocationClearIfStuck(nearestHostilePos);
            } else {
                attack(nearestHostilePos);
                moveToLocationClearIfStuck(nearestHostilePos);
            }
        }
    }


}