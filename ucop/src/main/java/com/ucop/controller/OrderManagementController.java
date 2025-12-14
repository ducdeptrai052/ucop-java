package com.ucop.controller;

import com.ucop.entity.Order;
import com.ucop.entity.Payment;
import com.ucop.entity.enums.OrderStatus;
import com.ucop.service.OrderService;
import com.ucop.service.PaymentService;
import com.ucop.util.CurrencyUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.util.List;

public class OrderManagementController {

    @FXML
    private TableView<Order> tblOrders;

    @FXML
    private TableColumn<Order, Long> colId;

    @FXML
    private TableColumn<Order, String> colCustomer;

    @FXML
    private TableColumn<Order, String> colStatus;

    @FXML
    private TableColumn<Order, String> colDate;

    @FXML
    private TableColumn<Order, String> colTotal;

    @FXML
    private ComboBox<OrderStatus> cbStatusFilter;

    private final PaymentService paymentService = new PaymentService();
    private final OrderService orderService = OrderService.getInstance();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(c ->
                new javafx.beans.property.SimpleLongProperty(c.getValue().getId()).asObject());

        colCustomer.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getCustomer() != null
                                ? c.getValue().getCustomer().getUsername()
                                : ""));

        colStatus.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getStatus() != null
                                ? c.getValue().getStatus().name()
                                : ""));

        colDate.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getOrderDate() != null
                                ? c.getValue().getOrderDate().toString()
                                : ""));

        colTotal.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        CurrencyUtil.format(c.getValue().getGrandTotal())));

        cbStatusFilter.setItems(FXCollections.observableArrayList(OrderStatus.values()));

        loadOrders(null);
    }

    @FXML
    private void handleFilter() {
        OrderStatus status = cbStatusFilter.getSelectionModel().getSelectedItem();
        loadOrders(status);
    }

    @FXML
    private void handleMarkPaid() {
        Order order = tblOrders.getSelectionModel().getSelectedItem();
        if (order == null) {
            alert("Chọn đơn hàng trước");
            return;
        }
        Payment p = pickFirstPayment(order);
        if (p == null) {
            alert("Đơn chưa có payment");
            return;
        }
        paymentService.markPaid(p, "manual");
        orderService.updateOrderStatus(order, OrderStatus.PAID);
        alert("Đánh dấu thanh toán thành công");
        loadOrders(cbStatusFilter.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void handleCreateShipment() {
        Order order = tblOrders.getSelectionModel().getSelectedItem();
        if (order == null) {
            alert("Chọn đơn hàng trước");
            return;
        }
        TextInputDialog dlg = new TextInputDialog();
        dlg.setHeaderText("Nhập mã vận đơn");
        dlg.setContentText("Tracking:");
        var tracking = dlg.showAndWait().orElse(null);
        if (tracking == null || tracking.isBlank()) return;
        orderService.createShipment(order, tracking, "Manual");
        alert("Đã tạo vận đơn");
        loadOrders(cbStatusFilter.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void handleRefund() {
        Order order = tblOrders.getSelectionModel().getSelectedItem();
        if (order == null) {
            alert("Chọn đơn hàng trước");
            return;
        }
        Payment p = pickFirstPayment(order);
        if (p == null) {
            alert("Đơn chưa có payment");
            return;
        }
        TextInputDialog dlg = new TextInputDialog();
        dlg.setHeaderText("Nhập số tiền hoàn");
        dlg.setContentText("Amount:");
        var val = dlg.showAndWait().orElse(null);
        if (val == null || val.isBlank()) return;
        try {
            var amount = new BigDecimal(val);
            orderService.refundPayment(p, amount);
            orderService.updateOrderStatus(order, OrderStatus.REFUNDED);
            alert("Hoàn tiền thành công");
            loadOrders(cbStatusFilter.getSelectionModel().getSelectedItem());
        } catch (Exception e) {
            alert("Lỗi hoàn tiền: " + e.getMessage());
        }
    }

    private Payment pickFirstPayment(Order order) {
        if (order.getPayments() == null || order.getPayments().isEmpty()) return null;
        return order.getPayments().iterator().next();
    }

    private void loadOrders(OrderStatus filter) {
        List<Order> data = orderService.findAllOrders();
        if (filter != null) {
            data = data.stream().filter(o -> filter == o.getStatus()).toList();
        }
        tblOrders.setItems(FXCollections.observableList(data));
    }

    private void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
