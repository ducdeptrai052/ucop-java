package com.ucop.controller;

import com.ucop.entity.Category;
import com.ucop.service.CatalogService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class CategoryController {

    @FXML private TableView<Category> tblCategories;
    @FXML private TableColumn<Category, Long> colId;
    @FXML private TableColumn<Category, String> colName;
    @FXML private TextField txtName;
    @FXML private Button btnSave;
    @FXML private Button btnDelete;

    private final CatalogService catalogService = new CatalogService();
    private Category selected;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleLongProperty(c.getValue().getId()).asObject());
        colName.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));

        loadData();

        tblCategories.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            selected = val;
            if (val != null) {
                txtName.setText(val.getName());
            } else {
                txtName.clear();
            }
        });
    }

    private void loadData() {
        tblCategories.setItems(FXCollections.observableList(catalogService.findAllCategories()));
    }

    @FXML
    private void handleNew() {
        selected = null;
        txtName.clear();
        tblCategories.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleSave() {
        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            showAlert("Lỗi", "Tên danh mục không được để trống");
            return;
        }
        if (selected == null) {
            Category c = new Category();
            c.setName(name);
            catalogService.saveCategory(c);
        } else {
            selected.setName(name);
            catalogService.saveCategory(selected);
        }
        loadData();
        handleNew();
    }

    @FXML
    private void handleDelete() {
        if (selected == null) return;
        if (confirm("Xoá danh mục?", "Bạn có chắc muốn xoá?")) {
            catalogService.deleteCategory(selected);
            loadData();
            handleNew();
        }
    }

    private void showAlert(String title, String msg) {
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
