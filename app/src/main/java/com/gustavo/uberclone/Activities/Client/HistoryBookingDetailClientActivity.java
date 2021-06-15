package com.gustavo.uberclone.Activities.Client;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.gustavo.uberclone.Providers.DriverProvider;
import com.gustavo.uberclone.Providers.HistoryBookingProvider;
import com.gustavo.uberclone.R;
import com.gustavo.uberclone.models.HistoryBooking;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class HistoryBookingDetailClientActivity extends AppCompatActivity {

    private TextView mTextViewName;
    private TextView mTextViewOrigin;
    private TextView mTextViewDestination;
    private  TextView mTextViewCalification;
    private RatingBar mRatingBar;
    private CircleImageView circleImageView;
    private CircleImageView  mCircleImageBack;
    private String mExtraHistoryBooking;
    private HistoryBookingProvider mHistoryBookingProvider;
    private DriverProvider mDriverProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_booking_detail_client);

        mTextViewName = findViewById(R.id.textViewNameHistoryBooking);
        mTextViewOrigin = findViewById(R.id.textViewOriginHistoyBookingDetail);
        mTextViewDestination = findViewById(R.id.textViewDestinationBookingDetail);
        mTextViewCalification = findViewById(R.id.textViewCalicationBookingDetail);
        mRatingBar = findViewById(R.id.ratingBarHistoryBookingDetail);
        circleImageView = findViewById(R.id.circleImageHistoryBookingDetail);

        mExtraHistoryBooking = getIntent().getStringExtra("idHistoryBooking");
        mHistoryBookingProvider = new HistoryBookingProvider();
        mDriverProvider = new DriverProvider();
        getHistoryBooking();
        mCircleImageBack = findViewById(R.id.circleImageBackHistoryBooking);
        mCircleImageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void getHistoryBooking() {
        mHistoryBookingProvider.getHistoryBooking(mExtraHistoryBooking).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull  DataSnapshot snapshot) {
                if (snapshot.exists()){
                    HistoryBooking historyBooking = snapshot.getValue(HistoryBooking.class);
                    mTextViewOrigin.setText(historyBooking.getOrigin());
                    mTextViewDestination.setText(historyBooking.getDestination());
                    mTextViewCalification.setText("tu calification: " + historyBooking.getCalificationDriver());
                    if (snapshot.hasChild("calificationClient")){
                        mRatingBar.setRating((float) historyBooking.getCalificationClient());
                    }

                    mDriverProvider.getDriver(historyBooking.getIdDriver()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull  DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                String name = snapshot.child("name").getValue().toString();
                                mTextViewName.setText(name);
                                if (snapshot.hasChild("image")){
                                    String image = snapshot.child("image").getValue().toString();
                                    Picasso.with(HistoryBookingDetailClientActivity.this).load(image).into(circleImageView);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull  DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });
    }
}