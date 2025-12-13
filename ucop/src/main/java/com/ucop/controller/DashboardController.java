package com.ucop.controller;

import com.ucop.service.ReportService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class DashboardController {

    @FXML
    private BarChart<String, Number> chartRevenue;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    @FXML
    private PieChart chartTopItems;

    @FXML
    private PieChart chartPaymentMethod;

    @FXML
    private PieChart chartOrderStatus;

    private final ReportService reportService = new ReportService();

    @FXML
    public void initialize() {
        loadRevenueChart();
        loadTopItemsChart();
        loadPaymentMethodChart();
        loadOrderStatusChart();
    }

    private void loadRevenueChart() {
        Map<String, BigDecimal> data = reportService.revenueLastDays(7);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh thu 7 ngày gần nhất");

        for (Map.Entry<String, BigDecimal> e : data.entrySet()) {
            series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
        }

        chartRevenue.getData().clear();
        chartRevenue.getData().add(series);
    }

    private void loadTopItemsChart() {
        List<Object[]> rows = reportService.topSellingItems(5);

        var pieData = FXCollections.<PieChart.Data>observableArrayList();
        for (Object[] row : rows) {
            String itemName = (String) row[0];
            Number qty = (Number) row[1];
            pieData.add(new PieChart.Data(itemName, qty.doubleValue()));
        }

        chartTopItems.setData(pieData);
    }

    private void loadPaymentMethodChart() {
        List<Object[]> rows = reportService.revenueByPaymentMethod();
        var pieData = FXCollections.<PieChart.Data>observableArrayList();
        for (Object[] row : rows) {
            String method = row[0] != null ? row[0].toString() : "UNKNOWN";
            Number total = (Number) row[1];
            pieData.add(new PieChart.Data(method, total.doubleValue()));
        }
        chartPaymentMethod.setData(pieData);
    }

    private void loadOrderStatusChart() {
        List<Object[]> rows = reportService.orderStatusSummary();
        var pieData = FXCollections.<PieChart.Data>observableArrayList();
        for (Object[] row : rows) {
            String status = row[0] != null ? row[0].toString() : "UNKNOWN";
            Number cnt = (Number) row[1];
            pieData.add(new PieChart.Data(status, cnt.doubleValue()));
        }
        chartOrderStatus.setData(pieData);
    }
}
