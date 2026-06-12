# Inventory & Order Management API

Java Spring Boot starter cho quản lý sản phẩm, tồn kho và đơn hàng. Project có JWT auth, transaction khi giữ/trừ/hoàn kho và Swagger UI.

Mặc định project chạy bằng H2 embedded in-memory, không cần Docker/Postgres. Data chỉ lưu tạm trong lúc API chạy và sẽ reset khi restart app.

## Chạy local không cần Docker

Yêu cầu Java 21 và Maven:

```bash
mvn spring-boot:run
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

Frontend web:

```text
http://localhost:8080/
```

Sau khi đăng nhập thành công, frontend chuyển tới:

```text
http://localhost:8080/dashboard.html
```

Các trang chức năng:

```text
http://localhost:8080/products.html
http://localhost:8080/inventory.html
http://localhost:8080/orders.html
```

H2 Console:

```text
http://localhost:8080/h2-console
```

Thông tin kết nối H2:

```text
JDBC URL: jdbc:h2:mem:inventorydb
User: sa
Password: <để trống>
```

Tài khoản seed sẵn:

```text
admin / admin123
user  / user123
```

## Chạy Docker tuỳ chọn

```bash
docker compose up --build
```

## Luồng API chính

1. `POST /api/auth/login` lấy JWT.
2. `POST /api/auth/register` đăng ký tài khoản người dùng thường.
3. Dùng `Authorization: Bearer <token>`.
4. `GET /api/products?q=mouse&page=0&size=10` tìm kiếm và phân trang sản phẩm.
5. `GET /api/inventory` xem tồn kho.
6. `POST /api/orders` tạo đơn, hệ thống giữ kho bằng transaction.
7. `POST /api/orders/{id}/confirm` xác nhận đơn, trừ phần kho đã giữ.
8. `POST /api/orders/{id}/cancel` hủy đơn pending, hoàn kho đã giữ.
9. `POST /api/orders/{id}/return` trả đơn confirmed, cộng lại kho.

## Tính năng nổi bật

- JWT authentication và phân quyền `ADMIN` / `USER`.
- Đăng ký tài khoản user.
- CRUD sản phẩm cho admin, kiểm tra trùng SKU.
- Tìm kiếm và phân trang danh sách sản phẩm.
- Theo dõi tồn kho, nhập thêm tồn kho cho admin.
- Luồng đơn hàng có transaction giữ/trừ/hoàn kho.
- Frontend nhiều trang: đăng nhập, trang chủ, sản phẩm, tồn kho, đơn hàng.
- UI có modal thêm/sửa sản phẩm, filter, pagination, loading và thông báo lỗi.

Ví dụ login:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

Ví dụ tạo đơn:

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"customerName":"Acme","items":[{"productId":1,"quantity":2}]}'
```
