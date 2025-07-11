package com.he161696.kingbarber.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.he161696.kingbarber.R;

import java.util.List;

public class ClientsAdapter extends BaseAdapter {
    private Context mContext;
    private List<Clients> list;

    public ClientsAdapter(Context mContext, List<Clients> list) {
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
        if (convertView==null){
            convertView= LayoutInflater.from(mContext)
                    .inflate(R.layout.item_clients,parent,false);
            holder=new ViewHolder();
            holder.ClientIdTv=convertView.findViewById(R.id.txtClientId);
            holder.FullNameTv=convertView.findViewById(R.id.txtFullName);
            holder.EmailTv=convertView.findViewById(R.id.txtEmail);
            convertView.setTag(holder);
        }
        else {
            holder=(ViewHolder) convertView.getTag();
        }
        Clients clients= list.get(position);
        return null;
    }
    static class ViewHolder{
        TextView ClientIdTv;
        TextView FullNameTv;
        TextView EmailTv;
    }
}
