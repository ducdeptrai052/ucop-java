package com.ucop.controller;

import com.ucop.entity.Order;
import com.ucop.service.OrderService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

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
    private TableColumn<Order, Number> colTotal;

    // dùng Singleton, không new nữa
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
                new javafx.beans.property.SimpleDoubleProperty(
                        c.getValue().getGrandTotal() != null
                                ? c.getValue().getGrandTotal().doubleValue()
                                : 0.0));

        loadOrders();
    }

    private void loadOrders() {
        tblOrders.setItems(FXCollections.observableList(orderService.findAllOrders()));
    }
}
