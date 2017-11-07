package com.example.klaudia.shoppinglist;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.beardedhen.androidbootstrap.BootstrapButton;

public class RegistationActivity extends AppCompatActivity {

    BootstrapButton signIn;
    BootstrapButton register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registation);

        signIn = (BootstrapButton) findViewById(R.id.regSignIn);
        register = (BootstrapButton) findViewById(R.id.regReg);

        SignIn();
        Register();
    }
    public void SignIn() {
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(),LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public void Register() {
        //TODO signIn form, server needed
    }
}
