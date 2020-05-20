package controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import util.DBConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class LoginFormController {
    public AnchorPane login;
    public Label lblSignUp;
    public JFXButton btnSignIn;
    public JFXTextField txtUsername;
    public JFXPasswordField txtPassword;
    public Label txtWarning;
    static String un;

    public void initialize(){


    }




    public void signUp_OnMouseClicked(MouseEvent mouseEvent) throws IOException {
        Parent root = FXMLLoader.load(this.getClass().getResource("/view/SignUpForm.fxml"));
        Scene subScene = new Scene(root);
        Stage primaryStage = (Stage) this.login.getScene().getWindow();
        primaryStage.setScene(subScene);
        Image icon = new Image(getClass().getResourceAsStream("/resource/icons8-add-user-male-96.png"));
        primaryStage.getIcons().add(icon);
        primaryStage.setTitle("SignUp");
        primaryStage.centerOnScreen();

    }

    int count = 0;
    public void btnSignIn_OnAction(ActionEvent event) throws SQLException, IOException, ParseException {

        String userName = txtUsername.getText();
        String password = txtPassword.getText();
        PreparedStatement pstm = DBConnection.getInstance().getConnection().prepareStatement("SELECT * FROM users WHERE username=? AND password=?");
        pstm.setObject(1, userName);
        pstm.setObject(2, password);
        ResultSet rst = pstm.executeQuery();

        if (rst.next()){
            un = rst.getString(2);
            if (rst.getString("status").equals("notActive")){
                new Alert(Alert.AlertType.INFORMATION,"The User is not activated by the admin, Please contact the admin for further information", ButtonType.OK).show();
                txtPassword.clear();
                txtUsername.clear();
                txtUsername.requestFocus();
                return;
            }

            Parent root = FXMLLoader.load(this.getClass().getResource("/view/Loading.fxml"));
            Scene subScene = new Scene(root);
            Stage primaryStage = (Stage) this.login.getScene().getWindow();
            primaryStage.resizableProperty().setValue(false);
            Image icon = new Image(getClass().getResourceAsStream("/resource/icons8-checked-user-male-48.png"));
            primaryStage.getIcons().add(icon);
            primaryStage.setTitle("Loading please wait!");
            primaryStage.setScene(subScene);
            primaryStage.centerOnScreen();

            un = rst.getString("userName");
        }else {
            count++;
            if (count == 4){
                txtWarning.setVisible(true);
                btnSignIn.setDisable(true);
                txtUsername.setDisable(true);
                txtPassword.setDisable(true);
                txtWarning.requestFocus();
                start();
            }
            new Alert(Alert.AlertType.ERROR,"The username or password is incorrect", ButtonType.OK).show();
            txtPassword.clear();
            txtUsername.clear();
            txtUsername.requestFocus();

        }



    }



    //================================ SET TIMER ======================================//
        static int  secondPassed;
    Timer myTimer;
    TimerTask task;
    public  void start(){
         task = new TimerTask() {
            @Override
            public void run() {
                secondPassed++;
                if (secondPassed == 10){
                    btnSignIn.setDisable(false);
                    txtWarning.setVisible(false);
                    txtUsername.setDisable(false);
                    txtPassword.setDisable(false);
                    count = 0;
                    secondPassed = 0;
                    myTimer.cancel();
                    task.cancel();

                    return;
                }

            }
        };
         myTimer = new Timer();
        myTimer.scheduleAtFixedRate(task,1000,1000);
    }

    public void txtPassword_onAction(ActionEvent event) throws ParseException, SQLException, IOException {
        btnSignIn_OnAction(event);
    }

    //==========================================================================//

}
