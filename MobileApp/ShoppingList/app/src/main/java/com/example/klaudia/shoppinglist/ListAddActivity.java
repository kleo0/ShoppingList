package com.example.klaudia.shoppinglist;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;

import java.util.ArrayList;
import java.util.Arrays;

public class ListAddActivity extends AppCompatActivity {

    BootstrapButton next;
    BootstrapButton previous;
    TextView instruction;
    ListView list;
    EditText edit;
    String currentName;
    String currentProducts;
    String currentUsers;
    String[] productArray;
    ArrayAdapter<String> arrayAdapter;
    ArrayList<String> arrayList;
    Integer step;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_add);

        next = new BootstrapButton(getBaseContext());
        next = (BootstrapButton) findViewById(R.id.addlistName);
        previous = new BootstrapButton(getBaseContext());
        previous = (BootstrapButton) findViewById(R.id.previous);
        instruction = (TextView) findViewById(R.id.instruction);
        list = (ListView) findViewById(R.id.productList);
        arrayList = new ArrayList<String>();

        edit = (EditText) findViewById(R.id.addListUserName);

        step = 0;
        previous.setVisibility(View.GONE);
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
        instruction.setText("Type products separate it by 'Enter'");
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
        instruction.setText("Type products separate it by 'Enter'");
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
    }

    public void CheckList() {
        instruction.setText("New Shopping List "+currentName);
        edit.setVisibility(View.GONE);
        next.setText("Create ");
        next.setBackgroundColor(Color.GREEN);
        list.setVisibility(View.VISIBLE);
        arrayList.clear();
        productArray = currentProducts.split("\\n");

        arrayList.addAll((Arrays.asList(productArray)));

        arrayAdapter = new ArrayAdapter<String>(this,R.layout.addlistrow,arrayList);
        list.setAdapter(arrayAdapter);

    }

    public void SaveData() {
        //TODO send to server
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }
}
