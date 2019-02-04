package org.firstinspires.ftc.teamcode.scheduler;

import static org.firstinspires.ftc.teamcode.scheduler.Utils.*;

public class EventButton
{
    String name;
    // Is the button actually pressed or not. This is true for the whole time the button is pressed
    public boolean isPressed;
    // Was the button JUST pressed (True only for the first loop when isPressed is true)
    public boolean onPress;
    // Was the button JUST released (True only for the first loop when isPressed is false)
    public boolean onRelease;

    public EventButton(String name)
    {
        this.name = name;
    }

    public void update(boolean isPressed)
    {
        boolean previousIsPressed = this.isPressed;
        this.isPressed = isPressed;

        // Was there a change in state?
        if (previousIsPressed == isPressed)
        {
            // No change
            onPress=false;
            onRelease=false;
        }
        else
        {
            // Something changed
            if (isPressed)
            {
                log("Button pressed:  %s", name);
                onPress = true;
                onRelease = false;
            }
            else
            {
                log("Button released: %s", name);
                onPress = false;
                onRelease = true;
            }
        }
    }
}
