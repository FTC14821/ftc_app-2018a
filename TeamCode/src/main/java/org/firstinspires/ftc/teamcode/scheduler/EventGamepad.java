package org.firstinspires.ftc.teamcode.scheduler;

import com.qualcomm.robotcore.hardware.Gamepad;

public class EventGamepad 
{
    String name;
    Gamepad actualGamepad;

    public EventButton dpad_up;
    public EventButton dpad_down;
    public EventButton dpad_left;
    public EventButton dpad_right;
    public EventButton a;
    public EventButton b;
    public EventButton x;
    public EventButton y;
    public EventButton start;
    public EventButton back;
    public EventButton left_bumper;
    public EventButton right_bumper;
    public EventButton left_stick_button;
    public EventButton right_stick_button;

    public float left_stick_x = 0f;
    public float left_stick_y = 0f;
    public float right_stick_x = 0f;
    public float right_stick_y = 0f;
    public float left_trigger = 0f;
    public float right_trigger = 0f;

    public EventGamepad(String name, Gamepad actualGamepad)
    {
        this.name=name;
        this.actualGamepad =actualGamepad;

        dpad_up=new EventButton(String.format("%s-dpad_up", name));
        dpad_down=new EventButton(String.format("%s-dpad_down", name));
        dpad_left=new EventButton(String.format("%s-dpad_left", name));
        dpad_right=new EventButton(String.format("%s-dpad_right", name));
        a=new EventButton(String.format("%s-a", name));
        b=new EventButton(String.format("%s-b", name));
        x=new EventButton(String.format("%s-x", name));
        y=new EventButton(String.format("%s-y", name));
        start=new EventButton(String.format("%s-start", name));
        back=new EventButton(String.format("%s-back", name));
        left_bumper=new EventButton(String.format("%s-left_bumper", name));
        right_bumper=new EventButton(String.format("%s-right_bumper", name));
        left_stick_button=new EventButton(String.format("%s-left_stick_button", name));
        right_stick_button=new EventButton(String.format("%s-right_stick_button", name));
        
        new RepeatedAction("EventGamepadRefresher-" + name)
        {
            @Override
            protected void doTask()
            {
                updateFromActualGamepad();
            }
        }.start();
    }
    
    protected void updateFromActualGamepad()
    {
        // Tell all the Button objects about the actualGamepad's boolean values
        dpad_up.update(actualGamepad.dpad_up);
        dpad_down.update(actualGamepad.dpad_down);
        dpad_left.update(actualGamepad.dpad_left);
        dpad_right.update(actualGamepad.dpad_right);
        a.update(actualGamepad.a);
        b.update(actualGamepad.b);
        x.update(actualGamepad.x);
        y.update(actualGamepad.y);
        start.update(actualGamepad.start);
        back.update(actualGamepad.back);
        left_bumper.update(actualGamepad.left_bumper);
        right_bumper.update(actualGamepad.right_bumper);
        left_stick_button.update(actualGamepad.left_stick_button);
        right_stick_button.update(actualGamepad.right_stick_button);

        // Just copy the float values
        left_stick_x = actualGamepad.left_stick_x;
        left_stick_y = actualGamepad.left_stick_y;
        right_stick_x = actualGamepad.right_stick_x;
        right_stick_y = actualGamepad.right_stick_y;
        left_trigger = actualGamepad.left_trigger;
        right_trigger = actualGamepad.right_trigger;

    }
}
