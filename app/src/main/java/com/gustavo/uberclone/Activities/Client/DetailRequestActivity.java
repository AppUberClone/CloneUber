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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.material.circularreveal.CircularRevealWidget;
import com.gustavo.uberclone.Providers.GoogleApiProvider;
import com.gustavo.uberclone.R;
import com.gustavo.uberclone.includes.MyToolbar;
import com.gustavo.uberclone.utils.DecodePoints;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailRequestActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;

    private Double  mExtraOriginLat;
    private Double mExtraOriginLng;
    private Double mExtraDestinationLat;
    private Double mExtraDestinationLng;
    private  String mExtraOrigin;
    private  String mExtraDestination;

    private LatLng mOriginLatLng;
    private LatLng mDestinationLatLng;

    private GoogleApiProvider mGoogleApiProvider;

    private List<LatLng> mPolylineList;
    private PolylineOptions mPolylineOptions;


    private TextView mTextViewOrigin;
    private  TextView mTextViewDestination;
    private  TextView mTextViewTime;
    private  TextView mTextViewDistance;
    private CircleImageView mCircleImageBack;

    private Button mButtonRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_request);

       // MyToolbar.show(this, "TUS DATOS", true);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        mExtraOriginLat =-17.339598304903827; /* getIntent().getDoubleExtra("origin_lat", 0); */
        mExtraOriginLng =-63.252528235316284;  /*getIntent().getDoubleExtra("origin_lng", 0); */
        mExtraDestinationLat =-17.3513322;  /*getIntent().getDoubleExtra("destination_lat", 0); */
        mExtraDestinationLng =-63.2570403;  /*getIntent().getDoubleExtra("destination_lng", 0); */
         mExtraOrigin ="origin";   /*getIntent().getStringExtra("origin");  */
         mExtraDestination ="destination";   /*getIntent().getStringExtra("destination"); */



        mOriginLatLng = new LatLng(mExtraOriginLat, mExtraOriginLng);
        mDestinationLatLng = new LatLng(mExtraDestinationLat, mExtraDestinationLng);

        mGoogleApiProvider = new GoogleApiProvider(DetailRequestActivity.this);

        mTextViewOrigin = findViewById(R.id.textViewOrigin);
        mTextViewDestination = findViewById(R.id.textViewDestination);
        mTextViewTime = findViewById(R.id.textViewTime);
        mTextViewDistance = findViewById(R.id.textViewDistance);

       /* mTextViewOrigin.setText(mExtraOrigin);
        mTextViewDestination.setText(mExtraDestination); */

        mButtonRequest = findViewById(R.id.btnRequestNow);
        mCircleImageBack = findViewById(R.id.circleImageBackHistoryBooking);

        mButtonRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoRequestDiver();
            }
        });

        mCircleImageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }

    private void gotoRequestDiver() {

        Intent intent = new Intent(DetailRequestActivity.this, RequestDriverActivity.class);
         intent.putExtra("origin_lat", mOriginLatLng.latitude);
         intent.putExtra("origin_lng", mOriginLatLng.longitude);
         intent.putExtra("origin", mExtraOrigin);
         intent.putExtra("destination", mExtraDestination);
         intent.putExtra("destination_lat", mDestinationLatLng.latitude);
         intent.putExtra("destination_lng", mDestinationLatLng.longitude);
        startActivity(intent);
        finish();
    }

    private  void  drawRoute(){
        mGoogleApiProvider.getDirections(mOriginLatLng, mDestinationLatLng).enqueue(new Callback<String>() {
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
                    mTextViewTime.setText(durationText);
                    mTextViewDistance.setText(distanceText);




                } catch (Exception e){
                    Log.d( "Error", "Error encontrado: " + e.getMessage());
                  }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

    @Override
    public void onMapReady(@NonNull  GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.addMarker(new MarkerOptions().position(mOriginLatLng).title("Origen").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_red)));
        mMap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                .target(mOriginLatLng)
                .zoom(14f)
                .build()
        ));
        drawRoute();
    }
}