package app;

import javafx.scene.control.ProgressBar;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;

/**
 * Threads de contrôle du véhicule ; singleton
 */
public class ControlThreading
{
    private Socket socket = null;

    private DataOutputStream oos = null;

    private static ControlThreading instance = null;

    private boolean up = false;

    private boolean down = false;

    private boolean left = false;

    private boolean right = false;

    private long lastSpeed = 0;

    private ControlThreading() {}

    public static ControlThreading getInstance()
    {
        if(instance == null) instance = new ControlThreading();

        return instance;
    }

    public synchronized boolean connect(String ip)
    {
        try
        {
            if(oos != null) oos.close();

            if(socket != null) socket.close();
        } catch (IOException e) {e.printStackTrace();}

        try {
            socket = new Socket(ip, 56987);
            oos = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;

    }

    private void stop()
    {
        send("stop");
    }

    private void resetRadius()
    {
        send("sweepstop");
    }

    private synchronized void send(String s)
    {
        if(oos == null || socket == null || !socket.isConnected()) return;

        try {
            byte[] r = Arrays.copyOfRange(s.getBytes(), 0, 1024);

            oos.write(r);
            oos.flush();

            Thread.sleep(20);
        } catch (IOException|InterruptedException e) {
            e.printStackTrace();
        }
    }

    //############################## KEY HANDLERS ############################

    public void upP(long speed)
    {
        if(down || up) return;
        up = true;

        if(speed != lastSpeed)
        {
            send("sets "+speed);
            lastSpeed = speed;
        }
        send("go");
    }

    public void upR()
    {
        up = false;
        stop();
    }

    public void downP(long speed)
    {
        if(down || up) return;
        down = true;

        if(speed != lastSpeed)
        {
            send("sets "+speed);
            lastSpeed = speed;
        }
        send("gor");
    }

    public void downR()
    {
        down = false;
        stop();
    }

    public void leftP()
    {
        if(right || left) return;
        left = true;

        send("sweepL");
    }

    public void leftR()
    {
        left = false;
        resetRadius();
    }

    public void rightP()
    {
        if(right || left) return;
        right = true;

        send("sweepR");
    }

    public void rightR()
    {
        right = false;
        resetRadius();
    }

}
