package com.he161696.kingbarber;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.app.ProgressDialog;
import android.content.Context;
import android.widget.ListView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.he161696.kingbarber.model.Appointments;
import com.he161696.kingbarber.model.Appointment_services;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class CheckScheduleActivity extends AppCompatActivity {
    private ListView listView;
    private ScheduleAdapter adapter;
    private List<Appointments> schedules;
    private List<Appointment_services> allServices;
    private String urlWithBarberId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_check_schedule);

        int barberId = getIntent().getIntExtra("barberId", -1);
        if (barberId == -1) {
            Toast.makeText(this, "Error: Missing barberId", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        urlWithBarberId = "http://10.0.2.2/api/get_appointments_barber.php?barberId=" + barberId;
        listView = findViewById(R.id.CheckedSchedule);
        schedules = new ArrayList<>();
        allServices = new ArrayList<>();
        loadSchedules();
    }

    private void loadSchedules() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Loading...");
        dialog.show();
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                urlWithBarberId,
                null,
                response -> {
                    try {
                        schedules.clear();
                        allServices.clear();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            String status = obj.getString("Status");
                            if (!"Confirmed".equalsIgnoreCase(status)) continue;
                            
                            Appointments a = new Appointments();
                            a.setAppointmentId(obj.getInt("AppointmentId"));
                            a.setClientId(obj.getInt("ClientId"));
                            a.setBarberId(obj.getInt("BarberId"));
                            a.setStatus(status);
                            a.setRating(obj.optInt("Rating", -1));
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            a.setAppointmentDate(sdf.parse(obj.getString("AppointmentDate")));
                            a.setStartTime(obj.getString("StartTime"));
                            schedules.add(a);

                            // Load services cho appointment này
                            if (obj.has("services")) {
                                JSONArray serviceArray = obj.getJSONArray("services");
                                for (int j = 0; j < serviceArray.length(); j++) {
                                    JSONObject serviceObj = serviceArray.getJSONObject(j);
                                    Appointment_services service = new Appointment_services(
                                        a.getAppointmentId(),
                                        serviceObj.getInt("ServiceId"),
                                        serviceObj.getString("ServiceName"),
                                        serviceObj.getDouble("Price"),
                                        serviceObj.getInt("ServiceTime")
                                    );
                                    allServices.add(service);
                                }
                            } else {
                                // Nếu API không trả về services, load riêng
                                loadServicesForAppointment(a.getAppointmentId());
                            }
                        }
                        adapter = new ScheduleAdapter(this, schedules, allServices);
                        listView.setAdapter(adapter);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error processing data", Toast.LENGTH_SHORT).show();
                    } finally {
                        dialog.dismiss();
                    }
                },
                error -> {
                    Toast.makeText(this, "Could not load schedule data", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
        );
        queue.add(request);
    }

    private void loadServicesForAppointment(int appointmentId) {
        String url = "http://10.0.2.2/api/get_appointment_services.php?appointmentId=" + appointmentId;
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject serviceObj = response.getJSONObject(i);
                            Appointment_services service = new Appointment_services(
                                appointmentId,
                                serviceObj.getInt("ServiceId"),
                                serviceObj.getString("ServiceName"),
                                serviceObj.getDouble("Price"),
                                serviceObj.getInt("ServiceTime")
                            );
                            allServices.add(service);
                        }
                        // Refresh adapter sau khi load xong services
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    // Nếu không load được services, vẫn hiển thị appointment với endTime mặc định
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                }
        );
        queue.add(request);
    }


    // Adapter cho layout item_schedule
    public static class ScheduleAdapter extends BaseAdapter {
        private final Context context;
        private final List<Appointments> list;
        private final List<Appointment_services> allServices;
        
        public ScheduleAdapter(Context context, List<Appointments> list, List<Appointment_services> allServices) {
            this.context = context;
            this.list = list;
            this.allServices = allServices;
        }
        
        @Override
        public int getCount() { return list.size(); }
        @Override
        public Object getItem(int position) { return list.get(position); }
        @Override
        public long getItemId(int position) { return position; }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_schedule, parent, false);
                holder = new ViewHolder();
                holder.dateTv = convertView.findViewById(R.id.txtScheduleDate);
                holder.startTimeTv = convertView.findViewById(R.id.txtStartTime);
                holder.endTimeTv = convertView.findViewById(R.id.txtEndTime);
                holder.btnComplete = convertView.findViewById(R.id.btnCompleteSchedule);
                holder.btnCancel = convertView.findViewById(R.id.btnCancelSchedule);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            
            Appointments appt = list.get(position);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String dateStr = dateFormat.format(appt.getAppointmentDate());
            holder.dateTv.setText("Date: " + dateStr);
            holder.startTimeTv.setText("Start Time: " + appt.getStartTime());
            
            // Tính EndTime dựa trên StartTime và tổng ServiceTime của tất cả service cho appointment này
            int totalServiceTime = 0;
            boolean hasServices = false;
            for (Appointment_services service : allServices) {
                if (service.getAppointmentId() == appt.getAppointmentId()) {
                    totalServiceTime += service.getServiceTime();
                    hasServices = true;
                }
            }
            
            String endTimeStr;
            if (hasServices && totalServiceTime > 0) {
                endTimeStr = calculateEndTime(appt.getStartTime(), totalServiceTime);
            } else {
                endTimeStr = "Loading...";
            }
            holder.endTimeTv.setText("End Time: " + endTimeStr);
            
            // Xử lý trạng thái nút dựa trên status
            if ("Completed".equalsIgnoreCase(appt.getStatus())) {
                holder.btnComplete.setText("Completed");
                holder.btnComplete.setEnabled(false);
                holder.btnComplete.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4CAF50")));
                holder.btnComplete.setOnClickListener(null);
                holder.btnCancel.setVisibility(View.GONE);
            } else if ("Cancelled".equalsIgnoreCase(appt.getStatus())) {
                holder.btnCancel.setText("Cancelled");
                holder.btnCancel.setEnabled(false);
                holder.btnCancel.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F44336")));
                holder.btnCancel.setOnClickListener(null);
                holder.btnComplete.setVisibility(View.GONE);
            } else if ("Confirmed".equalsIgnoreCase(appt.getStatus())) {
                // Chỉ xử lý khi status là Confirmed
                holder.btnComplete.setText("Complete");
                holder.btnComplete.setEnabled(true);
                holder.btnComplete.setBackgroundTintList(null);
                holder.btnCancel.setText("Cancel");
                holder.btnCancel.setEnabled(true);
                holder.btnCancel.setBackgroundTintList(null);
                
                // Xử lý sự kiện Complete/Cancel nếu cần
                holder.btnComplete.setOnClickListener(v -> {
                    new android.app.AlertDialog.Builder(context)
                            .setTitle("Complete appointment")
                            .setMessage("Are you sure you want to complete this appointment?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                updateStatus(appt.getAppointmentId(), "Completed", holder.btnComplete);
                                appt.setStatus("Completed");
                                notifyDataSetChanged();
                            })
                            .setNegativeButton("No", null)
                            .show();
                });
                holder.btnCancel.setOnClickListener(v -> {
                    new android.app.AlertDialog.Builder(context)
                            .setTitle("Cancel appointment")
                            .setMessage("Are you sure you want to cancel this appointment?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                updateStatus(appt.getAppointmentId(), "Cancelled", holder.btnCancel);
                                appt.setStatus("Cancelled");
                                notifyDataSetChanged();
                            })
                            .setNegativeButton("No", null)
                            .show();
                });
            }
            return convertView;
        }
        
        // Static utility method để tính toán endTime
        private static String calculateEndTime(String startTime, int totalServiceTimeMinutes) {
            try {
                String[] parts = startTime.split(":");
                int hour = Integer.parseInt(parts[0]);
                int min = Integer.parseInt(parts[1]);
                
                int totalMin = hour * 60 + min + totalServiceTimeMinutes;
                int endHour = totalMin / 60;
                int endMin = totalMin % 60;
                
                // Xử lý trường hợp qua ngày
                if (endHour >= 24) {
                    endHour = endHour % 24;
                }
                
                return String.format("%02d:%02d", endHour, endMin);
            } catch (Exception e) {
                e.printStackTrace();
                return "Error";
            }
        }
        
        // Method để cập nhật trạng thái appointment
        private void updateStatus(int appointmentId, String status, Button btn) {
            String url = "http://10.0.2.2/api/update_appointment_status.php";
            RequestQueue queue = Volley.newRequestQueue(context);
            com.android.volley.toolbox.StringRequest request = new com.android.volley.toolbox.StringRequest(
                    Request.Method.POST, url,
                    response -> {
                        Toast.makeText(context, "Update successful!", Toast.LENGTH_SHORT).show();
                        if (status.equals("Completed")) {
                            btn.setText("Completed");
                            btn.setEnabled(false);
                            btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4CAF50")));
                        } else if (status.equals("Cancelled")) {
                            btn.setText("Cancelled");
                            btn.setEnabled(false);
                            btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F44336")));
                        }
                        // Refresh adapter để cập nhật UI
                        notifyDataSetChanged();
                    },
                    error -> {
                        Toast.makeText(context, "Update failed!", Toast.LENGTH_SHORT).show();
                    }) {
                @Override
                protected java.util.Map<String, String> getParams() {
                    java.util.Map<String, String> params = new java.util.HashMap<>();
                    params.put("appointmentId", String.valueOf(appointmentId));
                    params.put("status", status);
                    return params;
                }
            };
            queue.add(request);
        }
        
        static class ViewHolder {
            TextView dateTv, startTimeTv, endTimeTv;
            Button btnComplete, btnCancel;
        }
    }
}