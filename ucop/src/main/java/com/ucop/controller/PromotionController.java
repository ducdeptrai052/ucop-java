package com.ucop.controller;

import com.ucop.dao.GenericDao;
import com.ucop.entity.Promotion;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class PromotionController {

    @FXML
    private TableView<Promotion> tblPromotions;

    @FXML
    private TableColumn<Promotion, Long> colId;

    @FXML
    private TableColumn<Promotion, String> colCode;

    @FXML
    private TableColumn<Promotion, String> colDescription;

    private final GenericDao<Promotion> promoDao = new GenericDao<>(Promotion.class);

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        tblPromotions.setItems(FXCollections.observableArrayList(promoDao.findAll()));
    }
}
