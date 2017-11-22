package com.example.klaudia.shoppinglist;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.style.UpdateAppearance;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.builder.Builders;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {

    MyAdapter dataAdapter = null;
    String lid;
    String name;
    ListView listView;
    StoreData storeData;
    ArrayList<Product> productList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        Intent i = getIntent();
        lid = i.getStringExtra("lid");
        name = i.getStringExtra("name");

        storeData = new StoreData(this);
        CheckButton();
        AddButton();
        SaveButton();
        ScanButton();
        productList = new ArrayList<Product>();
        ReadData();

    }

    public void DisplayList() {
        dataAdapter = new MyAdapter(this,
                R.layout.listrow, productList);
        listView = (ListView) findViewById(R.id.itemList);
        listView.setAdapter(dataAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // When clicked, show a toast with the TextView text
                Product product = (Product) parent.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(),
                        "Clicked on Row: " + product.getName(),
                        Toast.LENGTH_LONG).show();
            }
        });


    }

    public void CheckButton() {
        BootstrapButton myButton = (BootstrapButton) findViewById(R.id.deleteButton);
        myButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                StringBuffer responseText = new StringBuffer();
                responseText.append("The following were selected...\n");

                ArrayList<Product> productList2 = dataAdapter.ProductList;
                ArrayList<Product> template = new ArrayList<Product>();
                for (int i = 0; i < productList2.size(); i++) {
                    Product product = productList2.get(i);
                    if (!product.isSelected()) {
                        template.add(product);
                    }
                }
                dataAdapter.ProductList.clear();
                productList.clear();
                productList.addAll(template);
                DisplayList();


            }
        });


    }

    public void ReadData() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("list_id", lid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            Ion.with(getApplicationContext())
                    .load("http://skyapplab.duckdns.org:7777/list.php")
                    .setBodyParameter("token", URLEncoder.encode(storeData.GetToken(), "UTF-8"))
                    .setBodyParameter("action", "get")
                    .setBodyParameter("data", obj.toString())
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if (result != null) {
                                if (result.get("ERR").toString().equals("0")) {
                                    //pobieranie danych
                                    JsonObject array = result.get("JSON_DATA").getAsJsonObject();

                                    JsonArray products = array.get("products").getAsJsonArray();
                                    for (int i = 0; i < products.size(); i++) {
                                        Product product = new Product(products.get(i).getAsJsonObject().get("n") + " " +
                                                products.get(i).getAsJsonObject().get("q"), false);
                                        productList.add(product);
                                    }
                                    DisplayList();

                                } else {
                                    Toast.makeText(getApplicationContext(), "Send data error!" + result.get("ERR").toString(), Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Check internet connection!", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void AddButton() {
        BootstrapButton myButton = (BootstrapButton) findViewById(R.id.addButton);
        myButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //dialog button
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ListActivity.this);
                alertDialogBuilder.setTitle("Product");
                alertDialogBuilder.setMessage("Add new product");

                final EditText input = new EditText(ListActivity.this);
                input.setTextColor(Color.BLACK);
                alertDialogBuilder.setView(input);

                final String[] value = new String[1];
                alertDialogBuilder.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                UpdateList(input.getText().toString());
                                return;
                            }
                        });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();



            }

        });
    }

    public void UpdateList(String input) {
        ArrayList<Product> productList2 = new ArrayList<Product>();
        productList2.addAll(dataAdapter.ProductList);

        String[] p = input.split(" ");
        Product product = new Product(p[0]+ " " + p[1], false);
        productList2.add(product);
        dataAdapter.ProductList.clear();
        productList.clear();
        productList.addAll(productList2);
        DisplayList();
    }


    public void SaveButton() {
        BootstrapButton myButton = (BootstrapButton) findViewById(R.id.saveButton);
        myButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
            }

        });
    }

    public void ScanButton() {
        BootstrapButton myButton = (BootstrapButton) findViewById(R.id.scanButton);
        myButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
            }

        });
    }
}