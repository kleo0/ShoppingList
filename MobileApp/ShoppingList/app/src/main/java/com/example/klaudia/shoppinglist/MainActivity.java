package com.example.klaudia.shoppinglist;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.beardedhen.androidbootstrap.BootstrapButton;

public class MainActivity extends AppCompatActivity {

    BootstrapButton signOut;
    BootstrapButton listAdd;
    BootstrapButton listShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signOut = (BootstrapButton) findViewById(R.id.mainSignOut);
        listAdd = (BootstrapButton) findViewById(R.id.mainAddList);
        listShow = (BootstrapButton) findViewById(R.id.mainMainList);

        SignOut();
        ListAdd();
        ListShow();
    }

    public void SignOut() {
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    public void ListAdd() {
        listAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ListAddActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public void ListShow() {
        listShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), UserListActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
