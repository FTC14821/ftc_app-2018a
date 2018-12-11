package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "A: One_Mineral", group = "Tinkering")

public class Autonomous_One_Mineral extends AutonomousOpMode
{
        @Override
        void teamInit()
        {
                super.teamInit();
                robot.calibrateHook(opmodeAction);
        }

        @Override
        void teamRun()
        {
                robot.hookUp(opmodeAction, 1, false);
                robot.turnRight(opmodeAction, 15, 1);
                robot.hookDown(opmodeAction, 1, false);
                robot.inchmove( opmodeAction, 5, 0.4);
                robot.stop(opmodeAction, false);
                robot.turnLeft(opmodeAction, 60, 1);
                robot.inchmove(opmodeAction, 30, 1);
                robot.turnLeft(opmodeAction, 90, 1);
                robot.inchmove(opmodeAction, 30, 1);
                robot.turnLeft(opmodeAction, 180, 1);
                //FLING THE MARKER
                robot.inchmove(opmodeAction, 75,1);
                //FLING THE MARKER ARM INTO THE CRATER AFTER EXTENDOING THE ARM
                robot.hookDown(opmodeAction, 1, true);
        }
}
