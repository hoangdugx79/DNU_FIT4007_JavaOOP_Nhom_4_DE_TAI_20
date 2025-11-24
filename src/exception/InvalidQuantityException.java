package exception;

/**
 * Exception được ném ra khi số lượng nhập/xuất không hợp lệ (≤ 0 hoặc quá lớn)
 */
public class InvalidQuantityException extends Exception {
    private int quantity;
    private String reason;

    public InvalidQuantityException(int quantity, String reason) {
        super(String.format("Số lượng không hợp lệ: %d - Lý do: %s", quantity, reason));
        this.quantity = quantity;
        this.reason = reason;
    }

    public InvalidQuantityException(String message) {
        super(message);
    }

    // Getters
    public int getQuantity() { return quantity; }
    public String getReason() { return reason; }

    // Static factory methods để tạo exception thường dùng
    public static InvalidQuantityException negative(int quantity) {
        return new InvalidQuantityException(quantity, "Số lượng phải lớn hơn 0");
    }

    public static InvalidQuantityException tooLarge(int quantity, int maxAllowed) {
        return new InvalidQuantityException(quantity,
                String.format("Số lượng vượt quá giới hạn cho phép (%d)", maxAllowed));
    }

    public static InvalidQuantityException zero() {
        return new InvalidQuantityException(0, "Số lượng không được bằng 0");
    }
}
