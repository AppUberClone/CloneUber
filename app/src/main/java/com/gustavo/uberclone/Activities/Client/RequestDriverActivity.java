package com.gustavo.uberclone.Activities.Client;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.gustavo.uberclone.Providers.AuthProvider;
import com.gustavo.uberclone.Providers.ClientBookingProvider;
import com.gustavo.uberclone.Providers.GeofireProvider;
import com.gustavo.uberclone.Providers.GoogleApiProvider;
import com.gustavo.uberclone.Providers.NotificationProvider;
import com.gustavo.uberclone.Providers.TokenProvider;
import com.gustavo.uberclone.R;
import com.gustavo.uberclone.models.ClientBooking;
import com.gustavo.uberclone.models.FCMBody;
import com.gustavo.uberclone.models.FCMResponse;
import com.gustavo.uberclone.utils.DecodePoints;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestDriverActivity extends AppCompatActivity {

    private LottieAnimationView mAnimation;
    private TextView mTextViewLookingFor;
    private Button mButtonCancelRequest;

    private String mExtraOrigin;
    private String mExtraDestination;
    private double mExtraOriginLat;
    private  double mExtraOriginLng;
    private double mExtraDestinationLat;
    private double mExtraDestinationLng;
    private LatLng mOriginLatLng;
    private  LatLng mDestinationLatLng;

    GeofireProvider mGeoFireProvider;

    private  double mRadius = 0.1;
    private  boolean mDriverFound = false;
    private  String mIdDriverFound;
    private  LatLng mDriverFoundLatLng;

    private NotificationProvider mNotificationProvider;
    private TokenProvider mTokenProvider;
    private ClientBookingProvider mClientBookingProvider;
    private AuthProvider mAuthProvider;
    private GoogleApiProvider mGoogleApiProvider;

    private ValueEventListener mListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_driver);

        mAnimation = findViewById(R.id.animation);
        mTextViewLookingFor = findViewById(R.id.textViewLookingFor);
        mButtonCancelRequest = findViewById(R.id.btnCancelRequest);

        mAnimation.playAnimation();

        mExtraOrigin = getIntent().getStringExtra("origin");
        mExtraDestination = getIntent().getStringExtra("destination");
        mExtraOriginLat = getIntent().getDoubleExtra("origin_lat",0);
        mExtraOriginLng = getIntent().getDoubleExtra("origin_lng", 0);
        mExtraDestinationLat = getIntent().getDoubleExtra("destination_lat", 0);
        mExtraDestinationLng = getIntent().getDoubleExtra("destination_lng", 0);

        mOriginLatLng = new LatLng(mExtraOriginLat,mExtraOriginLng);
        mDestinationLatLng = new LatLng(mExtraDestinationLat,mExtraDestinationLng);

        mGeoFireProvider = new GeofireProvider("active_drivers");
        mNotificationProvider = new NotificationProvider();
        mAuthProvider = new AuthProvider();
        mTokenProvider = new TokenProvider();
        mClientBookingProvider = new ClientBookingProvider();
        mGoogleApiProvider = new GoogleApiProvider( RequestDriverActivity.this);

        mButtonCancelRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelRequest();
            }
        });

        getClosestDriver();
    }

    private void cancelRequest() {
            mClientBookingProvider.delete(mAuthProvider.getId()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                     sendNotificationCancel();
                }
            });
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
                    createClientBooking();
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

    private void createClientBooking(){

        mGoogleApiProvider.getDirections(mOriginLatLng, mDriverFoundLatLng).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                try {
                    JSONObject jsonObject = new JSONObject(response.body());
                    JSONArray jsonArray = jsonObject.getJSONArray("routes");
                    JSONObject route = jsonArray.getJSONObject(0);
                    JSONObject polylines = route.getJSONObject("overview_polyline");
                    String points = polylines.getString("points");

                    JSONArray  legs = route.getJSONArray( "legs");
                    JSONObject leg = legs.getJSONObject(0);
                    JSONObject distance = leg.getJSONObject("distance");
                    JSONObject duration = leg.getJSONObject("duration");
                    String distanceText = distance.getString("text");
                    String  durationText = duration.getString("text");

                    sendNotification(durationText, distanceText);

                } catch (Exception e){
                    Log.d( "Error", "Error encontrado: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });

    }

    private void sendNotificationCancel(){
        mTokenProvider.getToken(mIdDriverFound).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull  DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String token = snapshot.child("token").getValue().toString();
                    Map<String, String >  map = new HashMap<>();
                    map.put("title","VIAJE CANCELADO");
                    map.put("body",
                            "El cliente cancelo la solicitud"
                    );
                    FCMBody  fcmBody = new FCMBody(token,"high","4500s", map);
                    mNotificationProvider.sendNotification(fcmBody).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if (response.body() !=null){
                                if (response.body().getSuccess() == 1){
                                    Toast.makeText(RequestDriverActivity.this, "La solicitud se cancelo correctamente", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(RequestDriverActivity.this, MapClientActivity.class);
                                    startActivity(intent);
                                    finish();
                                    
                                    // Toast.makeText(RequestDriverActivity.this, "La notificacion ha sido enviada correctamente", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(RequestDriverActivity.this, "No se ah podido enviar la notificacion", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(RequestDriverActivity.this, "No se ah podido enviar la notificacion", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Log.d("Error", "Error " + t.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });
    }

    private void sendNotification(final String time, final String km) {

        mTokenProvider.getToken(mIdDriverFound).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull  DataSnapshot snapshot) {
                 if (snapshot.exists()){
                     String token = snapshot.child("token").getValue().toString();
                     Map<String, String >  map = new HashMap<>();
                     map.put("title","SOLICITUD DE SERVICIO A " +time + " DE TU POSICION");
                     map.put("body",
                             "Un cliente esta solicitando un servicio a una distancia de " + km + "\n" +
                             "Recoger en: " + mExtraOrigin + "\n" +
                             "Destino: " + mExtraDestination
                            );
                     map.put("idClient",mAuthProvider.getId());
                     map.put("origin",mExtraOrigin);
                     map.put("destination", mExtraDestination);
                     map.put("min", time);
                     map.put("distance", km);
                     FCMBody  fcmBody = new FCMBody(token,"high","4500s", map);
                     mNotificationProvider.sendNotification(fcmBody).enqueue(new Callback<FCMResponse>() {
                         @Override
                         public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                             if (response.body() !=null){
                                 if (response.body().getSuccess() == 1){
                                     ClientBooking clientBooking = new ClientBooking(
                                             mAuthProvider.getId(),
                                             mIdDriverFound,
                                             mExtraDestination,
                                             mExtraOrigin,
                                             time,
                                             km,
                                             "create",
                                             mExtraOriginLat,
                                             mExtraOriginLng,
                                             mExtraDestinationLat,
                                             mExtraDestinationLng
                                     );

                                     mClientBookingProvider.create(clientBooking).addOnSuccessListener(new OnSuccessListener<Void>() {
                                         @Override
                                         public void onSuccess(Void unused) {
                                             checkStatusClientBooking();
                                             // Toast.makeText(RequestDriverActivity.this, "La peticion se creo correctamente", Toast.LENGTH_SHORT).show();
                                         }
                                     });
                                    // Toast.makeText(RequestDriverActivity.this, "La notificacion ha sido enviada correctamente", Toast.LENGTH_SHORT).show();
                                 } else {
                                     Toast.makeText(RequestDriverActivity.this, "No se ah podido enviar la notificacion", Toast.LENGTH_SHORT).show();
                                 }
                             } else {
                                 Toast.makeText(RequestDriverActivity.this, "No se ah podido enviar la notificacion", Toast.LENGTH_SHORT).show();
                             }
                         }

                         @Override
                         public void onFailure(Call<FCMResponse> call, Throwable t) {
                             Log.d("Error", "Error " + t.getMessage());
                         }
                     });
                 }
            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });
    }

    public  void checkStatusClientBooking(){

             mListener = mClientBookingProvider.getStatus(mAuthProvider.getId()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull  DataSnapshot snapshot) {

                    if (snapshot.exists()){
                        String status = snapshot.getValue().toString();
                        if (status.equals("accept")){
                            Intent intent = new Intent(RequestDriverActivity.this, MapClientBookingActivity.class);
                            startActivity(intent);
                            finish();
                        } else if (status.equals("cancel")){
                            Toast.makeText(RequestDriverActivity.this, "El conductor no acepto el viaje", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(RequestDriverActivity.this, MapClientActivity.class);
                            startActivity(intent);
                            finish();
                           }
                    }
                }

                @Override
                public void onCancelled(@NonNull  DatabaseError error) {

                }
            });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mListener !=null){
            mClientBookingProvider.getStatus(mAuthProvider.getId()).removeEventListener(mListener);
        }
    }
}