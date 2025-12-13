package com.ucop.service;

import com.ucop.config.HibernateUtil;
import com.ucop.dao.GenericDao;
import com.ucop.entity.Account;
import com.ucop.entity.Role;
import com.ucop.entity.enums.RoleName;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class AccountService {

    private final GenericDao<Account> accountDao = new GenericDao<>(Account.class);

    public List<Account> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "select distinct a from Account a left join fetch a.roles",
                            Account.class)
                    .list();
        }
    }

    public Account findById(Long id) {
        return accountDao.findById(id);
    }

    public Account findByUsername(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "from Account a where a.username = :u",
                            Account.class)
                    .setParameter("u", username)
                    .uniqueResult();
        }
    }

    public void save(Account account) {
        accountDao.saveOrUpdate(account);
    }

    public void delete(Account account) {
        accountDao.delete(account);
    }

    public void addRole(Account account, RoleName roleName) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Role> q = session.createQuery("from Role r where r.name = :n", Role.class);
            q.setParameter("n", roleName);
            Role role = q.uniqueResult();
            if (role == null) {
                role = new Role(roleName);
                session.beginTransaction();
                session.persist(role);
                session.getTransaction().commit();
            }
            account.getRoles().add(role);
            save(account);
        }
    }

    public void changePassword(Account account, String newRawPassword) {
        account.setPasswordHash(hashPassword(newRawPassword));
        save(account);
    }

    public void lockAccount(Account account) {
        account.setLocked(true);
        save(account);
    }

    public void unlockAccount(Account account) {
        account.setLocked(false);
        save(account);
    }

    public Account createUser(String username, String rawPassword, String email) {
        Account acc = new Account();
        acc.setUsername(username);
        acc.setPasswordHash(hashPassword(rawPassword));
        acc.setEmail(email);
        save(acc);
        return acc;
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
