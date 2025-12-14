package com.ucop.controller;

import com.ucop.entity.Item;
import com.ucop.entity.Category;
import com.ucop.service.CatalogService;
import com.ucop.util.CurrencyUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.math.BigDecimal;
import java.nio.file.Path;

public class ItemController {

    @FXML private TableView<Item> tblItems;
    @FXML private TableColumn<Item, String> colSku;
    @FXML private TableColumn<Item, String> colName;
    @FXML private TableColumn<Item, String> colPrice;
    @FXML private TableColumn<Item, Boolean> colActive;

    @FXML private TextField txtSku;
    @FXML private TextField txtName;
    @FXML private TextField txtPrice;
    @FXML private CheckBox chkActive;
    @FXML private ComboBox<Category> cbCategory;

    private final CatalogService catalogService = new CatalogService();
    private Item selected;

    @FXML
    public void initialize() {
        colSku.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getSku()));
        colName.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));
        colPrice.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                CurrencyUtil.format(c.getValue().getPrice())));
        colActive.setCellValueFactory(c -> new javafx.beans.property.SimpleBooleanProperty(c.getValue().isActive()));

        cbCategory.setItems(FXCollections.observableList(catalogService.findAllCategories()));
        cbCategory.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        cbCategory.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });

        loadItems();

        tblItems.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            selected = val;
            if (val != null) {
                txtSku.setText(val.getSku());
                txtName.setText(val.getName());
                txtPrice.setText(val.getPrice().toString());
                chkActive.setSelected(val.isActive());
                cbCategory.getSelectionModel().select(val.getCategory());
            } else {
                clearForm();
            }
        });
    }

    @FXML
    private void handleExportCsv() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Chọn nơi lưu file CSV");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        Window window = tblItems.getScene().getWindow();
        var file = chooser.showSaveDialog(window);
        if (file != null) {
            try {
                catalogService.exportItemsToCsv(Path.of(file.toURI()));
                alert("Thông báo", "Xuất CSV thành công");
            } catch (Exception e) {
                alert("Lỗi", "Xuất CSV thất bại: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleImportCsv() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Chọn file CSV");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        Window window = tblItems.getScene().getWindow();
        var file = chooser.showOpenDialog(window);
        if (file != null) {
            try {
                catalogService.importItemsFromCsv(Path.of(file.toURI()));
                loadItems();
                alert("Thông báo", "Import CSV thành công");
            } catch (Exception e) {
                alert("Lỗi", "Import CSV thất bại: " + e.getMessage());
            }
        }
    }

    private void loadItems() {
        tblItems.setItems(FXCollections.observableList(catalogService.findAllItems()));
    }

    private void clearForm() {
        txtSku.clear();
        txtName.clear();
        txtPrice.clear();
        chkActive.setSelected(true);
        cbCategory.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleNew() {
        selected = null;
        clearForm();
        tblItems.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleSave() {
        try {
            String sku = txtSku.getText().trim();
            String name = txtName.getText().trim();
            BigDecimal price = new BigDecimal(txtPrice.getText().trim());
            Category category = cbCategory.getSelectionModel().getSelectedItem();

            if (sku.isEmpty() || name.isEmpty() || category == null) {
                alert("Lỗi", "Vui lòng nhập đủ thông tin");
                return;
            }

            Item item = selected == null ? new Item() : selected;
            item.setSku(sku);
            item.setName(name);
            item.setPrice(price);
            item.setActive(chkActive.isSelected());
            item.setCategory(category);

            catalogService.saveItem(item);
            loadItems();
            handleNew();
        } catch (Exception e) {
            alert("Lỗi", "Giá không hợp lệ");
        }
    }

    @FXML
    private void handleDelete() {
        if (selected == null) return;
        if (confirm("Xoá sản phẩm?", "Bạn có chắc muốn xoá?")) {
            catalogService.deleteItem(selected);
            loadItems();
            handleNew();
        }
    }

    private void alert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        a.setHeaderText(title);
        a.showAndWait();
    }

    private boolean confirm(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.OK, ButtonType.CANCEL);
        a.setHeaderText(title);
        return a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}
