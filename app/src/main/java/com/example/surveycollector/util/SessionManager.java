package com.example.surveycollector.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "survey_collector_session";
    private static final String KEY_ADMIN_ID = "admin_id";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private static volatile SessionManager INSTANCE;
    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    private SessionManager(Context context) {
        this.pref = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.editor = pref.edit();
    }

    public static SessionManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (SessionManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SessionManager(context);
                }
            }
        }
        return INSTANCE;
    }

    public void saveSession(int adminId) {
        editor.putInt(KEY_ADMIN_ID, adminId);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public int getAdminId() {
        return pref.getInt(KEY_ADMIN_ID, -1);
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}
