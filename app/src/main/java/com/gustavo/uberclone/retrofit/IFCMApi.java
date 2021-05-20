package com.gustavo.uberclone.retrofit;

import com.gustavo.uberclone.models.FCMBody;
import com.gustavo.uberclone.models.FCMResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMApi {

    @POST("fcm/send")

    @Headers({
            "Content-type:application/json",
            "Authorization:key=AAAAL4blEsI:APA91bFm9io6jk3BsAkNRl95YY_bT-FPIGO8yKfcSkfwwpuA3PWo4AjYuAWPCtarEZ-01VFtOxoW_9FfEA2wUjDfpk4vew-eZEUA8Oa12MsU0MbY077yiri0fREy8mWM9v1eazw1pIsl"
    })

    Call<FCMResponse> send(@Body FCMBody body);
}
