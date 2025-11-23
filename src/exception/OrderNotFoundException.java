package exception;

/**
 * Exception được ném ra khi không tìm thấy đơn hàng (nhập/xuất) theo ID
 */
public class OrderNotFoundException extends Exception {
    private String orderId;
    private String orderType; // "IMPORT" hoặc "EXPORT"

    public OrderNotFoundException(String orderId, String orderType) {
        super(String.format("Không tìm thấy đơn %s với ID: %s",
                orderType.equals("IMPORT") ? "nhập kho" : "xuất kho", orderId));
        this.orderId = orderId;
        this.orderType = orderType;
    }

    public OrderNotFoundException(String message) {
        super(message);
    }

    // Getters
    public String getOrderId() { return orderId; }
    public String getOrderType() { return orderType; }
}
