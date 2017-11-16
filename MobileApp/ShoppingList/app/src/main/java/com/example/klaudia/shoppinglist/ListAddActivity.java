package com.example.klaudia.shoppinglist;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

public class ListAddActivity extends AppCompatActivity {

    BootstrapButton next;
    BootstrapButton previous;
    TextView instruction;
    TextView userText;
    ListView list;
    EditText edit;
    String currentName;
    String currentProducts;
    String currentUsers;
    String[] productArray;
    String[] pqArray;
    ArrayAdapter<String> arrayAdapter;
    ArrayList<String> arrayList;
    JSONArray productsJArray;
    ArrayList usersArray;
    Integer step;

    StoreData storeData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_add);

        next = new BootstrapButton(getBaseContext());
        next = (BootstrapButton) findViewById(R.id.addlistName);
        previous = new BootstrapButton(getBaseContext());
        previous = (BootstrapButton) findViewById(R.id.previous);
        instruction = (TextView) findViewById(R.id.instruction);
        userText = (TextView) findViewById(R.id.userText);
        list = (ListView) findViewById(R.id.productList);
        arrayList = new ArrayList<String>();
        productsJArray = new JSONArray();
        usersArray = new ArrayList<>();

        storeData = new StoreData(this);

        edit = (EditText) findViewById(R.id.addListUserName);

        step = 0;
        previous.setVisibility(View.GONE);
        userText.setVisibility(View.GONE);
        CheckText();
        ButtonNext();
        ButtonPrevious();
    }

    public void CheckText() {
        edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                next.setEnabled(true);
                next.setShowOutline(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public void ButtonNext() {
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                switch (step) {
                    case 0:
                        if (edit.getText().toString().equals("")) {
                            Toast.makeText(getApplicationContext(), "Type name for list !", Toast.LENGTH_LONG).show();
                        } else {
                            step = 1;
                            SetAddProducts();
                        }
                        break;
                    case 1:
                        if (edit.getText().toString().equals("")) {
                            Toast.makeText(getApplicationContext(), "Type products !", Toast.LENGTH_LONG).show();
                        } else {
                            step = 2;
                            SetAddUsers();
                        }
                        break;
                    case 2:
                        step = 3;
                        CheckList();
                        break;
                    case 3:
                        SaveData();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    public void ButtonPrevious() {
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                switch (step) {
                    case 1:
                        step = 0;
                        SetAddNameListBack();
                        break;
                    case 2:
                        step = 1;
                        SetAddProductsBack();
                        break;
                    case 3:
                        step = 2;
                        SetAddUsersBack();
                    default:
                        break;
                }
            }
        });

    }

    public void SetAddProducts() {
        instruction.setText("Type products separate it by 'Enter' and quantity separate by 'Space'. Example: egg 3");
        currentName = edit.getText().toString();
        edit.setText(currentProducts);
        previous.setVisibility(View.VISIBLE);
        previous.setShowOutline(false);
        next.setShowOutline(true);
        next.setEnabled(false);
    }

    public void SetAddUsers() {
        instruction.setText("Type users separate it by 'Enter'");
        currentProducts = edit.getText().toString();
        edit.setText(currentUsers);
        previous.setVisibility(View.VISIBLE);
        previous.setShowOutline(false);
    }

    public void SetAddNameListBack() {
        instruction.setText("Type name for new list");
        edit.setText(currentName);
        previous.setVisibility(View.GONE);
        previous.setShowOutline(true);
        next.setShowOutline(true);
        next.setEnabled(false);
    }

    public void SetAddProductsBack() {
        instruction.setText("Type products separate it by 'Enter' and quantity separate by 'Space'. Example: egg 3");
        edit.setText(currentProducts);
        next.setShowOutline(true);
        next.setEnabled(false);
    }

    public void SetAddUsersBack() {
        instruction.setText("Type users separate it by 'Enter'");
        edit.setText(currentUsers);
        edit.setVisibility(View.VISIBLE);
        list.setVisibility(View.GONE);
        next.setText("NEXT ");
        next.setBackgroundColor(0x009966);
        next.setShowOutline(true);
        next.setEnabled(false);
        userText.setVisibility(View.GONE);
    }

    public void CheckList() {
        currentUsers = edit.getText().toString();
        instruction.setText("New Shopping List " + currentName);
        edit.setVisibility(View.GONE);
        next.setText("Create ");
        next.setBackgroundColor(Color.GREEN);

        userText.setVisibility(View.VISIBLE);
        userText.setText("USERS: " + currentUsers);

        list.setVisibility(View.VISIBLE);
        arrayList.clear();
        productArray = currentProducts.split("\\n");

        arrayList.addAll((Arrays.asList(productArray)));

        arrayAdapter = new ArrayAdapter<String>(this, R.layout.addlistrow, arrayList);
        list.setAdapter(arrayAdapter);

        //dzielenie produktu na nazwę i ilość
        for (int i = 0; i < productArray.length; i++) {
            pqArray = productArray[i].split(" ");
            Integer n = pqArray.length - 1;
            JSONObject obj = new JSONObject();
            if (TextUtils.isDigitsOnly(pqArray[n])) {
                String str = "";
                for (int k = 0; k < n; k++) {
                    str = str + pqArray[k];
                }
                try {
                    obj = new JSONObject("{n:" + str + ",q:" + pqArray[n] + "}");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                String str = "";
                for (int k = 0; k <= n; k++) {
                    str = str + pqArray[k];
                }
                try {
                    obj = new JSONObject("{n:" + str + ",q:1}");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            productsJArray.put(obj);
        }
        String[] users = currentUsers.split("\\n");
        for (int i = 0; i < users.length; i++) {
            usersArray.add(users[i]);
        }

    }

    public void SaveData() {

        final JSONObject sendJSON = new JSONObject();
        try {
            sendJSON.put("list_name", currentName);
            sendJSON.put("list_products", productsJArray);
            sendJSON.put("users", usersArray);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            Ion.with(getApplicationContext())
                    .load("http://skyapplab.duckdns.org:7777/list.php")
                    .setBodyParameter("token", URLEncoder.encode(storeData.GetToken(), "UTF-8"))
                    .setBodyParameter("action", "new")
                    .setBodyParameter("data", sendJSON.toString())
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if (result != null) {
                                if (result.get("ERR").toString().equals("0")) {
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Send data error!" + result.get("ERR").toString() + sendJSON.toString(), Toast.LENGTH_LONG).show();
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
