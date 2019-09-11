package com.moneycap.android.sdk.scanner.ui.activity;

import android.os.Build;
import android.os.Bundle;
import android.app.Dialog;
import android.content.Context;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.view.ViewGroup;
import android.widget.ListView;
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
import com.moneycap.android.sdk.scanner.ui.util.NotificationBar;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.CAMERA;

public class ScanActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler,
        DialogInterface.OnDismissListener {

    private Context mContext;

    private ZXingScannerView mScannerView;
    private static final int REQUEST_CAMERA = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NotificationBar.makeTransparent(this);
        mContext = ScanActivity.this;
        mScannerView = new ZXingScannerView(this);
        setContentView(mScannerView);
        startScanner();
    }

    private void startScanner(){
        Log.e("ZENO", "Starting...1");
        if(checkPermission()){
            Log.e("ZENO", "Starting...2");
            if(mScannerView == null){
                mScannerView = new ZXingScannerView(this);
                setContentView(mScannerView);
            }
            mScannerView.setResultHandler(this);
            mScannerView.startCamera();
        }else{
            Log.e("ZENO", "Starting...3");
            requestPermission();
        }
    }

    private boolean checkPermission(){
        Log.e("ZENO", "Starting...4");
        return ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(){
        Log.e("ZENO", "Starting...5");
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, REQUEST_CAMERA);
    }

    public void onRequestPermissionResult(int requestCode, String permissions[], int[] grantResults){
        Log.e("ZENO", "Retuening...1");
        switch(requestCode){
            case REQUEST_CAMERA:
                Log.e("ZENO", "Retuening...2");
                if(grantResults.length > 0){
                    Log.e("ZENO", "Retuening...3");
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted){
                        Log.e("ZENO", "Retuening...4");
                    }else{
                        Log.e("ZENO", "Retuening...5");
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                            Log.e("ZENO", "Retuening...6");
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
        String[] details = resultText.split("&");
        if(details.length == 3) {
            final FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag(ScannerDialog.FRAG_TAG);
            if (fragment == null) {
                final ScannerDialog dialog = ScannerDialog.newInstance(
                        details[0], details[1], details[2]);
                dialog.show(fragmentManager, ScannerDialog.FRAG_TAG);
            }
        }else{
            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag(MessageDialog.FRAG_TAG);
            if (fragment == null) {
                MessageDialog dialog = MessageDialog.newInstance(
                        "Not store item found for this QR Code", false);
                dialog.show(fragmentManager, MessageDialog.FRAG_TAG);
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
        private String name, company, price;
        public static final String FRAG_TAG = "SCANNER_FRAG";

        static public ScannerDialog newInstance(String name, String company, String price) {
            ScannerDialog dialog = new ScannerDialog();
            Bundle args = new Bundle();
            args.putString("NAME", name);
            args.putString("PRICE", price);
            args.putString("COMPANY", company);
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
            TextView nameView = dialogView.findViewById(R.id.name_view);
            TextView priceView = dialogView.findViewById(R.id.price_view);
            //TextView promoView = dialogView.findViewById(R.id.promo_view);
            TextView companyView = dialogView.findViewById(R.id.company_view);
            ListView listView = dialogView.findViewById(R.id.detail_list_view);
            listView.setDivider(null);
            ListAdapter listAdapter = new ListAdapter();
            listView.setAdapter(listAdapter);
            if (listView.getAdapter() == null) {
                listView.setAdapter(listAdapter);
            }
            listAdapter.notifyDataSetChanged();

            Button closeBtn = dialogView.findViewById(R.id.close_btn);
            closeBtn.setOnClickListener(v -> getDialog().dismiss());
            Button addBtn = dialogView.findViewById(R.id.add_btn);
            addBtn.setOnClickListener(v -> {
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
            name = getArguments().getString("NAME");
            company = getArguments().getString("COMPANY");
            nameView.setText(name);
            try {
                price = getArguments().getString("PRICE");
                //priceView.setText(currencyFormatter.format(price));
                priceView.setText(price);
            }catch (NumberFormatException e){}
            //promoView.setText(promo);
            companyView.setText(company);
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
