# UCOP – Hướng dẫn module & file liên quan

## Tổng quan
Ứng dụng JavaFX + Hibernate (MySQL) gồm 5 module chính tương ứng vai trò SV trong đề:
- **SV1 – Admin**: User, Role, Audit
- **SV2 – Catalog**: Category, Item, Stock
- **SV3 – Order**: Cart, Order, OrderItem
- **SV4 – Payment**: Payment, Refund
- **SV5 – Report**: Promotion, Report, Dashboard

Tree thư mục code chính:  
`src/main/java/com/ucop/...` (controller, service, entity)  
`src/main/resources/fxml/...` (UI FXML)  
`src/main/resources/hibernate.cfg.xml` (config DB)

## SV1 – Admin (User, Role, Audit)
- **UI/Controller**:  
  - `fxml/user-management.fxml`  
  - `controller/UserManagementController.java`
- **Service**:  
  - `service/AccountService.java` (CRUD user, đổi mật khẩu, khóa/mở khóa, gán role)
- **Entity**:  
  - `entity/Account.java`, `AccountProfile.java`, `Role.java`, `enums/RoleName.java`
- **Audit**:  
  - `entity/AuditableEntity.java`, `entity/AuditListener.java` (set created/updated)  
  - `security/SecurityContext.java` (giữ user đăng nhập)
- **Layout chính**:  
  - `fxml/main-layout.fxml`, `controller/MainLayoutController.java` (ẩn/hiện menu theo role, logout)

## SV2 – Catalog (Category, Item, Stock)
- **UI/Controller**:  
  - Category: `fxml/category-list.fxml`, `controller/CategoryController.java`  
  - Item: `fxml/item-list.fxml`, `controller/ItemController.java`  
  - Stock/Warehouse: `fxml/stock-list.fxml`, `controller/StockController.java`
- **Service**:  
  - `service/CatalogService.java` (CRUD category, item, warehouse/stock, import/export CSV)
- **Entity**:  
  - `entity/Category.java`, `Item.java`, `Warehouse.java`, `StockItem.java`

## SV3 – Order (Cart, Order, OrderItem)
- **UI/Controller**:  
  - Cart: `fxml/cart.fxml`, `controller/CartController.java`  
  - Orders: `fxml/order-list.fxml`, `controller/OrderManagementController.java`
- **Service**:  
  - `service/OrderService.java` (quản lý cart theo user, checkout, trạng thái đơn, shipment, giảm tồn)
- **Entity**:  
  - `entity/Cart.java`, `CartItem.java`, `Order.java`, `OrderItem.java`, `Shipment.java`  
  - Trạng thái: `enums/OrderStatus.java`

## SV4 – Payment (Payment, Refund)
- **UI/Controller**:  
  - Dùng chung trên `order-list.fxml` / `OrderManagementController.java` (mark paid, refund)
- **Service**:  
  - `service/PaymentService.java` (mark paid), refund trong `OrderService.checkoutCurrentCart` + `refundPayment`
- **Entity**:  
  - `entity/Payment.java`, enums `PaymentMethod.java`, `PaymentStatus.java`

## SV5 – Report (Promotion, Report, Dashboard)
- **UI/Controller**:  
  - Promotion: `fxml/promotion-list.fxml`, `controller/PromotionController.java`  
  - Dashboard: `fxml/dashboard.fxml`, `controller/DashboardController.java`
- **Service**:  
  - `service/ReportService.java` (doanh thu theo ngày, top item, theo phương thức thanh toán, trạng thái đơn)
  - Promo logic/usage trong `OrderService.java`
- **Entity**:  
  - `entity/Promotion.java`

## Chạy ứng dụng
1. Cấu hình DB MySQL trong `src/main/resources/hibernate.cfg.xml`.
2. Import script SQL (DB `ucop`, user mẫu: admin/admin123, staff1/staff123, cust1/customer123).
3. Build/Run:  
   ```
   cd ucop
   mvn clean package
   mvn javafx:run
   ```

## Lưu ý
- `SecurityContext` giữ user hiện tại để audit và ẩn/hiện menu.  
- RBAC hiện ẩn menu theo role; cần bổ sung kiểm soát sâu hơn nếu yêu cầu.  
- Một số text tiếng Việt bị lỗi dấu trong FXML (font/encoding), có thể chỉnh lại nếu cần.
