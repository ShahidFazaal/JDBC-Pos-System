package controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import util.DBConnection;
import util.ItemTM;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class ManageItemController {
    public AnchorPane manageItems;
    public TableView<ItemTM> tblItems;
    public JFXButton btnSave;
    public JFXButton btnDelete;
    public JFXTextField txtItemCode;
    public JFXTextField txtItemDescription;
    public JFXButton btnAddNewItem;
    public JFXTextField txtQTY;
    public JFXTextField txtUnitPrice;

    public void initialize() throws SQLException {
        txtItemCode.setDisable(true);
        txtItemDescription.setDisable(true);
        txtQTY.setDisable(true);
        txtUnitPrice.setDisable(true);
        tblItems.refresh();
        btnSave.setDisable(true);
        btnDelete.setDisable(false);
        loadDatabase();
        
        tblItems.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ItemTM>() {
            @Override
            public void changed(ObservableValue<? extends ItemTM> observable, ItemTM oldValue, ItemTM newValue) {
                ItemTM selectedItem = tblItems.getSelectionModel().getSelectedItem();
                if (selectedItem == null){
                    btnSave.setText("Save");
                    btnDelete.setDisable(true);
                    txtItemCode.clear();
                    txtItemDescription.clear();
                    txtQTY.clear();
                    txtUnitPrice.clear();
                    txtItemCode.setDisable(true);
                    txtQTY.setDisable(true);
                    txtItemDescription.setDisable(true);
                    txtUnitPrice.setDisable(true);
                    btnSave.setDisable(true);
                    btnAddNewItem.requestFocus();
                    return;
                }

                txtItemCode.setText(newValue.getCode());
                txtItemDescription.setText(newValue.getDescription());
                txtQTY.setText(newValue.getQtyOnHand() +"");
                txtUnitPrice.setText(newValue.getUnitPrice() +"");
                btnDelete.setDisable(false);
                btnSave.setText("Update");
                txtItemCode.setDisable(false);
                txtItemDescription.setDisable(false);
                txtQTY.setDisable(false);
                txtUnitPrice.setDisable(false);
                btnSave.setDisable(false);
            }
        });
        
        
        tblItems.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("code"));
        tblItems.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("description"));
        tblItems.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("qtyOnHand"));
        tblItems.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
    }

    public void navigateMainWindow(MouseEvent mouseEvent) throws IOException {
        URL resource = this.getClass().getResource("/view/Console.fxml");
        Parent root = FXMLLoader.load(resource);
        Scene scene = new Scene(root);
        Stage primaryStage = (Stage) (this.manageItems.getScene().getWindow());
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    public void btnNewItem_OnAction(ActionEvent event) {
        txtItemCode.setDisable(false);
        txtItemCode.clear();
        txtItemDescription.clear();
        txtQTY.clear();
        txtUnitPrice.clear();
        tblItems.getSelectionModel().clearSelection();
        txtItemDescription.setDisable(false);
        txtQTY.setDisable(false);
        txtUnitPrice.setDisable(false);
        txtItemDescription.requestFocus();
        btnSave.setDisable(false);
        generateId();
    }

    public void btnSave_onAction(ActionEvent event) throws SQLException {
        if (txtItemDescription.getText().trim().isEmpty() ||
                txtQTY.getText().trim().isEmpty() ||
                txtUnitPrice.getText().trim().isEmpty()){
            new Alert(Alert.AlertType.ERROR,"Description, Qty. on Hand or Unit Price can't be empty").show();
            return;
        }

        int qtyOnHand = Integer.parseInt(txtQTY.getText().trim());
        double unitPrice = Double.parseDouble(txtUnitPrice.getText().trim());

        if (qtyOnHand < 0 || unitPrice <= 0){
            new Alert(Alert.AlertType.ERROR, "Invalid Qty. or UnitPrice").show();
            return;
        }

        if (btnSave.getText().equals("Save")) {
            String code = txtItemCode.getText();
            String description = txtItemDescription.getText();
            PreparedStatement statement = DBConnection.getInstance().getConnection().prepareStatement("INSERT INTO items values (?,?,?,?)");
            statement.setObject(1,code);
            statement.setObject(2,description);
            statement.setObject(3,qtyOnHand);
            statement.setObject(4,unitPrice);
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,"Do you want to add new Item?", ButtonType.NO,ButtonType.YES);
            Optional<ButtonType> buttonType = alert.showAndWait();
            if (buttonType.get()==ButtonType.YES) {
                statement.executeUpdate();
                loadDatabase();
            }

            btnNewItem_OnAction(event);
        } else {
            PreparedStatement statement = DBConnection.getInstance().getConnection().prepareStatement("UPDATE items set description=?,qty=?,unitprice=? where itemId=?");
            statement.setObject(1,txtItemDescription.getText());
            statement.setObject(2,qtyOnHand);
            statement.setObject(3,unitPrice);
            statement.setObject(4,txtItemCode.getText());
            statement.executeUpdate();
            loadDatabase();
            tblItems.refresh();
            btnNewItem_OnAction(event);
        }
    }


    public void btnDelete_onAction(ActionEvent event) throws SQLException {
        String selectedItem = tblItems.getSelectionModel().getSelectedItem().getCode();
        PreparedStatement statement = DBConnection.getInstance().getConnection().prepareStatement("delete from items where itemId =?");
        statement.setObject(1,selectedItem);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,"Do you want to delete the item?", ButtonType.NO,ButtonType.YES);
        Optional<ButtonType>  buttonType = alert.showAndWait();
        if (buttonType.get()==ButtonType.YES) {
            statement.executeUpdate();
            loadDatabase();
            tblItems.getSelectionModel().clearSelection();

        }

    }

    public void loadDatabase() throws SQLException {
        try {
            ObservableList<ItemTM> items = tblItems.getItems();
            PreparedStatement statement = DBConnection.getInstance().getConnection().prepareStatement("select * from items");
            ResultSet resultSet = statement.executeQuery();
            items.clear();
            while (resultSet.next()) {
                String itemID = resultSet.getString(1);
                String itemDescription = resultSet.getString(2);
                int qty = resultSet.getInt(3);
                double unitPrice = resultSet.getDouble(4);

                items.add(new ItemTM(itemID,itemDescription,qty,unitPrice));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //connection.close();
    }

    public void generateId() {
        // Generate a new id

        ObservableList<ItemTM> items = tblItems.getItems();
        if (items.size() == 0) {
            txtItemCode.setText("I001");
        } else {
            ItemTM lastItem = items.get(items.size() - 1);
            String lastItemsCode = lastItem.getCode();
            String number = lastItemsCode.substring(1, 4);
            int newItemCode = Integer.parseInt(number) + 1;
            if (newItemCode < 10) {

                txtItemCode.setText("I00" + newItemCode);
            } else if (newItemCode < 100) {

                txtItemCode.setText("I0" + newItemCode);
            } else {

                txtItemCode.setText("I" + newItemCode);
            }
        }
    }

}