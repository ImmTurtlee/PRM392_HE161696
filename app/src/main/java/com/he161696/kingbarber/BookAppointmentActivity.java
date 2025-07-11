package com.he161696.kingbarber;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookAppointmentActivity extends AppCompatActivity {

    private Spinner spinnerBarber;
    private ListView listServices;
    private DatePicker datePicker;
    private TimePicker timePicker;
    private Button btnConfirmBooking;

    private List<Integer> barberIds = new ArrayList<>();
    private List<Integer> serviceIds = new ArrayList<>();

    private int clientId;
    private static final String GET_BARBERS_URL = "http://10.0.2.2/api/get_barbers.php";
    private static final String GET_SERVICES_URL = "http://10.0.2.2/api/get_services.php";
    private static final String BOOK_URL = "http://10.0.2.2/api/book_appointment.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_appointments);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle("Appointments");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        clientId = getIntent().getIntExtra("clientId", -1);

        spinnerBarber = findViewById(R.id.spinnerBarber);
        listServices = findViewById(R.id.listServices);
        datePicker = findViewById(R.id.datePicker);
        timePicker = findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);

        fetchBarbers();
        fetchServices();

        // Test API call
        testServiceAPI();

        // Thêm sự kiện click để xem thông tin dịch vụ
        listServices.setOnItemClickListener((parent, view, position, id) -> {
            showServiceInfoDialog(position);
        });

        btnConfirmBooking.setOnClickListener(v -> bookAppointment());
    }

    private void fetchBarbers() {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, GET_BARBERS_URL, null,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            JSONArray barbersArray = response.getJSONArray("barbers");
                            List<String> names = new ArrayList<>();
                            barberIds.clear();

                            for (int i = 0; i < barbersArray.length(); i++) {
                                JSONObject obj = barbersArray.getJSONObject(i);
                                names.add(obj.getString("FullName"));
                                barberIds.add(obj.getInt("BarberId"));
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                    android.R.layout.simple_spinner_item, names);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerBarber.setAdapter(adapter);
                        } else {
                            Toast.makeText(this, "Không có barber nào", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Lỗi xử lý JSON barber", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Lỗi tải danh sách barber", Toast.LENGTH_SHORT).show();
                }
        );

        queue.add(request);
    }


    private void fetchServices() {
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest request = new JsonArrayRequest(GET_SERVICES_URL,
                response -> {
                    List<String> names = new ArrayList<>();
                    serviceIds.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            names.add(obj.getString("Name") + " - " + obj.getInt("Price") + "đ");
                            serviceIds.add(obj.getInt("ServiceId"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, names);
                    listServices.setAdapter(adapter);
                },
                error -> Toast.makeText(this, "Lỗi tải dịch vụ", Toast.LENGTH_SHORT).show());

        queue.add(request);
    }
    
    // Test method để kiểm tra API call
    private void testServiceAPI() {
        // Test API call sau khi services được load
        listServices.post(() -> {
            if (serviceIds.size() > 0) {
                android.util.Log.d("SERVICE_API", "Testing API call for service ID: " + serviceIds.get(0));
                
                RequestQueue queue = Volley.newRequestQueue(this);
                JsonArrayRequest testRequest = new JsonArrayRequest(Request.Method.GET, GET_SERVICES_URL, null,
                        response -> {
                            try {
                                android.util.Log.d("SERVICE_API", "API Response length: " + response.length());
                                // Tìm service có ID tương ứng
                                int targetServiceId = serviceIds.get(0);
                                JSONObject targetService = null;
                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject service = response.getJSONObject(i);
                                    if (service.getInt("ServiceId") == targetServiceId) {
                                        targetService = service;
                                        break;
                                    }
                                }
                                if (targetService != null) {
                                    android.util.Log.d("SERVICE_API", "Found service: " + targetService.getString("Name"));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        },
                        error -> {
                            android.util.Log.e("SERVICE_API", "API Error: " + error.toString());
                        }
                );
                queue.add(testRequest);
            }
        });
    }

    private void bookAppointment() {
        int barberIndex = spinnerBarber.getSelectedItemPosition();
        int barberId = barberIds.get(barberIndex);

        List<Integer> selectedServiceIds = new ArrayList<>();
        for (int i = 0; i < listServices.getCount(); i++) {
            if (listServices.isItemChecked(i)) {
                selectedServiceIds.add(serviceIds.get(i));
            }
        }

        if (selectedServiceIds.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất 1 dịch vụ", Toast.LENGTH_SHORT).show();
            return;
        }

        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth() + 1;
        int year = datePicker.getYear();
        String date = String.format("%04d-%02d-%02d", year, month, day);

        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();
        String time = String.format("%02d:%02d", hour, minute);

        StringRequest request = new StringRequest(Request.Method.POST, BOOK_URL,
                response -> {
                    Log.d("BOOK", "Response: " + response);
                    Toast.makeText(this, "Đặt lịch thành công", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Lỗi đặt lịch: " + error.toString(), Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> m = new HashMap<>();
                m.put("clientId", String.valueOf(clientId));
                m.put("barberId", String.valueOf(barberId));
                m.put("date", date);
                m.put("time", time);
                m.put("services", new JSONArray(selectedServiceIds).toString());
                return m;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    // Hàm hiển thị dialog thông tin dịch vụ
    private void showServiceInfoDialog(int position) {
        try {
            // Kiểm tra position hợp lệ
            if (position < 0 || position >= serviceIds.size()) {
                Toast.makeText(this, "Dịch vụ không tồn tại", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Lấy thông tin từ API get_services.php
            int targetServiceId = serviceIds.get(position);
            
            RequestQueue queue = Volley.newRequestQueue(this);
            JsonArrayRequest serviceRequest = new JsonArrayRequest(Request.Method.GET, GET_SERVICES_URL, null,
                    response -> {
                        try {
                            // Tìm service có ID tương ứng
                            JSONObject targetService = null;
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject service = response.getJSONObject(i);
                                if (service.getInt("ServiceId") == targetServiceId) {
                                    targetService = service;
                                    break;
                                }
                            }
                            
                            if (targetService != null) {
                                String serviceName = targetService.getString("Name");
                                int price = targetService.getInt("Price");
                                int serviceTime = targetService.getInt("ServiceTime");

                                // Tạo dialog với thông tin chính xác
                                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                                builder.setTitle("Thông tin dịch vụ");
                                
                                String message = "Tên dịch vụ: " + serviceName + "\n" +
                                               "Giá: " + price + "đ\n" +
                                               "Thời gian: " + serviceTime + " phút\n\n";
                                
                                builder.setMessage(message);
                                builder.setPositiveButton("OK", null);
                                builder.show();
                            } else {
                                Toast.makeText(this, "Không tìm thấy thông tin dịch vụ", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Lỗi xử lý thông tin dịch vụ", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        error.printStackTrace();
                        Toast.makeText(this, "Lỗi tải thông tin dịch vụ", Toast.LENGTH_SHORT).show();
                    }
            );
            queue.add(serviceRequest);
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi hiển thị thông tin dịch vụ", Toast.LENGTH_SHORT).show();
        }
    }
}
