package com.ucop.controller;

import com.ucop.entity.Item;
import com.ucop.entity.StockItem;
import com.ucop.entity.Warehouse;
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
    @FXML private ComboBox<Item> cbItem;
    @FXML private ComboBox<Warehouse> cbWarehouse;
    @FXML private TextField txtOnHand;
    @FXML private TextField txtReserved;
    @FXML private TextField txtLowStock;
    @FXML private TextField txtWhCode;
    @FXML private TextField txtWhName;
    @FXML private TextField txtWhLocation;

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

        cbItem.setItems(FXCollections.observableArrayList(catalogService.findAllItems()));
        cbItem.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(com.ucop.entity.Item item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        cbItem.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(com.ucop.entity.Item item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });

        cbWarehouse.setItems(FXCollections.observableArrayList(catalogService.findAllWarehouses()));
        cbWarehouse.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(com.ucop.entity.Warehouse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        cbWarehouse.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(com.ucop.entity.Warehouse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });

        loadData();
    }

    @FXML
    private void handleSaveWarehouse() {
        Warehouse wh = new Warehouse();
        wh.setCode(txtWhCode.getText().trim());
        wh.setName(txtWhName.getText().trim());
        wh.setLocation(txtWhLocation.getText().trim());
        catalogService.saveWarehouse(wh);
        cbWarehouse.setItems(FXCollections.observableArrayList(catalogService.findAllWarehouses()));
    }

    @FXML
    private void handleSaveStock() {
        Item item = cbItem.getSelectionModel().getSelectedItem();
        Warehouse wh = cbWarehouse.getSelectionModel().getSelectedItem();
        if (item == null || wh == null) {
            alert("Chọn sản phẩm và kho");
            return;
        }
        try {
            int onHand = Integer.parseInt(txtOnHand.getText().trim());
            int reserved = Integer.parseInt(txtReserved.getText().trim());
            int low = Integer.parseInt(txtLowStock.getText().trim());
            catalogService.saveStockItem(wh, item, onHand, reserved, low);
            loadData();
        } catch (NumberFormatException e) {
            alert("Giá trị tồn không hợp lệ");
        }
    }

    private void loadData() {
        var all = catalogService.findAllStock();
        tblStock.setItems(FXCollections.observableList(all));

        int low = catalogService.findLowStock().size();
        lblLowStock.setText("Sản phẩm sắp hết hàng: " + low);
    }

    private void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
