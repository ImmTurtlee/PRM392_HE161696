package com.he161696.kingbarber.model;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.he161696.kingbarber.BookAppointmentActivity;
import com.he161696.kingbarber.R;

import java.util.List;

public class BarbersApdapter extends BaseAdapter {
    private Context mContext;
    private List<Barbers> list;
    private int clientId;
    public void setClientId(int clientId) {
        this.clientId = clientId;
    }
    public BarbersApdapter(Context mContext, List<Barbers> list) {
        this.mContext = mContext;
        this.list = list;
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext)
                    .inflate(R.layout.item_barbers, parent, false);
            holder = new ViewHolder();
            holder.BarbersIdTv = convertView.findViewById(R.id.txtBarberId);
            holder.FullNameTv = convertView.findViewById(R.id.txtFullName);
            holder.EmailTv = convertView.findViewById(R.id.txtEmail);
            holder.AverageRatingTv = convertView.findViewById(R.id.txtAverageRating);
            holder.RatingCountTv = convertView.findViewById(R.id.txtRatingCount);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Gán dữ liệu vào các TextView
        Barbers barbers = list.get(position);
        holder.BarbersIdTv.setText("Barber ID: #" + barbers.getBarberId());
        holder.FullNameTv.setText(barbers.getFullName());
        holder.EmailTv.setText(barbers.getEmail());
        holder.AverageRatingTv.setText("Average Rating: " + barbers.getAverageRating());
        holder.RatingCountTv.setText("Total Ratings: " + barbers.getRatingCount());
        holder.BookNowBtn = convertView.findViewById(R.id.btnBookNow);
        holder.BookNowBtn.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, BookAppointmentActivity.class);
            intent.putExtra("barberId", barbers.getBarberId());
            intent.putExtra("clientId", clientId); // nếu cần clientId
            mContext.startActivity(intent);
        });

        return convertView;
    }

    static class ViewHolder {
        TextView BarbersIdTv;
        TextView FullNameTv;
        TextView EmailTv;
        TextView AverageRatingTv;
        TextView RatingCountTv;
        Button BookNowBtn;

    }

}
