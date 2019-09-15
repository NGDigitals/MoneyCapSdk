package com.moneycap.android.sdk.preference;

import android.content.Context;

import java.util.Set;
import java.util.HashSet;

import com.moneycap.android.sdk.preference.SecurePreferences.Editor;

public class PrefManager {

    private Editor editor;
    private SecurePreferences pref;

    private static final String BOOKINGS_STRING = "BOOKINGS_STRING";

    public PrefManager(Context context) {
        pref = new SecurePreferences(context);
        editor = pref.edit();
    }

    public void saveContent(String content) {
        editor.putString(BOOKINGS_STRING, content);
        editor.commit();
    }

    public String getContent() {
        return pref.getString(BOOKINGS_STRING, null);
    }
}
