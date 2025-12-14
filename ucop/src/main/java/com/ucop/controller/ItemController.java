package com.ucop.controller;

import com.ucop.entity.Category;
import com.ucop.entity.Item;
import com.ucop.service.CatalogService;
import com.ucop.util.CurrencyUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    @FXML private Label lblSkuTitle;
    @FXML private TextField txtName;
    @FXML private TextField txtDescription;
    @FXML private TextField txtPrice;
    @FXML private TextField txtImageUrl;
    @FXML private CheckBox chkActive;
    @FXML private ComboBox<Category> cbCategory;
    @FXML private ComboBox<Category> cbCategoryFilter;
    @FXML private ImageView imgPreview;

    private final CatalogService catalogService = new CatalogService();
    private Item selected;

    @FXML
    public void initialize() {
        colSku.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getSku()));
        colName.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));
        colPrice.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                CurrencyUtil.format(c.getValue().getPrice())));
        colActive.setCellValueFactory(c -> new javafx.beans.property.SimpleBooleanProperty(c.getValue().isActive()));

        var categories = FXCollections.observableList(catalogService.findAllCategories());
        cbCategory.setItems(categories);
        cbCategoryFilter.setItems(categories);
        setupCategoryCombo(cbCategory);
        setupCategoryCombo(cbCategoryFilter);
        cbCategoryFilter.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> loadItems());

        updateImagePreview(null);
        loadItems();

        tblItems.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            selected = val;
            if (val != null) {
                txtSku.setText(val.getSku());
                lblSkuTitle.setText(val.getSku());
                txtName.setText(val.getName());
                txtDescription.setText(val.getDescription());
                txtPrice.setText(val.getPrice() != null ? val.getPrice().toString() : "");
                chkActive.setSelected(val.isActive());
                cbCategory.getSelectionModel().select(val.getCategory());
                txtImageUrl.setText(val.getImageUrl());
                updateImagePreview(val.getImageUrl());
            } else {
                clearForm();
            }
        });
    }

    private void setupCategoryCombo(ComboBox<Category> combo) {
        combo.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        combo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
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
        Category filter = cbCategoryFilter != null ? cbCategoryFilter.getSelectionModel().getSelectedItem() : null;
        if (filter != null) {
            tblItems.setItems(FXCollections.observableList(
                    catalogService.findItemsByCategory(filter.getId())));
        } else {
            tblItems.setItems(FXCollections.observableList(catalogService.findAllItems()));
        }
    }

    private void clearForm() {
        txtSku.clear();
        lblSkuTitle.setText("Mã sản phẩm");
        txtName.clear();
        txtDescription.clear();
        txtPrice.clear();
        chkActive.setSelected(true);
        cbCategory.getSelectionModel().clearSelection();
        txtImageUrl.clear();
        updateImagePreview(null);
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
            item.setDescription(txtDescription.getText());
            item.setPrice(price);
            item.setActive(chkActive.isSelected());
            item.setCategory(category);
            item.setImageUrl(txtImageUrl.getText());

            catalogService.saveItem(item);
            loadItems();
            handleNew();
        } catch (Exception e) {
            alert("Lỗi", "Giá không hợp lệ hoặc lỗi khác: " + e.getMessage());
        }
    }

    @FXML
    private void handleClearFilter() {
        if (cbCategoryFilter != null) {
            cbCategoryFilter.getSelectionModel().clearSelection();
            loadItems();
        }
    }

    @FXML
    private void handleDelete() {
        if (selected == null) return;
        if (confirm("Xoá sản phẩm?", "Bạn có chắc muốn xoá?")) {
            try {
                catalogService.deleteItem(selected);
                loadItems();
                handleNew();
            } catch (Exception ex) {
                alert("Lỗi", "Không thể xoá vì sản phẩm đã được dùng trong dữ liệu (đơn hàng, kho…).");
            }
        }
    }

    @FXML
    private void handleBrowseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Chọn ảnh sản phẩm");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("All", "*.*")
        );
        Window window = tblItems.getScene().getWindow();
        var file = chooser.showOpenDialog(window);
        if (file != null) {
            String url = file.toURI().toString();
            txtImageUrl.setText(url);
            updateImagePreview(url);
        }
    }

    @FXML
    private void handleClearImage() {
        txtImageUrl.clear();
        updateImagePreview(null);
    }

    private void updateImagePreview(String url) {
        String finalUrl = (url != null && !url.isBlank()) ? url : "https://via.placeholder.com/180x160?text=Image";
        try {
            imgPreview.setImage(new Image(finalUrl, true));
        } catch (Exception ignored) {
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
