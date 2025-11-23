import ui.ConsoleMenu;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public class Main {

    // Constants
    private static final String APP_NAME = "Warehouse Management System";
    private static final String VERSION = "1.0";
    private static final String[] REQUIRED_DIRS = {"data", "reports"};

    public static void main(String[] args) {
        try {

            // 3. Khởi tạo thư mục
            initializeDirectories();

            // 5. Khởi chạy ứng dụng
            ConsoleMenu menu = new ConsoleMenu();
            menu.start();

            // 6. Log khi thoát
            logShutdown();

        } catch (Exception e) {
            handleFatalError(e);
        }
    }

    /**
     * Khởi tạo các thư mục cần thiết
     */
    private static void initializeDirectories() {
        System.out.println("📁 Đang kiểm tra thư mục...");

        for (String dirName : REQUIRED_DIRS) {
            File directory = new File(dirName);
            if (!directory.exists()) {
                if (directory.mkdir()) {
                    System.out.println("   ✅ Đã tạo thư mục: " + dirName + "/");
                } else {
                    System.err.println("   ⚠️  Không thể tạo thư mục: " + dirName + "/");
                }
            } else {
                System.out.println("   ✓ Thư mục đã tồn tại: " + dirName + "/");
            }
        }
        System.out.println();
    }

    /**
     * Log khi thoát ứng dụng
     */
    private static void logShutdown() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        System.out.println("\n" + "━".repeat(85));
        System.out.println("🛑 Thoát hệ thống lúc: " + now.format(formatter));
        System.out.println("👋 Hẹn gặp lại!");
    }

    /**
     * Xử lý lỗi nghiêm trọng
     */
    private static void handleFatalError(Exception e) {
        System.err.println("\n" + "═".repeat(85));
        System.err.println("❌ LỖI NGHIÊM TRỌNG - HỆ THỐNG KHÔNG THỂ KHỞI ĐỘNG");
        System.err.println("═".repeat(85));
        System.err.println("Lỗi: " + e.getMessage());
        System.err.println("\nChi tiết:");
        e.printStackTrace();
        System.err.println("═".repeat(85));

        // Gợi ý khắc phục
        System.err.println("\n💡 Gợi ý khắc phục:");
        System.err.println("   1. Kiểm tra quyền ghi/đọc file");
        System.err.println("   2. Đảm bảo Java version ≥ 8");
        System.err.println("   3. Kiểm tra tất cả file .java đã compile");
        System.err.println("   4. Xem log chi tiết ở trên\n");

        System.exit(1);
    }
}
