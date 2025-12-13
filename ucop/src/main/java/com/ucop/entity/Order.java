package com.ucop.entity;

import com.ucop.entity.enums.OrderStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "orders")
public class Order extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Account customer;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PLACED;

    private LocalDateTime orderDate;

    @Column(precision = 18, scale = 2)
    private BigDecimal subtotal;

    @Column(precision = 18, scale = 2)
    private BigDecimal discountTotal;

    @Column(precision = 18, scale = 2)
    private BigDecimal taxTotal;

    @Column(precision = 18, scale = 2)
    private BigDecimal shippingFee;

    @Column(precision = 18, scale = 2)
    private BigDecimal grandTotal;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderItem> items = new HashSet<>();

    @OneToMany(mappedBy = "order")
    private Set<Payment> payments = new HashSet<>();

    @OneToMany(mappedBy = "order")
    private Set<Shipment> shipments = new HashSet<>();

    public Long getId() {
        return id;
    }

    public Account getCustomer() {
        return customer;
    }

    public void setCustomer(Account customer) {
        this.customer = customer;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDiscountTotal() {
        return discountTotal;
    }

    public void setDiscountTotal(BigDecimal discountTotal) {
        this.discountTotal = discountTotal;
    }

    public BigDecimal getTaxTotal() {
        return taxTotal;
    }

    public void setTaxTotal(BigDecimal taxTotal) {
        this.taxTotal = taxTotal;
    }

    public BigDecimal getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(BigDecimal shippingFee) {
        this.shippingFee = shippingFee;
    }

    public BigDecimal getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(BigDecimal grandTotal) {
        this.grandTotal = grandTotal;
    }

    public Set<OrderItem> getItems() {
        return items;
    }

    public Set<Payment> getPayments() {
        return payments;
    }

    public Set<Shipment> getShipments() {
        return shipments;
    }
}
