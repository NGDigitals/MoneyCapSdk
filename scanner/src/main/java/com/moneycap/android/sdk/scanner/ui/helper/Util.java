package com.moneycap.android.sdk.scanner.ui.helper;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;

import com.moneycap.android.sdk.scanner.ui.dialog.MessageDialog;

public class Util {
    public static void showMessageDialog(AppCompatActivity activity, String message, boolean finish) {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        String fragTag = MessageDialog.FRAG_TAG;
        Fragment fragment = fragmentManager.findFragmentByTag(fragTag);
        if (fragment == null) {
            MessageDialog dialog = MessageDialog.newInstance(message, finish);
            dialog.show(fragmentManager, fragTag);
        }
    }
}
