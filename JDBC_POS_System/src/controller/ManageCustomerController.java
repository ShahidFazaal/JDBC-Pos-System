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
import util.Customers;
import util.DBConnection;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class ManageCustomerController {
    static Connection connection;
    public AnchorPane manageCustomer;
    public TableView<Customers> tblCustomers;
    public JFXButton btnAddNewCustomer;
    public JFXButton btnDelete;
    public JFXButton btnSave;
    public JFXTextField txtCustomerId;
    public JFXTextField txtCustomerName;
    public JFXTextField txtAddress;



    public void initialize() throws SQLException {
        txtCustomerId.setDisable(true);
        txtCustomerName.setDisable(true);
        txtAddress.setDisable(true);
        tblCustomers.refresh();
        btnSave.setDisable(true);
        btnDelete.setDisable(false);
        loadDatabase();

        tblCustomers.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("customerId"));
        tblCustomers.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("customerName"));
        tblCustomers.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("address"));

        tblCustomers.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Customers>() {
            @Override
            public void changed(ObservableValue<? extends Customers> observable, Customers oldValue, Customers newValue) {
                Customers selectedItem = tblCustomers.getSelectionModel().getSelectedItem();
                if (selectedItem == null){
                    btnSave.setText("Save");
                    btnDelete.setDisable(true);
                    txtCustomerId.clear();
                    txtCustomerName.clear();
                    txtAddress.clear();
                    txtCustomerId.setDisable(true);
                    txtAddress.setDisable(true);
                    txtCustomerName.setDisable(true);
                    btnSave.setDisable(true);
                    btnAddNewCustomer.requestFocus();
                    return;
                }

                txtCustomerId.setText(newValue.getCustomerId());
                txtCustomerName.setText(newValue.getCustomerName());
                txtAddress.setText(newValue.getAddress());
                btnDelete.setDisable(false);
                btnSave.setText("Update");
                txtCustomerId.setDisable(false);
                txtCustomerName.setDisable(false);
                txtAddress.setDisable(false);
                btnSave.setDisable(false);
            }
        });




    }

    public void loadDatabase() throws SQLException {


        try {
            ObservableList<Customers> customers = tblCustomers.getItems();
            PreparedStatement statement = DBConnection.getInstance().getConnection().prepareStatement("select * from customers");
            ResultSet resultSet = statement.executeQuery();
            customers.clear();
            while (resultSet.next()) {
                String customerId = resultSet.getString(1);
                String customerName = resultSet.getString(2);
                String customerAddress = resultSet.getString(3);
                customers.add(new Customers(customerId, customerName, customerAddress));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //connection.close();
    }

    public void navigateMainWindow(MouseEvent mouseEvent) throws IOException {
        URL resource = this.getClass().getResource("/view/Console.fxml");
        Parent root = FXMLLoader.load(resource);
        Scene scene = new Scene(root);
        Stage primaryStage = (Stage) (this.manageCustomer.getScene().getWindow());
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    public void btnNewCustomer_OnAction(ActionEvent event) {
        txtCustomerId.setDisable(false);
        txtCustomerName.setDisable(false);
        txtAddress.setDisable(false);
        btnSave.setDisable(false);
        txtCustomerName.requestFocus();
        txtCustomerName.clear();
        txtAddress.clear();
        btnDelete.setDisable(true);
        btnSave.setText("Save");
        generateCustomerId();
    }

    public void btnSave_onAction(ActionEvent event) throws SQLException {
        String ID = txtCustomerId.getText();
        String name = txtCustomerName.getText();
        String address = txtAddress.getText();
        try {
            PreparedStatement statement = DBConnection.getInstance().getConnection().prepareStatement("INSERT INTO customers values (?,?,?)");
            statement.setObject(1,ID);
            statement.setObject(2,name);
            statement.setObject(3,address);
            statement.executeUpdate();
            loadDatabase();
            tblCustomers.refresh();
            txtAddress.clear();
            txtCustomerName.clear();
            txtCustomerId.clear();
            btnSave.setDisable(true);
            txtCustomerId.setDisable(true);
            txtCustomerName.setDisable(true);
            txtAddress.setDisable(true);


        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    public void btnDelete_onAction(ActionEvent event) throws Exception {
        String selectedItem = tblCustomers.getSelectionModel().getSelectedItem().getCustomerId();
        PreparedStatement statement = DBConnection.getInstance().getConnection().prepareStatement("delete from customers where customerId =?");
        statement.setObject(1,selectedItem);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,"Do you want to delete the customer?", ButtonType.NO,ButtonType.YES);
        Optional<ButtonType>  buttonType = alert.showAndWait();
        if (buttonType.get()==ButtonType.YES) {
            statement.executeUpdate();
            loadDatabase();
            tblCustomers.getSelectionModel().clearSelection();

        }

    }

    public void generateCustomerId() {
        ObservableList<Customers> customers = tblCustomers.getItems();
        if (customers.size() == 0) {
            txtCustomerId.setText("C001");
        } else {
            Customers lastItem = customers.get(customers.size() - 1);
            String lastItemsCode = lastItem.getCustomerId();
            String number = lastItemsCode.substring(1, 4);
            int newItemCode = Integer.parseInt(number) + 1;
            if (newItemCode < 10) {

                txtCustomerId.setText("C00" + newItemCode);
            } else if (newItemCode < 100) {

                txtCustomerId.setText("C0" + newItemCode);
            } else {

                txtCustomerId.setText("C" + newItemCode);
            }
        }

    }
}
