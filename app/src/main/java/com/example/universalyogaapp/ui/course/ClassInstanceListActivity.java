
// Activity hiển thị danh sách tất cả buổi học, cho phép tìm kiếm, sửa/xoá buổi học
// Activity to display list of all class instances, allowing search, edit/delete operations
package com.example.universalyogaapp.ui.course;

// Import các thư viện cần thiết cho Activity này
// Import necessary libraries for this Activity
import android.os.Bundle; // Bundle để truyền dữ liệu - Bundle for passing data
import android.text.Editable; // Interface cho text có thể chỉnh sửa - Interface for editable text
import android.text.TextWatcher; // Listener theo dõi thay đổi text - Listener for text changes
import android.widget.EditText; // Widget nhập text - Text input widget
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

// Activity hiển thị danh sách tất cả buổi học, cho phép tìm kiếm, sửa/xoá buổi học
// Activity to display list of all class instances, allowing search, edit/delete operations
public class ClassInstanceListActivity extends AppCompatActivity {
    // Khai báo các biến UI components - Declare UI component variables
    private EditText editTextSearch; // Ô tìm kiếm buổi học - Search input field for class instances
    private RecyclerView recyclerView; // RecyclerView hiển thị danh sách buổi học - RecyclerView to display class instance list
    private ClassInstanceAdapter adapter; // Adapter cho RecyclerView - Adapter for RecyclerView
    
    // Khai báo các biến dữ liệu - Declare data variables
    private AppDatabase db; // Room database instance - Room database instance
    private List<ClassInstance> allInstances = new ArrayList<>(); // Danh sách tất cả buổi học - List of all class instances
    private android.widget.Button buttonGoToCourseManage; // Nút chuyển về quản lý khoá học - Button to navigate to course management

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Gọi phương thức cha - Call parent method
        setContentView(R.layout.activity_class_instance_list); // Gán layout cho Activity - Set layout for Activity
        
        // Khởi tạo các view từ layout - Initialize views from layout
        editTextSearch = findViewById(R.id.editTextSearchClassInstance); // Tìm và gán ô tìm kiếm - Find and assign search input field
        recyclerView = findViewById(R.id.recyclerViewClassInstances); // Tìm và gán RecyclerView - Find and assign RecyclerView
        buttonGoToCourseManage = findViewById(R.id.buttonGoToCourseManage); // Tìm và gán nút chuyển trang - Find and assign navigation button
        
        // Thiết lập layout manager cho RecyclerView - Set up layout manager for RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // Sử dụng LinearLayoutManager dạng dọc - Use vertical LinearLayoutManager
        
        // Khởi tạo adapter cho danh sách buổi học - Initialize adapter for class instance list
        adapter = new ClassInstanceAdapter(new ArrayList<>(), new ClassInstanceAdapter.OnInstanceActionListener() { // Tạo adapter với danh sách rỗng và listener - Create adapter with empty list and listener
            @Override
            public void onEdit(ClassInstance instance) { // Phương thức được gọi khi sửa buổi học - Method called when editing instance
                // Truyền toàn bộ đối tượng ClassInstance sang màn hình chỉnh sửa - Pass complete ClassInstance object to edit screen
                android.content.Intent intent = new android.content.Intent(ClassInstanceListActivity.this, AddEditClassInstanceActivity.class); // Tạo Intent chuyển Activity - Create Intent to navigate Activity
                intent.putExtra("class_instance", instance); // Truyền đối tượng instance qua Intent - Pass instance object via Intent
                startActivity(intent); // Khởi động Activity - Start Activity
            }
            @Override
            public void onDelete(ClassInstance instance) { // Phương thức được gọi khi xoá buổi học - Method called when deleting instance
                // Xoá instance khỏi database và reload danh sách - Delete instance from database and reload list
                new android.app.AlertDialog.Builder(ClassInstanceListActivity.this) // Tạo dialog xác nhận - Create confirmation dialog
                        .setTitle("Delete Class Session") // Đặt tiêu đề dialog - Set dialog title
                        .setMessage("Are you sure you want to delete this class session?") // Đặt nội dung xác nhận - Set confirmation message
                        .setPositiveButton("Delete", (dialog, which) -> { // Nút xác nhận xoá - Delete confirmation button
                            db.classInstanceDao().deleteByLocalId(instance.getLocalId()); // Xoá khỏi database theo local ID - Delete from database by local ID
                            loadAllInstances(); // Tải lại danh sách - Reload list
                        })
                        .setNegativeButton("Cancel", null) // Nút huỷ (không làm gì) - Cancel button (do nothing)
                        .show(); // Hiển thị dialog - Show dialog
            }
        });
        recyclerView.setAdapter(adapter); // Gán adapter cho RecyclerView - Set adapter for RecyclerView
        
        db = AppDatabase.getInstance(getApplicationContext()); // Lấy instance database - Get database instance
        loadAllInstances(); // Load tất cả buổi học từ database - Load all instances from database
        
        // Thiết lập sự kiện tìm kiếm buổi học - Set up class instance search functionality
        editTextSearch.addTextChangedListener(new TextWatcher() { // Thêm listener theo dõi thay đổi text - Add text change listener
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {} // Trước khi text thay đổi (không xử lý) - Before text changes (no processing)
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { // Khi text đang thay đổi - When text is changing
                String input = s.toString(); // Lấy chuỗi nhập vào - Get input string
                if (input.trim().isEmpty()) { // Nếu chuỗi tìm kiếm rỗng - If search string is empty
                    // Nếu xoá tìm kiếm, reload toàn bộ danh sách - If search is cleared, reload complete list
                    adapter.setInstanceList(new ArrayList<>(allInstances)); // Hiển thị lại tất cả buổi học - Display all instances again
                } else { // Nếu có từ khoá tìm kiếm - If there's search keyword
                    filterResults(input); // Lọc kết quả theo từ khoá - Filter results by keyword
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {} // Sau khi text thay đổi (không xử lý) - After text changes (no processing)
        });
        
        // Thiết lập sự kiện chuyển về màn quản lý khoá học - Set up navigation to course management screen
        buttonGoToCourseManage.setOnClickListener(v -> { // Listener cho nút chuyển trang - Listener for navigation button
            startActivity(new android.content.Intent(ClassInstanceListActivity.this, CourseListActivity.class)); // Chuyển đến CourseListActivity - Navigate to CourseListActivity
        });
    }

    // Load tất cả buổi học từ database local - Load all class instances from local database
    private void loadAllInstances() {
        List<ClassInstanceEntity> entityList = db.classInstanceDao().getAllClassInstances(); // Lấy tất cả entity từ database - Get all entities from database
        allInstances.clear(); // Xoá danh sách cũ - Clear old list
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
        adapter.setInstanceList(new ArrayList<>(allInstances)); // Cập nhật adapter với danh sách mới - Update adapter with new list
    }

    // Lọc buổi học theo từ khoá (giáo viên, ngày, thứ, v.v.) - Filter class instances by keyword (teacher, date, day, etc.)
    private void filterResults(String keyword) {
        String lower = keyword.trim().toLowerCase(); // Chuyển từ khoá về chữ thường và loại bỏ khoảng trắng - Convert keyword to lowercase and trim whitespace
        List<ClassInstance> filtered = new ArrayList<>(); // Danh sách kết quả đã lọc - Filtered results list
        
        // Hỗ trợ nhiều định dạng ngày - Support multiple date formats
        SimpleDateFormat[] dateFormats = new SimpleDateFormat[] { // Mảng các định dạng ngày được hỗ trợ - Array of supported date formats
            new SimpleDateFormat("yyyy-MM-dd", Locale.US), // Định dạng ISO (2025-08-02) - ISO format
            new SimpleDateFormat("dd/MM/yyyy", Locale.US), // Định dạng châu Âu (02/08/2025) - European format
            new SimpleDateFormat("d/M/yyyy", Locale.US) // Định dạng ngắn (2/8/2025) - Short format
        };
        
        // Biến để lưu thứ từ ngày nhập vào - Variables to store day from input date
        String weekdayFromDate = null; // Thứ được parse từ ngày - Day parsed from date
        Date parsedDate = null; // Ngày đã parse - Parsed date
        
        // Thử parse keyword thành ngày với các định dạng khác nhau - Try parsing keyword as date with different formats
        for (SimpleDateFormat sdf : dateFormats) { // Duyệt qua từng định dạng - Iterate through each format
            try {
                parsedDate = sdf.parse(keyword); // Thử parse keyword thành ngày - Try parsing keyword as date
                if (parsedDate != null) { // Nếu parse thành công - If parsing successful
                    Calendar cal = Calendar.getInstance(); // Lấy instance Calendar - Get Calendar instance
                    cal.setTime(parsedDate); // Đặt thời gian cho Calendar - Set time for Calendar
                    String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"}; // Mảng tên các ngày - Array of day names
                    weekdayFromDate = days[cal.get(Calendar.DAY_OF_WEEK) - 1].toLowerCase(); // Lấy tên thứ và chuyển thành chữ thường - Get day name and convert to lowercase
                    break; // Thoát khỏi vòng lặp khi parse thành công - Break loop when parsing successful
                }
            } catch (ParseException ignored) {} // Bỏ qua lỗi parse - Ignore parse errors
        }
        
        // Từ khoá thứ tiếng Anh được hỗ trợ - Supported English day keywords
        String[] weekDays = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday", // Tên đầy đủ - Full names
                "mon", "tue", "wed", "thu", "fri", "sat", "sun"}; // Tên viết tắt - Abbreviated names

        // Chuẩn hóa keyword để so sánh từng phần ngày - Normalize keyword to compare date parts
        String[] keywordParts = lower.split("/|-"); // Chia keyword theo dấu / hoặc - - Split keyword by / or -

        // Duyệt qua tất cả buổi học để tìm kiếm - Iterate through all instances for searching
        for (ClassInstance instance : allInstances) { // Duyệt qua từng buổi học - Iterate through each instance
            boolean match = false; // Biến đánh dấu có khớp hay không - Variable to mark if there's a match
            
            // Tìm theo tên giáo viên - Search by teacher name
            if (instance.getTeacher() != null && !lower.isEmpty() && instance.getTeacher().toLowerCase().contains(lower)) match = true; // Nếu tên giáo viên chứa từ khoá - If teacher name contains keyword

            // Tìm theo ngày đúng định dạng hoặc partial date - Search by exact date format or partial date
            if (!match && instance.getDate() != null && !lower.isEmpty()) { // Nếu chưa khớp và có ngày học - If not matched yet and has date
                String dateStr = instance.getDate(); // Lấy chuỗi ngày yyyy-MM-dd - Get date string in yyyy-MM-dd format
                String[] dateParts = dateStr.split("-"); // Chia ngày thành các phần - Split date into parts
                // dateParts[0]=yyyy, [1]=MM, [2]=dd - dateParts[0]=year, [1]=month, [2]=day
                String day = dateParts.length > 2 ? dateParts[2] : ""; // Lấy ngày (dd) - Get day (dd)
                String month = dateParts.length > 1 ? dateParts[1] : ""; // Lấy tháng (MM) - Get month (MM)
                String year = dateParts.length > 0 ? dateParts[0] : ""; // Lấy năm (yyyy) - Get year (yyyy)
                
                // So sánh từng phần của keyword với ngày - Compare keyword parts with date
                if (keywordParts.length == 1) { // Nếu chỉ nhập 1 phần - If only 1 part entered
                    // Nếu chỉ nhập 1 phần (ví dụ "01"), so sánh với ngày, tháng, năm - If only 1 part (e.g. "01"), compare with day, month, year
                    if (day.equalsIgnoreCase(keywordParts[0]) || month.equalsIgnoreCase(keywordParts[0]) || year.equalsIgnoreCase(keywordParts[0])) { // So sánh không phân biệt chữ hoa thường - Case-insensitive comparison
                        match = true; // Đánh dấu khớp - Mark as matched
                    }
                } else if (keywordParts.length == 2) { // Nếu nhập 2 phần - If 2 parts entered
                    // Nếu nhập 2 phần (ví dụ "08/2025"), so sánh tháng/năm hoặc ngày/tháng - If 2 parts (e.g. "08/2025"), compare month/year or day/month
                    if ((month.equalsIgnoreCase(keywordParts[0]) && year.equalsIgnoreCase(keywordParts[1])) || // Tháng/năm - Month/year
                        (day.equalsIgnoreCase(keywordParts[0]) && month.equalsIgnoreCase(keywordParts[1]))) { // Ngày/tháng - Day/month
                        match = true; // Đánh dấu khớp - Mark as matched
                    }
                } else if (keywordParts.length == 3) { // Nếu nhập đủ 3 phần - If all 3 parts entered
                    // Nếu nhập đủ 3 phần, so sánh toàn bộ ngày - If all 3 parts entered, compare complete date
                    if ((day.equalsIgnoreCase(keywordParts[0]) && month.equalsIgnoreCase(keywordParts[1]) && year.equalsIgnoreCase(keywordParts[2])) || // dd/MM/yyyy
                        (year.equalsIgnoreCase(keywordParts[0]) && month.equalsIgnoreCase(keywordParts[1]) && day.equalsIgnoreCase(keywordParts[2]))) { // yyyy/MM/dd
                        match = true; // Đánh dấu khớp - Mark as matched
                    }
                }
                
                // Ngoài ra, so sánh chuỗi ngày với keyword (cho phép tìm theo 01/08, 08/2025, v.v.) - Additionally, compare date string with keyword (allows searching by 01/08, 08/2025, etc.)
                String dateDisplay = day + "/" + month + "/" + year; // Tạo chuỗi hiển thị dd/MM/yyyy - Create display string dd/MM/yyyy
                if (!match && dateDisplay.contains(lower)) match = true; // Nếu chuỗi hiển thị chứa keyword - If display string contains keyword
                if (!match && dateStr.contains(lower)) match = true; // Nếu chuỗi gốc chứa keyword - If original string contains keyword
            }

            // Tìm theo ngày nhập dạng dd/MM/yyyy - Search by date entered in dd/MM/yyyy format
            if (!match && parsedDate != null && instance.getDate() != null) { // Nếu chưa khớp và có ngày được parse - If not matched yet and has parsed date
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US); // Định dạng chuẩn của database - Standard database format
                    Date instDate = sdf.parse(instance.getDate()); // Parse ngày của buổi học - Parse instance date
                    if (instDate != null && instDate.equals(parsedDate)) match = true; // Nếu 2 ngày bằng nhau - If both dates are equal
                } catch (Exception ignored) {} // Bỏ qua lỗi parse - Ignore parse errors
            }

            // Tìm theo weekday từ ngày nhập - Search by weekday from entered date
            if (!match && weekdayFromDate != null && instance.getDate() != null && getWeekday(instance.getDate()).equals(weekdayFromDate)) match = true; // Nếu thứ trong tuần khớp - If weekday matches

            // Tìm theo từ khoá thứ tiếng Anh - Search by English day keywords
            if (!match && instance.getDate() != null && !lower.isEmpty()) { // Nếu chưa khớp và có ngày học - If not matched yet and has date
                String weekdayOfInstance = getWeekday(instance.getDate()); // Lấy thứ của buổi học - Get weekday of instance
                for (String wd : weekDays) { // Duyệt qua các từ khoá thứ - Iterate through day keywords
                    if (lower.equals(wd) && (weekdayOfInstance.equals(wd) || weekdayOfInstance.startsWith(wd) || weekdayOfInstance.contains(wd))) { // Nếu keyword khớp với thứ - If keyword matches day
                        match = true; // Đánh dấu khớp - Mark as matched
                        break; // Thoát khỏi vòng lặp - Break loop
                    }
                }
                // Partial match (ví dụ "tue" khớp "Tuesday") - Partial match (e.g. "tue" matches "Tuesday")
                if (!match && weekdayOfInstance.toLowerCase().contains(lower)) match = true; // Tìm kiếm một phần trong tên thứ - Partial search in day name
            }
            if (match) filtered.add(instance); // Nếu khớp, thêm vào danh sách kết quả - If matched, add to result list
        }
        adapter.setInstanceList(filtered); // Cập nhật adapter với danh sách đã lọc - Update adapter with filtered list
    }

    // Lấy thứ trong tuần từ chuỗi ngày yyyy-MM-dd - Get day of week from date string yyyy-MM-dd
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

    @Override
    protected void onResume() {
        super.onResume(); // Gọi phương thức cha - Call parent method
        loadAllInstances(); // Tự động reload khi có buổi học mới hoặc quay lại Activity - Auto reload when new instances added or returning to Activity
    }
}
