package com.example.geminipro.Adapter.Spinner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.geminipro.R;

import java.util.List;

public class CustomSpinnerAdapter extends ArrayAdapter<String> {

    private Context mContext;
    private List<String> mItems;
    private int mResource;

    public CustomSpinnerAdapter(@NonNull Context context, int resource, @NonNull List<String> items) {
        super(context, resource, items);
        mContext = context;
        mItems = items;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            view = inflater.inflate(mResource, parent, false);
        }

        TextView textView = view.findViewById(android.R.id.text1);
        textView.setText(mItems.get(position));

        return view;
    }
}
