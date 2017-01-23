package app;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.awt.*;
import java.awt.event.ActionEvent;

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
    private Label status;

    private boolean connected;

    private ControlThreading control = ControlThreading.getInstance();

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

}
