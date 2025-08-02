
// DAO cho bảng 'class_instances' trong Room Database
package com.example.universalyogaapp.dao;

import androidx.room.*; // Annotation cho Room
import com.example.universalyogaapp.db.ClassInstanceEntity; // Entity cho class_instances
import java.util.List; // Sử dụng List cho kết quả truy vấn


@Dao // Đánh dấu interface này là DAO cho Room
public interface ClassInstanceDao {

    // Thêm mới hoặc cập nhật class instance (nếu trùng khoá chính)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ClassInstanceEntity instance); // Trả về localId vừa insert

    // Cập nhật thông tin class instance
    @Update
    void update(ClassInstanceEntity instance);

    // Xoá một class instance
    @Delete
    void delete(ClassInstanceEntity instance);

    // Lấy các instance theo courseId trên Firebase (dùng cho hiển thị)
    @Query("SELECT * FROM class_instances WHERE courseId = :courseFirebaseId")
    List<ClassInstanceEntity> getInstancesForCourse(String courseFirebaseId);

    // Lấy các instance theo courseLocalId (dùng cho xử lý nội bộ)
    @Query("SELECT * FROM class_instances WHERE courseLocalId = :courseLocalId")
    List<ClassInstanceEntity> getInstancesForCourseByLocalId(int courseLocalId);

    // Lấy các instance chưa đồng bộ lên Firebase
    @Query("SELECT * FROM class_instances WHERE isSynced = 0")
    List<ClassInstanceEntity> getUnsyncedInstances();

    // Xoá instance theo firebaseId
    @Query("DELETE FROM class_instances WHERE firebaseId = :firebaseId")
    void deleteByFirebaseId(String firebaseId);

    // Xoá instance theo localId
    @Query("DELETE FROM class_instances WHERE id = :localId")
    void deleteByLocalId(int localId);

    // Lấy instance theo firebaseId
    @Query("SELECT * FROM class_instances WHERE firebaseId = :firebaseId LIMIT 1")
    ClassInstanceEntity getInstanceByFirebaseId(String firebaseId);

    // Lấy instance theo localId
    @Query("SELECT * FROM class_instances WHERE id = :localId LIMIT 1")
    ClassInstanceEntity getInstanceByLocalId(int localId);

    // Đánh dấu instance đã đồng bộ (isSynced=1) và cập nhật firebaseId
    @Query("UPDATE class_instances SET isSynced = 1, firebaseId = :firebaseId WHERE id = :id")
    void markInstanceAsSynced(int id, String firebaseId);

    // Xoá toàn bộ instance (dùng cho reset database)
    @Query("DELETE FROM class_instances")
    void deleteAllInstances();

    // Xoá instance theo courseId trên Firebase
    @Query("DELETE FROM class_instances WHERE courseId = :courseFirebaseId")
    void deleteInstancesByCourseFirebaseId(String courseFirebaseId);

    // Xoá instance theo courseLocalId
    @Query("DELETE FROM class_instances WHERE courseLocalId = :courseLocalId")
    void deleteInstancesByCourseLocalId(int courseLocalId);

    // Tìm các instance trùng ngày trong cùng khoá học
    @Query("SELECT * FROM class_instances WHERE courseId = :courseId AND date = :date")
    List<ClassInstanceEntity> findDuplicatesByDate(String courseId, String date);

    // Xoá các instance trùng ngày (giữ lại bản ghi có id nhỏ nhất)
    @Query("DELETE FROM class_instances WHERE id IN (SELECT id FROM class_instances WHERE courseId = :courseId AND date = :date AND id NOT IN (SELECT MIN(id) FROM class_instances WHERE courseId = :courseId AND date = :date))")
    void deleteDuplicateInstances(String courseId, String date);

    // Lấy thông tin các nhóm instance bị trùng (dùng cho cleanup)
    @Query("SELECT courseId, date, COUNT(*) as count FROM class_instances GROUP BY courseId, date HAVING COUNT(*) > 1")
    List<DuplicateInfo> getDuplicateInfo();

    // Lớp static lưu thông tin nhóm instance bị trùng
    static class DuplicateInfo {
        public String courseId; // ID khoá học
        public String date; // Ngày
        public int count; // Số lượng bản ghi trùng
    }

    // Tìm kiếm instance theo tên giáo viên (partial match, không phân biệt hoa thường)
    @Query("SELECT * FROM class_instances WHERE LOWER(teacher) LIKE '%' || LOWER(:teacher) || '%'")
    List<ClassInstanceEntity> searchByTeacher(String teacher);

    // Tìm kiếm instance theo ngày
    @Query("SELECT * FROM class_instances WHERE date = :date")
    List<ClassInstanceEntity> searchByDate(String date);

    // Để tìm theo thứ, cần lấy tất cả rồi lọc ở Java (date dạng yyyy-MM-dd)
    // List<ClassInstanceEntity> getAllInstances(); // Đã có qua các hàm trên

    // Lấy tất cả class instance
    @Query("SELECT * FROM class_instances")
    List<ClassInstanceEntity> getAllClassInstances();
}