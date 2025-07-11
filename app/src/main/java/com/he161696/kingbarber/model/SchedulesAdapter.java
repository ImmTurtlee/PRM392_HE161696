package com.he161696.kingbarber.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.he161696.kingbarber.BaseActivity;
import com.he161696.kingbarber.R;

import java.util.List;

public class SchedulesAdapter extends BaseAdapter {
    private Context mContext;
    private List<Barberschedules> list;

    public SchedulesAdapter(Context mContext, List<Barberschedules> list) {
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
        SchedulesAdapter.ViewHolder holder;
        if (convertView==null){
            convertView= LayoutInflater.from(mContext)
                    .inflate(R.layout.item_schedule,parent,false);
            holder=new SchedulesAdapter.ViewHolder();
            holder.WorkDateTv=convertView.findViewById(R.id.txtScheduleDate);
            holder.StartTimeTv=convertView.findViewById(R.id.txtStartTime);
            holder.EndTimeTv=convertView.findViewById(R.id.txtEndTime);
            convertView.setTag(holder);
        }
        else {
            holder=(SchedulesAdapter.ViewHolder) convertView.getTag();
        }
        Barberschedules schedules= list.get(position);
        return null;
    }
    static class ViewHolder {
        TextView WorkDateTv;
        TextView StartTimeTv;
        TextView EndTimeTv;
    }
}
