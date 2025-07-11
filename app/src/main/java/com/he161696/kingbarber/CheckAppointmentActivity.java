package com.he161696.kingbarber;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.widget.ListView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.he161696.kingbarber.model.Appointments;
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

public class CheckAppointmentActivity extends AppCompatActivity {
    private ListView listView;
    private AppointmentBarberAdapter adapter;
    private List<Appointments> appointments;
    private String urlWithBarberId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_check_appointment);
        int barberId = getIntent().getIntExtra("barberId", -1);
        if (barberId == -1) {
            Toast.makeText(this, "Error: Missing barberId", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        urlWithBarberId = "http://10.0.2.2/api/get_appointments_barber.php?barberId=" + barberId;
        listView = findViewById(R.id.CheckedAppointments);
        appointments = new ArrayList<>();
        loadAppointments();
    }

    private void loadAppointments() {
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
                        appointments.clear();
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
                            // Chỉ thêm các lịch chưa bị Cancelled
                            if (!"Cancelled".equalsIgnoreCase(a.getStatus())) {
                                appointments.add(a);
                            }
                        }
                        adapter = new AppointmentBarberAdapter(this, appointments);
                        listView.setAdapter(adapter);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error processing data", Toast.LENGTH_SHORT).show();
                    } finally {
                        dialog.dismiss();
                    }
                },
                error -> {
                    Toast.makeText(this, "Could not load appointment data", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
        );
        queue.add(request);
    }

    // Adapter cho layout item_appointment_barber
    public static class AppointmentBarberAdapter extends BaseAdapter {
        private final Context context;
        private final List<Appointments> list;
        public AppointmentBarberAdapter(Context context, List<Appointments> list) {
            this.context = context;
            this.list = list;
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
                convertView = LayoutInflater.from(context).inflate(R.layout.item_appointment_barber, parent, false);
                holder = new ViewHolder();
                holder.dateTv = convertView.findViewById(R.id.txtAppointmentDate);
                holder.timeTv = convertView.findViewById(R.id.txtStartTime);
                holder.btnAccept = convertView.findViewById(R.id.btnAccept);
                holder.btnCancel = convertView.findViewById(R.id.btnCancel);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Appointments appt = list.get(position);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String dateStr = dateFormat.format(appt.getAppointmentDate());
            holder.dateTv.setText("Date: " + dateStr);
            holder.timeTv.setText("Time: " + appt.getStartTime());

            // Mặc định hiển thị cả hai nút
            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnCancel.setVisibility(View.VISIBLE);

            // Nếu status là Cancelled thì chỉ hiện nút Cancelled (đỏ, disabled), ẩn nút Accept
            if ("Cancelled".equalsIgnoreCase(appt.getStatus())) {
                holder.btnCancel.setText("Cancelled");
                holder.btnCancel.setEnabled(false);
                holder.btnCancel.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F44336")));
                holder.btnAccept.setVisibility(View.GONE);
                holder.btnCancel.setOnClickListener(null);
            } else if ("Confirmed".equalsIgnoreCase(appt.getStatus())) {
                holder.btnAccept.setText("Confirmed");
                holder.btnAccept.setEnabled(false);
                holder.btnAccept.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4CAF50")));
                holder.btnAccept.setOnClickListener(null);
                holder.btnCancel.setVisibility(View.GONE);
            } else if ("Completed".equalsIgnoreCase(appt.getStatus())) {
                holder.btnAccept.setText("Completed");
                holder.btnAccept.setEnabled(false);
                holder.btnAccept.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4CAF50")));
                holder.btnAccept.setOnClickListener(null);
                holder.btnCancel.setVisibility(View.GONE);
            } else {
                // Chỉ xử lý khi status là Pending
                holder.btnAccept.setText("Accept");
                holder.btnAccept.setEnabled(true);
                holder.btnAccept.setBackgroundTintList(null);
                holder.btnAccept.setOnClickListener(v -> {
                    updateStatus(appt.getAppointmentId(), "Confirmed", holder.btnAccept);
                    appt.setStatus("Confirmed");
                    notifyDataSetChanged();
                });
                holder.btnCancel.setText("Cancel");
                holder.btnCancel.setEnabled(true);
                holder.btnCancel.setBackgroundTintList(null);
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
        static class ViewHolder {
            TextView dateTv, timeTv;
            Button btnAccept, btnCancel;
        }
        private void updateStatus(int appointmentId, String status, Button btn) {
            String url = "http://10.0.2.2/api/update_appointment_status.php";
            RequestQueue queue = Volley.newRequestQueue(context);
            com.android.volley.toolbox.StringRequest request = new com.android.volley.toolbox.StringRequest(
                    Request.Method.POST, url,
                    response -> {
                        Toast.makeText(context, "Update successful!", Toast.LENGTH_SHORT).show();
                        if (status.equals("Confirmed")) {
                            btn.setText("Confirmed");
                            btn.setEnabled(false);
                            btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4CAF50")));
                            
                            // Chỉ tự động cancel các appointment pending khác khi chuyển từ Pending sang Confirmed
                            autoCancelOtherPendingAppointments(appointmentId);
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
                    // Chỉ gửi autoCancelPending khi chuyển sang Confirmed
                    if (status.equals("Confirmed")) {
                        params.put("autoCancelPending", "true");
                    }
                    return params;
                }
            };
            queue.add(request);
        }
        
        // Method để tự động cancel các appointment pending khác của cùng client
        // Chỉ áp dụng khi chuyển từ Pending sang Confirmed
        private void autoCancelOtherPendingAppointments(int confirmedAppointmentId) {
            // Tìm clientId của appointment đã được confirmed
            int clientId = -1;
            for (Appointments appt : list) {
                if (appt.getAppointmentId() == confirmedAppointmentId) {
                    clientId = appt.getClientId();
                    break;
                }
            }
            
            if (clientId != -1) {
                // Tìm và cancel tất cả appointment pending khác của client này
                for (Appointments appt : list) {
                    if (appt.getClientId() == clientId && 
                        appt.getAppointmentId() != confirmedAppointmentId && 
                        "Pending".equalsIgnoreCase(appt.getStatus())) {
                        
                        // Gọi API để cancel appointment này
                        String url = "http://10.0.2.2/api/update_appointment_status.php";
                        RequestQueue queue = Volley.newRequestQueue(context);
                        com.android.volley.toolbox.StringRequest request = new com.android.volley.toolbox.StringRequest(
                                Request.Method.POST, url,
                                response -> {
                                    // Cập nhật trạng thái trong list
                                    appt.setStatus("Cancelled");
                                    notifyDataSetChanged();
                                },
                                error -> {
                                    // Log lỗi nếu có
                                }) {
                            @Override
                            protected java.util.Map<String, String> getParams() {
                                java.util.Map<String, String> params = new java.util.HashMap<>();
                                params.put("appointmentId", String.valueOf(appt.getAppointmentId()));
                                params.put("status", "Cancelled");
                                // Không gửi autoCancelPending để tránh vòng lặp
                                return params;
                            }
                        };
                        queue.add(request);
                    }
                }
            }
        }
    }
}