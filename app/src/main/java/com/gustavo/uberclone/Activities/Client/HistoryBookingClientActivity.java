package com.gustavo.uberclone.Activities.Client;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DownloadManager;
import android.os.Bundle;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.api.Distribution;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.gustavo.uberclone.Adapters.HistoryBookingClientAdapter;
import com.gustavo.uberclone.Providers.AuthProvider;
import com.gustavo.uberclone.R;
import com.gustavo.uberclone.includes.MyToolbar;
import com.gustavo.uberclone.models.HistoryBooking;

public class HistoryBookingClientActivity extends AppCompatActivity {

    RecyclerView mRecyclerView;
    private HistoryBookingClientAdapter mAdapter;
    private AuthProvider mAuthProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_booking_client);

        MyToolbar.show(this, "Historial de viajes", true);
        mRecyclerView = findViewById(R.id.recyclerViewHistoryBooking);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuthProvider = new AuthProvider();
        Query query = FirebaseDatabase.getInstance().getReference().child("HistoryBooking").orderByChild("idClient").equalTo(mAuthProvider.getId());

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<HistoryBooking>().setQuery(query, HistoryBooking.class).build();
        mAdapter = new HistoryBookingClientAdapter(options, HistoryBookingClientActivity.this);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }
}