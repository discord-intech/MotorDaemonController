package app;

import javafx.scene.control.ProgressBar;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Threads de contrôle du véhicule ; singleton
 */
public class ControlThreading
{

    private final int speedProgress = 50; // inc/sec out of 255 incs

    private final int directionProgress =5000; // inc/sec out of 1000000 incs

    private Socket socket = null;

    private DataOutputStream oos = null;

    private static ControlThreading instance = null;

    private Thread upThread;

    private boolean up = false;

    private Thread downThread;

    private boolean down = false;

    private Thread leftThread;

    private boolean left = false;

    private Thread rightThread;

    private boolean right = false;

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
        send("cr 1000000");
    }

    private synchronized void send(String s)
    {
        if(oos == null || socket == null || !socket.isConnected()) return;

        try {
            oos.writeBytes(s+"\r");
            oos.flush();

            Thread.sleep(20);
        } catch (IOException|InterruptedException e) {
            e.printStackTrace();
        }
    }

    //############################## KEY HANDLERS ############################

    public void upP(ProgressBar bar)
    {
        if(down || up) return;
        up = true;
        upThread = new Thread(() -> {
            int progress = 0;
            while(up)
            {
                progress += speedProgress;
                if(progress > 255) progress = 255;

                bar.setProgress(progress / 255.0);

                send("sets "+(int)((progress/255.0) * 5000));
                send("d "+500);

                try {
                    Thread.sleep((long) (1000.0 / (255./speedProgress)));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        upThread.start();
    }

    public void upR()
    {
        up = false;
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        stop();
    }

    public void downP(ProgressBar bar)
    {
        if(down || up) return;
        down = true;
        downThread = new Thread(() -> {
            int progress = 0;
            while(down)
            {
                progress += speedProgress;
                if(progress > 255) progress = 255;

                bar.setProgress(progress / 255.0);

                send("sets "+(int)((progress/255.0) * 5000));
                send("d "+(-500));

                try {
                    Thread.sleep((long) (1000.0 / (255./speedProgress)));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        downThread.start();
    }

    public void downR()
    {
        down = false;
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        stop();
    }

    public void leftP(ProgressBar bar)
    {
        if(right || left) return;
        left = true;
        leftThread = new Thread(() -> {
            int progress = 0;
            while(left)
            {
                progress += directionProgress;
                if(progress > 1000000) progress = 1000000;

                bar.setProgress(progress / 1000000.0);

                send("cr "+(int)(-(progress-1000000)));

                try {
                    Thread.sleep((long) (1000.0 / (1000000./directionProgress)));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        leftThread.start();
    }

    public void leftR()
    {
        left = false;
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        resetRadius();
    }

    public void rightP(ProgressBar bar)
    {
        if(right || left) return;
        right = true;
        rightThread = new Thread(() -> {
            int progress = 0;
            while(right)
            {
                progress += directionProgress;
                if(progress > 1000000) progress = 1000000;

                bar.setProgress(progress / 1000000.0);
                send("cr "+(int)(progress-1000000));

                try {
                    Thread.sleep((long) (1000.0 / (1000000.0/directionProgress)));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        rightThread.start();
    }

    public void rightR()
    {
        right = false;
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        resetRadius();
    }

}
