package com.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.qr_codescan.R;
import com.objects.Operation;

import java.util.List;

/**
 * Created by jieping_yang on 2017/7/10.
 */

public class OperationAdapter extends BaseAdapter {
    List<Operation> list;
    private LayoutInflater mInflater;
    private Context context;

    public OperationAdapter(Context context, List<Operation> list) {
        this.list = list;
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int postion) {
        return list.get(postion);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.operation_item, null);
            holder = new ViewHolder();
            holder.image = (ImageView) convertView.findViewById(R.id.image);
            holder.name = (TextView) convertView.findViewById(R.id.operationName);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();
        Operation operation = list.get(position);
        holder.image.setImageDrawable(operation.icon);
        holder.name.setText(operation.operationName);
        return convertView;
    }

    private static class ViewHolder {
        ImageView image;
        TextView name;

    }
}
