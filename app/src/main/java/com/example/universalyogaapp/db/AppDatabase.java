
// Lớp quản lý Room Database cho toàn bộ app (singleton)
package com.example.universalyogaapp.db;


import androidx.room.Database; // Annotation định nghĩa database, version, entity
import androidx.room.RoomDatabase; // Lớp cha cho database
import androidx.room.migration.Migration; // Hỗ trợ migration khi thay đổi schema
import androidx.sqlite.db.SupportSQLiteDatabase; // SQLite database cho migration


import com.example.universalyogaapp.dao.CourseDao; // DAO cho courses
import com.example.universalyogaapp.dao.ClassInstanceDao; // DAO cho class_instances
import com.example.universalyogaapp.db.ClassInstanceEntity; // Entity cho class_instances

@Database(entities = {CourseEntity.class, ClassInstanceEntity.class}, version = 8) // Định nghĩa entity và version
public abstract class AppDatabase extends RoomDatabase {
    // Migration từ version 7 lên 8: Đổi tên cột courseId thành courseid cho đồng bộ với entity
    public static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Room không hỗ trợ ALTER COLUMN, nên phải tạo bảng mới rồi copy dữ liệu
            database.execSQL("CREATE TABLE courses_new (localId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, firebaseId TEXT, courseid TEXT, name TEXT, schedule TEXT, time TEXT, description TEXT, note TEXT, upcomingDate TEXT, capacity INTEGER NOT NULL, duration INTEGER NOT NULL, price REAL NOT NULL, isSynced INTEGER NOT NULL)");
            database.execSQL("INSERT INTO courses_new (localId, firebaseId, courseid, name, schedule, time, description, note, upcomingDate, capacity, duration, price, isSynced) SELECT localId, firebaseId, courseId, name, schedule, time, description, note, upcomingDate, capacity, duration, price, isSynced FROM courses");
            database.execSQL("DROP TABLE courses");
            database.execSQL("ALTER TABLE courses_new RENAME TO courses");
        }
    };
    // Migration từ version 6 lên 7: Thêm cột courseId vào bảng courses
    public static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE courses ADD COLUMN courseId TEXT");
        }
    };

    // Trả về DAO cho courses
    public abstract CourseDao courseDao();
    // Trả về DAO cho class_instances
    public abstract ClassInstanceDao classInstanceDao();


    // Migration từ version 5 lên 6: Xoá cột teacher khỏi bảng courses
    public static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Tạo bảng mới không có cột teacher
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

            // Copy dữ liệu từ bảng cũ sang bảng mới (bỏ cột teacher)
            database.execSQL("INSERT INTO courses_new (localId, firebaseId, name, schedule, time, description, note, upcomingDate, capacity, duration, price, isSynced) " +
                    "SELECT localId, firebaseId, name, schedule, time, description, note, upcomingDate, capacity, duration, price, isSynced FROM courses");

            // Xoá bảng cũ
            database.execSQL("DROP TABLE courses");

            // Đổi tên bảng mới thành bảng gốc
            database.execSQL("ALTER TABLE courses_new RENAME TO courses");
        }
    };


    // Singleton instance cho database
    private static AppDatabase instance;

    // Hàm lấy instance duy nhất của database, đảm bảo chỉ tạo 1 lần
    public static synchronized AppDatabase getInstance(android.content.Context context) {
        if (instance == null) {
            instance = androidx.room.Room.databaseBuilder(
                context.getApplicationContext(), // Context app
                AppDatabase.class, // Class database
                "yoga-db" // Tên file database
            ).allowMainThreadQueries() // Cho phép query trên main thread (không khuyến khích cho production)
             .addMigrations(AppDatabase.MIGRATION_5_6, AppDatabase.MIGRATION_6_7, AppDatabase.MIGRATION_7_8) // Thêm các migration
            .build();
        }
        return instance;
    }
}