package com.example.klaudia.shoppinglist;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.TypefaceProvider;
import com.beardedhen.androidbootstrap.font.FontAwesome;
import com.beardedhen.androidbootstrap.font.IconSet;

public class ListAddActivity extends AppCompatActivity {

    BootstrapButton name;
    EditText currentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_add);

        name = new BootstrapButton(getBaseContext());
        name = (BootstrapButton) findViewById(R.id.addlistName);
        currentName  = (EditText) findViewById(R.id.addListUserName);

        CheckText();
        ButtonName();
    }

    public void CheckText() {
        currentName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                name.setEnabled(true);
                name.setShowOutline(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public void ButtonName() {
        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentName.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Type name for list !", Toast.LENGTH_LONG).show();
                } else {

                }
            }
        });
    }
}
