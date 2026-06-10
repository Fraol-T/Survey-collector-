package com.example.surveycollector.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import com.example.surveycollector.service.SyncService;

public class NetworkReceiver extends BroadcastReceiver {

    private static final String TAG = "NetworkReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm == null) {
            Log.w(TAG, "ConnectivityManager not available");
            return;
        }

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            Log.d(TAG, "Network connected — starting SyncService");
            Intent syncIntent = new Intent(context, SyncService.class);
            context.startService(syncIntent);
        } else {
            Log.d(TAG, "Network disconnected — no action taken");
        }
    }
}
