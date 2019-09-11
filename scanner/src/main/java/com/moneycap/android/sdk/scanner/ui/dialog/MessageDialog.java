package com.moneycap.android.sdk.scanner.ui.dialog;

import android.view.View;
import android.os.Bundle;
import android.app.Dialog;
import android.widget.Button;
import android.view.ViewGroup;
import android.content.Intent;
import android.widget.TextView;
import android.content.Context;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.content.DialogInterface;

import com.moneycap.android.sdk.scanner.R;

import androidx.fragment.app.DialogFragment;

import static android.app.Activity.RESULT_OK;

public class MessageDialog extends DialogFragment {

    private Context context;

    public static final String FRAG_TAG = "MESSAGE_FRAG";

    public static MessageDialog newInstance(String message, boolean finish) {
        MessageDialog dialog = new MessageDialog();
        Bundle args = new Bundle();
        args.putString("MESSAGE", message);
        args.putBoolean("FINISH", finish);
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
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme);
        ViewGroup viewGroup = getActivity().findViewById(android.R.id.content);

        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_message, viewGroup, false);
        builder.setView(dialogView);
        TextView messageView = dialogView.findViewById(R.id.message_view);
        messageView.setText(getArguments().getString("MESSAGE"));
        Button closeBtn = dialogView.findViewById(R.id.close_btn);
        closeBtn.setOnClickListener(v -> {
            getDialog().dismiss();
            boolean finish = getArguments().getBoolean("FINISH");
            if (finish){
                Intent intent = new Intent();
                intent.putExtra("FINISH", finish);
                getActivity().setResult(RESULT_OK, intent);
                getActivity().finish();
            }
        });
        setCancelable(false);
        return builder.create();
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        if (getActivity() instanceof DialogInterface.OnDismissListener) {
            ((DialogInterface.OnDismissListener) getActivity()).onDismiss(dialog);
        }
    }
}