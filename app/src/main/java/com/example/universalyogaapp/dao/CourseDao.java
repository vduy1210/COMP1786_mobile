
// DAO (Data Access Object) cho bảng 'courses' trong Room Database
package com.example.universalyogaapp.dao;

import com.example.universalyogaapp.db.CourseEntity; // Entity đại diện cho bảng courses
import androidx.room.*; // Annotation cho Room (Insert, Update, Delete, Query, Dao)
import java.util.List; // Sử dụng List cho kết quả truy vấn


@Dao // Đánh dấu interface này là DAO cho Room
public interface CourseDao {

    // Thêm mới hoặc cập nhật khoá học (nếu trùng khoá chính)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CourseEntity course); // Trả về localId vừa insert

    // Cập nhật thông tin khoá học
    @Update
    void update(CourseEntity course);

    // Xoá một khoá học
    @Delete
    void delete(CourseEntity course);

    // Lấy danh sách các khoá học chưa đồng bộ lên Firebase
    @Query("SELECT * FROM courses WHERE isSynced = 0")
    List<CourseEntity> getUnsyncedCourses();

    // Lấy toàn bộ khoá học
    @Query("SELECT * FROM courses")
    List<CourseEntity> getAllCourses();

    // Lấy khoá học theo firebaseId (dùng cho đồng bộ)
    @Query("SELECT * FROM courses WHERE firebaseId = :firebaseId LIMIT 1")
    CourseEntity getCourseByFirebaseId(String firebaseId);

    // Xoá khoá học theo firebaseId (dùng khi xoá trên cloud)
    @Query("DELETE FROM courses WHERE firebaseId = :firebaseId")
    void deleteByFirebaseId(String firebaseId);

    // Xoá toàn bộ khoá học (dùng cho reset database)
    @Query("DELETE FROM courses")
    void deleteAllCourses();

    // Đánh dấu khoá học đã đồng bộ (isSynced=1) và cập nhật firebaseId
    @Query("UPDATE courses SET isSynced = 1, firebaseId = :firebaseId WHERE localId = :localId")
    void markCourseAsSynced(int localId, String firebaseId);
}
