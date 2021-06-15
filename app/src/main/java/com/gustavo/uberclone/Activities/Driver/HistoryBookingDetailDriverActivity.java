package com.gustavo.uberclone.Activities.Driver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.gustavo.uberclone.Providers.ClientProvider;
import com.gustavo.uberclone.Providers.HistoryBookingProvider;
import com.gustavo.uberclone.R;
import com.gustavo.uberclone.models.HistoryBooking;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class HistoryBookingDetailDriverActivity extends AppCompatActivity {

    private TextView mTextViewName;
    private TextView mTextViewOrigin;
    private TextView mTextViewDestination;
    private TextView mTextViewCalification;
    private RatingBar mRatingBar;
    private CircleImageView mCircleImageHistoryBooking;
    private CircleImageView mCircleImageBack;

    private String mExtraId;

    private HistoryBookingProvider mHistoryBookingProvider;
    private ClientProvider mClientProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_booking_detail_driver);
        mTextViewName = findViewById(R.id.textViewNameHistoryBooking);
        mTextViewOrigin = findViewById(R.id.textViewOriginHistoyBookingDetail);
        mTextViewDestination = findViewById(R.id.textViewDestinationBookingDetail);
        mTextViewCalification = findViewById(R.id.textViewCalicationBookingDetail);
        mRatingBar = findViewById(R.id.ratingBarHistoryBookingDetail);
        mCircleImageHistoryBooking = findViewById(R.id.circleImageHistoryBookingDetail);
        mCircleImageBack = findViewById(R.id.circleImageBackHistoryBooking);
        mExtraId = getIntent().getStringExtra("idHistoryBooking");
        mHistoryBookingProvider = new HistoryBookingProvider();
        mClientProvider = new ClientProvider();
        getHistoryBooking();
        mCircleImageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void getHistoryBooking() {
        mHistoryBookingProvider.getHistoryBooking(mExtraId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull  DataSnapshot snapshot) {
                if (snapshot.exists()){
                    HistoryBooking historyBooking = snapshot.getValue(HistoryBooking.class);
                    mTextViewOrigin.setText(historyBooking.getOrigin());
                    mTextViewDestination.setText(historyBooking.getDestination());
                    mTextViewCalification.setText("Tu calificacion es :" + historyBooking.getCalificationClient());

                    if (snapshot.hasChild("calificationClient")){
                        mRatingBar.setRating( (float) historyBooking.getCalificationClient());
                    }

                    mClientProvider.getClient(historyBooking.getIdClient()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                String name = snapshot.child("name").getValue().toString();
                                mTextViewName.setText(name);
                                if (snapshot.hasChild("image")){
                                    String image = snapshot.child("image").getValue().toString();
                                    Picasso.with(HistoryBookingDetailDriverActivity.this).load(image).into(mCircleImageHistoryBooking);
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
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}