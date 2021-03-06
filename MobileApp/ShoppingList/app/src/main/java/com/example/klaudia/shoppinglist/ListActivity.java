package com.example.klaudia.shoppinglist;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Base64OutputStream;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ListActivity extends AppCompatActivity {

    MyAdapter dataAdapter = null;
    String lid;
    String name;
    ListView listView;
    StoreData storeData;
    ArrayList<Product> productList;
    JsonArray deleteList;
    JsonArray addList;

    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 100;
    public static final String ALLOW_KEY = "ALLOWED";
    public static final String CAMERA_PREF = "camera_pref";
    private static final int CAMERA_REQUEST = 1888;

    private String photoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        Intent i = getIntent();
        lid = i.getStringExtra("lid");
        name = i.getStringExtra("name");
        deleteList = new JsonArray();
        addList = new JsonArray();

        storeData = new StoreData(this);
        CheckButton();
        AddButton();
        SaveButton();
        ScanButton();
        productList = new ArrayList<Product>();
        ReadData();

        BootstrapButton statsButton = (BootstrapButton)findViewById(R.id.statsButton);
        statsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), StatsActivity.class);
                intent.putExtra("token", URLEncoder.encode(storeData.GetToken()));
                intent.putExtra("lid", lid);

                startActivity(intent);
            }
        });
    }

    public void DisplayList() {
        dataAdapter = new MyAdapter(this,
                R.layout.listrow, productList);
        listView = (ListView) findViewById(R.id.itemList);
        listView.setAdapter(dataAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // When clicked, show a toast with the TextView text
                Product product = (Product) parent.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(),
                        "Clicked on Row: " + product.getName(),
                        Toast.LENGTH_LONG).show();
            }
        });


    }

    public void CheckButton() {
        BootstrapButton myButton = (BootstrapButton) findViewById(R.id.deleteButton);
        myButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                StringBuffer responseText = new StringBuffer();
                responseText.append("The following were selected...\n");

                ArrayList<Product> productList2 = dataAdapter.ProductList;
                ArrayList<Product> template = new ArrayList<Product>();
                for (int i = 0; i < productList2.size(); i++) {
                    Product product = productList2.get(i);
                    if (!product.isSelected()) {
                        template.add(product);
                    } else {
                        //save data to send to server
                        JsonObject obj = new JsonObject();
                        String[] prod = product.getName().split(" ");
                        obj.addProperty("n",prod[0]);
                        obj.addProperty("q",prod[1]);
                        deleteList.add(obj);
                    }
                }
                dataAdapter.ProductList.clear();
                productList.clear();
                productList.addAll(template);
                DisplayList();


            }
        });


    }

    public void ReadData() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("list_id", lid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            Ion.with(getApplicationContext())
                    .load("http://skyapplab.duckdns.org:7777/list.php")
                    .setBodyParameter("token", URLEncoder.encode(storeData.GetToken(), "UTF-8"))
                    .setBodyParameter("action", "get")
                    .setBodyParameter("data", obj.toString())
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if (result != null) {
                                if (result.get("ERR").toString().equals("0")) {
                                    //pobieranie danych
                                    JsonObject array = result.get("JSON_DATA").getAsJsonObject();

                                    JsonArray products = array.get("products").getAsJsonArray();
                                    for (int i = 0; i < products.size(); i++) {
                                        String entry = products.get(i).getAsJsonObject().get("n") + " " +
                                                products.get(i).getAsJsonObject().get("q");
                                        entry = entry.replace("\"", "");
                                        Product product = new Product(entry, false);
                                        productList.add(product);
                                    }
                                    DisplayList();

                                } else {
                                    Toast.makeText(getApplicationContext(), "Send data error!" + result.get("ERR").toString(), Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Check internet connection!", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void AddButton() {
        BootstrapButton myButton = (BootstrapButton) findViewById(R.id.addButton);
        myButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //dialog button
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ListActivity.this);
                alertDialogBuilder.setTitle("Product");
                alertDialogBuilder.setMessage("Add new product");

                final EditText input = new EditText(ListActivity.this);
                input.setTextColor(Color.BLACK);
                alertDialogBuilder.setView(input);

                final String[] value = new String[1];
                alertDialogBuilder.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                UpdateList(input.getText().toString());
                                return;
                            }
                        });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();



            }

        });
    }

    public void UpdateList(String input) {
        ArrayList<Product> productList2 = new ArrayList<Product>();
        productList2.addAll(dataAdapter.ProductList);

        String[] p = input.split(" ");
        Product product = new Product(p[0]+ " " + p[1], false);
        productList2.add(product);

        //save data to send to server
        JsonObject obj = new JsonObject();
        obj.addProperty("n",p[0]);
        obj.addProperty("q",p[1]);
        addList.add(obj);

        dataAdapter.ProductList.clear();
        productList.clear();
        productList.addAll(productList2);
        DisplayList();
    }


    public void SaveButton() {
        BootstrapButton myButton = (BootstrapButton) findViewById(R.id.saveButton);
        myButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                JsonObject obj = new JsonObject();
                obj.addProperty("list_id",lid);
                obj.add("add",addList);
                obj.add("del",deleteList);
                Log.d("HELLO",obj.toString());
                try {
                    Ion.with(getApplicationContext())
                            .load("http://skyapplab.duckdns.org:7777/list.php")
                            .setBodyParameter("token", URLEncoder.encode(storeData.GetToken(), "UTF-8"))
                            .setBodyParameter("action", "mod")
                            .setBodyParameter("data", obj.toString())
                            .asJsonObject()
                            .setCallback(new FutureCallback<JsonObject>() {
                                @Override
                                public void onCompleted(Exception e, JsonObject result) {
                                    if (result != null) {
                                        if (result.get("ERR").toString().equals("0")) {
                                            //czy lista zapisana?
                                            Toast.makeText(getApplicationContext(),"Data seved!",Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Send data error!"
                                                    + result.get("ERR").toString(), Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Check internet connection!", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

        });
    }

    public void ScanButton() {
        BootstrapButton myButton = (BootstrapButton) findViewById(R.id.scanButton);
        myButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(ListActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    if (getFromPref(ListActivity.this, ALLOW_KEY)) {
                        showSettingsAlert();
                    } else if (ContextCompat.checkSelfPermission(ListActivity.this,
                            Manifest.permission.CAMERA)

                            != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(ListActivity.this,
                                Manifest.permission.CAMERA)) {
                            showAlert();
                        } else {
                            ActivityCompat.requestPermissions(ListActivity.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    MY_PERMISSIONS_REQUEST_CAMERA);
                        }
                    }
                } else {
                    openCamera();
                }
            }

        });
    }

    public static void saveToPreferences(Context context, String key, Boolean allowed) {
        SharedPreferences myPrefs = context.getSharedPreferences(CAMERA_PREF,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putBoolean(key, allowed);
        prefsEditor.apply();
    }

    public static Boolean getFromPref(Context context, String key) {
        SharedPreferences myPrefs = context.getSharedPreferences(CAMERA_PREF,
                Context.MODE_PRIVATE);
        return (myPrefs.getBoolean(key, false));
    }

    private void showAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(ListActivity.this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("App needs to access the Camera.");

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DONT ALLOW",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ALLOW",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ActivityCompat.requestPermissions(ListActivity.this,
                                new String[]{Manifest.permission.CAMERA},
                                MY_PERMISSIONS_REQUEST_CAMERA);
                    }
                });
        alertDialog.show();
    }

    private void showSettingsAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(ListActivity.this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("App needs to access the Camera.");

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DONT ALLOW",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //finish();
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "SETTINGS",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        startInstalledAppDetailsActivity(ListActivity.this);
                    }
                });

        alertDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                for (int i = 0, len = permissions.length; i < len; i++) {
                    String permission = permissions[i];

                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        boolean
                                showRationale =
                                ActivityCompat.shouldShowRequestPermissionRationale(
                                        this, permission);

                        if (showRationale) {
                            showAlert();
                        } else if (!showRationale) {
                            saveToPreferences(ListActivity.this, ALLOW_KEY, true);
                        }
                    }
                }
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public static void startInstalledAppDetailsActivity(final Activity context) {
        if (context == null) {
            return;
        }

        final Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + context.getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(i);
    }

    private void openCamera() {
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        startActivityForResult(intent, CAMERA_REQUEST);

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            }

            startActivityForResult(takePictureIntent, CAMERA_REQUEST);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        photoPath = image.getAbsolutePath();
        return image;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {

            File f = new File(photoPath);
            makeImageRecognition(f);
        }
    }

    private void makeImageRecognition(File file) {

        String encodedPhoto = getStringFile(file);
        Log.d("FILE", encodedPhoto);

        JsonObject obj = new JsonObject();
        obj.addProperty("img", encodedPhoto);

        //imageView.setImageBitmap(photo);
        try {
            Ion.with(getApplicationContext())
                    .load("http://skyapplab.duckdns.org:7777/list.php")
                    .setBodyParameter("token", URLEncoder.encode(storeData.GetToken(), "UTF-8"))
                    .setBodyParameter("action", "process")
                    .setBodyParameter("data", obj.toString())
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if (result != null) {
                                if (result.get("ERR").toString().equals("0")) {
                                    JsonArray array = result.get("JSON_DATA").getAsJsonArray();
                                    onDataRecognized(array);

                                } else if (result.get("ERR").toString().equals("11")) {
                                    Toast.makeText(getApplicationContext(),
                                            "Products from receipt cannot be recognized :( Try another shot!",
                                            Toast.LENGTH_LONG)
                                            .show();

                                } else {
                                    Toast.makeText(getApplicationContext(), "Send data error!" + result.get("ERR").toString(), Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Check internet connection!", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private String getStringFile(File f) {
        InputStream inputStream = null;
        String encodedFile= "", lastVal;
        try {
            inputStream = new FileInputStream(f.getAbsolutePath());

            byte[] buffer = new byte[10240];//specify the size to allow
            int bytesRead;
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            Base64OutputStream output64 = new Base64OutputStream(output, Base64.DEFAULT);

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                output64.write(buffer, 0, bytesRead);
            }
            output64.close();
            encodedFile =  output.toString();
        }
        catch (FileNotFoundException e1 ) {
            e1.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        lastVal = encodedFile;
        return lastVal;
    }

    private void onDataRecognized(JsonArray jsonArray) {
        Toast.makeText(getApplicationContext(), jsonArray.toString(), Toast.LENGTH_LONG).show();

        int n = jsonArray.size();
        for(int i=0; i<n; ++i) {
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
            String name = jsonObject.get("n").getAsString();
            int quantity = jsonObject.get("q").getAsInt();

            Product product = getProductByName(name);
            if(product == null) {
                continue;
            }

            product.setSelected(true);
        }

        DisplayList();
    }

    private Product getProductByName(String name) {
        for (Product product : productList) {
            if(product.name.contains(name)) {
                return product;
            }
        }

        return null;
    }
}