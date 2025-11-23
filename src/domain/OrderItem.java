package domain;

public class OrderItem {
    private Product product;
    private int quantity;
    private double unitPrice;

    public OrderItem(Product product, int quantity, double unitPrice) {
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    // Tính tổng tiền cho item này
    public double getSubtotal() {
        return quantity * unitPrice;
    }

    // Getters and Setters
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    // Convert to CSV
    public String toCSV(String orderId) {
        return String.format("%s,%s,%d,%.0f",
                orderId, product.getId(), quantity, unitPrice);
    }

    @Override
    public String toString() {
        return String.format("  - %-25s | SL: %4d | Đơn giá: %,12.0f | Tổng: %,12.0f",
                product.getName(), quantity, unitPrice, getSubtotal());
    }
}
