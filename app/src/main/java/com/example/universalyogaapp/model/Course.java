
// Model đại diện cho khoá học, dùng cho cả local và Firebase
package com.example.universalyogaapp.model;


public class Course {
    private String id; // ID trên Firebase
    private String courseId; // ID tự động tăng, đồng bộ với entity (dùng cho sync)
    private String name; // Tên khoá học
    private String schedule; // Lịch học (ví dụ: Mon, Wed, Fri - 18:00-19:00)
    private int capacity; // Sức chứa tối đa
    private double price; // Giá khoá học
    private int duration; // Thời lượng buổi học (phút)
    private String description; // Mô tả khoá học
    private String note;     // Ghi chú thêm
    private String time;     // Giờ học
    private String upcomingDate; // Ngày học tiếp theo (tính toán)
    private int localId; // ID local trong SQLite


    // Constructor rỗng (bắt buộc cho Firebase)
    public Course() {}


    // Constructor đầy đủ, dùng khi tạo mới hoặc mapping dữ liệu
    public Course(String id, String name, String schedule, String time, int capacity, double price, int duration, String description, String note, String upcomingDate, int localId) {
        this.id = id; // ID trên Firebase
        this.name = name; // Tên khoá học
        this.schedule = schedule; // Lịch học
        this.time = time; // Giờ học
        this.capacity = capacity; // Sức chứa
        this.price = price; // Giá
        this.duration = duration; // Thời lượng
        this.description = description; // Mô tả
        this.note = note; // Ghi chú
        this.upcomingDate = upcomingDate; // Ngày học tiếp theo
        this.localId = localId; // ID local
    }


    // Getter & Setter cho từng trường (bắt buộc cho Firebase, Room và adapter)
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSchedule() { return schedule; }
    public void setSchedule(String schedule) { this.schedule = schedule; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getUpcomingDate() { return upcomingDate; }
    public void setUpcomingDate(String upcomingDate) { this.upcomingDate = upcomingDate; }

    public int getLocalId() { return localId; }
    public void setLocalId(int localId) { this.localId = localId; }
} 