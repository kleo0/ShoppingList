package com.example.klaudia.shoppinglist;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.example.klaudia.shoppinglist.Product;
import com.example.klaudia.shoppinglist.R;

import java.util.ArrayList;

public class MyAdapter extends ArrayAdapter<Product> {

    public ArrayList<Product> ProductList;

    public MyAdapter(Context context, int textViewResourceId,
                     ArrayList<Product> ProductList) {
        super(context, textViewResourceId, ProductList);
        this.ProductList = new ArrayList<Product>();
        this.ProductList.addAll(ProductList);
    }

    private class ViewHolder {
        TextView name;
        CheckBox check;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        Log.v("ConvertView", String.valueOf(position));

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.listrow, null);

            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.textListRow);
            holder.check = (CheckBox) convertView.findViewById(R.id.checkboxListRow);
            convertView.setTag(holder);

            holder.check.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;
                    Product product = (Product) cb.getTag();
                    Toast.makeText(getContext().getApplicationContext(),
                            "Clicked on Checkbox: " + cb.getText() +
                                    " is " + cb.isChecked(),
                            Toast.LENGTH_LONG).show();
                    product.setSelected(cb.isChecked());
                }
            });
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        Product product = ProductList.get(position);
        holder.name.setText(" (" + product.getName() + ")");
        holder.name.setText(product.getName());
        holder.check.setChecked(product.isSelected());
        holder.check.setTag(product);

        return convertView;

    }
}
