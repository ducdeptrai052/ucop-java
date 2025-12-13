package com.ucop.service;

import com.ucop.config.HibernateUtil;
import com.ucop.entity.Order;
import com.ucop.entity.OrderItem;
import org.hibernate.Session;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportService {

    /**
     * Doanh thu theo ngày trong N ngày gần nhất
     */
    public Map<String, BigDecimal> revenueLastDays(int days) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            LocalDate from = LocalDate.now().minusDays(days - 1);

            var query = session.createQuery(
                    "select function('date', o.orderDate), sum(o.grandTotal) " +
                            "from Order o " +
                            "where o.orderDate >= :fromDate " +
                            "group by function('date', o.orderDate) " +
                            "order by function('date', o.orderDate)",
                    Object[].class
            );
            query.setParameter("fromDate", from.atStartOfDay());

            Map<String, BigDecimal> result = new LinkedHashMap<>();
            for (Object[] row : query.list()) {
                String date = row[0].toString();
                BigDecimal total = (BigDecimal) row[1];
                result.put(date, total);
            }
            return result;
        }
    }

    /**
     * Top sản phẩm bán chạy (tính theo số lượng)
     */
    public List<Object[]> topSellingItems(int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "select oi.item.name, sum(oi.quantity) " +
                                    "from OrderItem oi " +
                                    "group by oi.item.name " +
                                    "order by sum(oi.quantity) desc",
                            Object[].class
                    )
                    .setMaxResults(limit)
                    .list();
        }
    }
}
