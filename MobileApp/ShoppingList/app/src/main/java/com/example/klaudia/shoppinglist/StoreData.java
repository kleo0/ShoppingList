package com.example.klaudia.shoppinglist;

import android.app.Activity;
import android.content.SharedPreferences;


public class StoreData {
    Activity activity;
    private static final String TOKEN = "token";
    private static final String TOKEN_TEXT = "tokenText";
    private static final String LISTID = "listID";
    private static final String LISTID_TEXT = "listIDText";
    private SharedPreferences preferences;
    private SharedPreferences preferencesList;
    public StoreData(Activity activity) {
        this.activity = activity;
        preferences = activity.getSharedPreferences(TOKEN, Activity.MODE_PRIVATE);
        preferencesList = activity.getSharedPreferences(LISTID, Activity.MODE_PRIVATE);

    }

    public void SaveToken(String token) {
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putString(TOKEN_TEXT, token);
        preferencesEditor.apply();
    }

    public String GetToken() {
        return preferences.getString(TOKEN_TEXT,"");
    }

    public void StoreListId() {

    }
}
