package app;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.freedesktop.gstreamer.*;
import org.freedesktop.gstreamer.elements.AppSink;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;


public class Controller
{

    @FXML
    private Button connect_button;

    @FXML
    private Label position;

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
    private BackgroundCommunication background = BackgroundCommunication.getInstance();
    private AppSink videosink;
    private Pipeline pipe;
    private Bin bin;
    private Bus bus;
    private StringBuilder caps;
    private ImageContainer imageContainer;

    private int cursorX;
    private int cursorY;
    public final static double ratioX = 1.0;
    public final static double ratioY = 1.0;

    public static Circle spotRobot = new Circle(6);

    private ArrayList<Circle> pathSpots = new ArrayList<>();

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
        if(connected)
        {
            ip_typed.setDisable(true);
            background.setMotordaemonIsOnline(true);
            background.setPostionLabel(position);
            //   background.start();

            launchMap();
        }
    }

    private void launchMap()
    {
        Stage stage = new Stage();
        stage.setTitle("Map");

        BorderPane pane = new BorderPane();
        ImageView img = new ImageView("file:map.png");

        //img.fitWidthProperty().bind(stage.widthProperty());
        img.fitHeightProperty().bind(stage.heightProperty());


        pane.setCenter(img);

        ContextMenu cm = new ContextMenu();
        MenuItem cmItem1 = new MenuItem("GOTO");
        cmItem1.setOnAction(e -> {

            pane.getChildren().removeAll(pathSpots);
            pathSpots.clear();

            String path = control.goTo((int)(cursorX/ratioX), (int)(cursorY/ratioY), 0);
            for(String p : path.split(";"))
            {
                Circle spot = new Circle(4);
                spot.setFill(Color.DARKRED);
                spot.setCenterX(4.0f);
                spot.setCenterY(4.0f);

                double x = Float.parseFloat(p.split(":")[0]) * ratioX  - 3;
                double y = Float.parseFloat(p.split(":")[1]) * ratioY - 3;

                spot.setLayoutX(x);
                spot.setLayoutY(y);

                pane.getChildren().add(spot);
                pathSpots.add(spot);
            }

            ((Circle)(pane.getChildren().get(pane.getChildren().size()-1))).setFill(Color.BLUE);
        });

        cm.getItems().add(cmItem1);

        pane.setOnContextMenuRequested(contextMenuEvent -> {
            cursorX = (int)contextMenuEvent.getX();
            cursorY = (int)contextMenuEvent.getY();
            cm.show(stage, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
        });

        spotRobot.setFill(Color.GREEN);
        spotRobot.setCenterX(6.0f);
        spotRobot.setCenterY(6.0f);


        pane.getChildren().add(spotRobot);

        stage.setScene(new Scene(pane));

        stage.show();
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
        bin = Bin.launch("udpsrc port=56988 ! application/x-rtp, media=video, encoding-name=JPEG, clock-rate=90000, payload=26 ! rtpjitterbuffer ! rtpjpegdepay ! jpegdec ! videoconvert", true);
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
        public_ip.setDisable(true);
        video.setVisible(true);
    }

    @FXML
    void stopVideo(MouseEvent event)
    {
        pipe.stop();
        control.stopCamera();
        video.setVisible(false);
        public_ip.setDisable(false);
        stop_video.setDisable(true);
        stop_video.setVisible(false);
        video_button.setDisable(false);
        video_button.setVisible(true);
    }
}
