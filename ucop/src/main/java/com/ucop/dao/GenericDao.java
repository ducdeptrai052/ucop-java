package com.ucop.dao;

import com.ucop.config.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
import java.util.function.Consumer;

public class GenericDao<T> {

    private final Class<T> entityClass;

    public GenericDao(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public void saveOrUpdate(T entity) {
        executeInsideTransaction(session -> session.merge(entity));
    }

    public void delete(T entity) {
        executeInsideTransaction(session -> {
            T managed = session.contains(entity) ? entity : session.merge(entity);
            session.remove(managed);
        });
    }

    public T findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(entityClass, id);
        }
    }

    public List<T> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from " + entityClass.getSimpleName(), entityClass).list();
        }
    }

    private void executeInsideTransaction(Consumer<Session> action) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            action.accept(session);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw e;
        }
    }
}
