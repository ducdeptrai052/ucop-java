package com.ucop.controller;

import com.ucop.MainApplication;
import com.ucop.entity.Account;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class MainLayoutController {

    @FXML
    private Label lblCurrentUser;

    @FXML
    private StackPane contentPane;

    private Account currentAccount;

    @FXML
    public void initialize() {
        // có thể load dashboard mặc định ở đây nếu muốn
        showDashboard();
    }

    public void setCurrentAccount(Account acc) {
        this.currentAccount = acc;
        if (lblCurrentUser != null && acc != null) {
            lblCurrentUser.setText("Đang đăng nhập: " + acc.getUsername());
        }
        // khi login xong thì load dashboard
        showDashboard();
    }

    /* ======= CÁC HÀM CHUYỂN MÀN ======= */

    @FXML
    public void showDashboard() {
        setContent("/fxml/dashboard.fxml");
    }

    @FXML
    public void showUserManagement() {
        setContent("/fxml/user-management.fxml");
    }

    @FXML
    public void showCategory() {
        setContent("/fxml/category-list.fxml");
    }

    @FXML
    public void showItem() {
        setContent("/fxml/item-list.fxml");
    }

    @FXML
    public void showStock() {
        setContent("/fxml/stock-list.fxml");
    }

    @FXML
    public void showCart() {
        setContent("/fxml/cart.fxml");
    }

    @FXML
    public void showOrders() {
        setContent("/fxml/order-list.fxml");
    }

    @FXML
    public void showPromotions() {
        setContent("/fxml/promotion-list.fxml");
    }

    /* ======= HÀM LOAD FXML CON ======= */

    private void setContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource(fxmlPath));
            Node node = loader.load();
            contentPane.getChildren().setAll(node);
        } catch (Exception e) {
            e.printStackTrace();
            // em có thể log ra label hoặc Alert nếu muốn
        }
    }
}
