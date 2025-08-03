
// DAO (Data Access Object) for 'courses' table in Room Database
package com.example.universalyogaapp.dao;

import com.example.universalyogaapp.db.CourseEntity; // Entity representing courses table
import androidx.room.*; // Room annotations (Insert, Update, Delete, Query, Dao)
import java.util.List; // List for query results


@Dao // Mark this interface as DAO for Room
public interface CourseDao {

    // Insert new or update course (if primary key conflicts)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CourseEntity course); // Returns the inserted localId

    // Update course information
    @Update
    void update(CourseEntity course);

    // Delete a course
    @Delete
    void delete(CourseEntity course);

    // Get list of courses not synced to Firebase
    @Query("SELECT * FROM courses WHERE isSynced = 0")
    List<CourseEntity> getUnsyncedCourses();

    // Get all courses
    @Query("SELECT * FROM courses")
    List<CourseEntity> getAllCourses();

    // Get course by firebaseId (for synchronization)
    @Query("SELECT * FROM courses WHERE firebaseId = :firebaseId LIMIT 1")
    CourseEntity getCourseByFirebaseId(String firebaseId);

    // Delete course by firebaseId (when deleting from cloud)
    @Query("DELETE FROM courses WHERE firebaseId = :firebaseId")
    void deleteByFirebaseId(String firebaseId);

    // Delete all courses (for database reset)
    @Query("DELETE FROM courses")
    void deleteAllCourses();

    // Mark course as synced (isSynced=1) and update firebaseId
    @Query("UPDATE courses SET isSynced = 1, firebaseId = :firebaseId WHERE localId = :localId")
    void markCourseAsSynced(int localId, String firebaseId);
}
