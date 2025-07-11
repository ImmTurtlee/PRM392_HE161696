package com.he161696.kingbarber.model;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.he161696.kingbarber.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AppointmentsAdapter extends BaseAdapter {
    private boolean isPastAppointments = false;

    private Context mContext;
    private List<Appointments> list;
    private List<Appointment_services> services;  // Danh sách tất cả services của tất cả appointments

    public AppointmentsAdapter(Context mContext, List<Appointments> list) {
        this.mContext = mContext;
        this.list = list;
        this.services = new ArrayList<>();
    }
    public void setIsPastAppointments(boolean isPast) {
        this.isPastAppointments = isPast;
    }
    public void setServices(List<Appointment_services> services) {
        this.services = services;
    }

    public List<Appointment_services> getServices() {
        return services;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        TextView AppointmentDateTv;
        TextView StartTimeTv;
        TextView StatusTv;
        TextView TotalTimeTv;
        Button btnViewServices;
        Button btnRate;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_appointment, parent, false);
            holder = new ViewHolder();
            holder.AppointmentDateTv = convertView.findViewById(R.id.txtAppointmentDate);
            holder.StartTimeTv = convertView.findViewById(R.id.txtStartTime);
            holder.StatusTv = convertView.findViewById(R.id.txtStatus);
            holder.TotalTimeTv = convertView.findViewById(R.id.txtTotalTime);
            holder.btnViewServices = convertView.findViewById(R.id.btnViewServices);
            holder.btnRate = convertView.findViewById(R.id.btnRate);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Appointments appointment = list.get(position);

        // Format ngày
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateStr = dateFormat.format(appointment.getAppointmentDate());

        holder.AppointmentDateTv.setText("Date: " + dateStr);
        holder.StartTimeTv.setText("Time: " + appointment.getStartTime());
        holder.StatusTv.setText("Status: " + appointment.getStatus());

        // Tính total time từ services
        int totalTime = 0;
        int appointmentId = appointment.getAppointmentId();
        for (Appointment_services service : services) {
            if (service.getAppointmentId() == appointmentId) {
                totalTime += service.getServiceTime();
            }
        }
        holder.TotalTimeTv.setText("Total Time: " + totalTime + " min");

        holder.btnViewServices.setOnClickListener(v -> {
            // Lọc ra danh sách dịch vụ theo AppointmentId
            int AppointmentId = appointment.getAppointmentId();
            List<Appointment_services> filtered = new ArrayList<>();
            for (Appointment_services s : services) {
                if (s.getAppointmentId() == AppointmentId) {
                    filtered.add(s);
                }
            }

            if (filtered.isEmpty()) {
                new AlertDialog.Builder(mContext)
                        .setTitle("Dịch vụ")
                        .setMessage("Không có dịch vụ nào")
                        .setPositiveButton("OK", null)
                        .show();
            } else {
                StringBuilder sb = new StringBuilder();
                for (Appointment_services s : filtered) {
                    sb.append("- ").append(s.getServiceName())
                            .append(" (").append((int) s.getPrice()).append("đ)\n");
                }

                new AlertDialog.Builder(mContext)
                        .setTitle("Dịch vụ đã chọn")
                        .setMessage(sb.toString())
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
        
        // Ẩn/hiện nút đánh giá theo logic mới
        if (isPastAppointments && (appointment.getRating() == -1 || appointment.getRating() == 0)) {
            holder.btnRate.setVisibility(View.VISIBLE);
        } else {
            holder.btnRate.setVisibility(View.GONE);
        }

        holder.btnRate.setOnClickListener(v -> {
            // Hiển thị dialog_rating.xml
            View dialogView = LayoutInflater.from(mContext).inflate(R.layout.dialog_rating, null);
            android.widget.RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);

            new AlertDialog.Builder(mContext)
                    .setTitle("Rate")
                    .setView(dialogView)
                    .setPositiveButton("Send", (dialog, which) -> {
                        float rating = ratingBar.getRating();
                        int barberId = appointment.getBarberId();
                        sendRatingToServer(barberId, rating);
                        // Cập nhật rating cho appointment này và ẩn nút
                        appointment.setRating((int) rating);
                        notifyDataSetChanged();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        return convertView;
    }

    // Hàm gửi rating lên server
    private void sendRatingToServer(int barberId, float rating) {
        String url = "http://10.0.2.2/api/update_barber_rating.php";
        com.android.volley.RequestQueue queue = com.android.volley.toolbox.Volley.newRequestQueue(mContext);

        com.android.volley.toolbox.StringRequest request = new com.android.volley.toolbox.StringRequest(
                com.android.volley.Request.Method.POST, url,
                response -> {
                    android.widget.Toast.makeText(mContext, "successfully", android.widget.Toast.LENGTH_SHORT).show();
                },
                error -> {
                    android.widget.Toast.makeText(mContext, "Lỗi khi gửi đánh giá", android.widget.Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> params = new java.util.HashMap<>();
                params.put("barberId", String.valueOf(barberId));
                params.put("rating", String.valueOf(rating));
                return params;
            }
        };
        queue.add(request);
    }
}
