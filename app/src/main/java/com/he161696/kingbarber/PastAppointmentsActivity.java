package com.he161696.kingbarber;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.he161696.kingbarber.model.Appointments;
import com.he161696.kingbarber.model.Appointment_services;
import com.he161696.kingbarber.model.AppointmentsAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PastAppointmentsActivity extends AppCompatActivity {
    private ListView listView;
    private AppointmentsAdapter adapter;
    private List<Appointments> appointments;
    private List<Appointment_services> services;
    String urlWithClientId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointments);
        int clientId = getIntent().getIntExtra("clientId", -1);
        if (clientId == -1) {
            Toast.makeText(this, "Lỗi: Thiếu clientId", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        urlWithClientId = "http://10.0.2.2/api/get_appointments.php?clientId=" + clientId;
        listView = findViewById(R.id.listAppointments);
        appointments = new ArrayList<>();
        services = new ArrayList<>();
        loadAppointments();
    }
    private void loadAppointments() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Loading...");
        dialog.show();
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                urlWithClientId,
                null,
                response -> {
                    try {
                        appointments.clear();
                        services.clear();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            Appointments a = new Appointments();
                            a.setAppointmentId(obj.getInt("AppointmentId"));
                            a.setClientId(obj.getInt("ClientId"));
                            a.setBarberId(obj.getInt("BarberId"));
                            a.setStatus(obj.getString("Status"));
                            a.setRating(obj.optInt("Rating", -1));
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            a.setAppointmentDate(sdf.parse(obj.getString("AppointmentDate")));
                            a.setStartTime(obj.getString("StartTime"));
                            appointments.add(a);
                            // SỬA ĐOẠN NÀY THEO API MỚI
                            JSONArray serviceArray = obj.getJSONArray("services");
                            for (int j = 0; j < serviceArray.length(); j++) {
                                JSONObject svcObj = serviceArray.getJSONObject(j);
                                Appointment_services svc = new Appointment_services();
                                svc.setAppointmentId(obj.getInt("AppointmentId"));
                                svc.setServiceId(svcObj.getInt("ServiceId"));
                                svc.setServiceName(svcObj.getString("ServiceName"));
                                svc.setPrice(svcObj.getDouble("Price"));
                                if (svcObj.has("ServiceTime")) {
                                    svc.setServiceTime(svcObj.getInt("ServiceTime"));
                                }
                                services.add(svc);
                            }
                        }
                        // Lọc chỉ lấy trạng thái Completed và Cancelled
                        List<Appointments> filteredAppointments = new ArrayList<>();
                        for (Appointments appt : appointments) {
                            String status = appt.getStatus();
                            if ("Completed".equalsIgnoreCase(status) || "Cancelled".equalsIgnoreCase(status)) {
                                filteredAppointments.add(appt);
                            }
                        }
                        adapter = new AppointmentsAdapter(this, filteredAppointments);
                        adapter.setServices(services);
                        adapter.setIsPastAppointments(true);
                        listView.setAdapter(adapter);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                    } finally {
                        dialog.dismiss();
                    }
                },
                error -> {
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        String body = new String(error.networkResponse.data);
                        android.util.Log.e("API_ERROR", "Phản hồi lỗi từ server: " + body);
                    }
                    Toast.makeText(this, "Không thể tải dữ liệu lịch hẹn", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
        );
        queue.add(request);
    }
} 