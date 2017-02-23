package app;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import org.freedesktop.gstreamer.*;
import org.freedesktop.gstreamer.elements.AppSink;

import java.nio.ByteOrder;


public class Controller
{

    @FXML
    private Button connect_button;

    @FXML
    private Label speed_info;

    @FXML
    private Slider speed_slider;

    @FXML
    private TextField ip_typed;

    @FXML
    private ImageView video;

    @FXML
    private Button video_button;

    @FXML
    private Button stop_video;

    @FXML
    private Label status;

    @FXML
    private TextField public_ip;

    private boolean connected;

    private ControlThreading control = ControlThreading.getInstance();
    private AppSink videosink;
    private Pipeline pipe;
    private Bin bin;
    private Bus bus;
    private StringBuilder caps;
    private ImageContainer imageContainer;

    @FXML
    void onKeyPressed(KeyEvent event)
    {
        if(!connected) return;
        switch(event.getCode())
        {
            case Z: {
                control.upP((long) speed_slider.getValue());
                break;
            }
            case S: {
                control.downP((long) speed_slider.getValue());
                break;
            }
            case Q: {
                control.leftP();
                break;
            }
            case D: {
                control.rightP();
                break;
            }
        }
    }

    @FXML
    void onKeyReleased(KeyEvent event)
    {
        if(!connected) return;
        switch(event.getCode())
        {
            case Z: {
                control.upR();
                break;
            }
            case S: {
                control.downR();
                break;
            }
            case Q: {
                control.leftR();
                break;
            }
            case D: {
                control.rightR();
                break;
            }
        }
    }

    @FXML
    void setSpeed(MouseEvent event)
    {
        speed_info.setText("Speed : "+(long)speed_slider.getValue());
    }

    @FXML
    void connect(MouseEvent event)
    {
        connected = control.connect(ip_typed.getText());
        status.setText(connected ? "Connection successful !" : "Connection failed !");
        if(connected) ip_typed.setDisable(true);
    }

    @FXML
    void launchVideo(MouseEvent event)
    {
        control.startCamera(public_ip.getText());

        videosink = new AppSink("GstVideoComponent");
        videosink.set("emit-signals", true);
        AppSinkListener GstListener = new AppSinkListener();
        videosink.connect(GstListener);
        caps = new StringBuilder("video/x-raw, ");
        // JNA creates ByteBuffer using native byte order, set masks according to that.
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            caps.append("format=BGRx");
        } else {
            caps.append("format=xRGB");
        }
        videosink.setCaps(new Caps(caps.toString()));
        videosink.set("max-buffers", 5000);
        videosink.set("drop", true);
        bin = Bin.launch("udpsrc address=localhost port=56988 ! application/x-rtp, media=video, encoding-name=JPEG, clock-rate=90000, payload=26 ! rtpjitterbuffer ! rtpjpegdepay ! jpegdec ! videoconvert", true);
        pipe = new Pipeline();
        pipe.addMany(bin, videosink);

        bin.link(videosink);
        imageContainer = GstListener.getImageContainer();
        imageContainer.addListener((observable, oldValue, newValue) -> Platform.runLater(() -> video.setImage(newValue)));

        bus = pipe.getBus();
        bus.connect((Bus.MESSAGE) (arg0, arg1) -> System.out.println(arg1.getStructure()));
        pipe.play();
        video_button.setVisible(false);
        video_button.setDisable(true);
        stop_video.setVisible(true);
        stop_video.setDisable(false);
        video.setVisible(true);
    }

    @FXML
    void stopVideo(MouseEvent event)
    {
        pipe.stop();
        control.stopCamera();
        video.setVisible(false);
        stop_video.setDisable(true);
        stop_video.setVisible(false);
        video_button.setDisable(false);
        video_button.setVisible(true);
    }
}
