
// Room Database management class for the entire app (singleton)
package com.example.universalyogaapp.db;


import androidx.room.Database; // Annotation to define database, version, entity
import androidx.room.RoomDatabase; // Parent class for database
import androidx.room.migration.Migration; // Support migration when schema changes
import androidx.sqlite.db.SupportSQLiteDatabase; // SQLite database for migration


import com.example.universalyogaapp.dao.CourseDao; // DAO for courses
import com.example.universalyogaapp.dao.ClassInstanceDao; // DAO for class_instances
import com.example.universalyogaapp.db.ClassInstanceEntity; // Entity for class_instances

@Database(entities = {CourseEntity.class, ClassInstanceEntity.class}, version = 8) // Define entity and version
public abstract class AppDatabase extends RoomDatabase {
    // Migration from version 7 to 8: Rename courseId column to courseid for entity synchronization
    public static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Room doesn't support ALTER COLUMN, so need to create new table and copy data
            database.execSQL("CREATE TABLE courses_new (localId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, firebaseId TEXT, courseid TEXT, name TEXT, schedule TEXT, time TEXT, description TEXT, note TEXT, upcomingDate TEXT, capacity INTEGER NOT NULL, duration INTEGER NOT NULL, price REAL NOT NULL, isSynced INTEGER NOT NULL)");
            database.execSQL("INSERT INTO courses_new (localId, firebaseId, courseid, name, schedule, time, description, note, upcomingDate, capacity, duration, price, isSynced) SELECT localId, firebaseId, courseId, name, schedule, time, description, note, upcomingDate, capacity, duration, price, isSynced FROM courses");
            database.execSQL("DROP TABLE courses");
            database.execSQL("ALTER TABLE courses_new RENAME TO courses");
        }
    };
    // Migration from version 6 to 7: Add courseId column to courses table
    public static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE courses ADD COLUMN courseId TEXT");
        }
    };

    // Return DAO for courses
    public abstract CourseDao courseDao();
    // Return DAO for class_instances
    public abstract ClassInstanceDao classInstanceDao();


    // Migration from version 5 to 6: Remove teacher column from courses table
    public static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Create new table without teacher column
            database.execSQL("CREATE TABLE courses_new (" +
                    "localId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "firebaseId TEXT, " +
                    "name TEXT, " +
                    "schedule TEXT, " +
                    "time TEXT, " +
                    "description TEXT, " +
                    "note TEXT, " +
                    "upcomingDate TEXT, " +
                    "capacity INTEGER NOT NULL, " +
                    "duration INTEGER NOT NULL, " +
                    "price REAL NOT NULL, " +
                    "isSynced INTEGER NOT NULL)");

            // Copy data from old table to new table (exclude teacher column)
            database.execSQL("INSERT INTO courses_new (localId, firebaseId, name, schedule, time, description, note, upcomingDate, capacity, duration, price, isSynced) " +
                    "SELECT localId, firebaseId, name, schedule, time, description, note, upcomingDate, capacity, duration, price, isSynced FROM courses");

            // Drop old table
            database.execSQL("DROP TABLE courses");

            // Rename new table to original table name
            database.execSQL("ALTER TABLE courses_new RENAME TO courses");
        }
    };


    // Singleton instance for database
    private static AppDatabase instance;

    // Function to get unique database instance, ensuring only one creation
    public static synchronized AppDatabase getInstance(android.content.Context context) {
        if (instance == null) {
            instance = androidx.room.Room.databaseBuilder(
                context.getApplicationContext(), // App context
                AppDatabase.class, // Database class
                "yoga-db" // Database file name
            ).allowMainThreadQueries() // Allow queries on main thread (not recommended for production)
             .addMigrations(AppDatabase.MIGRATION_5_6, AppDatabase.MIGRATION_6_7, AppDatabase.MIGRATION_7_8) // Add migrations
            .build();
        }
        return instance;
    }
}