package app;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.util.Pair;

/**
 * Background communication /w MotorDaemon for real-time informations
 */
public class BackgroundCommunication extends Thread
{

    private Label position;

    private static BackgroundCommunication instance = null;

    private BackgroundCommunication()
    {
        control = ControlThreading.getInstance();
    }

    private ControlThreading control;

    private boolean motordaemonIsOnline = false;

    public float actualX;
    public float actualY;
    public float actualAngle;

    public static BackgroundCommunication getInstance()
    {
        if(instance == null) instance = new BackgroundCommunication();
        return instance;
    }

    @Override
    public void run()
    {
        while(motordaemonIsOnline)
        {
            // Actualize position
            Double[] p = control.getPosition();
            if(p != null)
            {
                actualX = p[0].floatValue();
                actualY = p[1].floatValue();
                actualAngle = p[2].floatValue();

                Platform.runLater(() -> {
                    Controller.spotRobot.setLayoutX((actualX + 1500) * Controller.ratioX -3);
                    Controller.spotRobot.setLayoutY((2000 - actualY) * Controller.ratioY -3);

                    position.setText("x = "+Float.toString(p[0].floatValue())+" ; y = "
                            + Float.toString(p[1].floatValue())+"\nθ = "+Float.toString(p[2].floatValue()));
                });
            }

            if(!control.isMoving())
            {
                control.stop();
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setMotordaemonIsOnline(boolean state)
    {
        this.motordaemonIsOnline = state;
    }

    public void setPostionLabel(Label pos) { this.position = pos; }

    public boolean getMotordaemonIsOnline()
    {
        return this.motordaemonIsOnline;
    }


}
