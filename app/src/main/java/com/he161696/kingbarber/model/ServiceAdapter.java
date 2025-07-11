package com.he161696.kingbarber.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.he161696.kingbarber.R;

import java.util.List;

public class ServiceAdapter extends BaseAdapter {
    private Context mContext;
    private List<Service> list;

    public ServiceAdapter(Context mContext, List<Service> list) {
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
        ServiceAdapter.ViewHolder holder;
        if (convertView==null){
            convertView= LayoutInflater.from(mContext)
                    .inflate(R.layout.item_service,parent,false);
            holder=new ServiceAdapter.ViewHolder();
            holder.NameTv=convertView.findViewById(R.id.txtServiceName);
            holder.PriceTv=convertView.findViewById(R.id.txtServicePrice);
            convertView.setTag(holder);
        }
        else {
            holder=(ServiceAdapter.ViewHolder) convertView.getTag();
        }
        Service service= list.get(position);
        return null;
    }
    static class ViewHolder {
        TextView NameTv;
        TextView PriceTv;
    }
}