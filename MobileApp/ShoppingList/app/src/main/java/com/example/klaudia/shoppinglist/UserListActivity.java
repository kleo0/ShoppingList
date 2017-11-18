package com.example.klaudia.shoppinglist;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import java.util.HashMap;
import java.util.Map;

public class UserListActivity extends AppCompatActivity {

    ListView userList;
    ArrayAdapter<String> arrayAdapter;
    ArrayList<String> arrayList;
    HashMap<String , String> hashMap;

    StoreData storeData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        userList = (ListView) findViewById(R.id.userList);
        arrayList = new ArrayList<String>();
        hashMap = new HashMap<String, String>();

        storeData = new StoreData(this);

        GetData();
    }

    public void SetList() {
        for(Map.Entry<String, String> entry : hashMap.entrySet()) {
            arrayList.add(entry.getValue());
        }
        arrayAdapter = new ArrayAdapter<String>(this, R.layout.addlistrow, arrayList);
        userList.setAdapter(arrayAdapter);
    }

    public void GetData() {
        try {
            Ion.with(getApplicationContext())
                    .load("http://skyapplab.duckdns.org:7777/list.php")
                    .setBodyParameter("token", URLEncoder.encode(storeData.GetToken(), "UTF-8"))
                    .setBodyParameter("action","dir")
                    .setBodyParameter("data","{}")
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
                                    for(int i = 0; i<jArray.length(); i++) {
                                        try {
                                            hashMap.put(jArray.getJSONObject(i).getString("id"),
                                                    jArray.getJSONObject(i).getString("n"));
                                        } catch (JSONException e1) {
                                            e1.printStackTrace();
                                        }
                                    }

                                    SetList();

                                } else {
                                    Toast.makeText(getApplicationContext(), "Send data error!"+result.get("ERR").toString(), Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(),"Check internet connection!", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
