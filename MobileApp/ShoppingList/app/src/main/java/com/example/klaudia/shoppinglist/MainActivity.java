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
    BootstrapButton statsShow;

    public final static String KEY_SHOW_STATS = "show stats";
    public final static String KEY_EDIT_LIST  = "edit list";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signOut = (BootstrapButton) findViewById(R.id.mainSignOut);
        listAdd = (BootstrapButton) findViewById(R.id.mainAddList);
        listShow = (BootstrapButton) findViewById(R.id.mainMainList);
        statsShow = (BootstrapButton) findViewById(R.id.mainStatistics);

        SignOut();
        ListAdd();
        ListShow();
        StatsShow();
    }

    public void SignOut() {
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

    }

    public void ListAdd() {
        listAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ListAddActivity.class);
                startActivity(intent);
            }
        });
    }

    public void ListShow() {
        listShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), UserListActivity.class);
                intent.putExtra("onclick", KEY_EDIT_LIST);
                startActivity(intent);
            }
        });
    }

    public void StatsShow() {
        statsShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), UserListActivity.class);
                intent.putExtra("onclick", KEY_SHOW_STATS);
                startActivity(intent);
            }
        });
    }
}
