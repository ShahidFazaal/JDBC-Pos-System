package controller;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class LoadingController {
    public AnchorPane loading;

    public void initialize() {
        new Loading().start();


    }

    //============================= Splash Screen Thread ======================================
    class Loading extends Thread{
        public void run(){
            try {
                Thread.sleep(3000);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        Parent root = null;
                        try {
                            root = FXMLLoader.load(this.getClass().getResource("/view/Console.fxml"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Scene mainScene = new Scene(root);
                        Stage primaryStage = new Stage();
                        primaryStage.setScene(mainScene);
                        primaryStage.setTitle("Pos System");
                        primaryStage.resizableProperty().setValue(false);
                        Image icon = new Image(getClass().getResourceAsStream("/resource/icons8-cash-register-64.png"));
                        primaryStage.getIcons().add(icon);
                        primaryStage.show();

                        loading.getScene().getWindow().hide();

                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    // ============================ End ==============================================================

}
