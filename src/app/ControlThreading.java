package app;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;
import javafx.scene.control.ProgressBar;
import javafx.util.Pair;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Threads de contrôle du véhicule ; singleton
 */
public class ControlThreading
{
    private Socket socket = null;

    private DataOutputStream oos = null;

    private BufferedReader iss = null;

    private static ControlThreading instance = null;

    private final int counterRepeatThreshhold = 6;

    private boolean up = false;

    private boolean down = false;

    private boolean left = false;

    private boolean right = false;

    private double x = 0;

    private double y = 0;

    private double o = 0;

    private int GOcounter = 0;

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
            iss = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            oos.write("motordaemon".getBytes());
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if(socket.isConnected()) return true;
        else
        {
            System.out.println("Socket rage-quit.");
            return false;
        }

    }

    public void stop()
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

        //System.out.println(s);

        try {
            byte[] r = Arrays.copyOfRange(s.getBytes(), 0, 65536);

            oos.write(r);
            oos.flush();

            Thread.sleep(20);
        } catch (IOException|InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized String goTo(int x, int y, float angle)
    {
        return sendAndReceive(
                "goto "+Integer.toString(x)+" "+Integer.toString(y)+" "+Float.toString(angle),
                1)[0];
    }

    private String[] sendAndReceive(String toSend, int numberOfLines)
    {
        if(oos == null || iss == null || socket == null || !socket.isConnected()) return null;

        send(toSend);

        String[] out = new String[numberOfLines];

        synchronized (iss)
        {
            try {

                for (int i = 0; i < numberOfLines; i++) {
                    TimeLimiter timeLimiter = new SimpleTimeLimiter();

                    out[i] = timeLimiter.callWithTimeout(iss::readLine, 5000, TimeUnit.MILLISECONDS);
                }

            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
            }
        }

        return out;
    }

    public void startCamera(String ip)
    {
        send("startcamera "+ip);
    }

    public void stopCamera()
    {
        send("stopcamera");
    }

    public Double[] getPosition()
    {
        String[] sl = sendAndReceive("pos", 1);

        if(sl == null) return null;

        String[] vals = sl[0].split(";");

        double pastX = x;
        double pastY = y;
        double pastO = o;

        try
        {
            x = Double.parseDouble(vals[0]);
            y = Double.parseDouble(vals[1]);
            o = Double.parseDouble(vals[2].replace("\r", "").replace("\n", ""));
        } catch (NumberFormatException e)
        {
            e.printStackTrace();
            x = pastX;
            y = pastY;
            o = pastO;
            return null;
        }
        return new Double[]{x,y,o};
    }

    public Double[] getPositionFast()
    {
        return new Double[]{x,y,o};
    }

    //############################## KEY HANDLERS ############################

    public void upP(long speed)
    {
        if(down || up)
        {
            GOcounter++;

            if(GOcounter < counterRepeatThreshhold) return;
        }

        GOcounter = 0;
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
        GOcounter = 0;
        stop();
    }

    public void downP(long speed)
    {
        if(down || up)
        {
            GOcounter++;

            if(GOcounter < counterRepeatThreshhold) return;
        }

        GOcounter = 0;
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
        GOcounter = 0;
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

    public void setAngle(float angle)
    {
        send("setang "+Float.toString(angle));
    }

    public void setSpeed(float speed) {
        send("sets "+Float.toString(speed));
    }

    public boolean isMoving()
    {
        return up || down;
    }
}
