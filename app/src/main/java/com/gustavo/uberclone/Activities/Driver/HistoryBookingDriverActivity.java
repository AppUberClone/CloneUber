package com.gustavo.uberclone.Activities.Driver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.gustavo.uberclone.Activities.Client.HistoryBookingClientActivity;
import com.gustavo.uberclone.Adapters.HistoryBookingDriverAdapter;
import com.gustavo.uberclone.Providers.AuthProvider;
import com.gustavo.uberclone.R;
import com.gustavo.uberclone.includes.MyToolbar;
import com.gustavo.uberclone.models.HistoryBooking;

public class HistoryBookingDriverActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private HistoryBookingDriverAdapter mAdapter;
    private AuthProvider mAuthProver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_booking_driver);
        MyToolbar.show(this,"Historial de Viajes",  true);
        mRecyclerView = findViewById(R.id.recyclerViewHistoryBooking);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuthProver = new AuthProvider();
        Query query = FirebaseDatabase.getInstance().getReference()
                      .child("HistoryBooking")
                      .orderByChild("idDriver")
                      .equalTo(mAuthProver.getId());
        FirebaseRecyclerOptions<HistoryBooking> options = new FirebaseRecyclerOptions
                                                                .Builder<HistoryBooking>()
                                                              .setQuery(query, HistoryBooking.class)
                                                              .build();
        mAdapter = new HistoryBookingDriverAdapter(options, HistoryBookingDriverActivity.this);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }
}