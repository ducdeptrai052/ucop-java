package com.ucop.controller;

import com.ucop.entity.StockItem;
import com.ucop.service.CatalogService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class StockController {

    @FXML private TableView<StockItem> tblStock;
    @FXML private TableColumn<StockItem, String> colItem;
    @FXML private TableColumn<StockItem, String> colWarehouse;
    @FXML private TableColumn<StockItem, Number> colOnHand;
    @FXML private TableColumn<StockItem, Number> colReserved;

    @FXML private Label lblLowStock;

    private final CatalogService catalogService = new CatalogService();

    @FXML
    public void initialize() {
        colItem.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getItem().getName()));
        colWarehouse.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getWarehouse().getName()));
        colOnHand.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(
                c.getValue().getOnHand()));
        colReserved.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(
                c.getValue().getReserved()));

        loadData();
    }

    private void loadData() {
        var all = catalogService.findAllStock();
        tblStock.setItems(FXCollections.observableList(all));

        int low = catalogService.findLowStock().size();
        lblLowStock.setText("Sản phẩm sắp hết hàng: " + low);
    }
}
