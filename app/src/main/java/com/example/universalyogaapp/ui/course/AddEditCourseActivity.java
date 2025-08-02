
// Activity cho chức năng thêm/sửa khoá học (Course)
// Activity for adding/editing course functionality
package com.example.universalyogaapp.ui.course;

// Import các thư viện cần thiết cho Activity này
// Import necessary libraries for this Activity
import android.os.Bundle; // Bundle để truyền dữ liệu - Bundle for passing data
import android.text.TextUtils; // Kiểm tra chuỗi rỗng - Check empty strings
import android.view.View; // View cơ bản - Basic view
import android.widget.Button; // Widget nút bấm - Button widget
import android.widget.EditText; // Widget nhập text - Text input widget
import android.widget.Toast; // Hiển thị thông báo - Display toast messages

import androidx.annotation.NonNull; // Annotation cho giá trị không null - NonNull annotation
import androidx.annotation.Nullable; // Annotation cho giá trị có thể null - Nullable annotation
import androidx.appcompat.app.AppCompatActivity; // Activity cơ bản của AppCompat - Base AppCompat activity
import androidx.appcompat.app.AlertDialog; // Dialog cảnh báo - Alert dialog

import com.example.universalyogaapp.R; // Resource layout - Layout resources
import com.example.universalyogaapp.firebase.FirebaseManager; // Quản lý đồng bộ Firebase - Firebase sync manager
import com.example.universalyogaapp.model.Course; // Model khoá học - Course model
import com.example.universalyogaapp.utils.DateUtils; // Tiện ích xử lý ngày tháng - Date utility functions
import com.google.android.material.chip.Chip; // Chip widget của Material Design - Material Design chip widget
import com.google.android.material.chip.ChipGroup; // Nhóm các chip - Group of chips
import com.google.android.material.textfield.TextInputEditText; // Text input với Material Design - Material Design text input
import com.google.firebase.database.DataSnapshot; // Snapshot dữ liệu Firebase - Firebase data snapshot
import com.google.firebase.database.DatabaseError; // Lỗi Firebase database - Firebase database error
import com.google.firebase.database.DatabaseReference; // Tham chiếu Firebase database - Firebase database reference
import com.google.firebase.database.ValueEventListener; // Listener cho sự kiện thay đổi giá trị - Value change event listener
import android.app.TimePickerDialog; // Dialog chọn thời gian - Time picker dialog
import java.util.Calendar; // Lịch - Calendar
import java.util.Arrays; // Tiện ích mảng - Array utilities
import java.util.List; // Interface danh sách - List interface
import java.util.stream.Collectors; // Thu thập stream - Stream collectors
import androidx.room.Room; // Room database builder - Room database builder
import com.example.universalyogaapp.db.AppDatabase; // Database ứng dụng - Application database
import com.example.universalyogaapp.db.CourseEntity; // Entity khoá học - Course entity
import android.net.ConnectivityManager; // Quản lý kết nối mạng - Network connectivity manager
import android.net.NetworkInfo; // Thông tin mạng - Network information

// Activity cho phép thêm hoặc sửa một khoá học
// Activity that allows adding or editing a course
public class AddEditCourseActivity extends AppCompatActivity {
    // Khai báo các biến UI components - Declare UI component variables
    private TextInputEditText editTextName, editTextTime, editTextCapacity, editTextPrice, editTextDuration, editTextDescription, editTextNote; // Các ô nhập liệu - Input fields
    private ChipGroup chipGroupSchedule; // Nhóm chip chọn ngày học - Chip group for selecting study days
    private Button buttonSave; // Nút lưu - Save button
    
    // Khai báo các biến dữ liệu và xử lý - Declare data and processing variables
    private FirebaseManager firebaseManager; // Quản lý đồng bộ Firebase - Firebase sync manager
    private Course editingCourse; // Nếu đang sửa, lưu course cần sửa - Course being edited if in edit mode
    private String courseId; // ID khoá học trên Firebase - Course ID on Firebase
    private AppDatabase db; // Room database - Room database instance

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Gọi phương thức cha - Call parent method
        setContentView(R.layout.activity_add_edit_course); // Gán layout cho Activity - Set layout for Activity

        // Khởi tạo database Room với migration - Initialize Room database with migration
        db = Room.databaseBuilder(
            getApplicationContext(), // Context ứng dụng - Application context
            AppDatabase.class, // Class database - Database class
            "yoga-db" // Tên database - Database name
        ).allowMainThreadQueries() // Cho phép query trên main thread - Allow main thread queries
                         .addMigrations(AppDatabase.MIGRATION_5_6) // Thêm migration từ version 5 lên 6 - Add migration from version 5 to 6
        .build(); // Xây dựng database - Build database

        // Khởi tạo các view từ layout - Initialize views from layout
        editTextName = findViewById(R.id.editTextName); // Tìm và gán ô nhập tên khoá học - Find and assign course name input field
        chipGroupSchedule = findViewById(R.id.chipGroupSchedule); // Tìm và gán nhóm chip lịch học - Find and assign schedule chip group
        editTextTime = findViewById(R.id.editTextTime); // Tìm và gán ô nhập giờ học - Find and assign time input field
        
        // Sự kiện chọn giờ học khi click - Time selection event when clicked
        editTextTime.setOnClickListener(new View.OnClickListener() { // Gán listener cho ô nhập giờ - Assign listener to time input field
            @Override
            public void onClick(View v) { // Khi click vào ô giờ - When time field is clicked
                showTimePicker(); // Hiển thị bộ chọn giờ - Show time picker
            }
        });
        
        // Sự kiện chọn giờ học khi focus - Time selection event when focused
        editTextTime.setOnFocusChangeListener(new View.OnFocusChangeListener() { // Gán listener focus cho ô nhập giờ - Assign focus listener to time input field
            @Override
            public void onFocusChange(View v, boolean hasFocus) { // Khi trạng thái focus thay đổi - When focus state changes
                if (hasFocus) { // Nếu được focus - If focused
                    showTimePicker(); // Hiển thị bộ chọn giờ - Show time picker
                }
            }
        });

        editTextCapacity = findViewById(R.id.editTextCapacity); // Tìm và gán ô nhập sức chứa - Find and assign capacity input field
        editTextPrice = findViewById(R.id.editTextPrice); // Tìm và gán ô nhập giá - Find and assign price input field
        editTextDuration = findViewById(R.id.editTextDuration); // Tìm và gán ô nhập thời lượng - Find and assign duration input field
        editTextDescription = findViewById(R.id.editTextDescription); // Tìm và gán ô nhập mô tả - Find and assign description input field
        editTextNote = findViewById(R.id.editTextNote); // Tìm và gán ô nhập ghi chú - Find and assign note input field
        buttonSave = findViewById(R.id.buttonSave); // Tìm và gán nút lưu - Find and assign save button

        firebaseManager = new FirebaseManager(); // Khởi tạo quản lý Firebase - Initialize Firebase manager

        // Lấy courseId từ Intent để xác định chế độ sửa/thêm - Get courseId from Intent to determine edit/add mode
        courseId = getIntent().getStringExtra("course_id"); // Lấy ID khoá học từ Intent - Get course ID from Intent
        if (courseId != null) { // Nếu có ID (chế độ sửa) - If ID exists (edit mode)
            setTitle("Edit Course"); // Đặt tiêu đề cho màn hình sửa - Set title for edit screen
            loadCourse(courseId); // Tải dữ liệu khoá học - Load course data
        } else { // Nếu không có ID (chế độ thêm) - If no ID (add mode)
            setTitle("Add New Course"); // Đặt tiêu đề cho màn hình thêm - Set title for add screen
        }

        // Sự kiện lưu khoá học - Save course event
        buttonSave.setOnClickListener(new View.OnClickListener() { // Gán listener cho nút lưu - Assign listener to save button
            @Override
            public void onClick(View v) { // Khi click nút lưu - When save button is clicked
                showConfirmDialog(); // Hiển thị dialog xác nhận - Show confirmation dialog
            }
        });
    }

    // Load dữ liệu khoá học từ Firebase để sửa - Load course data from Firebase for editing
    private void loadCourse(String id) {
        firebaseManager.getCourseById(id, new ValueEventListener() { // Lấy khoá học theo ID - Get course by ID
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) { // Khi dữ liệu thay đổi - When data changes
                editingCourse = snapshot.getValue(Course.class); // Chuyển đổi snapshot thành đối tượng Course - Convert snapshot to Course object
                if (editingCourse != null) { // Nếu có dữ liệu khoá học - If course data exists
                    editingCourse.setId(snapshot.getKey()); // Gán ID cho khoá học - Set ID for course
                    fillCourseData(editingCourse); // Đổ dữ liệu lên giao diện - Fill data to interface
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { // Khi có lỗi - When error occurs
                Toast.makeText(AddEditCourseActivity.this, "Error loading data", Toast.LENGTH_SHORT).show(); // Hiển thị thông báo lỗi - Show error message
            }
        });
    }

    // Đổ dữ liệu khoá học lên giao diện khi sửa - Fill course data to interface when editing
    private void fillCourseData(Course course) {
        editTextName.setText(course.getName()); // Đặt tên khoá học vào ô nhập - Set course name to input field

        // Đánh dấu các chip ngày học đã chọn - Mark selected study day chips
        String schedule = course.getSchedule(); // Lấy lịch học - Get schedule
        if (schedule != null && !schedule.isEmpty()) { // Nếu có lịch học - If schedule exists
            List<String> selectedDays = Arrays.asList(schedule.split(",")); // Chia lịch thành danh sách ngày - Split schedule into day list
            for (int i = 0; i < chipGroupSchedule.getChildCount(); i++) { // Duyệt qua tất cả chip - Iterate through all chips
                Chip chip = (Chip) chipGroupSchedule.getChildAt(i); // Lấy chip tại vị trí i - Get chip at position i
                if (selectedDays.contains(chip.getText().toString())) { // Nếu ngày này được chọn - If this day is selected
                    chip.setChecked(true); // Đánh dấu chip được chọn - Mark chip as checked
                }
            }
        }

        editTextTime.setText(course.getTime()); // Đặt giờ học vào ô nhập - Set time to input field

        editTextCapacity.setText(String.valueOf(course.getCapacity())); // Chuyển số thành chuỗi và đặt vào ô sức chứa - Convert number to string and set to capacity field
        editTextPrice.setText(String.valueOf(course.getPrice())); // Chuyển số thành chuỗi và đặt vào ô giá - Convert number to string and set to price field
        editTextDuration.setText(String.valueOf(course.getDuration())); // Chuyển số thành chuỗi và đặt vào ô thời lượng - Convert number to string and set to duration field
        editTextDescription.setText(course.getDescription()); // Đặt mô tả vào ô nhập - Set description to input field
        editTextNote.setText(course.getNote()); // Đặt ghi chú vào ô nhập - Set note to input field
    }

    // Kiểm tra kết nối mạng - Check network connectivity
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE); // Lấy service quản lý kết nối - Get connectivity manager service
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo(); // Lấy thông tin mạng đang hoạt động - Get active network info
        return activeNetworkInfo != null && activeNetworkInfo.isConnected(); // Trả về true nếu có mạng - Return true if network is available
    }

    // Hiển thị dialog xác nhận trước khi lưu khoá học - Show confirmation dialog before saving course
    private void showConfirmDialog() {
        String name = editTextName.getText().toString().trim(); // Lấy tên khoá học từ ô nhập - Get course name from input field
        List<String> selectedChips = new java.util.ArrayList<>(); // Khởi tạo danh sách ngày đã chọn - Initialize selected days list
        for (int id : chipGroupSchedule.getCheckedChipIds()) { // Duyệt qua các chip được chọn - Iterate through checked chips
            Chip chip = chipGroupSchedule.findViewById(id); // Tìm chip theo ID - Find chip by ID
            selectedChips.add(chip.getText().toString()); // Thêm text của chip vào danh sách - Add chip text to list
        }
        String schedule = String.join(",", selectedChips); // Nối các ngày bằng dấu phẩy - Join days with comma
        String upcomingDate = DateUtils.getNextUpcomingDate(schedule); // Tính ngày học tiếp theo - Calculate next upcoming date
        String time = editTextTime.getText().toString().trim(); // Lấy giờ học từ ô nhập - Get time from input field
        String capacityStr = editTextCapacity.getText().toString().trim(); // Lấy sức chứa từ ô nhập - Get capacity from input field
        String priceStr = editTextPrice.getText().toString().trim(); // Lấy giá từ ô nhập - Get price from input field
        String durationStr = editTextDuration.getText().toString().trim(); // Lấy thời lượng từ ô nhập - Get duration from input field
        String description = editTextDescription.getText().toString().trim(); // Lấy mô tả từ ô nhập - Get description from input field
        String note = editTextNote.getText().toString().trim(); // Lấy ghi chú từ ô nhập - Get note from input field
        
        // Kiểm tra dữ liệu đầu vào - Validate input data
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(schedule) || // Nếu tên hoặc lịch trống - If name or schedule is empty
                TextUtils.isEmpty(capacityStr) || TextUtils.isEmpty(priceStr) || // Nếu sức chứa hoặc giá trống - If capacity or price is empty
                TextUtils.isEmpty(durationStr)) { // Nếu thời lượng trống - If duration is empty
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show(); // Hiển thị thông báo lỗi - Show error message
            return; // Dừng xử lý - Stop processing
        }
        
        // Tạo message xác nhận - Create confirmation message
        StringBuilder message = new StringBuilder(); // Khởi tạo StringBuilder - Initialize StringBuilder
        message.append("Name: ").append(name).append("\n"); // Thêm tên khoá học - Add course name
        message.append("Schedule: ").append(schedule).append("\n"); // Thêm lịch học - Add schedule
        message.append("Time: ").append(time).append("\n"); // Thêm giờ học - Add time
        message.append("Capacity: ").append(capacityStr).append("\n"); // Thêm sức chứa - Add capacity
        message.append("Price: ").append(priceStr).append("\n"); // Thêm giá - Add price
        message.append("Duration: ").append(durationStr).append("\n"); // Thêm thời lượng - Add duration
        message.append("Description: ").append(description).append("\n"); // Thêm mô tả - Add description
        message.append("Note: ").append(note).append("\n"); // Thêm ghi chú - Add note
        
        new AlertDialog.Builder(this) // Tạo dialog xác nhận - Create confirmation dialog
            .setTitle("Confirm Course Details") // Đặt tiêu đề - Set title
            .setMessage(message.toString()) // Đặt nội dung - Set message
            .setPositiveButton("Confirm", (dialog, which) -> saveCourse()) // Nút xác nhận - Confirm button
            .setNegativeButton("Edit", null) // Nút sửa (không làm gì) - Edit button (do nothing)
            .show(); // Hiển thị dialog - Show dialog
    }

    // Lưu khoá học mới hoặc cập nhật khoá học cũ - Save new course or update existing course
    private void saveCourse() {
        String name = editTextName.getText().toString().trim(); // Lấy tên khoá học - Get course name
        List<String> selectedChips = new java.util.ArrayList<>(); // Khởi tạo danh sách các ngày học đã chọn - Initialize selected study days list
        for (int id : chipGroupSchedule.getCheckedChipIds()) { // Duyệt qua các chip được chọn - Iterate through checked chips
            Chip chip = chipGroupSchedule.findViewById(id); // Tìm chip theo ID - Find chip by ID
            selectedChips.add(chip.getText().toString()); // Thêm text chip vào danh sách - Add chip text to list
        }
        String schedule = String.join(",", selectedChips); // Nối các ngày thành chuỗi lịch học - Join days into schedule string
        String upcomingDate = DateUtils.getNextUpcomingDate(schedule); // Tính ngày học tiếp theo - Calculate next upcoming date
        String time = editTextTime.getText().toString().trim(); // Lấy giờ học - Get time
        String capacityStr = editTextCapacity.getText().toString().trim(); // Lấy sức chứa dạng chuỗi - Get capacity as string
        String priceStr = editTextPrice.getText().toString().trim(); // Lấy giá dạng chuỗi - Get price as string
        String durationStr = editTextDuration.getText().toString().trim(); // Lấy thời lượng dạng chuỗi - Get duration as string
        String description = editTextDescription.getText().toString().trim(); // Lấy mô tả - Get description
        String note = editTextNote.getText().toString().trim(); // Lấy ghi chú - Get note
        
        // Chuyển đổi chuỗi thành số - Convert strings to numbers
        int capacity = Integer.parseInt(capacityStr); // Chuyển sức chứa thành số nguyên - Convert capacity to integer
        double price = Double.parseDouble(priceStr); // Chuyển giá thành số thực - Convert price to double
        int duration = Integer.parseInt(durationStr); // Chuyển thời lượng thành số nguyên - Convert duration to integer

        if (editingCourse != null) { // Nếu đang sửa khoá học - If editing existing course
            // Nếu đang sửa khoá học - If editing existing course
            Course course = new Course( // Tạo đối tượng Course mới - Create new Course object
                editingCourse.getId(), name, schedule, time, // ID cũ, tên mới, lịch mới, giờ mới - Old ID, new name, new schedule, new time
                capacity, price, duration, description, note, upcomingDate, editingCourse.getLocalId() // Các thông tin mới - New information
            );

            if (isNetworkAvailable()) { // Nếu có kết nối mạng - If network is available
                // Nếu có mạng: cập nhật Firebase trước, sau đó local - If online: update Firebase first, then local
                DatabaseReference.CompletionListener listener = (error, ref) -> { // Listener xử lý kết quả - Result handling listener
                    if (error == null) { // Nếu không có lỗi - If no error
                        // Cập nhật database local - Update local database
                        CourseEntity entity = new CourseEntity(); // Tạo entity mới - Create new entity
                        entity.localId = editingCourse.getLocalId(); // Gán local ID cũ - Set old local ID
                        entity.firebaseId = editingCourse.getId(); // Gán Firebase ID cũ - Set old Firebase ID
                        entity.name = name; // Gán tên mới - Set new name
                        entity.schedule = schedule; // Gán lịch mới - Set new schedule
                        entity.time = time; // Gán giờ mới - Set new time
                        entity.capacity = capacity; // Gán sức chứa mới - Set new capacity
                        entity.price = price; // Gán giá mới - Set new price
                        entity.duration = duration; // Gán thời lượng mới - Set new duration
                        entity.description = description; // Gán mô tả mới - Set new description
                        entity.note = note; // Gán ghi chú mới - Set new note
                        entity.upcomingDate = upcomingDate; // Gán ngày học tiếp theo - Set next upcoming date
                        entity.isSynced = true; // Đánh dấu đã đồng bộ - Mark as synced

                        db.courseDao().update(entity); // Cập nhật vào database - Update to database
                        runOnUiThread(() -> { // Chạy trên UI thread - Run on UI thread
                            Toast.makeText(AddEditCourseActivity.this, "Course updated and synced!", Toast.LENGTH_SHORT).show(); // Hiển thị thông báo thành công - Show success message
                            finish(); // Đóng Activity - Close Activity
                        });
                    } else { // Nếu có lỗi - If error occurs
                        runOnUiThread(() -> { // Chạy trên UI thread - Run on UI thread
                            Toast.makeText(AddEditCourseActivity.this, "Failed to sync with server, saved locally.", Toast.LENGTH_SHORT).show(); // Hiển thị thông báo thất bại - Show failure message
                            finish(); // Đóng Activity - Close Activity
                        });
                    }
                };
                firebaseManager.updateCourse(course, listener); // Cập nhật lên Firebase - Update to Firebase
            } else { // Nếu không có mạng - If no network
                // Nếu offline: chỉ cập nhật local - If offline: only update locally
                CourseEntity entity = new CourseEntity(); // Tạo entity mới - Create new entity
                entity.localId = editingCourse.getLocalId(); // Gán local ID cũ - Set old local ID
                entity.firebaseId = editingCourse.getId(); // Gán Firebase ID cũ - Set old Firebase ID
                entity.name = name; // Gán tên mới - Set new name
                entity.schedule = schedule; // Gán lịch mới - Set new schedule
                entity.time = time; // Gán giờ mới - Set new time
                entity.capacity = capacity; // Gán sức chứa mới - Set new capacity
                entity.price = price; // Gán giá mới - Set new price
                entity.duration = duration; // Gán thời lượng mới - Set new duration
                entity.description = description; // Gán mô tả mới - Set new description
                entity.note = note; // Gán ghi chú mới - Set new note
                entity.upcomingDate = upcomingDate; // Gán ngày học tiếp theo - Set next upcoming date
                entity.isSynced = false; // Đánh dấu chưa đồng bộ - Mark as not synced

                db.courseDao().update(entity); // Cập nhật vào database local - Update to local database
                Toast.makeText(AddEditCourseActivity.this, "Course updated locally. Please sync to upload.", Toast.LENGTH_SHORT).show(); // Hiển thị thông báo - Show message
                finish(); // Đóng Activity - Close Activity
            }
        } else { // Nếu là thêm mới khoá học - If adding new course
            // Nếu là thêm mới khoá học - If adding new course
            CourseEntity entity = new CourseEntity(); // Tạo entity mới - Create new entity
            entity.name = name; // Gán tên - Set name
            entity.schedule = schedule; // Gán lịch học - Set schedule
            entity.time = time; // Gán giờ học - Set time
            entity.capacity = capacity; // Gán sức chứa - Set capacity
            entity.price = price; // Gán giá - Set price
            entity.duration = duration; // Gán thời lượng - Set duration
            entity.description = description; // Gán mô tả - Set description
            entity.note = note; // Gán ghi chú - Set note
            entity.upcomingDate = upcomingDate; // Gán ngày học tiếp theo - Set next upcoming date

            if (isNetworkAvailable()) { // Nếu có kết nối mạng - If network is available
                // Nếu có mạng: lưu local với isSynced=true, đẩy lên Firebase - If online: save locally with isSynced=true, push to Firebase
                entity.isSynced = true; // Đánh dấu đã đồng bộ - Mark as synced
                long localId = db.courseDao().insert(entity); // Lưu vào database và lấy local ID - Save to database and get local ID
                // Gán courseId = localId (tự động tăng, duy nhất) - Set courseId = localId (auto-increment, unique)
                entity.courseId = String.valueOf(localId); // Chuyển local ID thành chuỗi courseId - Convert local ID to courseId string
                db.courseDao().update(entity); // Cập nhật entity với courseId - Update entity with courseId
                
                Course course = new Course( // Tạo đối tượng Course cho Firebase - Create Course object for Firebase
                    entity.firebaseId, entity.name, entity.schedule, entity.time, // Firebase ID, tên, lịch, giờ - Firebase ID, name, schedule, time
                    entity.capacity, entity.price, entity.duration, entity.description, entity.note, entity.upcomingDate, (int) localId // Các thông tin khác và local ID - Other info and local ID
                );
                course.setCourseId(entity.courseId); // Gán courseId cho đối tượng Course - Set courseId for Course object
                
                DatabaseReference.CompletionListener listener = (error, ref) -> { // Listener xử lý kết quả Firebase - Firebase result handling listener
                    if (error == null) { // Nếu thành công - If successful
                        db.courseDao().markCourseAsSynced((int) localId, ref.getKey()); // Đánh dấu đã sync và cập nhật Firebase ID - Mark as synced and update Firebase ID
                        runOnUiThread(() -> { // Chạy trên UI thread - Run on UI thread
                            Toast.makeText(AddEditCourseActivity.this, "Course saved and synced!", Toast.LENGTH_SHORT).show(); // Hiển thị thông báo thành công - Show success message
                            finish(); // Đóng Activity - Close Activity
                        });
                    } else { // Nếu thất bại - If failed
                        runOnUiThread(() -> { // Chạy trên UI thread - Run on UI thread
                            Toast.makeText(AddEditCourseActivity.this, "Failed to sync with server, saved locally.", Toast.LENGTH_SHORT).show(); // Hiển thị thông báo thất bại - Show failure message
                            finish(); // Đóng Activity - Close Activity
                        });
                    }
                };
                
                if (entity.firebaseId == null || entity.firebaseId.isEmpty()) { // Nếu chưa có Firebase ID - If no Firebase ID yet
                    firebaseManager.addCourse(course, listener); // Thêm mới lên Firebase - Add new to Firebase
                } else { // Nếu đã có Firebase ID - If Firebase ID exists
                    course.setId(entity.firebaseId); // Gán Firebase ID - Set Firebase ID
                    firebaseManager.updateCourse(course, listener); // Cập nhật lên Firebase - Update to Firebase
                }
            } else { // Nếu không có mạng - If no network
                // Nếu offline: lưu local với isSynced=false - If offline: save locally with isSynced=false
                entity.isSynced = false; // Đánh dấu chưa đồng bộ - Mark as not synced
                long localId = db.courseDao().insert(entity); // Lưu vào database local - Save to local database
                entity.courseId = String.valueOf(localId); // Gán courseId = localId - Set courseId = localId
                db.courseDao().update(entity); // Cập nhật entity - Update entity
                Toast.makeText(AddEditCourseActivity.this, "Course saved locally. Please sync to upload.", Toast.LENGTH_SHORT).show(); // Hiển thị thông báo - Show message
                finish(); // Đóng Activity - Close Activity
            }
        }
    }

    // Hiển thị TimePicker để chọn giờ học - Display TimePicker for time selection
    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance(); // Lấy lịch hiện tại - Get current calendar
        int hour = calendar.get(Calendar.HOUR_OF_DAY); // Lấy giờ hiện tại (24h) - Get current hour (24h format)
        int minute = calendar.get(Calendar.MINUTE); // Lấy phút hiện tại - Get current minute
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute1) -> { // Tạo dialog chọn thời gian - Create time picker dialog
            String time = String.format("%02d:%02d", hourOfDay, minute1); // Định dạng thời gian HH:mm - Format time as HH:mm
            editTextTime.setText(time); // Đặt thời gian đã chọn vào ô nhập - Set selected time to input field
        }, hour, minute, true); // Khởi tạo với thời gian hiện tại, định dạng 24h - Initialize with current time, 24h format
        timePickerDialog.show(); // Hiển thị dialog - Show dialog
    }
}