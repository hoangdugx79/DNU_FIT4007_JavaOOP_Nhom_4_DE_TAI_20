package ui;

import domain.*;
import exception.*;
import repository.*;
import service.*;

import com.itextpdf.text.DocumentException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Console Menu cho ứng dụng quản lý kho
 */
public class ConsoleMenu {
    private Scanner scanner;
    private WarehouseService warehouseService;
    private ReportService reportService;
    private ProductRepository productRepository;
    private CustomerRepository customerRepository;
    private SupplierRepository supplierRepository;
    private OrderRepository orderRepository;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ConsoleMenu() {
        this.scanner = new Scanner(System.in);
        initializeRepositories();
        initializeServices();
        loadData();
    }

    private void initializeRepositories() {
        productRepository = new ProductRepository("data/products.csv");
        customerRepository = new CustomerRepository("data/customers.csv");
        supplierRepository = new SupplierRepository("data/suppliers.csv");
        orderRepository = new OrderRepository(
                "data/import_orders.csv",
                "data/export_orders.csv",
                "data/order_items.csv"
        );

        orderRepository.setCustomerRepository(customerRepository);
        orderRepository.setSupplierRepository(supplierRepository);
        orderRepository.setProductRepository(productRepository);
    }

    private void initializeServices() {
        warehouseService = new WarehouseService(
                productRepository,
                orderRepository,
                customerRepository,
                supplierRepository
        );
        reportService = new ReportService(productRepository, orderRepository);
    }

    private void loadData() {
        try {
            System.out.println("Đang tải dữ liệu...");
            productRepository.load();
            customerRepository.load();
            supplierRepository.load();
            orderRepository.load();
            System.out.println("✅ Đã tải dữ liệu thành công!");
        } catch (IOException e) {
            System.out.println("⚠️  Lỗi khi tải dữ liệu: " + e.getMessage());
        }
    }

    private void saveData() {
        try {
            productRepository.save();
            customerRepository.save();
            supplierRepository.save();
            orderRepository.save();
            System.out.println("✅ Đã lưu dữ liệu thành công!");
        } catch (IOException e) {
            System.out.println("❌ Lỗi khi lưu dữ liệu: " + e.getMessage());
        }
    }

    public void start() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║        HỆ THỐNG QUẢN LÝ KHO XUẤT NHẬP HÀNG                     ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

        boolean running = true;
        while (running) {
            displayMainMenu();
            int choice = getIntInput("Chọn chức năng: ");

            switch (choice) {
                case 1: productManagementMenu(); break;
                case 2: customerManagementMenu(); break;
                case 3: supplierManagementMenu(); break;
                case 4: importMenu(); break;
                case 5: exportMenu(); break;
                case 6: inventoryMenu(); break;
                case 7: reportMenu(); break;
                case 8:
                    saveData();
                    System.out.println("\n👋 Cảm ơn bạn đã sử dụng hệ thống!");
                    running = false;
                    break;
                default:
                    System.out.println("❌ Lựa chọn không hợp lệ!");
            }
        }
        scanner.close();
    }

    private void displayMainMenu() {
        System.out.println("\n╔════════════════ MENU CHÍNH ════════════════════╗");
        System.out.println("║ 1. Quản lý sản phẩm                            ║");
        System.out.println("║ 2. Quản lý khách hàng                          ║");
        System.out.println("║ 3. Quản lý nhà cung cấp                        ║");
        System.out.println("║ 4. Nhập kho                                    ║");
        System.out.println("║ 5. Xuất kho                                    ║");
        System.out.println("║ 6. Kiểm kê kho                                 ║");
        System.out.println("║ 7. Báo cáo & Thống kê                          ║");
        System.out.println("║ 8. Lưu và Thoát                                ║");
        System.out.println("╚════════════════════════════════════════════════╝");
    }

    // ========== QUẢN LÝ SẢN PHẨM ==========
    private void productManagementMenu() {
        System.out.println("\n╔════════ QUẢN LÝ SẢN PHẨM ════════╗");
        System.out.println("║ 1. Thêm sản phẩm                 ║");
        System.out.println("║ 2. Xem danh sách sản phẩm        ║");
        System.out.println("║ 3. Tìm kiếm sản phẩm             ║");
        System.out.println("║ 4. Cập nhật sản phẩm             ║");
        System.out.println("║ 5. Xóa sản phẩm                  ║");
        System.out.println("║ 0. Quay lại                      ║");
        System.out.println("╚══════════════════════════════════╝");

        int choice = getIntInput("Chọn: ");
        switch (choice) {
            case 1: addProduct(); break;
            case 2: viewAllProducts(); break;
            case 3: searchProduct(); break;
            case 4: updateProduct(); break;
            case 5: deleteProduct(); break;
            case 0: break;
            default: System.out.println("❌ Lựa chọn không hợp lệ!");
        }
    }

    private void addProduct() {
        System.out.println("\n=== THÊM SẢN PHẨM MỚI ===");
        System.out.println("Chọn loại sản phẩm:");
        System.out.println("1. Electronics");
        System.out.println("2. Clothing");
        System.out.println("3. Food");
        System.out.println("4. Furniture");

        int type = getIntInput("Loại: ");

        System.out.print("Tên sản phẩm: ");
        String name = scanner.nextLine();

        System.out.print("Danh mục: ");
        String category = scanner.nextLine();

        double importPrice = getDoubleInput("Giá nhập: ");
        double salePrice = getDoubleInput("Giá bán: ");
        int stock = getIntInput("Số lượng: ");

        Product product = null;

        try {
            switch (type) {
                case 1:
                    int warranty = getIntInput("Bảo hành (tháng): ");
                    product = new Electronics(name, category, importPrice, salePrice, stock, warranty);
                    break;
                case 2:
                    System.out.print("Size: ");
                    String size = scanner.nextLine();
                    System.out.print("Chất liệu: ");
                    String material = scanner.nextLine();
                    product = new Clothing(name, category, importPrice, salePrice, stock, size, material);
                    break;
                case 3:
                    System.out.print("Hạn sử dụng (yyyy-MM-dd): ");
                    LocalDate expiry = LocalDate.parse(scanner.nextLine(), DATE_FORMAT);
                    product = new Food(name, category, importPrice, salePrice, stock, expiry);
                    break;
                case 4:
                    System.out.print("Kích thước (cm): ");
                    String dimensions = scanner.nextLine();
                    double weight = getDoubleInput("Trọng lượng (kg): ");
                    product = new Furniture(name, category, importPrice, salePrice, stock, dimensions, weight);
                    break;
                default:
                    System.out.println("❌ Loại không hợp lệ!");
                    return;
            }

            productRepository.add(product);
            saveData();
            System.out.println("✅ Đã thêm sản phẩm: " + product.getId());
        } catch (Exception e) {
            System.out.println("❌ Lỗi: " + e.getMessage());
        }
    }

    private void viewAllProducts() {
        System.out.println("\n=== DANH SÁCH SẢN PHẨM ===");
        List<Product> products = productRepository.findAll();

        if (products.isEmpty()) {
            System.out.println("Chưa có sản phẩm nào!");
            return;
        }

        System.out.println(String.format("%-10s | %-25s | %-12s | %10s | %15s | %15s",
                "ID", "Tên", "Loại", "Tồn kho", "Giá nhập", "Giá bán"));
        System.out.println("-".repeat(100));

        for (Product product : products) {
            String name = product.getName().length() > 25 ?
                    product.getName().substring(0, 22) + "..." : product.getName();
            System.out.println(String.format("%-10s | %-25s | %-12s | %,10d | %,15.0f | %,15.0f",
                    product.getId(),
                    name,
                    product.getProductType(),
                    product.getStockQuantity(),
                    product.getImportPrice(),
                    product.getSalePrice()));
        }
        System.out.println("\nTổng: " + products.size() + " sản phẩm");
    }

    private void searchProduct() {
        System.out.print("\nNhập từ khóa tìm kiếm: ");
        String keyword = scanner.nextLine();

        List<Product> results = productRepository.search(keyword);

        if (results.isEmpty()) {
            System.out.println("❌ Không tìm thấy sản phẩm nào!");
            return;
        }

        System.out.println("\n=== KẾT QUẢ TÌM KIẾM ===");
        for (Product product : results) {
            System.out.println(product);
        }
    }

    private void updateProduct() {
        System.out.print("\nNhập ID sản phẩm cần cập nhật: ");
        String id = scanner.nextLine();

        Product product = productRepository.findById(id);
        if (product == null) {
            System.out.println("❌ Không tìm thấy sản phẩm!");
            return;
        }

        System.out.println("Sản phẩm hiện tại: " + product);
        System.out.print("Tên mới (Enter để giữ nguyên): ");
        String name = scanner.nextLine();
        if (!name.isEmpty()) product.setName(name);

        double newSalePrice = getDoubleInput("Giá bán mới (0 để giữ nguyên): ");
        if (newSalePrice > 0) product.setSalePrice(newSalePrice);

        try {
            productRepository.update(product);
            saveData();
            System.out.println("✅ Đã cập nhật sản phẩm!");
        } catch (ProductNotFoundException e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

    private void deleteProduct() {
        System.out.print("\nNhập ID sản phẩm cần xóa: ");
        String id = scanner.nextLine();

        try {
            productRepository.delete(id);
            saveData();
            System.out.println("✅ Đã xóa sản phẩm!");
        } catch (ProductNotFoundException e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

    // ========== QUẢN LÝ KHÁCH HÀNG ==========
    private void customerManagementMenu() {
        System.out.println("\n╔════════ QUẢN LÝ KHÁCH HÀNG ════════╗");
        System.out.println("║ 1. Thêm khách hàng                 ║");
        System.out.println("║ 2. Xem danh sách khách hàng        ║");
        System.out.println("║ 3. Tìm kiếm khách hàng             ║");
        System.out.println("║ 0. Quay lại                        ║");
        System.out.println("╚════════════════════════════════════╝");

        int choice = getIntInput("Chọn: ");
        switch (choice) {
            case 1: addCustomer(); break;
            case 2: viewAllCustomers(); break;
            case 3: searchCustomer(); break;
            case 0: break;
            default: System.out.println("❌ Lựa chọn không hợp lệ!");
        }
    }

    private void addCustomer() {
        System.out.println("\n=== THÊM KHÁCH HÀNG MỚI ===");
        System.out.print("Tên: ");
        String name = scanner.nextLine();
        System.out.print("Số điện thoại: ");
        String phone = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Địa chỉ: ");
        String address = scanner.nextLine();

        System.out.println("Loại khách hàng:");
        System.out.println("1. RETAIL (Lẻ)");
        System.out.println("2. WHOLESALE (Sỉ)");
        int typeChoice = getIntInput("Chọn: ");
        CustomerType type = typeChoice == 2 ? CustomerType.WHOLESALE : CustomerType.RETAIL;

        Customer customer = new Customer(name, phone, email, address, type);
        customerRepository.add(customer);
        saveData();
        System.out.println("✅ Đã thêm khách hàng: " + customer.getId());
    }

    private void viewAllCustomers() {
        System.out.println("\n=== DANH SÁCH KHÁCH HÀNG ===");
        List<Customer> customers = customerRepository.findAll();

        if (customers.isEmpty()) {
            System.out.println("Chưa có khách hàng nào!");
            return;
        }

        System.out.println(String.format("%-12s | %-25s | %-12s | %-30s | %s",
                "ID", "Tên", "SĐT", "Email", "Loại"));
        System.out.println("-".repeat(100));

        for (Customer customer : customers) {
            System.out.println(customer);
        }
        System.out.println("\nTổng: " + customers.size() + " khách hàng");
    }

    private void searchCustomer() {
        System.out.print("\nNhập từ khóa tìm kiếm: ");
        String keyword = scanner.nextLine();

        List<Customer> results = customerRepository.search(keyword);

        if (results.isEmpty()) {
            System.out.println("❌ Không tìm thấy khách hàng nào!");
            return;
        }

        System.out.println("\n=== KẾT QUẢ TÌM KIẾM ===");
        for (Customer customer : results) {
            System.out.println(customer);
        }
    }

    // ========== QUẢN LÝ NHÀ CUNG CẤP ==========
    private void supplierManagementMenu() {
        System.out.println("\n╔════════ QUẢN LÝ NHÀ CUNG CẤP ════════╗");
        System.out.println("║ 1. Thêm nhà cung cấp                 ║");
        System.out.println("║ 2. Xem danh sách nhà cung cấp        ║");
        System.out.println("║ 3. Tìm kiếm nhà cung cấp             ║");
        System.out.println("║ 0. Quay lại                          ║");
        System.out.println("╚══════════════════════════════════════╝");

        int choice = getIntInput("Chọn: ");
        switch (choice) {
            case 1: addSupplier(); break;
            case 2: viewAllSuppliers(); break;
            case 3: searchSupplier(); break;
            case 0: break;
            default: System.out.println("❌ Lựa chọn không hợp lệ!");
        }
    }

    private void addSupplier() {
        System.out.println("\n=== THÊM NHÀ CUNG CẤP MỚI ===");
        System.out.print("Tên: ");
        String name = scanner.nextLine();
        System.out.print("Số điện thoại: ");
        String phone = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Địa chỉ: ");
        String address = scanner.nextLine();
        System.out.print("Danh mục sản phẩm (phân cách bởi dấu phẩy): ");
        String categories = scanner.nextLine();

        Supplier supplier = new Supplier(name, phone, email, address, categories);
        supplierRepository.add(supplier);
        saveData();
        System.out.println("✅ Đã thêm nhà cung cấp: " + supplier.getId());
    }

    private void viewAllSuppliers() {
        System.out.println("\n=== DANH SÁCH NHÀ CUNG CẤP ===");
        List<Supplier> suppliers = supplierRepository.findAll();

        if (suppliers.isEmpty()) {
            System.out.println("Chưa có nhà cung cấp nào!");
            return;
        }

        for (Supplier supplier : suppliers) {
            System.out.println(supplier);
        }
        System.out.println("\nTổng: " + suppliers.size() + " nhà cung cấp");
    }

    private void searchSupplier() {
        System.out.print("\nNhập từ khóa tìm kiếm: ");
        String keyword = scanner.nextLine();

        List<Supplier> results = supplierRepository.search(keyword);

        if (results.isEmpty()) {
            System.out.println("❌ Không tìm thấy nhà cung cấp nào!");
            return;
        }

        System.out.println("\n=== KẾT QUẢ TÌM KIẾM ===");
        for (Supplier supplier : results) {
            System.out.println(supplier);
        }
    }

    // ========== NHẬP KHO ==========
    private void importMenu() {
        System.out.println("\n╔════════ NHẬP KHO ════════╗");
        System.out.println("║ 1. Tạo phiếu nhập mới    ║");
        System.out.println("║ 2. Xem phiếu nhập        ║");
        System.out.println("║ 3. Xác nhận nhập kho     ║");
        System.out.println("║ 4. Hủy phiếu nhập        ║");
        System.out.println("║ 0. Quay lại              ║");
        System.out.println("╚══════════════════════════╝");

        int choice = getIntInput("Chọn: ");
        switch (choice) {
            case 1: createImportOrder(); break;
            case 2: viewImportOrders(); break;
            case 3: confirmImportOrder(); break;
            case 4: cancelImportOrder(); break;
            case 0: break;
            default: System.out.println("❌ Lựa chọn không hợp lệ!");
        }
    }

    private void createImportOrder() {
        System.out.println("\n=== TẠO PHIẾU NHẬP KHO ===");

        System.out.println("\nDanh sách nhà cung cấp:");
        List<Supplier> suppliers = supplierRepository.findAll();
        if (suppliers.isEmpty()) {
            System.out.println("❌ Chưa có nhà cung cấp nào! Vui lòng thêm nhà cung cấp trước.");
            return;
        }

        for (int i = 0; i < suppliers.size(); i++) {
            Supplier s = suppliers.get(i);
            System.out.println((i + 1) + ". " + s.getId() + " - " + s.getName());
        }

        int supplierIndex = getIntInput("Chọn nhà cung cấp: ") - 1;
        if (supplierIndex < 0 || supplierIndex >= suppliers.size()) {
            System.out.println("❌ Lựa chọn không hợp lệ!");
            return;
        }
        Supplier supplier = suppliers.get(supplierIndex);

        System.out.print("Kho nhập: ");
        String warehouse = scanner.nextLine();

        List<OrderItem> items = new ArrayList<>();
        boolean addingItems = true;

        while (addingItems) {
            System.out.print("\nNhập ID sản phẩm (hoặc 'q' để kết thúc): ");
            String productId = scanner.nextLine();

            if (productId.equalsIgnoreCase("q")) {
                break;
            }

            Product product = productRepository.findById(productId);
            if (product == null) {
                System.out.println("❌ Không tìm thấy sản phẩm!");
                continue;
            }

            System.out.println("Sản phẩm: " + product.getName());
            int quantity = getIntInput("Số lượng: ");
            double price = getDoubleInput("Giá nhập: ");

            OrderItem item = new OrderItem(product, quantity, price);
            items.add(item);
            System.out.println("✅ Đã thêm: " + quantity + " x " + product.getName());
        }

        if (items.isEmpty()) {
            System.out.println("❌ Phải có ít nhất 1 sản phẩm!");
            return;
        }

        try {
            ImportOrder order = warehouseService.createImportOrder(
                    supplier.getId(), warehouse, items
            );
            saveData();
            System.out.println("\n✅ Đã tạo phiếu nhập: " + order.getId());
            System.out.println("   Tổng giá trị: " + String.format("%,.0f", order.getTotalAmount()) + " VNĐ");
            System.out.println("   Trạng thái: " + order.getStatus());
        } catch (Exception e) {
            System.out.println("❌ Lỗi: " + e.getMessage());
        }
    }

    private void viewImportOrders() {
        System.out.println("\n=== DANH SÁCH PHIẾU NHẬP ===");
        List<ImportOrder> orders = orderRepository.findAllImportOrders();

        if (orders.isEmpty()) {
            System.out.println("Chưa có phiếu nhập nào!");
            return;
        }

        System.out.println(String.format("%-15s | %-12s | %18s | %-12s | %-20s",
                "ID", "Ngày", "Tổng tiền", "Trạng thái", "Nhà cung cấp"));
        System.out.println("-".repeat(90));

        for (ImportOrder order : orders) {
            System.out.println(order);
        }
        System.out.println("\nTổng: " + orders.size() + " phiếu");
    }

    private void confirmImportOrder() {
        System.out.print("\nNhập ID phiếu nhập: ");
        String orderId = scanner.nextLine();

        try {
            warehouseService.confirmImport(orderId);
        } catch (OrderNotFoundException | IOException e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

    private void cancelImportOrder() {
        System.out.print("\nNhập ID phiếu nhập cần hủy: ");
        String orderId = scanner.nextLine();

        try {
            warehouseService.cancelOrder(orderId, "IMPORT");
        } catch (OrderNotFoundException | IOException e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

    // ========== XUẤT KHO ==========
    private void exportMenu() {
        System.out.println("\n╔════════ XUẤT KHO ════════╗");
        System.out.println("║ 1. Tạo phiếu xuất mới    ║");
        System.out.println("║ 2. Xem phiếu xuất        ║");
        System.out.println("║ 3. Xác nhận xuất kho     ║");
        System.out.println("║ 4. Hủy phiếu xuất        ║");
        System.out.println("║ 0. Quay lại              ║");
        System.out.println("╚══════════════════════════╝");

        int choice = getIntInput("Chọn: ");
        switch (choice) {
            case 1: createExportOrder(); break;
            case 2: viewExportOrders(); break;
            case 3: confirmExportOrder(); break;
            case 4: cancelExportOrder(); break;
            case 0: break;
            default: System.out.println("❌ Lựa chọn không hợp lệ!");
        }
    }

    private void createExportOrder() {
        System.out.println("\n=== TẠO PHIẾU XUẤT KHO ===");

        System.out.println("\nDanh sách khách hàng:");
        List<Customer> customers = customerRepository.findAll();
        if (customers.isEmpty()) {
            System.out.println("❌ Chưa có khách hàng nào! Vui lòng thêm khách hàng trước.");
            return;
        }

        for (int i = 0; i < customers.size(); i++) {
            Customer c = customers.get(i);
            System.out.println((i + 1) + ". " + c.getId() + " - " + c.getName());
        }

        int customerIndex = getIntInput("Chọn khách hàng: ") - 1;
        if (customerIndex < 0 || customerIndex >= customers.size()) {
            System.out.println("❌ Lựa chọn không hợp lệ!");
            return;
        }
        Customer customer = customers.get(customerIndex);

        System.out.print("Địa chỉ giao hàng: ");
        String address = scanner.nextLine();

        List<OrderItem> items = new ArrayList<>();
        boolean addingItems = true;

        while (addingItems) {
            System.out.print("\nNhập ID sản phẩm (hoặc 'q' để kết thúc): ");
            String productId = scanner.nextLine();

            if (productId.equalsIgnoreCase("q")) {
                break;
            }

            Product product = productRepository.findById(productId);
            if (product == null) {
                System.out.println("❌ Không tìm thấy sản phẩm!");
                continue;
            }

            System.out.println("Sản phẩm: " + product.getName());
            System.out.println("Tồn kho: " + product.getStockQuantity());

            int quantity = getIntInput("Số lượng: ");

            if (quantity > product.getStockQuantity()) {
                System.out.println("⚠️  Cảnh báo: Không đủ hàng! Tồn kho chỉ còn: " + product.getStockQuantity());
                System.out.print("Vẫn muốn thêm? (y/n): ");
                String confirm = scanner.nextLine();
                if (!confirm.equalsIgnoreCase("y")) {
                    continue;
                }
            }

            double price = getDoubleInput("Giá bán: ");

            OrderItem item = new OrderItem(product, quantity, price);
            items.add(item);
            System.out.println("✅ Đã thêm: " + quantity + " x " + product.getName());
        }

        if (items.isEmpty()) {
            System.out.println("❌ Phải có ít nhất 1 sản phẩm!");
            return;
        }

        try {
            ExportOrder order = warehouseService.createExportOrder(
                    customer.getId(), address, items
            );
            saveData();
            System.out.println("\n✅ Đã tạo phiếu xuất: " + order.getId());
            System.out.println("   Tổng giá trị: " + String.format("%,.0f", order.getTotalAmount()) + " VNĐ");
            System.out.println("   Trạng thái: " + order.getStatus());
        } catch (OutOfStockException e) {
            System.out.println("\n❌ " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Lỗi: " + e.getMessage());
        }
    }

    private void viewExportOrders() {
        System.out.println("\n=== DANH SÁCH PHIẾU XUẤT ===");
        List<ExportOrder> orders = orderRepository.findAllExportOrders();

        if (orders.isEmpty()) {
            System.out.println("Chưa có phiếu xuất nào!");
            return;
        }

        System.out.println(String.format("%-15s | %-12s | %18s | %-12s | %-20s",
                "ID", "Ngày", "Tổng tiền", "Trạng thái", "Khách hàng"));
        System.out.println("-".repeat(90));

        for (ExportOrder order : orders) {
            System.out.println(order);
        }
        System.out.println("\nTổng: " + orders.size() + " phiếu");
    }

    private void confirmExportOrder() {
        System.out.print("\nNhập ID phiếu xuất: ");
        String orderId = scanner.nextLine();

        try {
            warehouseService.confirmExport(orderId);
        } catch (OrderNotFoundException | OutOfStockException | IOException | ProductNotFoundException e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

    private void cancelExportOrder() {
        System.out.print("\nNhập ID phiếu xuất cần hủy: ");
        String orderId = scanner.nextLine();

        try {
            warehouseService.cancelOrder(orderId, "EXPORT");
        } catch (OrderNotFoundException | IOException e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

    // ========== KIỂM KÊ KHO ==========
    private void inventoryMenu() {
        System.out.println("\n╔════════ KIỂM KÊ KHO ════════╗");
        System.out.println("║ 1. Kiểm kê tồn kho          ║");
        System.out.println("║ 2. Sản phẩm sắp hết hàng    ║");
        System.out.println("║ 3. Sản phẩm theo loại       ║");
        System.out.println("║ 0. Quay lại                 ║");
        System.out.println("╚═════════════════════════════╝");

        int choice = getIntInput("Chọn: ");
        switch (choice) {
            case 1: performInventoryCheck(); break;
            case 2: viewLowStockProducts(); break;
            case 3: viewProductsByType(); break;
            case 0: break;
            default: System.out.println("❌ Lựa chọn không hợp lệ!");
        }
    }

    private void performInventoryCheck() {
        String report = warehouseService.performInventoryCheck();
        System.out.println(report);
    }

    private void viewLowStockProducts() {
        int threshold = getIntInput("\nNgưỡng cảnh báo (mặc định 10): ");
        if (threshold <= 0) threshold = 10;

        List<Product> lowStock = productRepository.getLowStockProducts(threshold);

        if (lowStock.isEmpty()) {
            System.out.println("✅ Không có sản phẩm nào sắp hết hàng!");
            return;
        }

        System.out.println("\n⚠️  SẢN PHẨM SẮP HẾT HÀNG (< " + threshold + "):");
        System.out.println(String.format("%-10s | %-30s | %10s | %15s",
                "ID", "Tên", "Tồn kho", "Loại"));
        System.out.println("-".repeat(80));

        for (Product product : lowStock) {
            String name = product.getName().length() > 30 ?
                    product.getName().substring(0, 27) + "..." : product.getName();
            System.out.println(String.format("%-10s | %-30s | %,10d | %15s",
                    product.getId(), name, product.getStockQuantity(), product.getProductType()));
        }
    }

    private void viewProductsByType() {
        System.out.println("\nChọn loại sản phẩm:");
        System.out.println("1. ELECTRONICS");
        System.out.println("2. CLOTHING");
        System.out.println("3. FOOD");
        System.out.println("4. FURNITURE");

        int choice = getIntInput("Chọn: ");
        String type = "";

        switch (choice) {
            case 1: type = "ELECTRONICS"; break;
            case 2: type = "CLOTHING"; break;
            case 3: type = "FOOD"; break;
            case 4: type = "FURNITURE"; break;
            default:
                System.out.println("❌ Lựa chọn không hợp lệ!");
                return;
        }

        List<Product> products = productRepository.findByType(type);

        if (products.isEmpty()) {
            System.out.println("Không có sản phẩm nào thuộc loại này!");
            return;
        }

        System.out.println("\n=== SẢN PHẨM LOẠI " + type + " ===");
        for (Product product : products) {
            System.out.println(product);
        }
        System.out.println("\nTổng: " + products.size() + " sản phẩm");
    }

    // ========== BÁO CÁO & THỐNG KÊ ==========
    private void reportMenu() {
        System.out.println("\n╔════════ BÁO CÁO & THỐNG KÊ ════════╗");
        System.out.println("║ 1. Báo cáo tồn kho                 ║");
        System.out.println("║ 2. Báo cáo nhập-xuất-tồn           ║");
        System.out.println("║ 3. Báo cáo doanh thu               ║");
        System.out.println("║ 4. Top sản phẩm bán chạy           ║");
        System.out.println("║ 5. Xu hướng bán hàng theo mùa      ║");
        System.out.println("║ 6. Xuất báo cáo (CSV/PDF)          ║");
        System.out.println("║ 0. Quay lại                        ║");
        System.out.println("╚════════════════════════════════════╝");

        int choice = getIntInput("Chọn: ");
        switch (choice) {
            case 1: showInventoryReport(); break;
            case 2: showImportExportReport(); break;
            case 3: showRevenueReport(); break;
            case 4: showTopSellingProducts(); break;
            case 5: showSeasonalTrend(); break;
            case 6: exportReportToFile(); break;
            case 0: break;
            default: System.out.println("❌ Lựa chọn không hợp lệ!");
        }
    }

    private void showInventoryReport() {
        String report = reportService.generateInventoryReport();
        System.out.println(report);
    }

    private void showImportExportReport() {
        System.out.println("\n=== BÁO CÁO NHẬP-XUẤT-TỒN ===");
        System.out.print("Từ ngày (yyyy-MM-dd): ");
        LocalDate fromDate = parseDate(scanner.nextLine());
        System.out.print("Đến ngày (yyyy-MM-dd): ");
        LocalDate toDate = parseDate(scanner.nextLine());

        if (fromDate == null || toDate == null) {
            System.out.println("❌ Ngày không hợp lệ!");
            return;
        }

        String report = reportService.generateImportExportReport(fromDate, toDate);
        System.out.println(report);
    }

    private void showRevenueReport() {
        System.out.println("\n=== BÁO CÁO DOANH THU ===");
        System.out.print("Từ ngày (yyyy-MM-dd): ");
        LocalDate fromDate = parseDate(scanner.nextLine());
        System.out.print("Đến ngày (yyyy-MM-dd): ");
        LocalDate toDate = parseDate(scanner.nextLine());

        if (fromDate == null || toDate == null) {
            System.out.println("❌ Ngày không hợp lệ!");
            return;
        }

        String report = reportService.generateRevenueReport(fromDate, toDate);
        System.out.println(report);
    }

    private void showTopSellingProducts() {
        int topN = getIntInput("\nTop bao nhiêu sản phẩm (mặc định 5): ");
        if (topN <= 0) topN = 5;

        String report = reportService.generateTopSellingReport(topN);
        System.out.println(report);
    }

    private void showSeasonalTrend() {
        String report = reportService.generateSeasonalTrendReport();
        System.out.println(report);
    }

    private void exportReportToFile() {
        System.out.println("\n╔════════ XUAT BAO CAO ════════╗");
        System.out.println("║ CSV Files:                   ║");
        System.out.println("║ 1. Bao cao ton kho - CSV     ║");
        System.out.println("║ 2. Bao cao doanh thu - CSV   ║");
        System.out.println("║ 3. Xu huong mua - CSV        ║");
        System.out.println("║                              ║");
        System.out.println("║ PDF Files:                   ║");
        System.out.println("║ 4. Bao cao ton kho - PDF     ║");
        System.out.println("║ 5. Bao cao doanh thu - PDF   ║");
        System.out.println("║ 6. Xu huong mua - PDF        ║");
        System.out.println("║ 0. Quay lai                  ║");
        System.out.println("╚══════════════════════════════╝");

        int choice = getIntInput("Chon: ");

        try {
            LocalDate fromDate = LocalDate.now().minusMonths(1);
            LocalDate toDate = LocalDate.now();

            switch (choice) {
                case 1:
                    System.out.println("Dang xuat CSV ton kho...");
                    reportService.exportToExcelCSV("inventory", null, null);
                    System.out.println("Hoan thanh!");
                    break;
                case 2:
                    System.out.println("Dang xuat CSV doanh thu...");
                    reportService.exportToExcelCSV("revenue", fromDate, toDate);
                    System.out.println("Hoan thanh!");
                    break;
                case 3:
                    System.out.println("Dang xuat CSV xu huong...");
                    String report3 = reportService.generateSeasonalTrendReport();
                    reportService.exportToCSV(report3, "seasonal_trend_" + LocalDate.now() + ".csv");
                    System.out.println("Hoan thanh!");
                    break;
                case 4:
                    System.out.println("Dang xuat PDF ton kho...");
                    reportService.exportToPDF("inventory", "ton_kho_" + LocalDate.now() + ".pdf");
                    System.out.println("Hoan thanh!");
                    break;
                case 5:
                    System.out.println("Dang xuat PDF doanh thu...");
                    reportService.exportToPDF("revenue", "doanh_thu_" + LocalDate.now() + ".pdf");
                    System.out.println("Hoan thanh!");
                    break;
                case 6:
                    System.out.println("Dang xuat PDF xu huong...");
                    reportService.exportToPDF("seasonal", "xu_huong_" + LocalDate.now() + ".pdf");
                    System.out.println("Hoan thanh!");
                    break;
                case 0:
                    System.out.println("Quay lai menu chinh");
                    break;
                default:
                    System.out.println("Lua chon khong hop le!");
                    return;
            }
        } catch (IOException | com.itextpdf.text.DocumentException e) {
            System.out.println("Loi khi xuat file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========== HELPER METHODS ==========
    private int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("❌ Vui lòng nhập số!");
            }
        }
    }

    private double getDoubleInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine();
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("❌ Vui lòng nhập số!");
            }
        }
    }

    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
