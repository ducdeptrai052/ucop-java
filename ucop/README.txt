UCOP - Universal Commerce & Operations Platform (JavaFX + Hibernate + MySQL)

Cấu trúc theo yêu cầu:
- JavaFX UI (FXML) với login, main layout, user/category/item/stock/cart/order/promotion/dashboard.
- Hibernate ORM + MySQL, 3-layer: DAO -> Service -> Controller.

Các bước chạy:
1. Cài MySQL, tạo user/password tương ứng.
2. Mở file src/main/resources/hibernate.cfg.xml và sửa:
   - hibernate.connection.url
   - hibernate.connection.username
   - hibernate.connection.password

3. Chạy script database/ucop_sample.sql để tạo DB `ucop`, role + tài khoản admin (admin/admin123).
4. Mở project bằng IntelliJ IDEA (Open pom.xml).
5. Run class com.ucop.MainApplication.
   VM options (ví dụ Windows, JavaFX SDK ở C:\javafx-sdk-21\lib):
   --module-path "C:\javafx-sdk-21\lib" --add-modules javafx.controls,javafx.fxml

Tài khoản mẫu:
- admin / admin123
