
// Entity đại diện cho bảng 'class_instances' trong Room Database
package com.example.universalyogaapp.db;


import androidx.room.Entity; // Annotation đánh dấu class là một entity của Room
import androidx.room.PrimaryKey; // Annotation đánh dấu trường là primary key
import androidx.room.Index;
import androidx.room.Ignore; // Annotation để Room bỏ qua constructor này khi mapping


@Entity(tableName = "class_instances") // Đặt tên bảng là 'class_instances'
public class ClassInstanceEntity {
    @PrimaryKey(autoGenerate = true) // id là khoá chính, tự động tăng
    public int id; // ID tự động tăng trong SQLite, duy nhất cho mỗi bản ghi

    public String courseId; // Firebase ID của khoá học (dùng cho hiển thị và đồng bộ)
    public int courseLocalId; // Local ID của khoá học (dùng cho xử lý nội bộ)
    public String firebaseId; // ID trên Firebase, null nếu chưa sync
    public String date; // Ngày học cụ thể (định dạng yyyy-MM-dd)
    public String teacher; // Tên giáo viên
    public String note; // Ghi chú thêm cho buổi học
    public boolean isSynced; // Đã đồng bộ với Firebase chưa

    // Constructor mặc định (bắt buộc cho Room)
    public ClassInstanceEntity() {}

    // Constructor đầy đủ, dùng khi tạo mới hoặc mapping dữ liệu
    @Ignore
    public ClassInstanceEntity(int id, String courseId, int courseLocalId, String firebaseId, String date, String teacher, String note, boolean isSynced) {
        this.id = id; // ID local
        this.courseId = courseId; // Firebase ID khoá học
        this.courseLocalId = courseLocalId; // Local ID khoá học
        this.firebaseId = firebaseId; // ID trên Firebase
        this.date = date; // Ngày học
        this.teacher = teacher; // Giáo viên
        this.note = note; // Ghi chú
        this.isSynced = isSynced; // Trạng thái đồng bộ
    }

    // Getter & Setter cho từng trường (bắt buộc cho Room và adapter)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public int getCourseLocalId() { return courseLocalId; }
    public void setCourseLocalId(int courseLocalId) { this.courseLocalId = courseLocalId; }

    public String getFirebaseId() { return firebaseId; }
    public void setFirebaseId(String firebaseId) { this.firebaseId = firebaseId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTeacher() { return teacher; }
    public void setTeacher(String teacher) { this.teacher = teacher; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public boolean isSynced() { return isSynced; }
    public void setSynced(boolean synced) { isSynced = synced; }
}