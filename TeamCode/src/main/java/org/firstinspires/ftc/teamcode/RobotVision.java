/* Copyright (c) 2018 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.Func;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer.CameraDirection;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This came from ConceptTensorFlowObjectDetectionOpMode
 *
 */
public class RobotVision  {
    private static final double RECOGNITION_CONFIDENCE_STANDARD = 0.80;

    public enum DETECTABLE_OBJECTS {GOLD, SILVER};

    private static final String TFOD_MODEL_ASSET = "RoverRuckus.tflite";
    private static final String LABEL_GOLD_MINERAL = "Gold Mineral";
    private static final String LABEL_SILVER_MINERAL = "Silver Mineral";

    // Store the most recent detections three different ways
    List<Recognition> objectsFromLeftToRight = new ArrayList<>();
    List<DETECTABLE_OBJECTS> objectColorsFromLeftToRight = new ArrayList<>();
    String objectColorStringFromLeftToRight="";

    /*
     * IMPORTANT: You need to obtain your own license key to use Vuforia. The string below with which
     * 'parameters.vuforiaLicenseKey' is initialized is for illustration only, and will not function.
     * A Vuforia 'Development' license key, can be obtained free of charge from the Vuforia developer
     * web site at https://developer.vuforia.com/license-manager.
     *
     * Vuforia license keys are always 380 characters long, and look as if they contain mostly
     * random data. As an example, here is a example of a fragment of a valid key:
     *      ... yIgIzTqZ4mWjk9wd3cZO9T1axEqzuhxoGlfOOI2dRzKS4T0hQ8kT ...
     * Once you've obtained a license key, copy the string from the Vuforia web site
     * and paste it in to your code on the next line, between the double quotes.
     */
    private static final String VUFORIA_KEY = "AY7PZCH/////AAABmaXSDk/F8Ur+q894HBt3xnAzDKfKpD4KX2HyyvQqWLuwdcswUmGmlRWQQQN5axyGDrPtUKa3hQFjFnlmJvfnV7UW/rs1vVnkMPldY5dDnpShDaWp/hYK9Z5ClhqHVxC4Mu7hMmWx+e3Hs/E2Zu68zV2WCFDU6q0mjYibSOBmcCGaEjvi5SJkMZUSz7dtWl8XXpBNKteP7ww8I87Wz+h2m1ntchdwZ4F2LIBdig+BreMF8T+LVc9sdTbXPLVYqFHl4+42No9QAFyjQYxQlVng7568NQvARinez+zhSCQW+WxNUE50qa9XXIed42hyVbzpWIrX/DBnp4i7MuWg+GHPVjg4WXkMF29njCP+AqV8hjkp";

    /**
     * {@link #vuforia} is the variable we will use to store our instance of the Vuforia
     * localization engine.
     */
    private VuforiaLocalizer vuforia;

    /**
     * {@link #tfod} is the variable we will use to store our instance of the Tensor Flow Object
     * Detection engine.
     */
    private TFObjectDetector tfod;
    boolean tfodIsActive = false;

    Robot robot;

    public RobotVision(ActionTracker action, Robot robot)
    {
        this.robot = robot;
    }

    public void init(ActionTracker action) {
        initTelemtry();

        if (!ClassFactory.getInstance().canCreateTFObjectDetector())
        {
            RobotLog.ww(Robot.ROBOT_TAG, "Phone not compatible with Object Detection");
            return;
        }

        // The TFObjectDetector uses the camera frames from the VuforiaLocalizer, so we create that
        // first.
        initVuforia();
        initTfod();
    }

    private void initTelemtry()
    {
        robot.telemetry.addLine("Vision")
                .addData("", new Func<String>() {
                    @Override
                    public String value() {
                        String status;
                        if (vuforia == null)
                            status = "Vuforia failure";
                        else if (tfod == null)
                            status = "TFObjectDetector failure";
                        else if (!tfodIsActive)
                            status = "Vision ready but inactive";
                        else
                            status = "Active";

                        return robot.opMode.saveTelemetryData(
                                "Vision",
                                "%s: #%d, Colors L->R: %s",
                                status, objectsFromLeftToRight.size(), objectColorStringFromLeftToRight);
                    }
                });
    }

    public boolean isReady()
    {
        return tfod != null;
    }


    public void activate(ActionTracker opmodeAction)
    {
        if (!isReady())
        {
            RobotLog.e(Robot.ROBOT_TAG, "Cannot activate vision: not initialized or init failed");
            return;
        }

        if (!tfodIsActive)
            tfod.activate();
        tfodIsActive = true;
    }

    public void deactivate(ActionTracker callingAction)
    {
        ActionTracker action = callingAction.startChildAction("DecativatingVision", null);
        if (tfodIsActive)
            tfod.deactivate();

        tfodIsActive = false;
        action.finish();
    }

    public void loop(ActionTracker action)
    {
        if (!tfodIsActive)
            return;

        // getUpdatedRecognitions() will return null if no new information is available since
        // the last time that call was made.
        List<Recognition> updatedRecognitions = tfod.getUpdatedRecognitions();

        if (updatedRecognitions != null)
        {
            //RobotLog.ww(Robot.ROBOT_TAG, "Recognitions: %s", updatedRecognitions);
            Collections.sort(updatedRecognitions, new Comparator<Recognition>()
            {
                @Override
                public int compare(Recognition object1, Recognition object2)
                {
                    if (object1.getTop() < object2.getTop())
                        return -1;
                    else if (object1.getTop() > object2.getTop())
                        return +1;
                    else
                        return 0;
                }
            });

            rememberLatestObjectRecogniations(action, updatedRecognitions);
        }
    }

    private void rememberLatestObjectRecogniations(ActionTracker callingAction, List<Recognition> updatedRecognitions)
    {
        // We save the objects three different ways (as Recogitions, as enumerated colors and as a string
        objectsFromLeftToRight.clear();

        for ( Recognition recog : updatedRecognitions )
        {
            if (recog.getConfidence() > RECOGNITION_CONFIDENCE_STANDARD)
            {
                callingAction.setStatus("Found object with sufficient recognition: %s", recog);
                objectsFromLeftToRight.add(recog);
            }
            else
            {
                callingAction.setStatus("Found object with insufficient recognition: %s", recog);
            }
        }

        callingAction.setStatus("%d out of %d recognizied objects met recognition standard %.2f",
            objectsFromLeftToRight.size(), updatedRecognitions.size(), RECOGNITION_CONFIDENCE_STANDARD);

        objectColorsFromLeftToRight.clear();

        StringBuilder objectColorStringBuilder = new StringBuilder();
        for (Recognition object : objectsFromLeftToRight)
        {
            if (object.getLabel().equalsIgnoreCase(LABEL_GOLD_MINERAL))
            {
                objectColorStringBuilder.append('G');
                objectColorsFromLeftToRight.add(DETECTABLE_OBJECTS.GOLD);
            }
            else if (object.getLabel().equalsIgnoreCase(LABEL_SILVER_MINERAL))
            {
                objectColorStringBuilder.append('S');
                objectColorsFromLeftToRight.add(DETECTABLE_OBJECTS.SILVER);
            }
            else
                objectColorStringBuilder.append('?');
        }
        objectColorStringFromLeftToRight =  objectColorStringBuilder.toString();

    }


    /**
     * Initialize the Vuforia localization engine.
     */
    private void initVuforia() {
        /*
         * Configure Vuforia by creating a Parameter object, and passing it to the Vuforia engine.
         */
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();

        parameters.vuforiaLicenseKey = VUFORIA_KEY;
        parameters.cameraDirection = CameraDirection.FRONT;

        //  Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters);

        // Loading trackables is not necessary for the Tensor Flow Object Detection engine.
    }

    /**
     * Initialize the Tensor Flow Object Detection engine.
     */
    private void initTfod() {
        int tfodMonitorViewId = robot.hardwareMap.appContext.getResources().getIdentifier(
            "tfodMonitorViewId", "id", robot.hardwareMap.appContext.getPackageName());
        TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);
        tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
        tfod.loadModelFromAsset(TFOD_MODEL_ASSET, LABEL_GOLD_MINERAL, LABEL_SILVER_MINERAL);
    }
}
