package domain;

import java.util.UUID;

public class Supplier {
    private String id;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String productCategories; // e.g., "Electronics,Clothing"

    // Constructor khi tạo mới
    public Supplier(String name, String phone, String email, String address, String productCategories) {
        this.id = "SUP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.productCategories = productCategories;
    }

    // Constructor khi load từ CSV
    public Supplier(String id, String name, String phone, String email, String address, String productCategories) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.productCategories = productCategories;
    }

    // Convert to CSV
    public String toCSV() {
        return String.format("%s,%s,%s,%s,%s,%s",
                id, name, phone, email, address, productCategories);
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getProductCategories() { return productCategories; }
    public void setProductCategories(String productCategories) { this.productCategories = productCategories; }

    @Override
    public String toString() {
        return String.format("%-12s | %-30s | %-12s | %-30s | %s",
                id, name, phone, email, productCategories);
    }
}
