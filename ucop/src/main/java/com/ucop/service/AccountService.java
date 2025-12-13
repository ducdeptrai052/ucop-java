package com.ucop.service;

import com.ucop.config.HibernateUtil;
import com.ucop.dao.GenericDao;
import com.ucop.entity.Account;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class AccountService {

    private final GenericDao<Account> accountDao = new GenericDao<>(Account.class);

    public List<Account> findAll() {
        return accountDao.findAll();
    }

    public void save(Account account) {
        accountDao.saveOrUpdate(account);
    }

    public void delete(Account account) {
        accountDao.delete(account);
    }

    public Account login(String username, String rawPassword) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            // DÙNG entity name = tên class "Account"
            Query<Account> q = session.createQuery(
                    "select a from Account a left join fetch a.roles " +
                            "where a.username = :u and a.locked = false",
                    Account.class
            );
            q.setParameter("u", username);

            Account acc = q.uniqueResult();
            if (acc == null) return null;

            String hashed = hashPassword(rawPassword);
            if (hashed.equals(acc.getPasswordHash())) {
                return acc;
            }
            return null;
        }
    }

    public String hashPassword(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
