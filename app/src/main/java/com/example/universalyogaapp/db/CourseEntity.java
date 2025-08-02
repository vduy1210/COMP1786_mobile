
// Entity đại diện cho bảng 'courses' trong Room Database
package com.example.universalyogaapp.db;


import androidx.room.Entity; // Annotation đánh dấu class là một entity của Room
import androidx.room.PrimaryKey; // Annotation đánh dấu trường là primary key
import androidx.room.Index;
import androidx.room.Ignore; // Annotation để Room bỏ qua constructor này khi mapping

@Entity(tableName = "courses") // Đặt tên bảng là 'courses'

// Lớp entity cho bảng 'courses', lưu thông tin khoá học
public class CourseEntity {
    @PrimaryKey(autoGenerate = true) // localId là khoá chính, tự động tăng
    public int localId; // ID tự động tăng trong SQLite, duy nhất cho mỗi bản ghi


    public String firebaseId; // ID trên Firebase, null nếu chưa sync

    @androidx.room.ColumnInfo(name = "courseid") // Ánh xạ với cột 'courseid' trong bảng
    public String courseId; // ID dùng chung cho local & cloud, thường = localId khi mới tạo

    // Thông tin khoá học
    public String name; // Tên khoá học
    public String schedule; // Lịch học (ví dụ: Monday,Tuesday)
    public String time; // Giờ học (ví dụ: 08:00)
    public String description; // Mô tả khoá học
    public String note; // Ghi chú thêm
    public String upcomingDate; // Ngày học tiếp theo (dùng cho hiển thị)

    public int capacity; // Số lượng tối đa học viên
    public int duration; // Thời lượng buổi học (phút)
    public double price; // Giá khoá học
    public boolean isSynced; // Đã đồng bộ với Firebase chưa


    // Constructor mặc định (bắt buộc cho Room)
    public CourseEntity() {}

    // Constructor đầy đủ, dùng khi tạo mới hoặc mapping dữ liệu
    @Ignore
    public CourseEntity(int localId, String firebaseId, String courseId, String name, String schedule, String time, String description, String note, String upcomingDate, int capacity, int duration, double price, boolean isSynced) {
        this.localId = localId; // ID local
        this.firebaseId = firebaseId; // ID trên Firebase
        this.courseId = courseId; // ID dùng chung
        this.name = name; // Tên khoá học
        this.schedule = schedule; // Lịch học
        this.time = time; // Giờ học
        this.description = description; // Mô tả
        this.note = note; // Ghi chú
        this.upcomingDate = upcomingDate; // Ngày học tiếp theo
        this.capacity = capacity; // Sức chứa
        this.duration = duration; // Thời lượng
        this.price = price; // Giá
        this.isSynced = isSynced; // Trạng thái đồng bộ
    }

    // Getter & Setter cho từng trường (bắt buộc cho Room và adapter)
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public int getLocalId() { return localId; }
    public void setLocalId(int localId) { this.localId = localId; }

    public String getFirebaseId() { return firebaseId; }
    public void setFirebaseId(String firebaseId) { this.firebaseId = firebaseId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSchedule() { return schedule; }
    public void setSchedule(String schedule) { this.schedule = schedule; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getUpcomingDate() { return upcomingDate; }
    public void setUpcomingDate(String upcomingDate) { this.upcomingDate = upcomingDate; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public boolean isSynced() { return isSynced; }
    public void setSynced(boolean synced) { isSynced = synced; }
}