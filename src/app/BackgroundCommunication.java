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
                Platform.runLater(() -> {
                    position.setText("x = "+Float.toString(p[0].floatValue())+" ; y = "
                            + Float.toString(p[1].floatValue())+"\nθ = "+Float.toString(p[2].floatValue()));
                });
            }

            try {
                Thread.sleep(100);
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
