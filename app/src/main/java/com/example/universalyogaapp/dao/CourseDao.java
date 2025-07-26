package com.example.universalyogaapp.dao;
import com.example.universalyogaapp.db.CourseEntity;
import androidx.room.*;
import java.util.List;

@Dao
public interface CourseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CourseEntity course);

    @Update
    void update(CourseEntity course);

    @Delete
    void delete(CourseEntity course);

    @Query("SELECT * FROM courses WHERE isSynced = 0")
    List<CourseEntity> getUnsyncedCourses();

    @Query("SELECT * FROM courses")
    List<CourseEntity> getAllCourses();

    @Query("DELETE FROM courses WHERE firebaseId = :firebaseId")
    void deleteByFirebaseId(String firebaseId);

    @Query("DELETE FROM courses")
    void deleteAllCourses();

    @Query("UPDATE courses SET isSynced = 1, firebaseId = :firebaseId WHERE localId = :localId")
    void markCourseAsSynced(int localId, String firebaseId);
}
