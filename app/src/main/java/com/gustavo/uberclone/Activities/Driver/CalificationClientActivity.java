package com.gustavo.uberclone.Activities.Driver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.gustavo.uberclone.Providers.ClientBookingProvider;
import com.gustavo.uberclone.Providers.HistoryBookingProvider;
import com.gustavo.uberclone.R;
import com.gustavo.uberclone.models.ClientBooking;
import com.gustavo.uberclone.models.HistoryBooking;

import java.util.Date;

public class CalificationClientActivity extends AppCompatActivity {

    private TextView mTextViewOrigin;
    private TextView mTextViewDestination;
    private RatingBar mRatinBar;
    private Button  mButtonCalification;



    ClientBookingProvider mClientBookingProvider;
    private String mExtraClientId;

    private HistoryBooking mHistoryBooking;
    private HistoryBookingProvider mHistoryBookingProvider;

    private float mCalification = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calification_client);

        mTextViewOrigin = findViewById(R.id.textViewOriginCalification);
        mTextViewDestination = findViewById(R.id.textViewDestinationCalification);
        mRatinBar= findViewById(R.id.ratinbarCalification);
        mButtonCalification = findViewById(R.id.btnCalificationDriver);
        mClientBookingProvider = new ClientBookingProvider();
        mHistoryBookingProvider = new HistoryBookingProvider();

        mExtraClientId = getIntent().getStringExtra("idClient");

        mRatinBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                mCalification = rating;
            }
        });
        mButtonCalification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calificate();
            }
        });
        getClientBooking();

    }

    private void getClientBooking(){
        mClientBookingProvider.getClientBooking(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull  DataSnapshot snapshot) {
                if (snapshot.exists()){
                    ClientBooking clientBooking = snapshot.getValue(ClientBooking.class);
                    mTextViewOrigin.setText(clientBooking.getOrigin());
                    mTextViewDestination.setText(clientBooking.getDestination());

                    mHistoryBooking = new HistoryBooking(
                            clientBooking.getIdHistoryBooking(),
                            clientBooking.getIdClient(),
                            clientBooking.getIdDriver(),
                            clientBooking.getDestination(),
                            clientBooking.getOrigin(),
                            clientBooking.getTime(),
                            clientBooking.getKm(),
                            clientBooking.getStatus(),
                            clientBooking.getOriginLat(),
                            clientBooking.getOriginLng(),
                            clientBooking.getDestinationLat(),
                            clientBooking.getDestinationLng()
                    );



                }

            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });
    }

    private void calificate() {
        if (mCalification > 0){
            mHistoryBooking.setCalificationClient(mCalification);
            mHistoryBooking.setTimestamp(new Date().getTime());
            mHistoryBookingProvider.getHistoryBooking(mHistoryBooking.getIdHistoryBooking()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull  DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            mHistoryBookingProvider.updateCalificationClient(mHistoryBooking.getIdHistoryBooking(), mCalification);
                            Toast.makeText(CalificationClientActivity.this, "La calificacion se guardo correctamente", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(CalificationClientActivity.this, MapDriverActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            mHistoryBookingProvider.create(mHistoryBooking).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(CalificationClientActivity.this, "La calificacion se guardo correctamente", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(CalificationClientActivity.this, MapDriverActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });

                         }
                }

                @Override
                public void onCancelled(@NonNull  DatabaseError error) {
                    Toast.makeText(CalificationClientActivity.this, "Se encontro un error :" +error, Toast.LENGTH_SHORT).show();

                }
            });

        } else {
            Toast.makeText(this, "Debe ingresar la calificacion", Toast.LENGTH_SHORT).show();
          }
    }
}