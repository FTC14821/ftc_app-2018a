package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "A: One_Mineral", group = "Tinkering")

public class Autonomous_One_Mineral extends AutonomousOpMode
{
        @Override
        void teamInit()
        {
                super.teamInit();
                robot.calibrateHook();
        }

        @Override
        void teamRun()
        {
                robot.hookUp(1, false);
                robot.turnRight(15, 1);
                robot.hookDown(1, false);
                robot.inchmove( 5, 0.4);
                robot.stop();
                robot.turnLeft(60, 1);
                robot.inchmove(30, 1);
                robot.turnLeft(90, 1);
                robot.inchmove(30, 1);
                robot.turnLeft(180, 1);
                //FLING THE MARKER
                robot.inchmove(75,1);
                //FLING THE MARKER ARM INTO THE CRATER AFTER EXTENDOING THE ARM
                robot.hookDown(1, true);
        }
}
