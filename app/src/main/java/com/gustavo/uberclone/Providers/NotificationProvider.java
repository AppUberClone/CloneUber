package com.gustavo.uberclone.Providers;

import androidx.core.app.NotificationCompat;

import com.gustavo.uberclone.models.FCMBody;
import com.gustavo.uberclone.models.FCMResponse;
import com.gustavo.uberclone.retrofit.IFCMApi;
import com.gustavo.uberclone.retrofit.RetrofitClient;

import retrofit2.Call;

public class NotificationProvider {


    String url = "https://fcm.googleapis.com";

    public NotificationProvider(){

    }

    public Call<FCMResponse> sendNotification(FCMBody body){
        return RetrofitClient.getClientObject(url).create(IFCMApi.class).send(body);
    }


}
