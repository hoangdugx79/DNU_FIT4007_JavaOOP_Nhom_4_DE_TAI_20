package domain;

public class Electronics extends Product {
    private int warrantyMonths;

    public Electronics(String name, String category, double importPrice, double salePrice,
                       int stockQuantity, int warrantyMonths) {
        super(name, category, importPrice, salePrice, stockQuantity);
        this.warrantyMonths = warrantyMonths;
    }

    public Electronics(String id, String name, String category, double importPrice,
                       double salePrice, int stockQuantity, int warrantyMonths) {
        super(id, name, category, importPrice, salePrice, stockQuantity);
        this.warrantyMonths = warrantyMonths;
    }

    @Override
    public double calculateProfit() {
        // Electronics: lợi nhuận 15-20% (tính trên tổng giá trị tồn kho)
        return (salePrice - importPrice) * stockQuantity;
    }

    @Override
    public String getProductType() {
        return "ELECTRONICS";
    }

    @Override
    public String toCSV() {
        return String.format("%s,%s,%s,%s,%.0f,%.0f,%d,%d,",
                id, getProductType(), name, category, importPrice, salePrice, stockQuantity, warrantyMonths);
    }

    public int getWarrantyMonths() { return warrantyMonths; }
    public void setWarrantyMonths(int warrantyMonths) { this.warrantyMonths = warrantyMonths; }

    @Override
    public String toString() {
        return super.toString() + String.format(" | %s | BH: %d tháng", getProductType(), warrantyMonths);
    }
}
