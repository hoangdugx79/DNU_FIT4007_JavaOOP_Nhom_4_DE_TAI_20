HỆ THỐNG QUẢN LÝ KHO XUẤT NHẬP HÀNG
Giới thiệu
Hệ thống quản lý kho xuất nhập hàng là ứng dụng Java Console Application giúp quản lý hoạt động nhập xuất hàng hóa trong kho, bao gồm quản lý sản phẩm, nhà cung cấp, khách hàng, phiếu nhập, phiếu xuất, kiểm kê kho và báo cáo thống kê.

Trạng thái dự án: Hoàn thành 95%

Công nghệ: Java OOP, CSV File Storage, iText PDF Library

Mục lục
Giới thiệu
Yêu cầu hệ thống
Cấu trúc dự án
Cài đặt
Chức năng chính
Dataset mẫu
Test Cases cốt lõi
Thiết kế OOP
Lưu ý quan trọng
Phân công (3 thành viên)
Tác giả
Yêu cầu hệ thống
Phần mềm bắt buộc
Java Development Kit (JDK): Phiên bản 11 trở lên
IDE: IntelliJ IDEA
Thư viện bên ngoài
iText PDF: Version 5.5.13 (để xuất báo cáo PDF)
Download: iText 5.5.13
File cần tải: itextpdf-5.5.13.jar
Cấu trúc dự án
WarehouseManagement/ │ ├── src/ │ ├── domain/ # 13 files - Domain Models │ │ ├── Product.java (Abstract class) │ │ ├── Electronics.java │ │ ├── Clothing.java │ │ ├── Food.java │ │ ├── Furniture.java │ │ ├── Order.java (Abstract class) │ │ ├── ImportOrder.java │ │ ├── ExportOrder.java │ │ ├── OrderItem.java │ │ ├── Customer.java │ │ ├── CustomerType.java (Enum) │ │ ├── Supplier.java │ │ └── OrderStatus.java (Enum) │ │ │ ├── exception/ # 4 files - Custom Exceptions │ │ ├── OutOfStockException.java │ │ ├── ProductNotFoundException.java │ │ ├── OrderNotFoundException.java │ │ └── InvalidQuantityException.java │ │ │ ├── interfaces/ # 3 files - Interfaces │ │ ├── Persistable.java │ │ ├── Reportable.java │ │ └── Searchable.java │ │ │ ├── repository/ # 4 files - Data Access Layer │ │ ├── ProductRepository.java │ │ ├── CustomerRepository.java │ │ ├── SupplierRepository.java │ │ └── OrderRepository.java │ │ │ ├── service/ # 2 files - Business Logic │ │ ├── WarehouseService.java │ │ └── ReportService.java │ │ │ ├── ui/ # 1 file - Console Interface │ │ └── ConsoleMenu.java │ │ │ └── Main.java # Entry point │ ├── data/ # 6 CSV files - Data Storage │ ├── products.csv │ ├── customers.csv │ ├── suppliers.csv │ ├── import_orders.csv │ ├── export_orders.csv │ └── order_items.csv │ ├── reports/ # Folder for generated reports │ └── (các file báo cáo sẽ được tạo ở đây) │ |── lib/ │ ├── itextpdf-5.5.13.jar └── README.md # Hướng dẫn sử dụng

Cài đặt
Bước 1: Cấu hình thư viện iText
IntelliJ IDEA:

Nhấn chuột phải project → Open Module Settings (F4)
Libraries → + → Java → Chọn lib/itextpdf-5.5.13.jar
OK và Apply
Eclipse:

Nhấn chuột phải project → Build Path → Configure Build Path
Libraries → Add External JARs → Chọn lib/itextpdf-5.5.13.jar
Apply and Close
Bước 2: Chạy ứng dụng
Mở file Main.java
Nhấn chuột phải → Run 'Main.Main()'
Chức năng chính
1. Quản lý Sản phẩm
Thêm/Sửa/Xóa: CRUD sản phẩm
Tìm kiếm: Theo ID hoặc tên
4 loại: ELECTRONICS, CLOTHING, FOOD, FURNITURE
2. Quản lý Nhà cung cấp & Khách hàng
Thông tin: ID, tên, địa chỉ, điện thoại, email
CRUD đầy đủ
3. Phiếu nhập kho
Tạo phiếu nhập từ nhà cung cấp
Xác nhận phiếu → Tự động tăng tồn kho
Trạng thái: PENDING / COMPLETED / CANCELLED
4. Phiếu xuất kho
Tạo phiếu xuất cho khách hàng
Kiểm tra tồn kho trước khi xuất
Xác nhận phiếu → Tự động trừ tồn kho
5. Báo cáo & Thống kê
Báo cáo tồn kho: Chi tiết từng sản phẩm
Báo cáo nhập-xuất-tồn: Theo thời gian
Báo cáo doanh thu: Tính lợi nhuận, tỷ suất
Xu hướng theo mùa: Phân tích 4 mùa (Xuân/Hạ/Thu/Đông)
Top sản phẩm bán chạy
6. Xuất báo cáo
CSV: Format bảng đẹp, UTF-8 BOM (mở Excel không lỗi)
PDF: Bảng đầy đủ, tiếng Việt không dấu
Dataset mẫu
Sản phẩm (26 items)
6 Electronics: Laptop, iPhone, Samsung, MacBook, Sony, AirPods
5 Clothing: Áo sơ mi, Quần jean, Áo khoác, Váy, Áo thun
5 Food: Mì Hảo Hảo, Coca, Oreo, Sữa Vinamilk, Mì Kokomi
5 Furniture: Bàn làm việc, Ghế văn phòng, Tủ sách, Giường, Kệ tivi
5 Others: Máy ảnh, Giày Nike, Trà sữa, Bàn ăn, Màn hình LG
Đơn hàng mẫu
10 phiếu nhập kho (~200 triệu)
16 phiếu xuất kho (~524 triệu doanh thu, 135 triệu lợi nhuận)
Test Cases cốt lõi
Test 1: Thêm sản phẩm
Input: ID=P027, Tên="Laptop HP", Loại=ELECTRONICS, Giá nhập=13tr, Giá bán=16tr, Tồn=20
Output: ✅ Thêm thành công, xuất hiện trong danh sách

Test 2: Tạo phiếu nhập
Input: NCC=SUP001, Sản phẩm: P001 (10 cái), P002 (5 cái)
Output: ✅ Phiếu IMP-xxx tạo thành công, trạng thái PENDING

Test 3: Xác nhận phiếu nhập
Input: Xác nhận phiếu IMP-xxx
Output: ✅ Trạng thái → COMPLETED, Tồn P001: 25→35, P002: 30→35

Test 4: Xuất kho vượt tồn
Input: Xuất P001 số lượng 100 (tồn chỉ 35)
Output: ❌ Lỗi "Sản phẩm P001 chỉ còn 35 trong kho!"

Test 5: Báo cáo doanh thu
Input: Từ 01/11/2025 đến 30/11/2025
Output:

Tổng doanh thu: 524,140,000 VND
Tổng chi phí: 389,390,000 VND
Lợi nhuận: 134,750,000 VND
Tỷ suất lợi nhuận: 25.71%
Test 6: Xuất PDF tồn kho
Input: Chọn "Báo cáo tồn kho - PDF"
Output: ✅ File reports/ton_kho_2025-11-23.pdf với bảng 26 sản phẩm

Thiết kế OOP
Kế thừa
Product (abstract) ├── Electronics ├── Clothing ├── Food └── Furniture

Order (abstract) ├── ImportOrder └── ExportOrder

Interface
Persistable: load(), save()
Reportable: generateReport()
Exception
OutOfStockException: Xuất vượt tồn
ProductNotFoundException: Sản phẩm không tồn tại
OrderNotFoundException: Đơn hàng không tồn tại
Lưu ý quan trọng
CSV
Encoding: UTF-8 with BOM
Separator: Dấu phẩy (,)
Format ngày: dd/MM/yyyy
PDF
Font: HELVETICA (không hỗ trợ Unicode)
Giải pháp: Tự động chuyển tiếng Việt có dấu → không dấu
Ví dụ: "Áo sơ mi" → "Ao so mi"
Backup
⚠️ Quan trọng: Backup thư mục data/ trước khi test để tránh mất dữ liệu mẫu

Phân công (3 thành viên)
Thành viên A: Domain & Core Logic (Product, Order, kế thừa)
Thành viên B: Repository & Data (CSV import/export, validate)
Thành viên C: Service & UI (Business logic, báo cáo, PDF/CSV, menu)

Tác giả
Nhóm: [Nhóm 4]
Thành viên:
[ NGUYỄN ĐỨC DŨNG ] - [1871040008]
[ ĐINH ĐỨC THỌ ] - [MSSV]
[ NGUYỄN ĐỨC HUY ] - [MSSV]
Môn học: Lập trình hướng đối tượng (OOP)
Giảng viên: [Tên GV]
Phiên bản: 1.0.0
Ngày: 10/11/2025
