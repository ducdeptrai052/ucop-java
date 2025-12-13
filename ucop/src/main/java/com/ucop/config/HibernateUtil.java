package com.ucop.config;

import com.ucop.entity.*;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

public class HibernateUtil {

    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            // Đọc cấu hình từ hibernate.cfg.xml
            StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                    .configure() // mặc định đọc /hibernate.cfg.xml trong classpath
                    .build();

            MetadataSources sources = new MetadataSources(registry)
                    .addAnnotatedClass(Account.class)
                    .addAnnotatedClass(AccountProfile.class)
                    .addAnnotatedClass(Role.class)
                    .addAnnotatedClass(Category.class)
                    .addAnnotatedClass(Item.class)
                    .addAnnotatedClass(Warehouse.class)
                    .addAnnotatedClass(StockItem.class)
                    .addAnnotatedClass(Cart.class)
                    .addAnnotatedClass(CartItem.class)
                    .addAnnotatedClass(Order.class)
                    .addAnnotatedClass(OrderItem.class)
                    .addAnnotatedClass(Payment.class)
                    .addAnnotatedClass(Promotion.class);

            return sources.buildMetadata().buildSessionFactory();
        } catch (Exception ex) {
            System.err.println("❌ Initial SessionFactory creation failed: " + ex.getMessage());
            ex.printStackTrace();
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
