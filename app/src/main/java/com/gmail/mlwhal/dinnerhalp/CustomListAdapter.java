package com.gmail.mlwhal.dinnerhalp;

import android.app.Activity;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by marika on 9/24/15.
 * See http://www.androidinterview.com/android-custom-listview-with-image-and-text-using-arrayadapter/
 */

class CustomListAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] itemName;
    private final Integer[] imageId;

    CustomListAdapter(Activity context, String[] itemName, Integer[] imageId) {
        super(context, R.layout.list_item, itemName);

        this.context = context;
        this.itemName = itemName;
        this.imageId = imageId;
    }

    @Override
    @NonNull
    public View getView(int position, View view, @NonNull ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.list_item, null, true);

        ImageView imageView = rowView.findViewById(R.id.image_list_item);
        TextView textView = rowView.findViewById(R.id.text_list_item);

        imageView.setImageResource(imageId[position]);
        textView.setText(itemName[position]);

        return rowView;
    }
}
