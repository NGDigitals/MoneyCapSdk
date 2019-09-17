package com.moneycap.android.sdk.scanner.ui.activity;

import java.util.Locale;
import java.util.HashMap;
import java.text.NumberFormat;

import android.os.Build;
import android.os.Bundle;
import android.app.Dialog;
import android.content.Intent;
import android.content.Context;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.BaseAdapter;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import androidx.fragment.app.Fragment;
import android.content.DialogInterface;
import androidx.core.app.ActivityCompat;
import android.content.pm.PackageManager;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.Result;

import com.moneycap.android.sdk.scanner.R;
import com.moneycap.android.sdk.scanner.ui.dialog.MessageDialog;
import com.moneycap.android.sdk.scanner.ui.helper.NotificationBar;
import com.moneycap.android.sdk.scanner.ui.helper.Util;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.CAMERA;

public class ScanActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler,
        DialogInterface.OnDismissListener {

    private Context mContext;

    private HashMap<String, String> bookingMap;
    private static Locale locale =
            new Locale("en", "US");
    private static NumberFormat currencyFormatter =
            NumberFormat.getCurrencyInstance(locale);

    private ZXingScannerView mScannerView;
    private static final int REQUEST_CAMERA = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NotificationBar.makeTransparent(this);
        mContext = ScanActivity.this;
        mScannerView = new ZXingScannerView(this);
        setContentView(mScannerView);
        Intent intent = getIntent();
        bookingMap = (HashMap<String, String>)intent.getSerializableExtra("BOOKING_MAP");
        if(bookingMap.size() == 0) {
            Util.showMessageDialog(this, "Booking details not found", true);
        }else{
            startScanner();
        }
    }

    private void startScanner(){
        if(checkPermission()){
            if(mScannerView == null){
                mScannerView = new ZXingScannerView(this);
                setContentView(mScannerView);
            }
            mScannerView.setResultHandler(this);
            mScannerView.startCamera();
        }else{
            requestPermission();
        }
    }

    private boolean checkPermission(){
        return ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, REQUEST_CAMERA);
    }

    public void onRequestPermissionResult(int requestCode, String permissions[], int[] grantResults){
        switch(requestCode){
            case REQUEST_CAMERA:
                if(grantResults.length > 0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted){
                    }else{
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                            if(shouldShowRequestPermissionRationale(CAMERA)){
                                showMessageOKCancel(
                                        "You need to allow access to both the permission",
                                        (dialog, which) -> {
                                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                                                requestPermissions(new String[]{CAMERA}, REQUEST_CAMERA);
                                            }
                                        });
                            }
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener){
        new AlertDialog.Builder(mContext)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult){
        String resultText = rawResult.getText();
        String[] details = resultText.split("\\*\\*\\*");
        if(details == null) {
            Util.showMessageDialog(this, "Merchant details not found", true);
        }else{
            try{
                if (bookingMap.get("merchant_id").equals(details[0]) == false) {
                    Util.showMessageDialog(this, "Invalid merchant ID", true);
                }else if (bookingMap.get("merchant_type").equals(details[1]) == false) {
                    Util.showMessageDialog(this, "Invalid merchant type", true);
                }else{
                    final FragmentManager fragmentManager = getSupportFragmentManager();
                    Fragment fragment = fragmentManager.findFragmentByTag(ScannerDialog.FRAG_TAG);
                    if (fragment == null) {
                        final ScannerDialog dialog = ScannerDialog.newInstance(bookingMap);
                        dialog.show(fragmentManager, ScannerDialog.FRAG_TAG);
                    }
                }
            }catch (ArrayIndexOutOfBoundsException ex){
                Util.showMessageDialog(this, "Whoops, looks like something went wrong.", true);
            }

        }
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        mScannerView.resumeCameraPreview(ScanActivity.this);
    }

    public static class ScannerDialog extends DialogFragment {

        private Context context;
        private int size = 3;
        public static final String FRAG_TAG = "SCANNER_FRAG";

        static public ScannerDialog newInstance(HashMap<String, String> map) {
            ScannerDialog dialog = new ScannerDialog();
            Bundle args = new Bundle();
            args.putSerializable("BOOKING_MAP", map);
            dialog.setArguments(args);
            return dialog;
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            this.context = context;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        @NonNull
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context, R.style.CustomDialogTheme);
            ViewGroup viewGroup = getActivity().findViewById(android.R.id.content);

            View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_scanner,
                    viewGroup, false);
            HashMap<String, String> map = (HashMap<String, String>)
                    getArguments().getSerializable("BOOKING_MAP");
            TextView pickupView = dialogView.findViewById(R.id.pickup_view);
            TextView dropoffView = dialogView.findViewById(R.id.dropoff_view);
            pickupView.setText(map.get("start_terminal"));
            dropoffView.setText(map.get("stop_terminal"));
            TextView totalView = dialogView.findViewById(R.id.total_view);
            try {
                double amount = Double.parseDouble(map.get("amount"));
                totalView.setText(""+currencyFormatter.format(amount)+"");
            }catch (NumberFormatException ex){}
            Button cancelBtn = dialogView.findViewById(R.id.cancel_btn);
            cancelBtn.setOnClickListener(v -> getDialog().dismiss());
            Button checkInBtn = dialogView.findViewById(R.id.check_in_btn);
            checkInBtn.setOnClickListener(v -> {
                getDialog().dismiss();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                String fragTag = MessageDialog.FRAG_TAG;
                Fragment fragment = fragmentManager.findFragmentByTag(fragTag);
                if (fragment == null) {
                    MessageDialog dialog = MessageDialog.newInstance(
                            "This item has been added to your cart", false);
                    dialog.show(fragmentManager, fragTag);
                }
            });
            builder.setView(dialogView);
            builder.setCancelable(false);
            return builder.create();
        }

        class ListAdapter extends BaseAdapter {

            @Override
            public int getCount() {return size;}

            @Override
            public Object getItem(int i) {
                return null;
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(final int i, View view, ViewGroup parent) {

                LayoutInflater inflater = LayoutInflater.from(context);
                View rowView= inflater.inflate(R.layout.dialog_scanner_info, parent, false);
                TextView titleView = rowView.findViewById(R.id.title_view);
                TextView textView = rowView.findViewById(R.id.text_view);
                switch (i){
                    case 0:
                        //titleView.setText(getActivity().getResources().getString(R.string.serial_no));
                        textView.setText("N/A");
                        break;
                    case 1:
                        //titleView.setText(getActivity().getResources().getString(R.string.batch_no));
                        textView.setText("N/A");
                        break;
                    case 2:
                        //titleView.setText(getActivity().getResources().getString(R.string.expire_date));
                        textView.setText("N/A");
                        break;
                }
                return rowView;
            }
        }

        @Override
        public void onDismiss(final DialogInterface dialog) {
            super.onDismiss(dialog);
            if (getActivity() instanceof DialogInterface.OnDismissListener) {
                ((DialogInterface.OnDismissListener) getActivity()).onDismiss(dialog);
            }
        }
    }
}
