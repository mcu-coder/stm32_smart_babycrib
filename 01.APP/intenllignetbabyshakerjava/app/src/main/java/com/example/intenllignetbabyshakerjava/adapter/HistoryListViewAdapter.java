package com.example.intenllignetbabyshakerjava.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.example.intenllignetbabyshakerjava.databinding.HistoryListviewItemBinding;
import com.example.intenllignetbabyshakerjava.db.Nurse;

import java.util.List;

public class HistoryListViewAdapter extends BaseAdapter {
    private List<Nurse> listData;
    private Context context;

    public HistoryListViewAdapter(List<Nurse> listData, Context context) {
        this.listData = listData;
        this.context = context;
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public Object getItem(int i) {
        return listData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            HistoryListviewItemBinding binding = HistoryListviewItemBinding.inflate(LayoutInflater.from(context), viewGroup, false);
            view = binding.getRoot();
            holder = new ViewHolder(binding);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        initView(holder, i);
        return view;
    }

    private void initView(ViewHolder holder, int index) {
        holder.binding.indexText.setText(String.valueOf(listData.get(index).getNid()));
        holder.binding.dateTime.setText(listData.get(index).getNdatetime());
    }

    private class ViewHolder {
        HistoryListviewItemBinding binding;

        public ViewHolder(HistoryListviewItemBinding binding) {
            this.binding = binding;
        }
    }
}
