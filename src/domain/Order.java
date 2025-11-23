package domain;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class Order {
    protected String id;
    protected LocalDate orderDate;
    protected double totalAmount;
    protected OrderStatus status;
    protected List<OrderItem> items;

    protected static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Constructor khi tạo mới
    public Order(LocalDate orderDate) {
        this.id = generateOrderId();
        this.orderDate = orderDate;
        this.items = new ArrayList<>();
        this.status = OrderStatus.PENDING;
        this.totalAmount = 0;
    }

    // Constructor khi load từ CSV
    public Order(String id, LocalDate orderDate, double totalAmount, OrderStatus status) {
        this.id = id;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.status = status;
        this.items = new ArrayList<>();
    }

    // Abstract methods
    public abstract String getOrderType();
    public abstract void calculateTotal();
    public abstract String toCSV();

    // Generate unique order ID
    protected abstract String generateOrderId();

    // Thêm item vào đơn hàng
    public void addItem(OrderItem item) {
        this.items.add(item);
        calculateTotal();
    }

    // Xóa item khỏi đơn hàng
    public void removeItem(OrderItem item) {
        this.items.remove(item);
        calculateTotal();
    }

    // Getters and Setters
    public String getId() { return id; }
    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) {
        this.items = items;
        calculateTotal();
    }

    @Override
    public String toString() {
        return String.format("%-12s | %s | %,18.0f | %-12s | Items: %d",
                id, orderDate.format(DATE_FORMAT), totalAmount, status, items.size());
    }
}
