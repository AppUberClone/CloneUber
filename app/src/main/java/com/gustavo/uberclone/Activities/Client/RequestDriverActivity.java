package com.gustavo.uberclone.Activities.Client;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.gustavo.uberclone.Providers.GeofireProvider;
import com.gustavo.uberclone.Providers.NotificationProvider;
import com.gustavo.uberclone.Providers.TokenProvider;
import com.gustavo.uberclone.R;
import com.gustavo.uberclone.models.FCMBody;
import com.gustavo.uberclone.models.FCMResponse;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestDriverActivity extends AppCompatActivity {

    private LottieAnimationView mAnimation;
    private TextView mTextViewLookingFor;
    private Button mButtonCancelRequest;

    private double mExtraOriginLat;
    private  double mExtraOriginLng;
    private LatLng mOriginLatLng;

    GeofireProvider mGeoFireProvider;

    private  double mRadius = 0.1;
    private  boolean mDriverFound = false;
    private  String mIdDriverFound;
    private  LatLng mDriverFoundLatLng;

    private NotificationProvider mNotificationProvider;
    private TokenProvider mTokenProvider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_driver);

        mAnimation = findViewById(R.id.animation);
        mTextViewLookingFor = findViewById(R.id.textViewLookingFor);
        mButtonCancelRequest = findViewById(R.id.btnCancelRequest);

        mAnimation.playAnimation();

        mExtraOriginLat = getIntent().getDoubleExtra("origin_lat",0);
        mExtraOriginLng = getIntent().getDoubleExtra("origin_lng", 0);
        mOriginLatLng = new LatLng(mExtraOriginLat,mExtraOriginLng);

        mGeoFireProvider = new GeofireProvider();
        mNotificationProvider = new NotificationProvider();
        mTokenProvider = new TokenProvider();


        getClosestDriver();
    }

    private  void getClosestDriver(){

        mGeoFireProvider.getActiveDrivers(mOriginLatLng, mRadius).addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                if (!mDriverFound){
                    mDriverFound = true;
                    mIdDriverFound = key;
                    mDriverFoundLatLng = new LatLng(location.latitude, location.longitude);
                    mTextViewLookingFor.setText("CONDUCTOR ENCOTRADO\nESPERANDO RESPUESTA");
                    sendNotification();
                    Log.d("DRIVER", "ID: " + mIdDriverFound);
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                // INGRESA CUANDO TERMINA LA BUSQUEDA DEL CONDUCTOR EN UN RADIO DE 0.1 KM
                if (!mDriverFound){
                    mRadius = mRadius + 0.1f;
                    //NO SE ENCONTRO NINGUN CONDUCTOR
                    if (mRadius > 6){
                        Toast.makeText(RequestDriverActivity.this, "NO SE ENCONTRO UN CONDUCTOR", Toast.LENGTH_SHORT).show();
                        return;
                    } else{
                        getClosestDriver();
                        }

                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void sendNotification() {

        mTokenProvider.getToken(mIdDriverFound).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull  DataSnapshot snapshot) {

                 String token = snapshot.child("token").getValue().toString();
                Map<String, String >  map = new HashMap<>();
                map.put("title","SOLICITUD DE SERVICIO");
                map.put("body","Cliente esta solicitando un servicio");
                FCMBody  fcmBody = new FCMBody(token,"high", map);
                mNotificationProvider.sendNotification(fcmBody).enqueue(new Callback<FCMResponse>() {
                    @Override
                    public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if (response.body() !=null){
                                if (response.body().getSuccess() == 1){
                                    Toast.makeText(RequestDriverActivity.this, "La notificacion ha sido enviada correctamente", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(RequestDriverActivity.this, "No se ah podido enviar la notificacion", Toast.LENGTH_SHORT).show();
                                  }
                            }
                    }

                    @Override
                    public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Log.d("Error", "Error " + t.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });
    }
}