package app;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

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
            case UP: {
                control.upP((long) speed_slider.getValue());
                break;
            }
            case DOWN: {
                control.downP((long) speed_slider.getValue());
                break;
            }
            case LEFT: {
                control.leftP();
                break;
            }
            case RIGHT: {
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
            case UP: {
                control.upR();
                break;
            }
            case DOWN: {
                control.downR();
                break;
            }
            case LEFT: {
                control.leftR();
                break;
            }
            case RIGHT: {
                control.rightR();
                break;
            }
        }
    }

    @FXML
    void setSpeed(ActionEvent event)
    {
        speed_info.setText("Speed : "+speed_slider.getValue());
    }

    @FXML
    void connect(MouseEvent event)
    {

        connected = control.connect(ip_typed.getText());
        status.setText(connected ? "Connection successful !" : "Connection failed !");
        if(connected) ip_typed.setDisable(true);
    }

}
