package com.ucop.service;

import com.ucop.config.HibernateUtil;
import com.ucop.entity.*;
import com.ucop.entity.Shipment;
import com.ucop.entity.enums.OrderStatus;
import com.ucop.entity.enums.PaymentMethod;
import com.ucop.entity.enums.PaymentStatus;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Service xử lý Cart, Order, Payment, Promotion.
 */
public class OrderService {

    /* ========== SINGLETON ========== */

    private static final OrderService INSTANCE = new OrderService();

    public static OrderService getInstance() {
        return INSTANCE;
    }

    private OrderService() {
    }

    /* ========== TRẠNG THÁI GLOBAL ========== */

    // user đang đăng nhập
    private Account currentAccount;

    public Account getCurrentAccount() {
        return currentAccount;
    }

    public void setCurrentAccount(Account currentAccount) {
        this.currentAccount = currentAccount;
    }

    /* ========== CART ========== */

    public Cart getOrCreateCartForCurrentUser() {
        if (currentAccount == null) {
            throw new IllegalStateException("Chưa đăng nhập");
        }
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            Cart cart = s.createQuery(
                            "select distinct c from Cart c left join fetch c.items i " +
                                    "where c.customer.id = :cid and c.status = :status",
                            Cart.class)
                    .setParameter("cid", currentAccount.getId())
                    .setParameter("status", OrderStatus.CART)
                    .setMaxResults(1)
                    .uniqueResult();
            if (cart == null) {
                Transaction tx = s.beginTransaction();
                cart = new Cart();
                cart.setCustomer(currentAccount);
                cart.setStatus(OrderStatus.CART);
                s.persist(cart);
                tx.commit();
            }
            return cart;
        }
    }

    public Cart loadCartWithItems(Long cartId) {
        if (cartId == null) return null;
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            return s.createQuery(
                            "select distinct c from Cart c left join fetch c.items ci left join fetch ci.item " +
                                    "where c.id = :id",
                            Cart.class)
                    .setParameter("id", cartId)
                    .uniqueResult();
        }
    }

    public Cart updateCartItemQty(Long cartId, Long itemId, int qty) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = s.beginTransaction();
            Cart cart = s.createQuery(
                            "select c from Cart c left join fetch c.items ci where c.id = :cid",
                            Cart.class)
                    .setParameter("cid", cartId)
                    .uniqueResult();
            if (cart == null) throw new IllegalArgumentException("Cart không tồn tại");
            CartItem target = cart.getItems().stream()
                    .filter(ci -> ci.getItem().getId().equals(itemId))
                    .findFirst().orElse(null);
            if (target == null) throw new IllegalArgumentException("Item không có trong cart");
            target.setQuantity(qty);
            s.merge(target);
            tx.commit();
            return loadCartWithItems(cartId);
        }
    }

    public Cart removeCartItem(Long cartId, Long itemId) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = s.beginTransaction();
            Cart cart = s.createQuery(
                            "select c from Cart c left join fetch c.items ci where c.id = :cid",
                            Cart.class)
                    .setParameter("cid", cartId)
                    .uniqueResult();
            if (cart == null) throw new IllegalArgumentException("Cart không tồn tại");
            CartItem target = cart.getItems().stream()
                    .filter(ci -> ci.getItem().getId().equals(itemId))
                    .findFirst().orElse(null);
            if (target != null) {
                cart.getItems().remove(target);
                CartItem managed = s.get(CartItem.class, target.getId());
                if (managed != null) s.remove(managed);
            }
            tx.commit();
            return loadCartWithItems(cartId);
        }
    }

    public Cart addItemToCart(Cart cart, Item item, int qty) {
        if (qty <= 0) throw new IllegalArgumentException("Số lượng phải > 0");
        if (currentAccount == null) throw new IllegalStateException("Chưa đăng nhập");

        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = s.beginTransaction();

            Cart managed = (cart != null && cart.getId() != null)
                    ? s.get(Cart.class, cart.getId())
                    : null;
            if (managed == null) {
                managed = new Cart();
                managed.setCustomer(currentAccount);
                managed.setStatus(OrderStatus.CART);
                s.persist(managed);
            }
            Set<CartItem> items = managed.getItems();

            CartItem existing = null;
            for (CartItem ci : items) {
                if (ci.getItem().getId().equals(item.getId())) {
                    existing = ci;
                    break;
                }
            }
            if (existing == null) {
                CartItem ci = new CartItem();
                ci.setCart(managed);
                ci.setItem(item);
                ci.setQuantity(qty);
                ci.setUnitPrice(item.getPrice());
                ci.setDiscountAmount(BigDecimal.ZERO);
                s.persist(ci);
                items.add(ci);
            } else {
                existing.setQuantity(existing.getQuantity() + qty);
                s.merge(existing);
            }

            tx.commit();
            return managed;
        }
    }

    /* ========== PROMOTION ========== */

    public Promotion findValidPromotion(String code) {
        if (code == null || code.isBlank()) return null;
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            Promotion promo = s.createQuery(
                            "from Promotion p where p.code = :c",
                            Promotion.class)
                    .setParameter("c", code)
                    .setMaxResults(1)
                    .uniqueResult();
            return validatePromotion(promo, null) ? promo : null;
        }
    }

    private boolean validatePromotion(Promotion promo, BigDecimal subtotal) {
        if (promo == null) return false;
        LocalDateTime now = LocalDateTime.now();
        if (promo.getStartDate() != null && now.isBefore(promo.getStartDate())) return false;
        if (promo.getEndDate() != null && now.isAfter(promo.getEndDate())) return false;
        if (promo.getMaxUsage() != null && promo.getUsedCount() != null
                && promo.getUsedCount() >= promo.getMaxUsage()) return false;
        if (promo.getMinOrderValue() != null && subtotal != null
                && subtotal.compareTo(promo.getMinOrderValue()) < 0) return false;
        return true;
    }

    /* ========== ORDER ========== */

    public List<Order> findAllOrders() {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            return s.createQuery(
                            "select distinct o from Order o " +
                                    "left join fetch o.customer " +
                                    "left join fetch o.items " +
                                    "left join fetch o.payments " +
                                    "left join fetch o.shipments",
                            Order.class)
                    .list();
        }
    }

    public void updateOrderStatus(Order order, OrderStatus newStatus) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = s.beginTransaction();
            Order managed = s.get(Order.class, order.getId());
            managed.setStatus(newStatus);
            tx.commit();
        }
    }

    public Shipment createShipment(Order order, String trackingNumber, String carrier) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = s.beginTransaction();
            Order managed = s.get(Order.class, order.getId());
            if (managed == null) throw new IllegalArgumentException("Order không tồn tại");

            Shipment shipment = new Shipment();
            shipment.setOrder(managed);
            shipment.setTrackingNumber(trackingNumber);
            shipment.setCarrier(carrier);
            shipment.setStatus("CREATED");
            shipment.setShippedAt(LocalDateTime.now());
            s.persist(shipment);

            managed.setStatus(OrderStatus.SHIPPED);
            tx.commit();
            return shipment;
        }
    }

    /**
     * Checkout: dùng currentAccount + cart hiện có trong DB
     */
    public Order checkoutCurrentCart(Promotion promo, PaymentMethod method) {
        if (currentAccount == null) {
            throw new IllegalStateException("Chưa set currentAccount cho OrderService");
        }
        if (method == null) {
            throw new IllegalArgumentException("Chưa chọn phương thức thanh toán");
        }

        Cart cart = getOrCreateCartForCurrentUser();

        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = s.beginTransaction();

            Cart managedCart = s.createQuery(
                            "select distinct c from Cart c left join fetch c.items i left join fetch i.item " +
                                    "where c.id = :id",
                            Cart.class)
                    .setParameter("id", cart.getId())
                    .uniqueResult();
            if (managedCart == null || managedCart.getItems().isEmpty()) {
                throw new IllegalStateException("Giỏ hàng trống");
            }

            // Tính subtotal
            BigDecimal subtotal = BigDecimal.ZERO;
            for (CartItem ci : managedCart.getItems()) {
                BigDecimal line = ci.getUnitPrice()
                        .multiply(BigDecimal.valueOf(ci.getQuantity()))
                        .subtract(ci.getDiscountAmount());
                subtotal = subtotal.add(line);
            }

            // Validate promotion với subtotal
            Promotion appliedPromo = null;
            if (promo != null && validatePromotion(promo, subtotal)) {
                appliedPromo = promo;
            }

            BigDecimal discountCart = BigDecimal.ZERO;
            if (appliedPromo != null) {
                if (appliedPromo.getDiscountPercent() != null) {
                    discountCart = subtotal
                            .multiply(BigDecimal.valueOf(appliedPromo.getDiscountPercent()))
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                } else if (appliedPromo.getDiscountValue() != null) {
                    discountCart = appliedPromo.getDiscountValue();
                }
            }

            BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.1)); // VAT 10% demo
            BigDecimal shipping = subtotal.compareTo(BigDecimal.ZERO) > 0 ? BigDecimal.valueOf(30000) : BigDecimal.ZERO;
            BigDecimal fee = BigDecimal.ZERO;
            if (method == PaymentMethod.COD) {
                fee = BigDecimal.valueOf(15000);
            } else if (method == PaymentMethod.GATEWAY) {
                fee = subtotal.multiply(BigDecimal.valueOf(0.02)); // 2% phí cổng
            }

            BigDecimal grand = subtotal.subtract(discountCart).add(tax).add(shipping).add(fee);

            // Tạo Order
            Order order = new Order();
            order.setCustomer(currentAccount);
            order.setOrderDate(LocalDateTime.now());
            order.setStatus((method == PaymentMethod.COD || method == PaymentMethod.BANK_TRANSFER)
                    ? OrderStatus.PENDING_PAYMENT
                    : OrderStatus.PAID);
            order.setSubtotal(subtotal);
            order.setDiscountTotal(discountCart);
            order.setTaxTotal(tax);
            order.setShippingFee(shipping);
            order.setGrandTotal(grand);
            s.persist(order);

            // Tạo OrderItem
            for (CartItem ci : managedCart.getItems()) {
                OrderItem oi = new OrderItem();
                oi.setOrder(order);
                oi.setItem(ci.getItem());
                oi.setQuantity(ci.getQuantity());
                oi.setUnitPrice(ci.getUnitPrice());
                oi.setDiscountAmount(ci.getDiscountAmount());
                s.persist(oi);

                // Giảm tồn kho
                StockItem stock = s.createQuery(
                                "from StockItem si where si.item.id = :iid",
                                StockItem.class)
                        .setParameter("iid", ci.getItem().getId())
                        .setMaxResults(1)
                        .uniqueResult();
                if (stock != null) {
                    stock.setOnHand(Math.max(0, stock.getOnHand() - ci.getQuantity()));
                    s.merge(stock);
                }
            }

            // Payment
            Payment p = new Payment();
            p.setOrder(order);
            p.setAmount(grand);
            p.setMethod(method);
            p.setStatus((method == PaymentMethod.COD || method == PaymentMethod.BANK_TRANSFER)
                    ? PaymentStatus.PENDING
                    : PaymentStatus.PAID);
            p.setPaidAt(p.getStatus() == PaymentStatus.PAID ? LocalDateTime.now() : null);
            s.persist(p);

            // Cập nhật promotion usage
            if (appliedPromo != null) {
                Integer used = appliedPromo.getUsedCount() == null ? 0 : appliedPromo.getUsedCount();
                appliedPromo.setUsedCount(used + 1);
                s.merge(appliedPromo);
            }

            // Đóng cart
            managedCart.setStatus(OrderStatus.CLOSED);
            s.merge(managedCart);

            tx.commit();
            return order;
        }
    }

    /* ========== PAYMENT / REFUND ========== */

    public void refundPayment(Payment payment, BigDecimal amount) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = s.beginTransaction();
            Payment managed = s.get(Payment.class, payment.getId());
            if (managed == null) throw new IllegalArgumentException("Payment không tồn tại");

            int cmp = amount.compareTo(managed.getAmount());
            if (cmp >= 0) {
                managed.setStatus(PaymentStatus.REFUNDED);
            } else {
                managed.setStatus(PaymentStatus.PARTIALLY_REFUNDED);
            }
            managed.setPaidAt(LocalDateTime.now());
            tx.commit();
        }
    }
}
