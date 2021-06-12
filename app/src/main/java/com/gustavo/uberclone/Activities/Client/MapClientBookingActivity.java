package com.gustavo.uberclone.Activities.Client;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.gustavo.uberclone.Providers.AuthProvider;
import com.gustavo.uberclone.Providers.ClientBookingProvider;
import com.gustavo.uberclone.Providers.ClientProvider;
import com.gustavo.uberclone.Providers.DriverProvider;
import com.gustavo.uberclone.Providers.GeofireProvider;
import com.gustavo.uberclone.Providers.GoogleApiProvider;
import com.gustavo.uberclone.Providers.TokenProvider;
import com.gustavo.uberclone.R;
import com.gustavo.uberclone.utils.DecodePoints;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapClientBookingActivity extends AppCompatActivity implements OnMapReadyCallback {


    AuthProvider mAuthProvider;
    GeofireProvider mGeofireProvider;
    private ClientBookingProvider mClientBookingProvider;
    private  TokenProvider mTokenProvider;
    private  ClientProvider mClientProvider;
    private DriverProvider mDriverProvider;

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;

    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocation;


    private Marker mMarkerDriver;



    private boolean mIsFirstTime = true;

    private  String mOrigin;
    private LatLng mOriginLatLng;
    private  LatLng mDriverLatLng;

    private String mDestination;
    private LatLng mDestinationLatLng;

    private  String mIdDriver;


    private TextView mTextviewDriverBooking;
    private TextView mTextViewEmailDriverBooking;
    private TextView mTextViewOriginBooking;
    private TextView mTextViewDestinationBooking;
    private TextView mTextViewStatusBooking;
    private ImageView mImageViewBooking;


    private GoogleApiProvider mGoogleApiProvider;
    private List<LatLng> mPolylineList;
    private PolylineOptions mPolylineOptions;

    private ValueEventListener mListener;
    private ValueEventListener mListenerStatus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_client_booking);
        mTextviewDriverBooking = findViewById(R.id.textViewDriverBooking);
        mTextViewEmailDriverBooking  = findViewById(R.id.textViewEmailDriverBooking);
        mTextViewOriginBooking = findViewById(R.id.textViewOriginBooking);
        mTextViewDestinationBooking = findViewById(R.id.textViewDestinationBooking);
        mTextViewStatusBooking = findViewById(R.id.textViewStatusBooking);

        mAuthProvider = new AuthProvider();
        mGeofireProvider = new GeofireProvider("drivers_working");
        mTokenProvider = new TokenProvider();
        mClientProvider = new ClientProvider();
        mClientBookingProvider = new ClientBookingProvider();
        mDriverProvider = new DriverProvider();
        mImageViewBooking = findViewById(R.id.imageViewClientBooking);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        }

        mMapFragment.getMapAsync(this);
         mAuthProvider = new AuthProvider();
        mClientBookingProvider = new ClientBookingProvider();
        mGoogleApiProvider = new GoogleApiProvider(MapClientBookingActivity.this);

        getStatus();
        getClientBooking();

    }

    private void getStatus() {

        mListenerStatus = mClientBookingProvider.getStatus(mAuthProvider.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull  DataSnapshot snapshot) {

                if (snapshot.exists()){

                    String status = snapshot.getValue().toString();
                    if (status.equals("accept")){
                        mTextViewStatusBooking.setText("Estado: Aceptado");
                    }

                    if (status.equals("start")){
                        mTextViewStatusBooking.setText("Estado: Viaje Iniciado");
                        startBooking();
                    } else if (status.equals("finish")){
                        mTextViewStatusBooking.setText("Estado: Viaje Finalizado");
                          finishBooking();
                      }
                }
            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });
    }

    private void finishBooking() {
        Intent intent = new Intent(MapClientBookingActivity.this, CalificationDriverActivity.class);
        startActivity(intent);
    }

    private void startBooking() {

        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));
        drawRoute(mDestinationLatLng);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mListener !=null){
            mGeofireProvider.getDriverLocation(mIdDriver).removeEventListener(mListener);
        }

        if (mListenerStatus != null) {
            mClientBookingProvider.getStatus(mAuthProvider.getId()).removeEventListener(mListenerStatus);
        }
    }

    private void getClientBooking() {
        mClientBookingProvider.getClientBooking(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){

                    String origin = snapshot.child("origin").getValue().toString();
                    String destination = snapshot.child("destination").getValue().toString();
                    String idDriver = snapshot.child("idDriver").getValue().toString();
                    mIdDriver = idDriver;
                    double originLat = Double.parseDouble(snapshot.child("originLat").getValue().toString());
                    double originLng = Double.parseDouble(snapshot.child("originLng").getValue().toString());
                    double destinationLat = Double.parseDouble(snapshot.child("destinationLat").getValue().toString());
                    double destinationLng = Double.parseDouble(snapshot.child("destinationLng").getValue().toString());
                    mOriginLatLng = new LatLng(originLat, originLng);
                    mDestinationLatLng = new LatLng(destinationLat, destinationLng);
                    mTextViewOriginBooking.setText("recoger en: " + origin);
                    mTextViewDestinationBooking.setText("destino: " + destination);
                    mMap.addMarker(new MarkerOptions().position(mOriginLatLng).title("Recoger aqui").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_red)));
                    getClient(idDriver);
                    getDriverLocation(idDriver);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private  void getClient(String idDriver){
        mDriverProvider.getDriver(idDriver).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull  DataSnapshot snapshot) {
                 if (snapshot.exists()){
                     String name = snapshot.child("name").getValue().toString();
                     String email = snapshot.child("email").getValue().toString();
                     String image = "";

                     if (snapshot.hasChild("image")){
                         image = snapshot.child("image").getValue().toString();
                         Picasso.with(MapClientBookingActivity.this).load(image).into(mImageViewBooking);
                     }

                     mTextviewDriverBooking.setText(name);
                     mTextViewEmailDriverBooking.setText(email);
                 }
            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });
    }

    private void getDriverLocation(String idDriver) {
         mListener = mGeofireProvider.getDriverLocation(idDriver).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull  DataSnapshot snapshot) {
                        if (snapshot.exists()){

                            double lat = Double.parseDouble(snapshot.child("0").getValue().toString());
                            double lng = Double.parseDouble(snapshot.child("1").getValue().toString());
                            mDriverLatLng = new LatLng(lat,lng);
                            if (mMarkerDriver != null){
                                mMarkerDriver.remove();
                            }
                            mMarkerDriver =mMap.addMarker( new MarkerOptions().position(
                                    new LatLng(lat, lng)
                                    )
                                            .title("Tu conductor")
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_car)));
                            if (mIsFirstTime){
                                mIsFirstTime = false;
                                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                                        new CameraPosition.Builder()
                                                .target(mDriverLatLng)
                                                .zoom(14f)
                                                .build()
                                ));
                                drawRoute(mOriginLatLng);
                            }
                        }
            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });
    }

    private  void  drawRoute( LatLng latLng){
        mGoogleApiProvider.getDirections(mDriverLatLng, latLng).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                try {
                    JSONObject jsonObject = new JSONObject(response.body());
                    JSONArray jsonArray = jsonObject.getJSONArray("routes");
                    JSONObject route = jsonArray.getJSONObject(0);
                    JSONObject polylines = route.getJSONObject("overview_polyline");
                    String points = polylines.getString("points");
                    mPolylineList = DecodePoints.decodePoly(points);
                    mPolylineOptions = new PolylineOptions();
                    mPolylineOptions.color(ContextCompat.getColor(getApplicationContext(),R.color.black));
                    mPolylineOptions.width(13f);
                    mPolylineOptions.startCap(new SquareCap());
                    mPolylineOptions.jointType(JointType.ROUND);
                    mPolylineOptions.addAll(mPolylineList);
                    mMap.addPolyline(mPolylineOptions);

                    JSONArray  legs = route.getJSONArray( "legs");
                    JSONObject leg = legs.getJSONObject(0);
                    JSONObject distance = leg.getJSONObject("distance");
                    JSONObject duration = leg.getJSONObject("duration");
                    String distanceText = distance.getString("text");
                    String  durationText = duration.getString("text");

                } catch (Exception e){
                    Log.d( "Error", "Error encontrado: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);



    }
}