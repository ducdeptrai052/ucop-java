package com.ucop.service;

import com.ucop.config.HibernateUtil;
import com.ucop.entity.Category;
import com.ucop.entity.Item;
import com.ucop.entity.StockItem;
import com.ucop.entity.Warehouse;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.FileReader;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.nio.file.Path;
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
                // Xóa trước các bản ghi phụ thuộc (stock, cart) để tránh lỗi FK.
                s.createQuery("delete from StockItem si where si.item.id = :iid")
                        .setParameter("iid", managed.getId())
                        .executeUpdate();
                s.createQuery("delete from CartItem ci where ci.item.id = :iid")
                        .setParameter("iid", managed.getId())
                        .executeUpdate();
                // Lịch sử OrderItem nên giữ lại; nếu có FK, xóa sẽ bị chặn -> để người dùng biết.
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

    public void saveWarehouse(Warehouse wh) {
        Transaction tx = null;
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            tx = s.beginTransaction();
            if (wh.getId() == null) {
                s.persist(wh);
            } else {
                s.merge(wh);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
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

    public void saveStockItem(Warehouse wh, Item item, int onHand, int reserved, int lowStock) {
        Transaction tx = null;
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            tx = s.beginTransaction();
            StockItem stock = s.createQuery(
                            "from StockItem si where si.item.id = :iid and si.warehouse.id = :wid",
                            StockItem.class)
                    .setParameter("iid", item.getId())
                    .setParameter("wid", wh.getId())
                    .setMaxResults(1)
                    .uniqueResult();
            if (stock == null) {
                stock = new StockItem();
                stock.setItem(item);
                stock.setWarehouse(wh);
            }
            stock.setOnHand(onHand);
            stock.setReserved(reserved);
            stock.setLowStockThreshold(lowStock);
            s.merge(stock);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    /* ============ CSV IMPORT/EXPORT ============ */

    public void exportItemsToCsv(Path path) throws Exception {
        List<Item> items = findAllItems();
        try (CSVWriter writer = new CSVWriter(new FileWriter(path.toFile()))) {
            writer.writeNext(new String[]{"sku", "name", "price", "active"});
            for (Item i : items) {
                writer.writeNext(new String[]{
                        i.getSku(),
                        i.getName(),
                        i.getPrice() != null ? i.getPrice().toString() : "",
                        Boolean.toString(i.isActive())
                });
            }
        }
    }

    public void importItemsFromCsv(Path path) throws Exception {
        try (CSVReader reader = new CSVReader(new FileReader(path.toFile()))) {
            List<String[]> rows = reader.readAll();
            if (rows.isEmpty()) return;
            for (int i = 1; i < rows.size(); i++) {
                String[] r = rows.get(i);
                if (r.length < 3) continue;
                Item item = new Item();
                item.setSku(r[0]);
                item.setName(r[1]);
                item.setPrice(new BigDecimal(r[2]));
                item.setActive(r.length > 3 ? Boolean.parseBoolean(r[3]) : true);
                saveItem(item);
            }
        }
    }

    public void exportCategoriesToCsv(Path path) throws Exception {
        List<Category> cats = findAllCategories();
        try (CSVWriter writer = new CSVWriter(new FileWriter(path.toFile()))) {
            writer.writeNext(new String[]{"name"});
            for (Category c : cats) {
                writer.writeNext(new String[]{c.getName()});
            }
        }
    }

    public void importCategoriesFromCsv(Path path) throws Exception {
        try (CSVReader reader = new CSVReader(new FileReader(path.toFile()))) {
            List<String[]> rows = reader.readAll();
            if (rows.isEmpty()) return;
            for (int i = 1; i < rows.size(); i++) {
                String[] r = rows.get(i);
                if (r.length == 0 || r[0].isBlank()) continue;
                Category c = new Category();
                c.setName(r[0]);
                saveCategory(c);
            }
        }
    }

}
