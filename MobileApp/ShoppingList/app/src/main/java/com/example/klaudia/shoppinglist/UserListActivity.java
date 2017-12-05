package com.example.klaudia.shoppinglist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class UserListActivity extends AppCompatActivity {

    ListView userList;
    ArrayAdapter<String> arrayAdapter;
    ArrayList<String> arrayList;
    ArrayList<String> nameList;
    ArrayList<String> idList;

    StoreData storeData;
    String action;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        userList = (ListView) findViewById(R.id.userList);
        arrayList = new ArrayList<String>();
        idList = new ArrayList<String>();
        nameList = new ArrayList<String>();

        Intent intent = getIntent();
        action = intent.getStringExtra("onclick");


        storeData = new StoreData(this);

        ShowList();
        GetData();
    }

    public void SetList() {
        for (String entry : nameList) {
            arrayList.add(entry);
        }
        arrayAdapter = new ArrayAdapter<String>(this, R.layout.addlistrowbutton, arrayList);
        userList.setAdapter(arrayAdapter);
    }

    public void GetData() {
        try {
            Ion.with(getApplicationContext())
                    .load("http://skyapplab.duckdns.org:7777/list.php")
                    .setBodyParameter("token", URLEncoder.encode(storeData.GetToken(), "UTF-8"))
                    .setBodyParameter("action", "dir")
                    .setBodyParameter("data", "{}")
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if (result != null) {
                                if (result.get("ERR").toString().equals("0")) {
                                    //pobieranie danych
                                    String array = result.get("JSON_DATA").toString();

                                    JSONArray jArray = new JSONArray();
                                    try {
                                        jArray = new JSONArray(array);
                                    } catch (JSONException e1) {
                                        e1.printStackTrace();
                                    }
                                    for (int i = 0; i < jArray.length(); i++) {
                                        try {
                                            idList.add(jArray.getJSONObject(i).getString("id"));
                                            nameList.add(jArray.getJSONObject(i).getString("n"));
                                        } catch (JSONException e1) {
                                            e1.printStackTrace();
                                        }
                                    }

                                    SetList();

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

    public void ShowList() {
        userList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if(action.equals(MainActivity.KEY_EDIT_LIST)) {

                    Intent intent = new Intent(view.getContext(), ListActivity.class);
                    intent.putExtra("lid", idList.get(i));
                    intent.putExtra("name", nameList.get(i));
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(view.getContext(), StatsActivity.class);
                    intent.putExtra("lid", idList.get(i));
                    intent.putExtra("token", URLEncoder.encode(storeData.GetToken()));
                    startActivity(intent);
                }
            }
        });

    }
}
