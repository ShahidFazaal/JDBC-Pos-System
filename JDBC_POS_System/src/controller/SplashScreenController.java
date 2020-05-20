package controller;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class SplashScreenController {
    public AnchorPane welcome;

    public void initialize(){
        new Splash().start();

    }
    //============================= Splash Screen Thread ======================================
    class Splash extends Thread{
        public void run(){
            try {
                Thread.sleep(5000);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        Parent root = null;
                        try {
                            root = FXMLLoader.load(this.getClass().getResource("/view/LoginForm.fxml"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Scene mainScene = new Scene(root);
                        Stage primaryStage = new Stage();
                        primaryStage.setScene(mainScene);
                        primaryStage.setTitle("Login");
                        primaryStage.resizableProperty().setValue(false);
                        Image icon = new Image(getClass().getResourceAsStream("/resource/icons8-checked-user-male-48.png"));
                        primaryStage.getIcons().add(icon);
                        primaryStage.show();

                        welcome.getScene().getWindow().hide();

                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    // ============================ End ==============================================================

}
