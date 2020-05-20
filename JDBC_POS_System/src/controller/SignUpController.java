package controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class SignUpController {
    public Label lblLogin;
    public AnchorPane signUp;

    public void lblLogin_OnMouseClicked(MouseEvent mouseEvent) throws IOException {
        Parent root = FXMLLoader.load(this.getClass().getResource("/view/LoginForm.fxml"));
        Scene subScene = new Scene(root);
        Stage primaryStage = (Stage) this.signUp.getScene().getWindow();
        primaryStage.setScene(subScene);
        primaryStage.resizableProperty().setValue(false);
        primaryStage.centerOnScreen();
        Image icon = new Image(getClass().getResourceAsStream("/resource/icons8-checked-user-male-48.png"));
        primaryStage.getIcons().add(icon);
    }

}
