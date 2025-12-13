package com.ucop.service;

import com.ucop.config.HibernateUtil;
import com.ucop.dao.GenericDao;
import com.ucop.entity.Payment;
import com.ucop.entity.enums.PaymentStatus;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.util.List;

public class PaymentService {

    private final GenericDao<Payment> paymentDao = new GenericDao<>(Payment.class);

    public List<Payment> findAll() {
        return paymentDao.findAll();
    }

    public void markPaid(Payment payment, String transactionCode) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            payment = session.merge(payment);
            payment.setStatus(PaymentStatus.PAID);
            payment.setTransactionCode(transactionCode);
            payment.setPaidAt(LocalDateTime.now());
            tx.commit();
        }
    }
}
