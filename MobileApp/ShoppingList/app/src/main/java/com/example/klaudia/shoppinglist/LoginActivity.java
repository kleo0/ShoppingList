package com.example.klaudia.shoppinglist;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

public class LoginActivity extends AppCompatActivity {

    BootstrapButton signIn;
    BootstrapButton register;
    EditText login;
    EditText password;
    StoreData storeData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signIn = (BootstrapButton) findViewById(R.id.logLog);
        register = (BootstrapButton) findViewById(R.id.logReg);
        login = (EditText) findViewById(R.id.login);
        password = (EditText) findViewById(R.id.password);
        storeData = new StoreData(this);

        SignIn();
        Register();
    }

    public void SignIn() {
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!login.getText().toString().equals("") &&
                        !password.getText().toString().equals("")) {

                    Ion.with(getApplicationContext())
                            .load("http://skyapplab.duckdns.org:7777/login.php")
                            .setBodyParameter("nickname", login.getText().toString())
                            .setBodyParameter("password", password.getText().toString())
                            .asJsonObject()
                            .setCallback(new FutureCallback<JsonObject>() {
                                @Override
                                public void onCompleted(Exception e, JsonObject result) {
                                    if (result.get("ERR").toString().equals("0")) {
                                        storeData.SaveToken(result.get("TOKEN").toString());
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Login error. Try again!", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                } else {
                    Toast.makeText(getApplicationContext(), "Login and password are required!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void Register() {
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), RegistationActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }


}
