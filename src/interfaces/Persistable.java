package interfaces;

import java.io.IOException;

/**
 * Interface cho các class cần lưu trữ và đọc dữ liệu từ file CSV
 * Repository classes sẽ implement interface này
 */
public interface Persistable {

    /**
     * Lưu toàn bộ dữ liệu vào file CSV
     * @throws IOException nếu có lỗi khi ghi file
     */
    void save() throws IOException;

    /**
     * Đọc toàn bộ dữ liệu từ file CSV
     * @throws IOException nếu có lỗi khi đọc file
     */
    void load() throws IOException;

    /**
     * Lấy đường dẫn file CSV
     * @return đường dẫn đầy đủ của file
     */
    String getFilePath();

    /**
     * Xóa toàn bộ dữ liệu (clear cache trong memory)
     */
    void clear();
}
