package com.gustavo.uberclone.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.gustavo.uberclone.Activities.Driver.MapDriverBookingActivity;
import com.gustavo.uberclone.Providers.AuthProvider;
import com.gustavo.uberclone.Providers.ClientBookingProvider;
import com.gustavo.uberclone.Providers.GeofireProvider;

public class AcceptReceiver extends BroadcastReceiver {

    private    ClientBookingProvider mClientBookingProvider;
    private GeofireProvider mGeofireProvider;
    private AuthProvider mAuthProvider;
    @Override
    public void onReceive(Context context, Intent intent) {

                mGeofireProvider = new GeofireProvider("active_drivers");
                mAuthProvider = new AuthProvider();
                mGeofireProvider.removeLocation(mAuthProvider.getId());
                String idClient = intent.getExtras().getString("idClient");
                mClientBookingProvider = new ClientBookingProvider();
                mClientBookingProvider.updateStatus(idClient,"accept");

        NotificationManager manager =  (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(2);

        Intent intent1 = new Intent(context, MapDriverBookingActivity.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent1.setAction(Intent.ACTION_RUN);
        intent1.putExtra("idClient", idClient);
        context.startActivity(intent1);

    }
}
