package service;

import domain.*;
import exception.*;
import repository.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * Service xử lý logic nghiệp vụ nhập/xuất kho
 */
public class WarehouseService {
    private ProductRepository productRepository;
    private OrderRepository orderRepository;
    private CustomerRepository customerRepository;
    private SupplierRepository supplierRepository;

    public WarehouseService(ProductRepository productRepository,
                            OrderRepository orderRepository,
                            CustomerRepository customerRepository,
                            SupplierRepository supplierRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.supplierRepository = supplierRepository;
    }

    /**
     * Tạo phiếu nhập kho
     * @param supplierId ID nhà cung cấp
     * @param warehouseLocation Vị trí kho
     * @param items Danh sách sản phẩm nhập
     * @return ImportOrder đã tạo
     */
    public ImportOrder createImportOrder(String supplierId, String warehouseLocation,
                                         List<OrderItem> items)
            throws Exception {

        // Validate supplier
        Supplier supplier = supplierRepository.findById(supplierId);
        if (supplier == null) {
            throw new Exception("Không tìm thấy nhà cung cấp với ID: " + supplierId);
        }

        // Validate items
        if (items == null || items.isEmpty()) {
            throw new InvalidQuantityException("Đơn nhập hàng phải có ít nhất 1 sản phẩm");
        }

        for (OrderItem item : items) {
            if (item.getQuantity() <= 0) {
                throw InvalidQuantityException.negative(item.getQuantity());
            }
        }

        // Tạo đơn nhập
        ImportOrder order = new ImportOrder(LocalDate.now(), supplier, warehouseLocation);

        // Thêm items
        for (OrderItem item : items) {
            order.addItem(item);
        }

        // Lưu order
        orderRepository.addImportOrder(order);

        return order;
    }

    /**
     * Xác nhận nhập kho - cập nhật tồn kho sản phẩm
     * @param orderId ID đơn nhập
     */
    public void confirmImport(String orderId) throws OrderNotFoundException, IOException {
        ImportOrder order = orderRepository.findImportOrderById(orderId);
        if (order == null) {
            throw new OrderNotFoundException(orderId, "IMPORT");
        }

        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new IllegalStateException("Đơn hàng đã được xác nhận trước đó");
        }

        // Cập nhật tồn kho cho từng sản phẩm
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.increaseStock(item.getQuantity());

            try {
                productRepository.update(product);
            } catch (ProductNotFoundException e) {
                // Product không tồn tại, thêm mới
                productRepository.add(product);
            }
        }

        // Đổi trạng thái order
        order.setStatus(OrderStatus.COMPLETED);

        // Lưu changes
        productRepository.save();
        orderRepository.save();

        System.out.println("✅ Đã nhập kho thành công đơn: " + orderId);
        System.out.println("   Tổng giá trị: " + String.format("%,.0f", order.getTotalAmount()) + " VNĐ");
    }

    /**
     * Tạo phiếu xuất kho
     * @param customerId ID khách hàng
     * @param deliveryAddress Địa chỉ giao hàng
     * @param items Danh sách sản phẩm xuất
     * @return ExportOrder đã tạo
     */
    public ExportOrder createExportOrder(String customerId, String deliveryAddress,
                                         List<OrderItem> items)
            throws Exception {

        // Validate customer
        Customer customer = customerRepository.findById(customerId);
        if (customer == null) {
            throw new Exception("Không tìm thấy khách hàng với ID: " + customerId);
        }

        // Validate items và kiểm tra tồn kho
        if (items == null || items.isEmpty()) {
            throw new InvalidQuantityException("Đơn xuất hàng phải có ít nhất 1 sản phẩm");
        }

        for (OrderItem item : items) {
            if (item.getQuantity() <= 0) {
                throw InvalidQuantityException.negative(item.getQuantity());
            }

            // Kiểm tra tồn kho
            Product product = item.getProduct();
            if (!product.hasEnoughStock(item.getQuantity())) {
                throw new OutOfStockException(
                        product.getId(),
                        product.getName(),
                        item.getQuantity(),
                        product.getStockQuantity()
                );
            }
        }

        // Tạo đơn xuất
        ExportOrder order = new ExportOrder(LocalDate.now(), customer, deliveryAddress);

        // Thêm items
        for (OrderItem item : items) {
            order.addItem(item);
        }

        // Lưu order
        orderRepository.addExportOrder(order);

        return order;
    }

    /**
     * Xác nhận xuất kho - trừ tồn kho sản phẩm
     * @param orderId ID đơn xuất
     */
    public void confirmExport(String orderId)
            throws OrderNotFoundException, OutOfStockException, IOException, ProductNotFoundException {

        ExportOrder order = orderRepository.findExportOrderById(orderId);
        if (order == null) {
            throw new OrderNotFoundException(orderId, "EXPORT");
        }

        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new IllegalStateException("Đơn hàng đã được xác nhận trước đó");
        }

        // Kiểm tra lại tồn kho (phòng trường hợp thay đổi)
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            if (!product.hasEnoughStock(item.getQuantity())) {
                throw new OutOfStockException(
                        product.getId(),
                        product.getName(),
                        item.getQuantity(),
                        product.getStockQuantity()
                );
            }
        }

        // Trừ tồn kho cho từng sản phẩm
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.decreaseStock(item.getQuantity());
            productRepository.update(product);
        }

        // Đổi trạng thái order
        order.setStatus(OrderStatus.COMPLETED);

        // Lưu changes
        productRepository.save();
        orderRepository.save();

        System.out.println("✅ Đã xuất kho thành công đơn: " + orderId);
        System.out.println("   Tổng giá trị: " + String.format("%,.0f", order.getTotalAmount()) + " VNĐ");
    }

    /**
     * Hủy đơn hàng
     * @param orderId ID đơn hàng
     * @param orderType "IMPORT" hoặc "EXPORT"
     */
    public void cancelOrder(String orderId, String orderType)
            throws OrderNotFoundException, IOException {

        if (orderType.equals("IMPORT")) {
            ImportOrder order = orderRepository.findImportOrderById(orderId);
            if (order == null) {
                throw new OrderNotFoundException(orderId, "IMPORT");
            }
            order.setStatus(OrderStatus.CANCELLED);
        } else {
            ExportOrder order = orderRepository.findExportOrderById(orderId);
            if (order == null) {
                throw new OrderNotFoundException(orderId, "EXPORT");
            }
            order.setStatus(OrderStatus.CANCELLED);
        }

        orderRepository.save();
        System.out.println("✅ Đã hủy đơn: " + orderId);
    }

    /**
     * Kiểm kê kho - so sánh tồn kho thực tế với hệ thống
     */
    public String performInventoryCheck() {
        StringBuilder report = new StringBuilder();
        report.append("\n╔════════════════════════════════════════════════════════════════╗\n");
        report.append("║              BÁO CÁO KIỂM KÊ KHO                               ║\n");
        report.append("╚════════════════════════════════════════════════════════════════╝\n");
        report.append("Ngày kiểm kê: ").append(LocalDate.now()).append("\n\n");

        List<Product> products = productRepository.findAll();

        report.append(String.format("%-10s | %-30s | %-15s | %10s\n",
                "ID", "Tên sản phẩm", "Loại", "Tồn kho"));
        report.append("-".repeat(80)).append("\n");

        int totalProducts = 0;
        long totalStock = 0;

        for (Product product : products) {
            report.append(String.format("%-10s | %-30s | %-15s | %,10d\n",
                    product.getId(),
                    product.getName().length() > 30 ? product.getName().substring(0, 27) + "..." : product.getName(),
                    product.getProductType(),
                    product.getStockQuantity()));

            totalProducts++;
            totalStock += product.getStockQuantity();
        }

        report.append("-".repeat(80)).append("\n");
        report.append(String.format("Tổng số loại sản phẩm: %d\n", totalProducts));
        report.append(String.format("Tổng số lượng tồn kho: %,d\n", totalStock));

        // Cảnh báo sản phẩm sắp hết hàng
        List<Product> lowStock = productRepository.getLowStockProducts(10);
        if (!lowStock.isEmpty()) {
            report.append("\n⚠️  CẢNH BÁO: Các sản phẩm sắp hết hàng (< 10):\n");
            for (Product product : lowStock) {
                report.append(String.format("   - %s: %d\n", product.getName(), product.getStockQuantity()));
            }
        }

        return report.toString();
    }

    // Getters
    public ProductRepository getProductRepository() { return productRepository; }
    public OrderRepository getOrderRepository() { return orderRepository; }
    public CustomerRepository getCustomerRepository() { return customerRepository; }
    public SupplierRepository getSupplierRepository() { return supplierRepository; }
}


