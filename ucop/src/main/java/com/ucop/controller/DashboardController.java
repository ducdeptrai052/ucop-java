package com.ucop.controller;

import com.ucop.service.ReportService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import com.ucop.util.CurrencyUtil;

import java.math.BigDecimal;

public class DashboardController {

    @FXML private Label lblTotalRevenue;
    @FXML private Label lblTotalOrders;
    @FXML private Label lblItemsSold;
    @FXML private Label lblRevenueByPayment;
    @FXML private Label lblOrderStatus;

    private final ReportService reportService = new ReportService();

    @FXML
    public void initialize() {
        loadStats();
    }

    private void loadStats() {
        BigDecimal totalRevenue = reportService.totalRevenue();
        long totalOrders = reportService.totalOrders();
        long itemsSold = reportService.totalItemsSold();

        lblTotalRevenue.setText(CurrencyUtil.format(totalRevenue));
        lblTotalOrders.setText(Long.toString(totalOrders));
        lblItemsSold.setText(Long.toString(itemsSold));

        var paymentRows = reportService.revenueByPaymentMethod();
        StringBuilder pm = new StringBuilder();
        paymentRows.forEach(r -> pm.append(r[0]).append(": ").append(r[1]).append("; "));
        lblRevenueByPayment.setText(pm.toString());

        var statusRows = reportService.orderStatusSummary();
        StringBuilder st = new StringBuilder();
        statusRows.forEach(r -> st.append(r[0]).append(": ").append(r[1]).append("; "));
        lblOrderStatus.setText(st.toString());
    }
}
