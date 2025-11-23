package interfaces;

import java.util.List;

/**
 * Generic interface cho chức năng tìm kiếm
 * @param <T> Kiểu dữ liệu của entity cần tìm
 */
public interface Searchable<T> {

    /**
     * Tìm kiếm theo ID
     * @param id ID cần tìm
     * @return Object tìm thấy hoặc null
     */
    T findById(String id);

    /**
     * Tìm kiếm theo tên (tìm gần đúng)
     * @param name Tên cần tìm
     * @return Danh sách các object tìm thấy
     */
    List<T> findByName(String name);

    /**
     * Lấy toàn bộ dữ liệu
     * @return Danh sách tất cả object
     */
    List<T> findAll();

    /**
     * Tìm kiếm với điều kiện tùy chỉnh
     * @param criteria Tiêu chí tìm kiếm
     * @return Danh sách các object thỏa mãn điều kiện
     */
    List<T> search(String criteria);
}
