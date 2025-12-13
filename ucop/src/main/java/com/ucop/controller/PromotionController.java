package com.ucop.controller;

import com.ucop.dao.GenericDao;
import com.ucop.entity.Promotion;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PromotionController {

    @FXML
    private TableView<Promotion> tblPromotions;

    @FXML
    private TableColumn<Promotion, Long> colId;

    @FXML
    private TableColumn<Promotion, String> colCode;

    @FXML
    private TableColumn<Promotion, String> colDescription;

    @FXML
    private TableColumn<Promotion, String> colUsed;

    @FXML private TextField txtCode;
    @FXML private TextField txtDescription;
    @FXML private TextField txtPercent;
    @FXML private TextField txtValue;
    @FXML private TextField txtStart;
    @FXML private TextField txtEnd;
    @FXML private TextField txtMaxUsage;
    @FXML private TextField txtMinOrder;

    private final GenericDao<Promotion> promoDao = new GenericDao<>(Promotion.class);
    private Promotion selected;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colUsed.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                (c.getValue().getUsedCount() != null ? c.getValue().getUsedCount() : 0) + "/" +
                        (c.getValue().getMaxUsage() != null ? c.getValue().getMaxUsage() : "∞")));

        loadData();

        tblPromotions.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            selected = val;
            if (val != null) {
                txtCode.setText(val.getCode());
                txtDescription.setText(val.getDescription());
                txtPercent.setText(val.getDiscountPercent() != null ? val.getDiscountPercent().toString() : "");
                txtValue.setText(val.getDiscountValue() != null ? val.getDiscountValue().toString() : "");
                txtStart.setText(val.getStartDate() != null ? val.getStartDate().toLocalDate().toString() : "");
                txtEnd.setText(val.getEndDate() != null ? val.getEndDate().toLocalDate().toString() : "");
                txtMaxUsage.setText(val.getMaxUsage() != null ? val.getMaxUsage().toString() : "");
                txtMinOrder.setText(val.getMinOrderValue() != null ? val.getMinOrderValue().toString() : "");
            } else {
                handleNew();
            }
        });
    }

    @FXML
    private void handleNew() {
        selected = null;
        txtCode.clear();
        txtDescription.clear();
        txtPercent.clear();
        txtValue.clear();
        txtStart.clear();
        txtEnd.clear();
        txtMaxUsage.clear();
        txtMinOrder.clear();
        tblPromotions.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleSave() {
        try {
            if (txtCode.getText().trim().isEmpty()) {
                alert("Nhập code");
                return;
            }
            Promotion p = selected == null ? new Promotion() : selected;
            p.setCode(txtCode.getText().trim());
            p.setDescription(txtDescription.getText());
            p.setDiscountPercent(txtPercent.getText().isBlank() ? null : Integer.parseInt(txtPercent.getText().trim()));
            p.setDiscountValue(txtValue.getText().isBlank() ? null : new BigDecimal(txtValue.getText().trim()));
            p.setStartDate(parseDate(txtStart.getText()));
            p.setEndDate(parseDate(txtEnd.getText()));
            p.setMaxUsage(txtMaxUsage.getText().isBlank() ? null : Integer.parseInt(txtMaxUsage.getText().trim()));
            p.setMinOrderValue(txtMinOrder.getText().isBlank() ? null : new BigDecimal(txtMinOrder.getText().trim()));

            promoDao.saveOrUpdate(p);
            loadData();
            handleNew();
        } catch (Exception e) {
            alert("Lỗi lưu khuyến mãi: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        if (selected == null) return;
        promoDao.delete(selected);
        loadData();
        handleNew();
    }

    private LocalDateTime parseDate(String txt) {
        if (txt == null || txt.isBlank()) return null;
        return LocalDate.parse(txt.trim()).atStartOfDay();
    }

    private void loadData() {
        tblPromotions.setItems(FXCollections.observableArrayList(promoDao.findAll()));
    }

    private void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
