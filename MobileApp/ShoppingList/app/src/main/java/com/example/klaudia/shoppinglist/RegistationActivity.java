package com.example.klaudia.shoppinglist;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

public class RegistationActivity extends AppCompatActivity {

    BootstrapButton signIn;
    BootstrapButton register;
    EditText login;
    EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registation);

        signIn = (BootstrapButton) findViewById(R.id.regSignIn);
        register = (BootstrapButton) findViewById(R.id.regReg);
        login = (EditText) findViewById(R.id.login);
        password = (EditText) findViewById(R.id.password);

        SignIn();
        Register();
    }

    public void SignIn() {
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public void Register() {
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!login.getText().toString().equals("") &&
                        !password.getText().toString().equals("")) {

                    Ion.with(getApplicationContext())
                            .load("http://skyapplab.duckdns.org:7777/register.php")
                            .setBodyParameter("nickname", login.getText().toString())
                            .setBodyParameter("password", password.getText().toString())
                            .asJsonObject()
                            .setCallback(new FutureCallback<JsonObject>() {
                                @Override
                                public void onCompleted(Exception e, JsonObject result) {
                                   if (result.get("ERR").toString().equals("0")) {
                                       Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                       startActivity(intent);
                                       finish();
                                   } else {
                                       Toast.makeText(getApplicationContext(), "Registration error. Try again!", Toast.LENGTH_LONG).show();
                                   }
                                }
                            });
                } else {
                    Toast.makeText(getApplicationContext(), "Login and password are required!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
