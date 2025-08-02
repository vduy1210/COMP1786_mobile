
// Activity hiển thị danh sách khoá học, thống kê, tìm kiếm, đồng bộ dữ liệu
// Activity to display course list, statistics, search, and data synchronization
package com.example.universalyogaapp.ui.course; // Khai báo package - Package declaration

import android.content.Intent; // Import Intent để chuyển màn hình - Import Intent for screen navigation
import android.os.Bundle; // Import Bundle để truyền dữ liệu - Import Bundle for data passing
import android.text.Editable; // Import Editable cho TextWatcher - Import Editable for TextWatcher
import android.text.TextWatcher; // Import TextWatcher để lắng nghe thay đổi text - Import TextWatcher to listen for text changes
import android.view.View; // Import View cho UI components - Import View for UI components
import android.widget.TextView; // Import TextView để hiển thị text - Import TextView for text display
import android.widget.Toast; // Import Toast để hiển thị thông báo - Import Toast for notifications
import android.widget.Button; // Import Button cho các nút bấm - Import Button for clickable buttons
import android.app.AlertDialog; // Import AlertDialog cho dialog xác nhận - Import AlertDialog for confirmation dialogs

import androidx.annotation.NonNull; // Import NonNull annotation - Import NonNull annotation
import androidx.appcompat.app.AppCompatActivity; // Import AppCompatActivity làm lớp cha - Import AppCompatActivity as parent class
import androidx.recyclerview.widget.LinearLayoutManager; // Import LinearLayoutManager cho RecyclerView - Import LinearLayoutManager for RecyclerView
import androidx.recyclerview.widget.RecyclerView; // Import RecyclerView để hiển thị danh sách - Import RecyclerView for list display

import com.example.universalyogaapp.R; // Import resource file - Import resource file
import com.example.universalyogaapp.firebase.FirebaseManager; // Import FirebaseManager để đồng bộ - Import FirebaseManager for synchronization
import com.example.universalyogaapp.model.Course; // Import model Course - Import Course model
import com.google.android.material.button.MaterialButton; // Import MaterialButton cho UI đẹp - Import MaterialButton for beautiful UI
import com.google.android.material.textfield.TextInputEditText; // Import TextInputEditText cho input field - Import TextInputEditText for input field
import com.google.firebase.database.DataSnapshot; // Import DataSnapshot để đọc dữ liệu Firebase - Import DataSnapshot for Firebase data reading
import com.google.firebase.database.DatabaseError; // Import DatabaseError để xử lý lỗi - Import DatabaseError for error handling
import com.google.firebase.database.ValueEventListener; // Import ValueEventListener để lắng nghe Firebase - Import ValueEventListener for Firebase listening

import java.util.ArrayList; // Import ArrayList cho danh sách - Import ArrayList for lists
import java.util.List; // Import List interface - Import List interface
import androidx.room.Room; // Import Room database builder - Import Room database builder
import com.example.universalyogaapp.db.AppDatabase; // Import AppDatabase cho Room - Import AppDatabase for Room
import com.example.universalyogaapp.db.CourseEntity; // Import CourseEntity cho Room - Import CourseEntity for Room
import com.example.universalyogaapp.db.ClassInstanceEntity; // Import ClassInstanceEntity cho Room - Import ClassInstanceEntity for Room
import com.example.universalyogaapp.dao.ClassInstanceDao; // Import DAO cho buổi học - Import DAO for class instances
import com.example.universalyogaapp.model.ClassInstance; // Import model ClassInstance - Import ClassInstance model
import java.text.ParseException; // Import ParseException để xử lý lỗi parse - Import ParseException for parse error handling
import java.text.SimpleDateFormat; // Import SimpleDateFormat để format ngày - Import SimpleDateFormat for date formatting
import java.util.Date; // Import Date cho xử lý ngày tháng - Import Date for date handling
import java.util.Locale; // Import Locale cho định dạng theo vùng - Import Locale for regional formatting

// Activity hiển thị danh sách khoá học, thống kê, tìm kiếm, đồng bộ dữ liệu
// Activity to display course list, statistics, search, and data synchronization
public class CourseListActivity extends AppCompatActivity { // Lớp Activity kế thừa AppCompatActivity - Activity class extending AppCompatActivity
    private RecyclerView recyclerView; // RecyclerView hiển thị danh sách khoá học - RecyclerView to display course list
    private CourseAdapter adapter; // Adapter cho RecyclerView - Adapter for RecyclerView
    private List<Course> courseList; // Danh sách khoá học đang hiển thị - Currently displayed course list
    private List<Course> fullCourseList; // Danh sách đầy đủ để lọc - Full course list for filtering
    private FirebaseManager firebaseManager; // Quản lý đồng bộ Firebase - Firebase synchronization manager
    private TextView textViewStatsCourses, textViewStatsStudents, textViewStatsRevenue; // Các TextView thống kê - Statistics TextViews
    private AppDatabase db; // Room database - Room database instance
    private Button buttonSync; // Nút đồng bộ dữ liệu - Data synchronization button

    @Override
    protected void onCreate(Bundle savedInstanceState) { // Phương thức được gọi khi Activity được tạo - Method called when Activity is created
        super.onCreate(savedInstanceState); // Gọi phương thức cha - Call parent method
        setContentView(R.layout.activity_course_list); // Gán layout cho Activity - Set layout for Activity

        // Khởi tạo database - Initialize database
        db = Room.databaseBuilder( // Xây dựng Room database - Build Room database
            getApplicationContext(), // Context ứng dụng - Application context
            AppDatabase.class, // Class database - Database class
            "yoga-db" // Tên database - Database name
        ).allowMainThreadQueries() // Cho phép query trên main thread - Allow main thread queries
                         .addMigrations(AppDatabase.MIGRATION_5_6) // Thêm migration từ version 5 lên 6 - Add migration from version 5 to 6
        .build(); // Xây dựng database - Build database

        // Khởi tạo các view và adapter - Initialize views and adapter
        recyclerView = findViewById(R.id.recyclerViewCourses); // Tìm RecyclerView trong layout - Find RecyclerView in layout
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // Gán layout manager dạng danh sách - Set linear layout manager
        courseList = new ArrayList<>(); // Khởi tạo danh sách khoá học hiện tại - Initialize current course list
        fullCourseList = new ArrayList<>(); // Khởi tạo danh sách đầy đủ - Initialize full course list
        adapter = new CourseAdapter(); // Tạo adapter mới - Create new adapter
        recyclerView.setAdapter(adapter); // Gán adapter cho RecyclerView - Set adapter for RecyclerView
        firebaseManager = new FirebaseManager(); // Khởi tạo Firebase manager - Initialize Firebase manager
        textViewStatsCourses = findViewById(R.id.textViewStatsCourses); // Tìm TextView thống kê số khoá học - Find TextView for course statistics
        textViewStatsStudents = findViewById(R.id.textViewStatsStudents); // Tìm TextView thống kê số học viên - Find TextView for student statistics
        textViewStatsRevenue = findViewById(R.id.textViewStatsRevenue); // Tìm TextView thống kê doanh thu - Find TextView for revenue statistics
        loadCourses(); // Load dữ liệu khoá học - Load course data

        // Sự kiện click vào item khoá học để xem chi tiết - Click event on course item to view details
        adapter.setOnItemClickListener(new CourseAdapter.OnItemClickListener() { // Thiết lập listener cho item click - Set listener for item click
            @Override
            public void onItemClick(Course course) { // Khi click vào một khoá học - When clicking on a course
                Intent intent = new Intent(CourseListActivity.this, CourseDetailActivity.class); // Tạo Intent chuyển màn hình - Create Intent for screen navigation
                intent.putExtra("course_id", course.getId()); // Truyền ID khoá học - Pass course ID
                startActivity(intent); // Khởi chạy Activity chi tiết - Start detail Activity
            }
        });

        // Sự kiện thêm khoá học mới - Event for adding new course
        MaterialButton buttonAddCourse = findViewById(R.id.buttonAddCourse); // Tìm nút thêm khoá học - Find add course button
        buttonAddCourse.setOnClickListener(new View.OnClickListener() { // Thiết lập listener cho nút thêm - Set listener for add button
            @Override
            public void onClick(View v) { // Khi nhấn nút thêm - When add button is clicked
                Intent intent = new Intent(CourseListActivity.this, AddEditCourseActivity.class); // Tạo Intent chuyển màn hình - Create Intent for screen navigation
                startActivity(intent); // Khởi chạy Activity thêm/sửa - Start add/edit Activity
            }
        });

        // Sự kiện tìm kiếm khoá học - Event for course search
        TextInputEditText editTextSearch = findViewById(R.id.editTextSearch); // Tìm ô tìm kiếm - Find search input field
        editTextSearch.addTextChangedListener(new TextWatcher() { // Thêm listener lắng nghe thay đổi text - Add listener for text changes
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {} // Trước khi text thay đổi - Before text changes
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { // Khi text đang thay đổi - When text is changing
                filterCourses(s.toString()); // Lọc khoá học theo từ khoá - Filter courses by keyword
            }
            @Override
            public void afterTextChanged(Editable s) {} // Sau khi text thay đổi - After text changes
        });
        // Sự kiện đồng bộ dữ liệu - Event for data synchronization
        buttonSync = findViewById(R.id.buttonSync); // Tìm nút đồng bộ - Find sync button
        buttonSync.setOnClickListener(new View.OnClickListener() { // Thiết lập listener cho nút đồng bộ - Set listener for sync button
            @Override
            public void onClick(View v) { // Khi nhấn nút đồng bộ - When sync button is clicked
                // Ngăn đồng bộ nhiều lần liên tiếp - Prevent multiple consecutive syncs
                if (!buttonSync.isEnabled()) { // Nếu nút đang bị vô hiệu hoá - If button is disabled
                    return; // Thoát khỏi phương thức - Exit method
                }
                // Hiển thị trạng thái đang đồng bộ - Display syncing status
                buttonSync.setEnabled(false); // Vô hiệu hoá nút - Disable button
                buttonSync.setText("Syncing..."); // Đổi text nút - Change button text
                // Thực hiện đồng bộ toàn bộ - Perform complete synchronization
                performCompleteSync(); // Gọi phương thức đồng bộ - Call sync method
            }
        });

        // Sự kiện xem tất cả buổi học - Event for viewing all class instances
        MaterialButton buttonAllClassInstances = findViewById(R.id.buttonAllClassInstances); // Tìm nút xem tất cả buổi học - Find view all instances button
        buttonAllClassInstances.setOnClickListener(new View.OnClickListener() { // Thiết lập listener cho nút - Set listener for button
            @Override
            public void onClick(View v) { // Khi nhấn nút - When button is clicked
                Intent intent = new Intent(CourseListActivity.this, ClassInstanceListActivity.class); // Tạo Intent chuyển màn hình - Create Intent for screen navigation
                startActivity(intent); // Khởi chạy Activity danh sách buổi học - Start class instance list Activity
            }
        });

        // Tự động đồng bộ khoá học lên Firebase khi mở activity - Auto sync courses to Firebase when opening activity
        syncCoursesToFirebase(); // Gọi phương thức đồng bộ - Call sync method
    }

    @Override
    protected void onResume() { // Phương thức được gọi khi Activity quay lại tiền cảnh - Method called when Activity returns to foreground
        super.onResume(); // Gọi phương thức cha - Call parent method
        loadCourses(); // Tải lại danh sách khoá học - Reload course list
    }

    // Load danh sách khoá học từ database local, cập nhật thống kê
    // Load course list from local database, update statistics
    private void loadCourses() { // Phương thức tải danh sách khoá học - Method to load course list
        courseList.clear(); // Xoá danh sách hiện tại - Clear current list
        fullCourseList.clear(); // Xoá danh sách đầy đủ - Clear full list
        List<CourseEntity> entities = db.courseDao().getAllCourses(); // Lấy tất cả khoá học từ database - Get all courses from database
        int totalCourses = 0; // Biến đếm tổng số khoá học - Variable to count total courses
        int totalStudents = 0; // Biến đếm tổng số học viên - Variable to count total students
        double totalRevenueUSD = 0; // Biến tính tổng doanh thu - Variable to calculate total revenue
        for (CourseEntity entity : entities) { // Duyệt qua từng entity - Iterate through each entity
            Course course = new Course( // Tạo đối tượng Course từ entity - Create Course object from entity
                entity.firebaseId, // Firebase ID - Firebase ID
                entity.name, // Tên khoá học - Course name
                entity.schedule, // Lịch học - Schedule
                entity.time, // Giờ học - Time
                entity.capacity, // Sức chứa - Capacity
                entity.price, // Giá - Price
                entity.duration, // Thời lượng - Duration
                entity.description, // Mô tả - Description
                entity.note, // Ghi chú - Note
                entity.upcomingDate, // Ngày sắp tới - Upcoming date
                entity.localId // truyền localId từ entity - pass localId from entity
            );
            courseList.add(course); // Thêm vào danh sách hiển thị - Add to display list
            fullCourseList.add(course); // Thêm vào danh sách đầy đủ - Add to full list
            totalCourses++; // Tăng số lượng khoá học - Increment course count
            totalStudents += entity.capacity; // Cộng dồn số học viên - Accumulate student count
            totalRevenueUSD += entity.price * entity.capacity; // Tính tổng doanh thu - Calculate total revenue
        }
        // Hiển thị thống kê - Display statistics
        textViewStatsCourses.setText(String.valueOf(totalCourses)); // Hiển thị số khoá học - Display course count
        textViewStatsStudents.setText(String.valueOf(totalStudents)); // Hiển thị số học viên - Display student count
        textViewStatsRevenue.setText(formatCurrencyUSD(totalRevenueUSD) + " $"); // Hiển thị doanh thu - Display revenue
        adapter.setCourseList(courseList); // Cập nhật adapter với danh sách mới - Update adapter with new list
    }

    // Lọc khoá học theo từ khoá (tên, lịch, ngày, v.v.)
    // Filter courses by keyword (name, schedule, date, etc.)
    private void filterCourses(String keyword) { // Phương thức lọc khoá học - Method to filter courses
        List<Course> filtered = new ArrayList<>(); // Danh sách kết quả lọc - Filtered result list
        String lowerKeyword = keyword.toLowerCase(); // Chuyển từ khoá về chữ thường - Convert keyword to lowercase
        SimpleDateFormat[] dateFormats = new SimpleDateFormat[] {
            new SimpleDateFormat("yyyy-MM-dd", Locale.US),
            new SimpleDateFormat("dd/MM/yyyy", Locale.US)
        };
        String dayOfWeek = null;
        // Thử parse ngày để lọc theo thứ
        for (SimpleDateFormat sdf : dateFormats) {
            try {
                Date date = sdf.parse(keyword);
                if (date != null) {
                    String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.setTime(date);
                    dayOfWeek = days[cal.get(java.util.Calendar.DAY_OF_WEEK) - 1];
                }
            } catch (ParseException ignored) {}
        }
        for (Course course : fullCourseList) {
            boolean match = false;
            // Nếu nhập là ngày, lọc theo thứ
            if (dayOfWeek != null) {
                match = course.getSchedule() != null && course.getSchedule().toLowerCase().contains(dayOfWeek.toLowerCase());
            } else {
                // Nếu nhập là tên thứ
                String[] weekDays = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday", "mon", "tue", "wed", "thu", "fri", "sat", "sun"};
                for (String wd : weekDays) {
                    if (lowerKeyword.equals(wd) && course.getSchedule() != null && course.getSchedule().toLowerCase().contains(wd)) {
                        match = true;
                        break;
                    }
                }

                // Nếu nhập là tên khoá học hoặc lịch
                if (!match && course.getName() != null && course.getName().toLowerCase().contains(lowerKeyword)) {
                    match = true;
                }
                if (!match && course.getSchedule() != null && course.getSchedule().toLowerCase().contains(lowerKeyword)) {
                    match = true;
                }
            }
            if (match) {
                filtered.add(course);
            }
        }
        adapter.setCourseList(filtered);
    }

    // Định dạng số tiền USD
    private String formatCurrencyUSD(double value) {
        return String.format("% ,.2f", value);
    }

    // Đẩy các khoá học chưa đồng bộ lên Firebase
    public void syncCoursesToFirebase() {
        List<CourseEntity> unsynced = db.courseDao().getUnsyncedCourses();
        FirebaseManager firebaseManager = new FirebaseManager();

        for (CourseEntity entity : unsynced) {
            Course course = new Course(
                null, entity.name, entity.schedule, entity.time,
                entity.capacity, entity.price, entity.duration, entity.description, entity.note, entity.upcomingDate, entity.localId
            );
            firebaseManager.addCourse(course, (error, ref) -> {
                if (error == null) {
                    entity.isSynced = true;
                    entity.firebaseId = ref.getKey();
                    db.courseDao().update(entity);
                }
            });
        }
        // Đã bỏ phần reload lại toàn bộ từ Firebase về Room
    }

    // Đẩy các buổi học chưa đồng bộ lên Firebase
    public void syncClassInstancesToFirebase() {
        List<ClassInstanceEntity> unsynced = db.classInstanceDao().getUnsyncedInstances();
        FirebaseManager firebaseManager = new FirebaseManager();

        if (unsynced.isEmpty()) {
            Toast.makeText(CourseListActivity.this, "All class instances are already synced", Toast.LENGTH_SHORT).show();
            return;
        }

        final int[] syncedCount = {0};
        final int totalCount = unsynced.size();

        for (ClassInstanceEntity entity : unsynced) {
            // Nếu đã có Firebase ID thì bỏ qua
            if (entity.firebaseId != null) {
                continue;
            }

            // Tạo ClassInstance với courseId là firebaseId
            ClassInstance instance = new ClassInstance(
                entity.firebaseId, 
                entity.courseId, // Đây là firebaseId của khoá học
                entity.date, 
                entity.teacher, 
                entity.note, 
                entity.id
            );

            firebaseManager.addClassInstance(instance, (error, ref) -> {
                if (error == null) {
                    // Cập nhật local entity với Firebase ID và đánh dấu đã sync
                    entity.isSynced = true;
                    entity.firebaseId = ref.getKey();
                    db.classInstanceDao().update(entity);
                    syncedCount[0]++;
                } else {
                    // Nếu sync thất bại, giữ lại để retry
                    Toast.makeText(CourseListActivity.this, "Failed to sync class instance", Toast.LENGTH_SHORT).show();
                }

                // Kiểm tra đã xử lý hết chưa
                if (syncedCount[0] + (totalCount - unsynced.size()) == totalCount) {
                    runOnUiThread(() -> {
                        Toast.makeText(CourseListActivity.this, 
                            "Synced " + syncedCount[0] + " of " + totalCount + " class instances", 
                            Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }
    }

    // Lấy dữ liệu buổi học từ Firebase về local
    public void pullClassInstancesFromFirebase() {
        FirebaseManager firebaseManager = new FirebaseManager();

        // Lấy tất cả khoá học
        List<CourseEntity> courses = db.courseDao().getAllCourses();

        for (CourseEntity course : courses) {
            if (course.firebaseId != null) {
                firebaseManager.getClassInstancesByCourseId(course.firebaseId, new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            ClassInstance firebaseInstance = child.getValue(ClassInstance.class);
                            if (firebaseInstance != null) {
                                firebaseInstance.setId(child.getKey());

                                // Kiểm tra instance đã tồn tại local chưa (theo firebaseId)
                                ClassInstanceEntity existingEntity = db.classInstanceDao().getInstanceByFirebaseId(child.getKey());

                                if (existingEntity == null) {
                                    // Kiểm tra trùng ngày để tránh duplicate
                                    List<ClassInstanceEntity> similarInstances = db.classInstanceDao().getInstancesForCourse(course.firebaseId);
                                    boolean isDuplicate = false;

                                    for (ClassInstanceEntity entity : similarInstances) {
                                        if (entity.date.equals(firebaseInstance.getDate())) {
                                            isDuplicate = true;
                                            break;
                                        }
                                    }

                                    if (!isDuplicate) {
                                        // Tạo entity mới local
                                        ClassInstanceEntity newEntity = new ClassInstanceEntity();
                                        newEntity.firebaseId = child.getKey();
                                        newEntity.courseId = course.firebaseId;
                                        newEntity.courseLocalId = course.localId;
                                        newEntity.date = firebaseInstance.getDate();
                                        newEntity.teacher = ""; // Chưa có thông tin giáo viên
                                        newEntity.note = firebaseInstance.getNote();
                                        newEntity.isSynced = true;

                                        db.classInstanceDao().insert(newEntity);
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(CourseListActivity.this, "Failed to pull class instances", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    // Thực hiện đồng bộ toàn bộ: xoá duplicate, đẩy lên Firebase, lấy về local
    private void performCompleteSync() {
        // Xoá duplicate trước
        cleanupDuplicateInstances();

        // Đẩy dữ liệu local lên Firebase (chỉ dữ liệu chưa sync)
        syncCoursesToFirebase();
        syncClassInstancesToFirebase();

        // Lấy dữ liệu từ Firebase về local (delay để tránh xung đột)
        buttonSync.postDelayed(() -> {
            pullClassInstancesFromFirebase();
        }, 2000); // Delay 2 giây

        // Bật lại nút sau delay
        buttonSync.postDelayed(() -> {
            buttonSync.setEnabled(true);
            buttonSync.setText("Sync Data");
            Toast.makeText(CourseListActivity.this, "Sync completed", Toast.LENGTH_SHORT).show();
        }, 5000); // Delay 5 giây
    }

    // Xoá các buổi học duplicate (giữ lại bản có ID nhỏ nhất)
    private void cleanupDuplicateInstances() {
        List<ClassInstanceDao.DuplicateInfo> duplicates = db.classInstanceDao().getDuplicateInfo();

        int cleanedCount = 0;
        for (ClassInstanceDao.DuplicateInfo duplicate : duplicates) {
            // Xoá duplicate, giữ lại bản có ID nhỏ nhất
            db.classInstanceDao().deleteDuplicateInstances(duplicate.courseId, duplicate.date);
            cleanedCount++;
        }

        if (cleanedCount > 0) {
            Toast.makeText(this, "Cleaned up " + cleanedCount + " duplicate entries", Toast.LENGTH_SHORT).show();
        }
    }

    // Hàm gọi xoá duplicate từ UI
    public void manualCleanupDuplicates() {
        new AlertDialog.Builder(this)
                .setTitle("Clean Up Duplicates")
                .setMessage("This will remove duplicate class instances. Continue?")
                .setPositiveButton("Clean Up", (dialog, which) -> {
                    cleanupDuplicateInstances();
                    // Refresh lại danh sách khoá học
                    loadCourses();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}