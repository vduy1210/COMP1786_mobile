// Activity tìm kiếm buổi học với các tiêu chí khác nhau
// Activity for searching class instances with different criteria
package com.example.universalyogaapp.ui.course;

// Import các thư viện cần thiết cho Activity tìm kiếm
// Import necessary libraries for search Activity
import android.os.Bundle; // Bundle để truyền dữ liệu - Bundle for passing data
import android.text.Editable; // Interface cho text có thể chỉnh sửa - Interface for editable text
import android.text.TextWatcher; // Listener theo dõi thay đổi text - Listener for text changes
import android.view.View; // View cơ bản - Basic view
import android.widget.EditText; // Widget nhập text - Text input widget
import android.widget.Toast; // Hiển thị thông báo ngắn - Display short messages
import androidx.annotation.Nullable; // Annotation cho giá trị có thể null - Nullable annotation
import androidx.appcompat.app.AppCompatActivity; // Activity cơ bản của AppCompat - Base AppCompat activity
import androidx.recyclerview.widget.LinearLayoutManager; // Layout manager cho RecyclerView - Layout manager for RecyclerView
import androidx.recyclerview.widget.RecyclerView; // RecyclerView cho danh sách - RecyclerView for lists
import com.example.universalyogaapp.R; // Resource layout - Layout resources
import com.example.universalyogaapp.db.AppDatabase; // Database ứng dụng - Application database
import com.example.universalyogaapp.db.ClassInstanceEntity; // Entity buổi học - Class instance entity
import com.example.universalyogaapp.model.ClassInstance; // Model buổi học - Class instance model
import java.text.ParseException; // Exception khi parse lỗi - Parse exception
import java.text.SimpleDateFormat; // Định dạng ngày - Date formatting
import java.util.ArrayList; // Danh sách ArrayList - ArrayList collection
import java.util.Calendar; // Lịch - Calendar
import java.util.Date; // Ngày tháng - Date
import java.util.List; // Interface danh sách - List interface
import java.util.Locale; // Địa phương hoá - Localization

// Activity chuyên dụng cho việc tìm kiếm buổi học
// Dedicated Activity for searching class instances
public class ClassInstanceSearchActivity extends AppCompatActivity {
    // Khai báo các biến UI components - Declare UI component variables
    private EditText editTextSearch; // Ô nhập từ khoá tìm kiếm - Search keyword input field
    private RecyclerView recyclerViewResults; // RecyclerView hiển thị kết quả tìm kiếm - RecyclerView to display search results
    private ClassInstanceAdapter adapter; // Adapter cho RecyclerView - Adapter for RecyclerView
    
    // Khai báo các biến dữ liệu - Declare data variables
    private AppDatabase db; // Room database instance - Room database instance
    private List<ClassInstance> allInstances = new ArrayList<>(); // Danh sách tất cả buổi học để tìm kiếm - List of all instances for searching

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Gọi phương thức cha - Call parent method
        setContentView(R.layout.activity_class_instance_search); // Gán layout cho Activity - Set layout for Activity
        
        // Khởi tạo các view từ layout - Initialize views from layout
        editTextSearch = findViewById(R.id.editTextSearch); // Tìm và gán ô tìm kiếm - Find and assign search input field
        recyclerViewResults = findViewById(R.id.recyclerViewResults); // Tìm và gán RecyclerView kết quả - Find and assign results RecyclerView
        
        // Thiết lập layout manager cho RecyclerView - Set up layout manager for RecyclerView
        recyclerViewResults.setLayoutManager(new LinearLayoutManager(this)); // Sử dụng LinearLayoutManager dạng dọc - Use vertical LinearLayoutManager
        
        // Khởi tạo adapter với danh sách rỗng và listener - Initialize adapter with empty list and listener
        adapter = new ClassInstanceAdapter(new ArrayList<>(), new ClassInstanceAdapter.OnInstanceActionListener() { // Tạo adapter với listener - Create adapter with listener
            @Override
            public void onEdit(ClassInstance instance) { // Phương thức được gọi khi sửa buổi học - Method called when editing instance
                // Tuỳ chọn: hiển thị chi tiết hoặc sửa trong ngữ cảnh tìm kiếm - Optional: show details or edit in search context
            }
            @Override
            public void onDelete(ClassInstance instance) { // Phương thức được gọi khi xoá buổi học - Method called when deleting instance
                // Tuỳ chọn: không làm gì hoặc hiển thị dialog trong ngữ cảnh tìm kiếm - Optional: do nothing or show dialog in search context
            }
        });
        recyclerViewResults.setAdapter(adapter); // Gán adapter cho RecyclerView - Set adapter for RecyclerView
        
        // Khởi tạo database và load dữ liệu - Initialize database and load data
        db = AppDatabase.getInstance(getApplicationContext()); // Lấy instance database - Get database instance
        List<ClassInstanceEntity> entityList = db.classInstanceDao().getAllClassInstances(); // Lấy tất cả entity từ database - Get all entities from database
        allInstances = new ArrayList<>(); // Khởi tạo danh sách mới - Initialize new list
        for (ClassInstanceEntity entity : entityList) { // Duyệt qua từng entity - Iterate through each entity
            allInstances.add(new ClassInstance( // Chuyển đổi entity thành model và thêm vào danh sách - Convert entity to model and add to list
                entity.firebaseId, // Firebase ID của buổi học - Firebase ID of instance
                entity.courseId, // ID khoá học - Course ID
                entity.date, // Ngày học - Date
                entity.teacher, // Tên giáo viên - Teacher name
                entity.note, // Ghi chú - Note
                entity.id // Local ID - Local ID
            ));
        }
        adapter.setInstanceList(allInstances); // Hiển thị tất cả buổi học ban đầu - Display all instances initially
        
        // Thiết lập listener theo dõi thay đổi text trong ô tìm kiếm - Set up text change listener for search input
        editTextSearch.addTextChangedListener(new TextWatcher() { // Thêm TextWatcher - Add TextWatcher
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {} // Trước khi text thay đổi (không xử lý) - Before text changes (no processing)
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { // Khi text đang thay đổi - When text is changing
                filterResults(s.toString()); // Lọc kết quả theo từ khoá mới - Filter results by new keyword
            }
            
            @Override
            public void afterTextChanged(Editable s) {} // Sau khi text thay đổi (không xử lý) - After text changes (no processing)
        });
    }

    // Phương thức lọc kết quả tìm kiếm theo từ khoá - Method to filter search results by keyword
    private void filterResults(String keyword) {
        String lower = keyword.toLowerCase(); // Chuyển từ khoá về chữ thường - Convert keyword to lowercase
        List<ClassInstance> filtered = new ArrayList<>(); // Danh sách kết quả đã lọc - Filtered results list
        
        // Khởi tạo SimpleDateFormat để parse ngày - Initialize SimpleDateFormat for date parsing
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US); // Định dạng ngày chuẩn - Standard date format
        String weekday = null; // Biến lưu thứ trong tuần - Variable to store weekday
        
        // Thử parse keyword thành ngày để tìm theo thứ - Try parsing keyword as date to search by weekday
        try {
            Date date = sdf.parse(keyword); // Parse keyword thành Date object - Parse keyword to Date object
            if (date != null) { // Nếu parse thành công - If parsing successful
                Calendar cal = Calendar.getInstance(); // Lấy instance Calendar - Get Calendar instance
                cal.setTime(date); // Đặt thời gian cho Calendar - Set time for Calendar
                String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"}; // Mảng tên các ngày - Array of day names
                weekday = days[cal.get(Calendar.DAY_OF_WEEK) - 1].toLowerCase(); // Lấy tên thứ và chuyển thành chữ thường - Get day name and convert to lowercase
            }
        } catch (ParseException ignored) {} // Bỏ qua lỗi parse - Ignore parse errors
        
        // Duyệt qua tất cả buổi học để tìm kiếm - Iterate through all instances for searching
        for (ClassInstance instance : allInstances) { // Duyệt qua từng buổi học - Iterate through each instance
            boolean match = false; // Biến đánh dấu có khớp hay không - Variable to mark if there's a match
            
            // Tìm theo tên giáo viên - Search by teacher name
            if (instance.getTeacher() != null && !lower.isEmpty() && instance.getTeacher().toLowerCase().contains(lower)) match = true; // Nếu tên giáo viên chứa từ khoá - If teacher name contains keyword
            
            // Tìm theo ngày chính xác - Search by exact date
            if (!match && instance.getDate() != null && instance.getDate().equals(keyword)) match = true; // Nếu ngày khớp chính xác - If date matches exactly
            
            // Tìm theo thứ trong tuần (từ ngày được parse) - Search by weekday (from parsed date)
            if (!match && weekday != null && instance.getDate() != null && getWeekday(instance.getDate()).equals(weekday)) match = true; // Nếu thứ trong tuần khớp - If weekday matches
            
            if (match) filtered.add(instance); // Nếu khớp, thêm vào danh sách kết quả - If matched, add to result list
        }
        adapter.setInstanceList(filtered); // Cập nhật adapter với danh sách đã lọc - Update adapter with filtered list
    }

    // Phương thức helper lấy thứ trong tuần từ chuỗi ngày - Helper method to get weekday from date string
    private String getWeekday(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US); // Định dạng ngày chuẩn - Standard date format
            Date date = sdf.parse(dateStr); // Parse chuỗi thành Date object - Parse string to Date object
            if (date != null) { // Nếu parse thành công - If parsing successful
                Calendar cal = Calendar.getInstance(); // Lấy instance Calendar - Get Calendar instance
                cal.setTime(date); // Đặt thời gian cho Calendar - Set time for Calendar
                String[] days = {"sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday"}; // Mảng tên các ngày (chữ thường) - Array of day names (lowercase)
                return days[cal.get(Calendar.DAY_OF_WEEK) - 1]; // Trả về tên thứ (Calendar.DAY_OF_WEEK bắt đầu từ 1) - Return day name (Calendar.DAY_OF_WEEK starts from 1)
            }
        } catch (Exception ignored) {} // Bắt và bỏ qua exception - Catch and ignore exceptions
        return ""; // Trả về chuỗi rỗng nếu có lỗi - Return empty string if error
    }
}
