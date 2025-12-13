package com.ucop.controller;

import com.ucop.entity.Account;
import com.ucop.service.AccountService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class UserManagementController {

    @FXML
    private TableView<Account> tblUsers;

    @FXML
    private TableColumn<Account, Long> colId;

    @FXML
    private TableColumn<Account, String> colUsername;

    @FXML
    private TableColumn<Account, String> colEmail;

    private final AccountService accountService = new AccountService();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        tblUsers.setItems(FXCollections.observableArrayList(accountService.findAll()));
    }
}
