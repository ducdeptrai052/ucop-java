package com.ucop.controller;

import com.ucop.entity.Account;
import com.ucop.entity.AccountProfile;
import com.ucop.entity.enums.RoleName;
import com.ucop.service.AccountService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.stream.Collectors;

public class UserManagementController {

    @FXML
    private TableView<Account> tblUsers;

    @FXML
    private TableColumn<Account, Long> colId;

    @FXML
    private TableColumn<Account, String> colUsername;

    @FXML
    private TableColumn<Account, String> colEmail;

    @FXML
    private TableColumn<Account, Boolean> colLocked;

    @FXML
    private TableColumn<Account, String> colRoles;

    @FXML private TextField txtUsername;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<RoleName> cbRole;
    @FXML private CheckBox chkLocked;
    @FXML private TextField txtFullName;
    @FXML private TextField txtPhone;
    @FXML private TextField txtAddress;

    private final AccountService accountService = new AccountService();
    private Account selected;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleLongProperty(c.getValue().getId()).asObject());
        colUsername.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getUsername()));
        colEmail.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEmail()));
        colLocked.setCellValueFactory(c -> new javafx.beans.property.SimpleBooleanProperty(c.getValue().isLocked()));
        colRoles.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getRoles().stream()
                        .map(r -> r.getName().name())
                        .collect(Collectors.joining(", "))
        ));

        cbRole.setItems(FXCollections.observableArrayList(RoleName.values()));

        loadUsers();

        tblUsers.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            selected = val;
            if (val != null) {
                txtUsername.setText(val.getUsername());
                txtEmail.setText(val.getEmail());
                chkLocked.setSelected(val.isLocked());
                if (val.getProfile() != null) {
                    txtFullName.setText(val.getProfile().getFullName());
                    txtPhone.setText(val.getProfile().getPhone());
                    txtAddress.setText(val.getProfile().getAddress());
                }
            } else {
                handleNew();
            }
        });
    }

    @FXML
    private void handleNew() {
        selected = null;
        txtUsername.clear();
        txtEmail.clear();
        txtPassword.clear();
        txtFullName.clear();
        txtPhone.clear();
        txtAddress.clear();
        cbRole.getSelectionModel().clearSelection();
        chkLocked.setSelected(false);
        tblUsers.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleSave() {
        String username = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();
        String pwd = txtPassword.getText() != null ? txtPassword.getText().trim() : "";

        if (username.isEmpty() || email.isEmpty()) {
            alert("Vui lòng nhập đủ username/email");
            return;
        }

        if (selected == null) {
            if (pwd.isEmpty()) {
                alert("Nhập mật khẩu cho tài khoản mới");
                return;
            }
            Account acc = accountService.createUser(username, pwd, email);
            applyProfile(acc);
            accountService.save(acc);
            if (cbRole.getSelectionModel().getSelectedItem() != null) {
                accountService.addRole(acc, cbRole.getSelectionModel().getSelectedItem());
            }
        } else {
            selected.setEmail(email);
            selected.setLocked(chkLocked.isSelected());
            applyProfile(selected);
            accountService.save(selected);
            if (!pwd.isEmpty()) {
                accountService.changePassword(selected, pwd);
            }
        }
        loadUsers();
        handleNew();
    }

    @FXML
    private void handleDelete() {
        if (selected == null) return;
        if (confirm("Xóa tài khoản?", "Bạn có chắc muốn xóa?")) {
            accountService.delete(selected);
            loadUsers();
            handleNew();
        }
    }

    @FXML
    private void handleChangePassword() {
        if (selected == null) {
            alert("Chọn tài khoản trước");
            return;
        }
        String pwd = txtPassword.getText() != null ? txtPassword.getText().trim() : "";
        if (pwd.isEmpty()) {
            alert("Nhập mật khẩu mới");
            return;
        }
        accountService.changePassword(selected, pwd);
        alert("Đổi mật khẩu thành công");
        txtPassword.clear();
    }

    @FXML
    private void handleAssignRole() {
        if (selected == null) {
            alert("Chọn tài khoản trước");
            return;
        }
        RoleName role = cbRole.getSelectionModel().getSelectedItem();
        if (role == null) {
            alert("Chọn role");
            return;
        }
        accountService.addRole(selected, role);
        loadUsers();
    }

    private void loadUsers() {
        tblUsers.setItems(FXCollections.observableArrayList(accountService.findAll()));
    }

    private void applyProfile(Account acc) {
        AccountProfile profile = acc.getProfile();
        if (profile == null) {
            profile = new AccountProfile();
            acc.setProfile(profile);
        }
        profile.setFullName(txtFullName.getText());
        profile.setPhone(txtPhone.getText());
        profile.setAddress(txtAddress.getText());
    }

    private void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private boolean confirm(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.OK, ButtonType.CANCEL);
        a.setHeaderText(title);
        return a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}
