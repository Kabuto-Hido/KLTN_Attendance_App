package hcmute.edu.vn.tlcn.attendanceapp.Utility;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.material.button.MaterialButton;
import hcmute.edu.vn.tlcn.attendanceapp.R;

public class InternetCheckService extends BroadcastReceiver {
    AlertDialog dialog;
    @Override
    public void onReceive(Context context, Intent intent) {
        if(!NetworkUtil.isConnectedToInternet(context)){
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View layout_dialog = LayoutInflater.from(context).inflate(R.layout.check_internet_dialog, null);
            builder.setView(layout_dialog);

            MaterialButton btnRetry = (MaterialButton) layout_dialog.findViewById(R.id.btnRetry);

            dialog = builder.create();
            dialog.getWindow().setGravity(Gravity.CENTER);
            dialog.setCancelable(false);
            dialog.show();

            btnRetry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    onReceive(context,intent);
                }
            });
        }
    }
}
