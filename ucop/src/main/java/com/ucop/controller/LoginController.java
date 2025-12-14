package com.ucop.controller;

import com.ucop.MainApplication;
import com.ucop.entity.Account;
import com.ucop.service.AccountService;
import com.ucop.service.OrderService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Label lblError;

    private final AccountService accountService = new AccountService();

    @FXML
    private void handleLogin(ActionEvent event) {
        lblError.setText("");

        String u = txtUsername.getText() != null ? txtUsername.getText().trim() : "";
        String p = txtPassword.getText() != null ? txtPassword.getText().trim() : "";

        if (u.isEmpty() || p.isEmpty()) {
            lblError.setText("Vui lòng nhập username và password");
            return;
        }

        try {
            // kiểm tra tài khoản
            Account acc = accountService.login(u, p);

            if (acc == null) {
                lblError.setText("Sai thông tin đăng nhập hoặc tài khoản bị khóa");
                return;
            }

            // set user hiện tại cho OrderService (để dùng cho Cart/Order)
            OrderService.getInstance().setCurrentAccount(acc);

            // load giao diện chính
            FXMLLoader loader = new FXMLLoader(
                    MainApplication.class.getResource("/fxml/main-layout.fxml"));
            Scene scene = new Scene(loader.load());

            MainLayoutController controller = loader.getController();
            controller.setCurrentAccount(acc);

            Stage stage = (Stage) txtUsername.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("UCOP - Main");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            lblError.setText("Lỗi tải giao diện chính");
        }
    }
}
