package com.ucop.controller;

import com.ucop.MainApplication;
import com.ucop.entity.Account;
import com.ucop.entity.enums.RoleName;
import com.ucop.security.SecurityContext;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainLayoutController {

    @FXML
    private Label lblCurrentUser;

    @FXML
    private StackPane contentPane;

    @FXML private Button btnDashboard;
    @FXML private Button btnUser;
    @FXML private Button btnCategory;
    @FXML private Button btnItem;
    @FXML private Button btnStock;
    @FXML private Button btnCart;
    @FXML private Button btnOrders;
    @FXML private Button btnPromotions;

    @FXML
    public void initialize() {
        showDashboard();
    }

    public void setCurrentAccount(Account acc) {
        if (lblCurrentUser != null && acc != null) {
            lblCurrentUser.setText("Dang dang nh?p: " + acc.getUsername());
        }
        SecurityContext.setCurrentUser(acc);
        applyRoleVisibility(acc);
        showDashboard();
    }

    private void applyRoleVisibility(Account acc) {
        if (acc == null || acc.getRoles() == null) return;
        boolean isAdmin = acc.getRoles().stream().anyMatch(r -> r.getName() == RoleName.ADMIN);
        boolean isStaff = acc.getRoles().stream().anyMatch(r -> r.getName() == RoleName.STAFF);
        boolean isCustomer = acc.getRoles().stream().anyMatch(r -> r.getName() == RoleName.CUSTOMER);

        if (isAdmin) return;

        if (isStaff) {
            if (btnUser != null) btnUser.setVisible(false);
            if (btnPromotions != null) btnPromotions.setVisible(false);
        }

        if (isCustomer && !isStaff) {
            if (btnUser != null) btnUser.setVisible(false);
            if (btnCategory != null) btnCategory.setVisible(false);
            if (btnItem != null) btnItem.setVisible(false);
            if (btnStock != null) btnStock.setVisible(false);
            if (btnPromotions != null) btnPromotions.setVisible(false);
        }
    }

    /* ======= CAC HAM CHUY?N MAN ======= */

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

    @FXML
    public void handleLogout() {
        SecurityContext.clear();
        try {
            FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("/fxml/login.fxml"));
            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(loader.load()));
            stage.setTitle("UCOP - Login");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ======= HAM LOAD FXML CON ======= */

    private void setContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource(fxmlPath));
            Node node = loader.load();
            contentPane.getChildren().setAll(node);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
