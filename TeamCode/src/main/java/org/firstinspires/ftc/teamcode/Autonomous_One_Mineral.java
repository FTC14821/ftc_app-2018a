package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "A: One_Mineral", group = "Tinkering")

public class Autonomous_One_Mineral extends AutonomousOpMode
{
        @Override
        void teamInit()
        {
                super.teamInit();
                new OngoingAction_CalibrateHook().waitUntilFinished();
        }

        @Override
        public void teamRun()
        {
                robot.startMovingHookUp(1);
                robot.startTurningRight(15).waitUntilFinished();
                robot.startMovingHookDown(1);
                robot.startInchMove( 5, 0.4).waitUntilFinished();
                robot.stopDrivingWheels_raw();
                robot.startTurningLeft(60).waitUntilFinished();
                robot.startInchMove(30, 1).waitUntilFinished();
                robot.startTurningLeft(90).waitUntilFinished();
                robot.startInchMove(30, 1).waitUntilFinished();
                robot.startTurningLeft(180).waitUntilFinished();
                //FLING THE MARKER
                robot.startInchMove(75,1).waitUntilFinished();
                //FLING THE MARKER ARM INTO THE CRATER AFTER EXTENDOING THE ARM
                robot.startMovingHookDown(1).waitUntilFinished();
        }
}
