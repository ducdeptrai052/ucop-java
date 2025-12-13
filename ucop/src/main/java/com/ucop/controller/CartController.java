package com.ucop.controller;

import com.ucop.entity.Cart;
import com.ucop.entity.CartItem;
import com.ucop.entity.Item;
import com.ucop.entity.Order;
import com.ucop.entity.Promotion;
import com.ucop.entity.enums.PaymentMethod;
import com.ucop.service.CatalogService;
import com.ucop.service.OrderService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.util.ArrayList;

public class CartController {

    @FXML
    private ComboBox<Item> cbItem;

    @FXML
    private TextField txtQty;

    @FXML
    private TableView<CartItem> tblCart;

    @FXML
    private TableColumn<CartItem, String> colItem;

    @FXML
    private TableColumn<CartItem, Number> colQty;

    @FXML
    private TableColumn<CartItem, Number> colPrice;

    @FXML
    private Label lblSubtotal;

    @FXML
    private Label lblDiscount;

    @FXML
    private Label lblTax;

    @FXML
    private Label lblShipping;

    @FXML
    private Label lblGrandTotal;

    @FXML
    private TextField txtPromotionCode;

    @FXML
    private ComboBox<PaymentMethod> cbPaymentMethod;

    private final CatalogService catalogService = new CatalogService();
    private final OrderService orderService = OrderService.getInstance();

    private Cart currentCart;
    private Promotion currentPromotion;

    @FXML
    public void initialize() {
        // combobox sản phẩm
        cbItem.setItems(FXCollections.observableList(catalogService.findAllItems()));

        // phương thức thanh toán (enum)
        cbPaymentMethod.setItems(FXCollections.observableArrayList(PaymentMethod.values()));

        // cấu hình cột bảng
        colItem.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getItem().getName()));
        colQty.setCellValueFactory(c ->
                new javafx.beans.property.SimpleIntegerProperty(c.getValue().getQuantity()));
        colPrice.setCellValueFactory(c ->
                new javafx.beans.property.SimpleDoubleProperty(
                        c.getValue().getUnitPrice().doubleValue()));

        // lấy cart hiện tại của user
        currentCart = orderService.getOrCreateCartForCurrentUser();
        reloadCart();
    }

    /* ======== HÀNH ĐỘNG UI ======== */

    @FXML
    private void handleAddItem() {
        Item it = cbItem.getSelectionModel().getSelectedItem();
        if (it == null) {
            alert("Vui lòng chọn sản phẩm");
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(txtQty.getText().trim());
        } catch (NumberFormatException e) {
            alert("Số lượng không hợp lệ");
            return;
        }
        if (qty <= 0) {
            alert("Số lượng phải > 0");
            return;
        }

        currentCart = orderService.addItemToCart(currentCart, it, qty);
        reloadCart();
        txtQty.clear();
    }

    @FXML
    private void handleRemoveSelected() {
        CartItem ci = tblCart.getSelectionModel().getSelectedItem();
        if (ci == null || currentCart == null) return;
        currentCart = orderService.removeCartItem(currentCart.getId(), ci.getItem().getId());
        reloadCart();
    }

    @FXML
    private void handleUpdateQty() {
        CartItem ci = tblCart.getSelectionModel().getSelectedItem();
        if (ci == null || currentCart == null) return;
        try {
            int qty = Integer.parseInt(txtQty.getText().trim());
            currentCart = orderService.updateCartItemQty(currentCart.getId(), ci.getItem().getId(), qty);
            reloadCart();
        } catch (NumberFormatException e) {
            alert("Số lượng không hợp lệ");
        }
    }

    @FXML
    private void handleApplyPromotion() {
        String code = txtPromotionCode.getText().trim();
        if (code.isEmpty()) {
            currentPromotion = null;
            recalcTotals();
            return;
        }

        currentPromotion = orderService.findValidPromotion(code);
        if (currentPromotion == null) {
            alert("Không tìm thấy mã giảm giá");
        }
        recalcTotals();
    }

    @FXML
    private void handleCheckout() {
        PaymentMethod method = cbPaymentMethod.getSelectionModel().getSelectedItem();
        if (method == null) {
            alert("Vui lòng chọn phương thức thanh toán");
            return;
        }

        try {
            Order order = orderService.checkoutCurrentCart(currentPromotion, method);
            alert("Tạo đơn thành công, mã đơn: " + order.getId());

            // tạo cart mới và reload
            currentCart = orderService.getOrCreateCartForCurrentUser();
            currentPromotion = null;
            txtPromotionCode.clear();
            cbPaymentMethod.getSelectionModel().clearSelection();
            reloadCart();
        } catch (Exception e) {
            e.printStackTrace();
            alert("Lỗi khi tạo đơn: " + e.getMessage());
        }
    }

    /* ======== HÀM PHỤ ======== */

    private void reloadCart() {
        if (currentCart == null) {
            tblCart.setItems(FXCollections.observableArrayList());
            recalcTotals();
            return;
        }

        Cart fresh = orderService.loadCartWithItems(currentCart.getId());
        currentCart = fresh != null ? fresh : currentCart;

        ObservableList<CartItem> data =
                FXCollections.observableArrayList(new ArrayList<>(currentCart.getItems()));
        tblCart.setItems(data);
        recalcTotals();
    }

    private void recalcTotals() {
        BigDecimal subtotal = BigDecimal.ZERO;

        if (currentCart != null && currentCart.getItems() != null) {
            for (CartItem ci : currentCart.getItems()) {
                BigDecimal line = ci.getUnitPrice()
                        .multiply(BigDecimal.valueOf(ci.getQuantity()))
                        .subtract(ci.getDiscountAmount());
                subtotal = subtotal.add(line);
            }
        }

        BigDecimal discountCart = BigDecimal.ZERO;
        if (currentPromotion != null) {
            if (currentPromotion.getDiscountPercent() != null) {
                discountCart = subtotal
                        .multiply(BigDecimal.valueOf(currentPromotion.getDiscountPercent()))
                        .divide(BigDecimal.valueOf(100));
            } else if (currentPromotion.getDiscountValue() != null) {
                discountCart = currentPromotion.getDiscountValue();
            }
        }

        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.1)); // VAT 10% demo
        BigDecimal shipping = subtotal.compareTo(BigDecimal.ZERO) > 0
                ? BigDecimal.valueOf(30000)
                : BigDecimal.ZERO;

        BigDecimal grand = subtotal.subtract(discountCart).add(tax).add(shipping);

        lblSubtotal.setText(subtotal.toString());
        lblDiscount.setText(discountCart.toString());
        lblTax.setText(tax.toString());
        lblShipping.setText(shipping.toString());
        lblGrandTotal.setText(grand.toString());
    }

    private void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
