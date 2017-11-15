package com.example.klaudia.shoppinglist;

import android.app.Activity;
import android.content.SharedPreferences;


public class StoreData {
    Activity activity;
    private static final String TOKEN = "token";
    private static final String TOKEN_TEXT = "tokenText";
    private SharedPreferences preferences;

    public StoreData(Activity activity) {
        this.activity = activity;
        preferences = activity.getSharedPreferences(TOKEN, Activity.MODE_PRIVATE);

    }

    public void SaveToken(String token) {
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putString(TOKEN_TEXT, token);
        preferencesEditor.apply();
    }

    public String GetToken() {
        return preferences.getString(TOKEN_TEXT,"");
    }
}
