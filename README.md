# UCOP - Hướng dẫn module, file, chạy demo

## Tổng quan
Ứng dụng JavaFX + Hibernate (MySQL) gồm 5 module chính theo vai trò:
- SV1 - Admin: User, Role, Audit
- SV2 - Catalog: Category, Item, Stock
- SV3 - Order: Cart, Order, OrderItem
- SV4 - Payment: Payment, Refund
- SV5 - Report: Promotion, Report, Dashboard

Thư mục chính:
- `src/main/java/com/ucop/...` (controller, service, entity)
- `src/main/resources/fxml/...` (UI FXML)
- `src/main/resources/hibernate.cfg.xml` (config DB)

## SV1 - Admin (User, Role, Audit)
- UI/Controller: `fxml/user-management.fxml`, `controller/UserManagementController.java` (CRUD user, đổi mật khẩu, khóa/mở)
- Service: `service/AccountService.java` (CRUD user/role/profile, đổi mật khẩu, khóa/mở, kiểm tra trùng)
- Entity: `Account.java`, `AccountProfile.java`, `Role.java`, `enums/RoleName.java`
- Audit: `AuditableEntity.java`, `AuditListener.java`; `security/SecurityContext.java` (giữ user đăng nhập)
- Layout chính: `fxml/main-layout.fxml`, `controller/MainLayoutController.java` (ẩn/hiện menu theo role, logout)

## SV2 - Catalog (Category, Item, Stock)
- UI/Controller:
  - Category: `fxml/category-list.fxml`, `controller/CategoryController.java` (CRUD, import/export CSV)
  - Item: `fxml/item-list.fxml`, `controller/ItemController.java` (form ảnh/link mô tả, scroll, lọc danh mục, import/export CSV)
  - Stock: `fxml/stock-list.fxml`, `controller/StockController.java` (tồn kho per warehouse, cảnh báo low stock)
- Service: `service/CatalogService.java` (CRUD category/item/warehouse/stock, import/export CSV, lọc item theo category)
- Entity: `Category.java`, `Item.java` (có `imageUrl`), `Warehouse.java`, `StockItem.java`
- Shop dùng chung dữ liệu: `fxml/product-grid.fxml`, `controller/ProductGridController.java` (hiển thị sản phẩm cho customer)

## SV3 - Order (Cart, Order, OrderItem)
- UI/Controller:
  - Shop/Cart: `product-grid.fxml`, `cart.fxml`, `ProductGridController`, `CartController` (thêm giỏ, áp mã, thanh toán)
  - Orders: `order-list.fxml`, `OrderManagementController` (xem/đổi trạng thái, shipment)
- Service: `service/OrderService.java` (cart theo user, checkout, shipment, validate promo/minOrder, tính phí/tax)
- Entity: `Cart.java`, `CartItem.java`, `Order.java`, `OrderItem.java`, `Shipment.java`, `enums/OrderStatus.java`

## SV4 - Payment (Payment, Refund)
- UI: chung với Orders (`order-list.fxml`) – mark paid/refund
- Service: `service/PaymentService.java`; refund/checkout trong `OrderService.checkoutCurrentCart` / `refundPayment`
- Entity: `Payment.java`, `enums/PaymentMethod.java`, `PaymentStatus.java`
- (Chưa tích hợp gateway thật; có thể thêm dialog QR giả lập nếu cần)

## SV5 - Report (Promotion, Report, Dashboard)
- UI/Controller: `promotion-list.fxml` (promo CRUD), `dashboard.fxml` (thống kê text)
- Service: `service/ReportService.java` (doanh thu, top item, theo phương thức thanh toán, trạng thái đơn); promo logic trong `OrderService.java`
- Entity: `Promotion.java`

## Chạy ứng dụng
1. Cấu hình MySQL trong `src/main/resources/hibernate.cfg.xml`.
2. Import `ucop.sql` (đã có cột `image_url`, seed MacBook, kho HN01/HCM01, đơn mẫu, promo).
3. Tài khoản mẫu:
   - admin / admin123
   - staff / staff123
   - customer / customer123
4. Build/Run:
   ```
   cd ucop
   mvn clean package
   mvn javafx:run
   ```

## Dữ liệu mẫu
- Category Laptop (id=1001), 20 sản phẩm MacBook (có thể bổ sung `image_url` để hiển thị ảnh).
- Kho: HN01, HCM01; tồn kho mẫu gắn theo SKU.
- Mã khuyến mãi: `PROMO10` (giảm 10%).
- Đơn mẫu (PAID) cho customer.

## Giao diện / Lưu ý
- Customer: login vào Shop/Cart (grid sản phẩm), menu quản trị ẩn.
- Admin Item: form ảnh, mô tả, giá, danh mục, có scroll.
- Promotions: FXML đã sửa lỗi “No resources specified”.
- VNPAY/QR giả lập: chưa tích hợp; có thể thêm dialog sinh QR (QR server online) và đánh dấu Payment PAID thủ công nếu cần.

## Lưu ý RBAC
- Admin: full menu.
- Staff: Orders/Stock/Cart (ẩn User/Promotion).
- Customer: Shop/Cart (ẩn Dashboard/Admin menu).
