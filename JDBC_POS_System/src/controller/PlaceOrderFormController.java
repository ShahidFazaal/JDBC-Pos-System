package controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import com.mysql.cj.x.protobuf.MysqlxCrud;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import util.Customers;
import util.DBConnection;
import util.ItemTM;
import util.OrderDetailTM;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;

public class PlaceOrderFormController {
    public JFXTextField txtDescription;
    public JFXTextField txtCustomerName;
    public JFXTextField txtQtyOnHand;
    public JFXButton btnSave;
    public JFXTextField txtUnitPrice;
    public JFXComboBox<Customers> cmbCustomerId;
    public JFXComboBox<ItemTM> cmbItemCode;
    public JFXTextField txtQty;
    public Label lblTotal;
    public JFXButton btnPlaceOrder;
    public AnchorPane root;
    public Label lblId;
    public Label lblDate;
    public JFXButton btnAddNewOrder;
    public TableView<OrderDetailTM> tblOrderDetails;
    public ImageView home;
    private boolean readOnly = false;


    public void initialize() throws SQLException {


        // Let's set the date
        LocalDate today = LocalDate.now();
        lblDate.setText(today.toString());

        // Let's load all the customer ids
        ObservableList<Customers> customers = cmbCustomerId.getItems();
        PreparedStatement statement = DBConnection.getInstance().getConnection().prepareStatement("SELECT * from customers");
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            String id = resultSet.getString(1);
            String name = resultSet.getString(2);
            String address = resultSet.getString(3);
            customers.add(new Customers(id, name, address));
        }
        // When user selects a customer id
        cmbCustomerId.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Customers>() {
            @Override
            public void changed(ObservableValue<? extends Customers> observable, Customers oldValue, Customers newValue) {
                if (newValue == null) {
                    txtCustomerName.clear();
                    return;
                }
                txtCustomerName.setText(newValue.getCustomerName());
            }
        });


        // Let's load all the Item ids
        ObservableList<ItemTM> items = cmbItemCode.getItems();
        PreparedStatement DbItems = DBConnection.getInstance().getConnection().prepareStatement("SELECT * from items");
        ResultSet rs1 = DbItems.executeQuery();
        while (rs1.next()) {
            String id = rs1.getString(1);
            String description = rs1.getString(2);
            int qty = rs1.getInt(3);
            double unitPrice = rs1.getDouble(4);
            items.add(new ItemTM(id, description, qty, unitPrice));
        }

        // When user Selected the item ids
        cmbItemCode.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ItemTM>() {

            @Override
            public void changed(ObservableValue<? extends ItemTM> observable, ItemTM oldValue, ItemTM newValue) {
                if (newValue == null) {
                    txtUnitPrice.clear();
                    txtDescription.clear();
                    txtQtyOnHand.clear();
                    btnSave.setDisable(true);
                    return;
                }
                txtQty.requestFocus();
                btnSave.setDisable(false);
                txtDescription.setText(newValue.getDescription());
                txtUnitPrice.setText(newValue.getUnitPrice() + "");
                calculateQtyOnHand(newValue);
            }
        });


        // Let's map columns
        tblOrderDetails.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("code"));
        tblOrderDetails.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("description"));
        tblOrderDetails.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("qty"));
        tblOrderDetails.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        tblOrderDetails.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("total"));

        tblOrderDetails.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<OrderDetailTM>() {
            @Override
            public void changed(ObservableValue<? extends OrderDetailTM> observable, OrderDetailTM oldValue, OrderDetailTM selectedOrderDetail) {

                if (selectedOrderDetail == null) {
                    return;
                }

                String selectedItemCode = selectedOrderDetail.getCode();
                ObservableList<ItemTM> items = cmbItemCode.getItems();
                for (ItemTM item : items) {
                    if (item.getCode().equals(selectedItemCode)) {
                        cmbItemCode.getSelectionModel().select(item);
                        txtQtyOnHand.setText(item.getQtyOnHand() + "");
                        txtQty.setText(selectedOrderDetail.getQty() + "");
                        if (!readOnly){
                            btnSave.setText("Update");
                        }
                        if (readOnly){
                            txtQty.setEditable(false);
                            btnSave.setDisable(true);
                            tblOrderDetails.requestFocus();
                        }
                        cmbItemCode.setDisable(true);
                        Platform.runLater(() -> {
                            txtQty.requestFocus();
                        });
                        break;
                    }
                }
            }
        });



        generateOrderId();


    }


    public void btnAddNew_OnAction(ActionEvent actionEvent) throws SQLException {
        generateOrderId();

    }

    public void btnAdd_OnAction(ActionEvent actionEvent) throws SQLException {
        // Let's do some validation
        if (txtQty.getText().trim().isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Qty can't be empty", ButtonType.OK).show();
            return;
        }
        int qty = Integer.parseInt(txtQty.getText());
        if (qty < 1 || qty > Integer.parseInt(txtQtyOnHand.getText())) {
            new Alert(Alert.AlertType.ERROR, "Invalid Qty.", ButtonType.OK).show();
            return;
        }

        ItemTM selectedItem = cmbItemCode.getSelectionModel().getSelectedItem();
        ObservableList<OrderDetailTM> orderDetails = tblOrderDetails.getItems();

        if (btnSave.getText().equals("Add")) {
            boolean exist = false;
            for (OrderDetailTM orderDetail : orderDetails) {
                if (orderDetail.getCode().equals(selectedItem.getCode())) {
                    exist = true;
                    orderDetail.setQty(orderDetail.getQty() + qty);
                    orderDetail.setTotal(orderDetail.getQty() * orderDetail.getUnitPrice());
                    tblOrderDetails.refresh();
                    break;
                }
            }

            if (!exist) {
                orderDetails.add(new OrderDetailTM(selectedItem.getCode(),
                        selectedItem.getDescription(),
                        qty,
                        selectedItem.getUnitPrice(), qty * selectedItem.getUnitPrice()));
            }

            calculateTotal();
            txtQty.clear();
            cmbItemCode.getSelectionModel().clearSelection();

            cmbItemCode.requestFocus();
        } else {
            // Update
            OrderDetailTM selectedOrderDetail = tblOrderDetails.getSelectionModel().getSelectedItem();
            selectedOrderDetail.setQty(qty);
            selectedOrderDetail.setTotal(qty * selectedOrderDetail.getUnitPrice());
            tblOrderDetails.refresh();

            tblOrderDetails.getSelectionModel().clearSelection();
            btnSave.setText("Add");
            cmbItemCode.setDisable(false);
            cmbItemCode.getSelectionModel().clearSelection();
            txtQty.clear();
            calculateTotal();
            cmbItemCode.requestFocus();
        }

    }

    public void btnPlaceOrder_OnAction(ActionEvent actionEvent) throws SQLException {
        // Validation
        if (cmbCustomerId.getSelectionModel().getSelectedIndex() == -1) {
            new Alert(Alert.AlertType.ERROR, "You need to select a customer", ButtonType.OK).show();
            cmbCustomerId.requestFocus();
            return;
        }

        if (tblOrderDetails.getItems().size() == 0) {
            new Alert(Alert.AlertType.ERROR, "", ButtonType.OK).show();
            cmbItemCode.requestFocus();
            return;
        }

        // Let's save the order

        //lets Update the database;
        PreparedStatement statement = DBConnection.getInstance().getConnection().prepareStatement("insert into orderdetail values (?,?,?,?,?)");
        statement.setObject(1, lblId.getText());
        statement.setObject(2, lblDate.getText());
        statement.setObject(3, cmbCustomerId.getSelectionModel().getSelectedItem().getCustomerId());
        statement.setObject(4, cmbCustomerId.getSelectionModel().getSelectedItem().getCustomerName());
        String total = lblTotal.getText().trim();
        String substring = total.substring(7, 12);
        statement.setObject(5, Double.parseDouble(substring));
        statement.executeUpdate();


        ObservableList<OrderDetailTM> olOrderDetails = tblOrderDetails.getItems();
        for (OrderDetailTM orderDetail : olOrderDetails) {
            // Let's update the stock
            System.out.println(orderDetail.getQty());
            updateStockQty(orderDetail.getCode(), orderDetail.getQty());
            PreparedStatement statement1 = DBConnection.getInstance().getConnection().prepareStatement("insert into orders values (?,?,?,?,?)");
            statement1.setObject(1, lblId.getText());
            statement1.setObject(2, orderDetail.getCode());
            statement1.setObject(3, orderDetail.getQty());
            statement1.setObject(4, orderDetail.getUnitPrice());
            statement1.setObject(5, orderDetail.getTotal());
            statement1.executeUpdate();
        }

        new Alert(Alert.AlertType.INFORMATION, "done", ButtonType.OK).showAndWait();

        tblOrderDetails.getItems().clear();
        txtQty.clear();
        cmbCustomerId.getSelectionModel().clearSelection();
        cmbItemCode.getSelectionModel().clearSelection();
        calculateTotal();
        generateOrderId();

    }

    private void updateStockQty(String itemCode, int qty) throws SQLException {
        PreparedStatement statement1 = DBConnection.getInstance().getConnection().prepareStatement("select qty from items where itemId =?");
        statement1.setObject(1, itemCode);
        ResultSet resultSet = statement1.executeQuery();
        resultSet.next();
        int currentQty = resultSet.getInt("qty");

        PreparedStatement statement = DBConnection.getInstance().getConnection().prepareStatement("UPDATE items set qty=? where itemId=?");
        statement.setObject(1, currentQty - qty);
        statement.setObject(2, itemCode);
        statement.executeUpdate();
    }


    @FXML
    private void navigateToHome(MouseEvent event) throws IOException {
        URL resource = this.getClass().getResource("/view/Console.fxml");
        Parent root = FXMLLoader.load(resource);
        Scene scene = new Scene(root);
        Stage primaryStage = (Stage) (this.root.getScene().getWindow());
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    public void txtQty_OnAction(ActionEvent actionEvent) throws SQLException {
        btnAdd_OnAction(actionEvent);
    }

    public void calculateTotal() {
        ObservableList<OrderDetailTM> orderDetails = tblOrderDetails.getItems();
        double netTotal = 0;
        for (OrderDetailTM orderDetail : orderDetails) {
            netTotal += orderDetail.getTotal();
        }
        NumberFormat numberInstance = NumberFormat.getNumberInstance();
        numberInstance.setMaximumFractionDigits(2);
        numberInstance.setMinimumFractionDigits(2);
        numberInstance.setGroupingUsed(false);
        String formattedText = numberInstance.format(netTotal);
        lblTotal.setText("Total: " + formattedText);

    }

    private void generateOrderId() throws SQLException {
        ResultSet resultSet = DBConnection.getInstance().getConnection().createStatement().executeQuery("select orderId from orderdetail order by 1 desc");
        if (resultSet.next()) {
            String orderId = resultSet.getString(1);
            String number = orderId.substring(2, 5);
            int orderCode = Integer.parseInt(number) + 1;
            if (orderCode < 10) {

                lblId.setText("OD00" + orderCode);
            } else if (orderCode < 100) {

                lblId.setText("OD0" + orderCode);
            } else {

                lblId.setText("OD" + orderCode);
            }
        } else {
            lblId.setText("OD001");
        }
    }

    void initializeWithSearchOrderForm(String orderId) throws SQLException {
        lblId.setText(orderId);
        ResultSet rs = DBConnection.getInstance().getConnection().createStatement().executeQuery("select * from orderdetail where orderId='" + orderId + "'");
        readOnly = true;
        while (rs.next()) {
            {
                lblDate.setText(rs.getDate(2) + "");

                // To select the customer
                String customerId = rs.getString(3);
                ResultSet rs2 = DBConnection.getInstance().getConnection().createStatement().executeQuery("select customerName from customers where customerId='" + customerId + "'");
                if (rs2.next()) {
                    for (Customers customer : cmbCustomerId.getItems()) {
                        if (customer.getCustomerId().equals(customerId)) {
                            cmbCustomerId.getSelectionModel().select(customer);
                            break;
                        }
                    }

                }
            }


            ResultSet rs3 = DBConnection.getInstance().getConnection().createStatement().executeQuery("select * from orders where orderId = '"+orderId+"'");
            while(rs3.next()) {
                String description = null;
                for (ItemTM item : cmbItemCode.getItems()) {
                    if (item.getCode().equals(rs3.getString(2))) {
                        description = item.getDescription();
                        break;
                    }
                }
                OrderDetailTM orderDetailTM = new OrderDetailTM(
                        rs3.getString(2),
                        description,
                        rs3.getInt(3),
                        rs3.getDouble(4),
                        rs3.getDouble(5)
                );
                tblOrderDetails.getItems().add(orderDetailTM);
                calculateTotal();
            }


                cmbCustomerId.setDisable(true);
                cmbItemCode.setDisable(true);
                btnSave.setDisable(true);
                btnPlaceOrder.setVisible(false);
                break;

        }
    }

    private void calculateQtyOnHand(ItemTM item) {
        txtQtyOnHand.setText(item.getQtyOnHand() + "");
        ObservableList<OrderDetailTM> orderDetails = tblOrderDetails.getItems();
        for (OrderDetailTM orderDetail : orderDetails) {
            if (orderDetail.getCode().equals(item.getCode())) {
                int displayQty = item.getQtyOnHand() - orderDetail.getQty();
                txtQtyOnHand.setText(displayQty + "");
                break;
            }
        }
    }


}
