package domain;

import java.time.LocalDate;

public class ExportOrder extends Order {
    private Customer customer;
    private String deliveryAddress;

    // Constructor khi tạo mới
    public ExportOrder(LocalDate orderDate, Customer customer, String deliveryAddress) {
        super(orderDate);
        this.customer = customer;
        this.deliveryAddress = deliveryAddress;
    }

    // Constructor khi load từ CSV
    public ExportOrder(String id, LocalDate orderDate, double totalAmount,
                       OrderStatus status, Customer customer, String deliveryAddress) {
        super(id, orderDate, totalAmount, status);
        this.customer = customer;
        this.deliveryAddress = deliveryAddress;
    }

    @Override
    public String getOrderType() {
        return "EXPORT";
    }

    @Override
    protected String generateOrderId() {
        return "EXP-" + System.currentTimeMillis();
    }

    @Override
    public void calculateTotal() {
        double total = 0;
        for (OrderItem item : items) {
            total += item.getSubtotal();
        }
        this.totalAmount = total;
    }

    @Override
    public String toCSV() {
        return String.format("%s,%s,%s,%.0f,%s,%s",
                id,
                customer != null ? customer.getId() : "",
                orderDate.format(DATE_FORMAT),
                totalAmount,
                status,
                deliveryAddress);
    }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    @Override
    public String toString() {
        return super.toString() + String.format(" | KH: %s | Địa chỉ: %s",
                customer != null ? customer.getName() : "N/A", deliveryAddress);
    }
}
