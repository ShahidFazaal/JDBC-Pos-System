package controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import util.DBConnection;
import util.OrderTM;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

public class SearchOrderController {

    public AnchorPane search;
    public TextField txtSearch;
    public TableView<OrderTM> tblOrders;
    private ArrayList<OrderTM> orders = new ArrayList<>();

    public void initialize() throws SQLException {
        ResultSet resultSet = DBConnection.getInstance().getConnection().createStatement().executeQuery("select * from orderdetail");
        while (resultSet.next()){
            String orderId = resultSet.getString(1);
            Date orderDate = resultSet.getDate(2);
            String CustomerId = resultSet.getString(3);
            String customerName = resultSet.getString(4);
            Double orderTotal = resultSet.getDouble(5);
            OrderTM o = new OrderTM(orderId, orderDate, CustomerId, customerName, orderTotal);
            orders.add(o);

            tblOrders.getItems().add(o);

        }

        tblOrders.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("orderId"));
        tblOrders.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        tblOrders.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("customerId"));
        tblOrders.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("customerName"));
        tblOrders.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("orderTotal"));


            txtSearch.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    ObservableList<OrderTM> searchOrders = tblOrders.getItems();
                    searchOrders.clear();
                    for (OrderTM order : orders) {
                        if ((order.getOrderId().contains(newValue)||
                                order.getCustomerId().contains(newValue) ||
                                order.getCustomerName().contains(newValue) ||
                                order.getOrderDate().toString().contains(newValue))){
                            searchOrders.add(order);
                        }
                    }

                }
            });

        }

    public void tblOrders_OnMouseClicked(MouseEvent mouseEvent) throws IOException, SQLException {
        if (tblOrders.getSelectionModel().getSelectedItem() == null){
            return;
        }
        if (mouseEvent.getClickCount() == 2){
            FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("/view/PlaceOrderForm.fxml"));
            Parent root = fxmlLoader.load();
            PlaceOrderFormController controller = (PlaceOrderFormController) fxmlLoader.getController();
            controller.initializeWithSearchOrderForm(tblOrders.getSelectionModel().getSelectedItem().getOrderId());
            controller.txtCustomerName.setEditable(false);
            controller.txtDescription.setEditable(false);
            controller.txtQtyOnHand.setEditable(false);
            controller.txtUnitPrice.setEditable(false);
            controller.btnAddNewOrder.setDisable(true);
            controller.tblOrderDetails.getSelectionModel().select(0);
            controller.home.setDisable(true);
            Scene orderScene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(orderScene);
            stage.centerOnScreen();
            stage.show();
        }






    }

    public void navigateToHome(MouseEvent mouseEvent) throws IOException {
        URL resource = this.getClass().getResource("/view/Console.fxml");
        Parent root = FXMLLoader.load(resource);
        Scene scene = new Scene(root);
        Stage primaryStage = (Stage)(this.search.getScene().getWindow());
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }
}
