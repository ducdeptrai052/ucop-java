package com.ucop.controller;

import com.ucop.entity.Cart;
import com.ucop.entity.Category;
import com.ucop.entity.Item;
import com.ucop.service.CatalogService;
import com.ucop.service.OrderService;
import com.ucop.util.CurrencyUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ProductGridController {

    @FXML private ComboBox<Category> cbCategory;
    @FXML private ComboBox<String> cbSort;
    @FXML private TextField txtSearchName;
    @FXML private TextField txtSearchSku;
    @FXML private FlowPane flowProducts;

    private final CatalogService catalogService = new CatalogService();
    private final OrderService orderService = OrderService.getInstance();
    private Cart currentCart;

    @FXML
    public void initialize() {
        var categories = FXCollections.observableList(catalogService.findAllCategories());
        cbCategory.setItems(categories);
        cbCategory.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        cbCategory.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });

        cbSort.setItems(FXCollections.observableArrayList(
                "Mặc định", "Giá tăng dần", "Giá giảm dần"));
        cbSort.getSelectionModel().selectFirst();

        // Khi thay đổi filter -> reload
        cbCategory.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> loadProducts());
        cbSort.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> loadProducts());
        txtSearchName.textProperty().addListener((o, a, b) -> loadProducts());
        txtSearchSku.textProperty().addListener((o, a, b) -> loadProducts());

        loadProducts();
    }

    @FXML
    private void handleRefresh() {
        loadProducts();
    }

    private void loadProducts() {
        List<Item> items = catalogService.findAllItems();

        String nameFilter = txtSearchName.getText() != null ? txtSearchName.getText().trim().toLowerCase(Locale.ROOT) : "";
        String skuFilter = txtSearchSku.getText() != null ? txtSearchSku.getText().trim().toLowerCase(Locale.ROOT) : "";
        Category cat = cbCategory.getSelectionModel().getSelectedItem();

        Predicate<Item> predicate = i -> true;
        if (!nameFilter.isEmpty()) {
            predicate = predicate.and(i -> i.getName() != null && i.getName().toLowerCase(Locale.ROOT).contains(nameFilter));
        }
        if (!skuFilter.isEmpty()) {
            predicate = predicate.and(i -> i.getSku() != null && i.getSku().toLowerCase(Locale.ROOT).contains(skuFilter));
        }
        if (cat != null) {
            predicate = predicate.and(i -> i.getCategory() != null && cat.getId().equals(i.getCategory().getId()));
        }

        items = items.stream().filter(predicate).collect(Collectors.toList());

        String sort = cbSort.getSelectionModel().getSelectedItem();
        if ("Giá tăng dần".equals(sort)) {
            items.sort(Comparator.comparing(Item::getPrice, Comparator.nullsLast(BigDecimal::compareTo)));
        } else if ("Giá giảm dần".equals(sort)) {
            items.sort(Comparator.comparing(Item::getPrice, Comparator.nullsLast(BigDecimal::compareTo)).reversed());
        }

        renderGrid(items);
    }

    private void renderGrid(List<Item> items) {
        flowProducts.getChildren().clear();
        for (Item item : items) {
            VBox card = createCard(item);
            flowProducts.getChildren().add(card);
        }
    }

    private VBox createCard(Item item) {
        VBox box = new VBox(6);
        box.setPrefWidth(180);
        box.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 4; -fx-padding: 8;");

        ImageView img = new ImageView();
        img.setFitWidth(150);
        img.setFitHeight(120);
        img.setPreserveRatio(true);
        try {
            String url = (item.getImageUrl() != null && !item.getImageUrl().isBlank())
                    ? item.getImageUrl()
                    : "https://via.placeholder.com/150?text=No+Image";
            img.setImage(new Image(url, true));
        } catch (Exception ignored) { }

        Label name = new Label(item.getName());
        name.setStyle("-fx-font-weight: bold;");

        Label price = new Label(CurrencyUtil.format(item.getPrice()));
        Button btnAdd = new Button("Thêm vào giỏ");
        btnAdd.setOnAction(e -> addToCart(item));

        box.getChildren().addAll(img, name, price, btnAdd);
        return box;
    }

    private void addToCart(Item item) {
        try {
            currentCart = orderService.addItemToCart(currentCart, item, 1);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Đã thêm vào giỏ");
            alert.setHeaderText("Thành công");
            alert.showAndWait();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Không thể thêm vào giỏ: " + e.getMessage());
            alert.setHeaderText("Lỗi");
            alert.showAndWait();
        }
    }
}
