package com.ucop.service;

import com.ucop.config.HibernateUtil;
import com.ucop.entity.*;
import com.ucop.entity.enums.OrderStatus;
import com.ucop.entity.enums.PaymentMethod;
import com.ucop.entity.enums.PaymentStatus;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service xử lý Cart, Order, Promotion (Singleton).
 */
public class OrderService {

    /* ========== SINGLETON ========== */

    private static final OrderService INSTANCE = new OrderService();

    public static OrderService getInstance() {
        return INSTANCE;
    }

    // private constructor để tránh new bên ngoài
    private OrderService() {
    }

    /* ========== TRẠNG THÁI GLOBAL ========== */

    // user đang đăng nhập
    private Account currentAccount;

    // cart hiện tại của user (demo: giữ trên RAM)
    private Cart currentCart;

    public Account getCurrentAccount() {
        return currentAccount;
    }

    public void setCurrentAccount(Account currentAccount) {
        this.currentAccount = currentAccount;
        // đổi user thì clear cart
        this.currentCart = null;
    }

    /* ========== CART ========== */

    public Cart getOrCreateCartForCurrentUser() {
        if (currentCart == null) {
            currentCart = new Cart();
            // giả định trong entity Cart đã khởi tạo Set<CartItem>,
            // nếu chưa thì anh thêm new HashSet<>() trong Cart
        }
        return currentCart;
    }

    public void addItemToCart(Cart cart, Item item, int qty) {
        Set<CartItem> items = cart.getItems();
        if (items == null) {
            // phòng trường hợp Cart chưa khởi tạo items
            items = new HashSet<>();
            cart.getItems().addAll(items); // tránh dùng setItems (Cart không có)
        }

        CartItem existing = null;
        for (CartItem ci : items) {
            if (ci.getItem().getId().equals(item.getId())) {
                existing = ci;
                break;
            }
        }
        if (existing == null) {
            CartItem ci = new CartItem();
            ci.setCart(cart);
            ci.setItem(item);
            ci.setQuantity(qty);
            ci.setUnitPrice(item.getPrice());
            ci.setDiscountAmount(BigDecimal.ZERO);
            items.add(ci);
        } else {
            existing.setQuantity(existing.getQuantity() + qty);
        }
    }

    /* ========== PROMOTION ========== */

    public Promotion findValidPromotion(String code) {
        if (code == null || code.isBlank()) return null;
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            // đơn giản: tìm theo code, chưa filter active/expired để khỏi lệch field
            return s.createQuery(
                            "from Promotion p where p.code = :c",
                            Promotion.class)
                    .setParameter("c", code)
                    .setMaxResults(1)
                    .uniqueResult();
        }
    }

    /* ========== ORDER ========== */

    public List<Order> findAllOrders() {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            return s.createQuery(
                            "from Order o join fetch o.customer",
                            Order.class)
                    .list();
        }
    }

    /**
     * Checkout: dùng currentCart + currentAccount
     */
    public Order checkoutCurrentCart(Promotion promo) {
        if (currentAccount == null) {
            throw new IllegalStateException("Chưa set currentAccount cho OrderService");
        }
        if (currentCart == null || currentCart.getItems() == null || currentCart.getItems().isEmpty()) {
            throw new IllegalStateException("Giỏ hàng trống");
        }

        Transaction tx = null;
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            tx = s.beginTransaction();

            // Tính subtotal
            BigDecimal subtotal = BigDecimal.ZERO;
            for (CartItem ci : currentCart.getItems()) {
                BigDecimal line = ci.getUnitPrice()
                        .multiply(BigDecimal.valueOf(ci.getQuantity()))
                        .subtract(ci.getDiscountAmount());
                subtotal = subtotal.add(line);
            }

            // Giảm giá cart-level
            BigDecimal discountCart = BigDecimal.ZERO;
            if (promo != null) {
                if (promo.getDiscountPercent() != null) {
                    discountCart = subtotal
                            .multiply(BigDecimal.valueOf(promo.getDiscountPercent()))
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                } else if (promo.getDiscountValue() != null) {
                    discountCart = promo.getDiscountValue();
                }
            }

            BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.1)); // VAT 10% demo
            BigDecimal shipping = BigDecimal.valueOf(30000);             // 30k demo
            BigDecimal grand = subtotal.subtract(discountCart).add(tax).add(shipping);

            // Tạo Order
            Order order = new Order();
            order.setCustomer(currentAccount);
            order.setOrderDate(LocalDateTime.now());
            order.setStatus(OrderStatus.PAID); // dùng enum của anh
            order.setSubtotal(subtotal);
            order.setDiscountTotal(discountCart);
            order.setTaxTotal(tax);
            order.setShippingFee(shipping);
            order.setGrandTotal(grand);

            s.persist(order);

            // Tạo OrderItem
            for (CartItem ci : currentCart.getItems()) {
                OrderItem oi = new OrderItem();
                oi.setOrder(order);
                oi.setItem(ci.getItem());
                oi.setQuantity(ci.getQuantity());
                oi.setUnitPrice(ci.getUnitPrice());
                oi.setDiscountAmount(ci.getDiscountAmount());
                s.persist(oi);
            }

            // Payment dùng enum
            Payment p = new Payment();
            p.setOrder(order);
            p.setAmount(grand);
//            p.setMethod(PaymentMethod.CASH);     // enum của anh
            p.setStatus(PaymentStatus.PAID);     // enum của anh
            p.setPaidAt(LocalDateTime.now());
            s.persist(p);

            tx.commit();

            // clear cart
            currentCart = null;

            return order;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }
}
