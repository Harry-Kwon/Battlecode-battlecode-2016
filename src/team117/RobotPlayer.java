package team117;

import battlecode.common.*;

public class RobotPlayer {

    public static void run(RobotController rc) {

        try{
            RobotType type = rc.getType();
            RobotActor actor;
            switch(type) {
                case ARCHON:
                    actor = new ArchonActor(rc);
                    break;
                case SOLDIER:
                    actor = new SoldierActor(rc);
                    break;
                case TURRET:
                    actor = new MobileTurretActor(rc);
                    break;
                case TTM:
                	actor = new MobileTurretActor(rc);
                	break;
                case SCOUT:
                    actor = new ScoutActor(rc);
                    break;
                case GUARD:
                	actor = new GuardActor(rc);
                	break;
                default:
                    actor = new RobotActor(rc);
                    break;
            }

            actor.act();

        } catch(Exception e) {e.printStackTrace();}
    }
}
