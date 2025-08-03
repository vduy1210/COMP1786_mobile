
// DAO for 'class_instances' table in Room Database
package com.example.universalyogaapp.dao;

import androidx.room.*; // Room annotations
import com.example.universalyogaapp.db.ClassInstanceEntity; // Entity for class_instances
import java.util.List; // List for query results


@Dao // Mark this interface as DAO for Room
public interface ClassInstanceDao {

    // Insert new or update class instance (if primary key conflicts)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ClassInstanceEntity instance); // Returns the inserted localId

    // Update class instance information
    @Update
    void update(ClassInstanceEntity instance);

    // Delete a class instance
    @Delete
    void delete(ClassInstanceEntity instance);

    // Get instances by courseId on Firebase (for display)
    @Query("SELECT * FROM class_instances WHERE courseId = :courseFirebaseId")
    List<ClassInstanceEntity> getInstancesForCourse(String courseFirebaseId);

    // Get instances by courseLocalId (for internal processing)
    @Query("SELECT * FROM class_instances WHERE courseLocalId = :courseLocalId")
    List<ClassInstanceEntity> getInstancesForCourseByLocalId(int courseLocalId);

    // Get instances not synced to Firebase
    @Query("SELECT * FROM class_instances WHERE isSynced = 0")
    List<ClassInstanceEntity> getUnsyncedInstances();

    // Delete instance by firebaseId
    @Query("DELETE FROM class_instances WHERE firebaseId = :firebaseId")
    void deleteByFirebaseId(String firebaseId);

    // Delete instance by localId
    @Query("DELETE FROM class_instances WHERE id = :localId")
    void deleteByLocalId(int localId);

    // Get instance by firebaseId
    @Query("SELECT * FROM class_instances WHERE firebaseId = :firebaseId LIMIT 1")
    ClassInstanceEntity getInstanceByFirebaseId(String firebaseId);

    // Get instance by localId
    @Query("SELECT * FROM class_instances WHERE id = :localId LIMIT 1")
    ClassInstanceEntity getInstanceByLocalId(int localId);

    // Mark instance as synced (isSynced=1) and update firebaseId
    @Query("UPDATE class_instances SET isSynced = 1, firebaseId = :firebaseId WHERE id = :id")
    void markInstanceAsSynced(int id, String firebaseId);

    // Delete all instances (for database reset)
    @Query("DELETE FROM class_instances")
    void deleteAllInstances();

    // Delete instances by courseId on Firebase
    @Query("DELETE FROM class_instances WHERE courseId = :courseFirebaseId")
    void deleteInstancesByCourseFirebaseId(String courseFirebaseId);

    // Delete instances by courseLocalId
    @Query("DELETE FROM class_instances WHERE courseLocalId = :courseLocalId")
    void deleteInstancesByCourseLocalId(int courseLocalId);

    // Find instances with duplicate dates in the same course
    @Query("SELECT * FROM class_instances WHERE courseId = :courseId AND date = :date")
    List<ClassInstanceEntity> findDuplicatesByDate(String courseId, String date);

    // Delete duplicate instances (keep the record with smallest id)
    @Query("DELETE FROM class_instances WHERE id IN (SELECT id FROM class_instances WHERE courseId = :courseId AND date = :date AND id NOT IN (SELECT MIN(id) FROM class_instances WHERE courseId = :courseId AND date = :date))")
    void deleteDuplicateInstances(String courseId, String date);

    // Get information about duplicate instance groups (for cleanup)
    @Query("SELECT courseId, date, COUNT(*) as count FROM class_instances GROUP BY courseId, date HAVING COUNT(*) > 1")
    List<DuplicateInfo> getDuplicateInfo();

    // Static class for storing duplicate instance group information
    static class DuplicateInfo {
        public String courseId; // Course ID
        public String date; // Date
        public int count; // Number of duplicate records
    }

    // Search instances by teacher name (partial match, case insensitive)
    @Query("SELECT * FROM class_instances WHERE LOWER(teacher) LIKE '%' || LOWER(:teacher) || '%'")
    List<ClassInstanceEntity> searchByTeacher(String teacher);

    // Search instances by date
    @Query("SELECT * FROM class_instances WHERE date = :date")
    List<ClassInstanceEntity> searchByDate(String date);

    // To search by day of week, need to get all and filter in Java (date format: yyyy-MM-dd)
    // List<ClassInstanceEntity> getAllInstances(); // Already available through above functions

    // Get all class instances
    @Query("SELECT * FROM class_instances")
    List<ClassInstanceEntity> getAllClassInstances();
}