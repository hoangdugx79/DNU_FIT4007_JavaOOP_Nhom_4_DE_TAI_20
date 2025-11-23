package domain;

public class Clothing extends Product {
    private String size;
    private String material;

    public Clothing(String name, String category, double importPrice, double salePrice,
                    int stockQuantity, String size, String material) {
        super(name, category, importPrice, salePrice, stockQuantity);
        this.size = size;
        this.material = material;
    }

    public Clothing(String id, String name, String category, double importPrice,
                    double salePrice, int stockQuantity, String size, String material) {
        super(id, name, category, importPrice, salePrice, stockQuantity);
        this.size = size;
        this.material = material;
    }

    @Override
    public double calculateProfit() {
        // Clothing: lợi nhuận 30-40% (cao hơn vì giá trị gia tăng)
        return (salePrice - importPrice) * stockQuantity * 1.1;
    }

    @Override
    public String getProductType() {
        return "CLOTHING";
    }

    @Override
    public String toCSV() {
        return String.format("%s,%s,%s,%s,%.0f,%.0f,%d,%s,%s",
                id, getProductType(), name, category, importPrice, salePrice, stockQuantity, size, material);
    }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }

    @Override
    public String toString() {
        return super.toString() + String.format(" | %s | Size: %s | Chất liệu: %s",
                getProductType(), size, material);
    }
}
