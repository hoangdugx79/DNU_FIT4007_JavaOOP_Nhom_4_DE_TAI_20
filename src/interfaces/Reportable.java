package interfaces;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * Interface cho các class cần tạo báo cáo và thống kê
 * Service classes sẽ implement interface này
 */
public interface Reportable {

    /**
     * Tạo báo cáo tồn kho hiện tại
     * @return Chuỗi báo cáo dạng text
     */
    String generateInventoryReport();

    /**
     * Tạo báo cáo nhập-xuất-tồn theo khoảng thời gian
     * @param fromDate Từ ngày
     * @param toDate Đến ngày
     * @return Chuỗi báo cáo dạng text
     */
    String generateImportExportReport(LocalDate fromDate, LocalDate toDate);

    /**
     * Tạo báo cáo doanh thu theo khoảng thời gian
     * @param fromDate Từ ngày
     * @param toDate Đến ngày
     * @return Chuỗi báo cáo dạng text
     */
    String generateRevenueReport(LocalDate fromDate, LocalDate toDate);

    /**
     * Xuất báo cáo ra file CSV
     * @param reportContent Nội dung báo cáo
     * @param fileName Tên file xuất
     * @throws IOException nếu có lỗi khi ghi file
     */
    void exportToCSV(String reportContent, String fileName) throws IOException;

    /**
     * Lấy danh sách top N sản phẩm bán chạy
     * @param topN Số lượng top (ví dụ: 5)
     * @return Danh sách product ID và số lượng đã bán
     */
    List<String> getTopSellingProducts(int topN);
}
