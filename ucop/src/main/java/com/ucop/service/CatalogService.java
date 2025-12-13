package com.ucop.service;

import com.ucop.config.HibernateUtil;
import com.ucop.entity.Category;
import com.ucop.entity.Item;
import com.ucop.entity.StockItem;
import com.ucop.entity.Warehouse;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class CatalogService {

    /* ============ CATEGORY ============ */

    public List<Category> findAllCategories() {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            return s.createQuery("from Category", Category.class).list();
        }
    }

    public void saveCategory(Category c) {
        Transaction tx = null;
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            tx = s.beginTransaction();
            if (c.getId() == null) {
                s.persist(c);
            } else {
                s.merge(c);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public void deleteCategory(Category c) {
        if (c == null || c.getId() == null) return;
        Transaction tx = null;
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            tx = s.beginTransaction();
            Category managed = s.get(Category.class, c.getId());
            if (managed != null) {
                s.remove(managed);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    /* ============ ITEM ============ */

    public List<Item> findAllItems() {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            return s.createQuery("from Item i join fetch i.category", Item.class).list();
        }
    }

    public void saveItem(Item item) {
        Transaction tx = null;
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            tx = s.beginTransaction();
            if (item.getId() == null) {
                s.persist(item);
            } else {
                s.merge(item);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public void deleteItem(Item item) {
        if (item == null || item.getId() == null) return;
        Transaction tx = null;
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            tx = s.beginTransaction();
            Item managed = s.get(Item.class, item.getId());
            if (managed != null) {
                s.remove(managed);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    /* ============ WAREHOUSE / STOCK ============ */

    public List<Warehouse> findAllWarehouses() {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            return s.createQuery("from Warehouse", Warehouse.class).list();
        }
    }

    public List<StockItem> findAllStock() {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            return s.createQuery(
                    "from StockItem si join fetch si.item join fetch si.warehouse",
                    StockItem.class
            ).list();
        }
    }

    /** Sản phẩm sắp hết hàng: onHand - reserved <= lowStockThreshold */
    public List<StockItem> findLowStock() {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            return s.createQuery(
                    "from StockItem si join fetch si.item " +
                            "where (si.onHand - si.reserved) <= si.lowStockThreshold",
                    StockItem.class
            ).list();
        }
    }
}
