package com.example.klaudia.shoppinglist;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.beardedhen.androidbootstrap.BootstrapButton;

public class LoginActivity extends AppCompatActivity {

    BootstrapButton signIn;
    BootstrapButton register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signIn = (BootstrapButton) findViewById(R.id.logLog);
        register = (BootstrapButton) findViewById(R.id.logReg);

        SignIn();
        Register();
    }

    public void SignIn() {
        //TODO signIn form, server needed
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
