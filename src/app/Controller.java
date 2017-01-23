package app;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public class Controller {

    @FXML
    private ProgressBar up_bar;

    @FXML
    private ProgressBar down_bar;

    @FXML
    private ProgressBar left_bar;

    @FXML
    private ProgressBar right_bar;

    @FXML
    private Button connect_button;

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
                control.upP(up_bar);
                break;
            }
            case DOWN: {
                control.downP(down_bar);
                break;
            }
            case LEFT: {
                control.leftP(left_bar);
                break;
            }
            case RIGHT: {
                control.rightP(right_bar);
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
                up_bar.setProgress(0);
                break;
            }
            case DOWN: {
                control.downR();
                down_bar.setProgress(0);
                break;
            }
            case LEFT: {
                control.leftR();
                left_bar.setProgress(0);
                break;
            }
            case RIGHT: {
                control.rightR();
                right_bar.setProgress(0);
                break;
            }
        }
    }


    @FXML
    void connect(MouseEvent event)
    {

        connected = control.connect(ip_typed.getText());
        status.setText(connected ? "Connection successful !" : "Connection failed !");
        if(connected) ip_typed.setDisable(true);
    }

}
