package com.gustavo.uberclone.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.gustavo.uberclone.Activities.Client.HistoryBookingDetailClientActivity;
import com.gustavo.uberclone.Activities.Driver.HistoryBookingDetailDriverActivity;
import com.gustavo.uberclone.Providers.ClientProvider;
import com.gustavo.uberclone.R;
import com.gustavo.uberclone.models.HistoryBooking;
import com.squareup.picasso.Picasso;

public class HistoryBookingDriverAdapter extends FirebaseRecyclerAdapter<HistoryBooking, HistoryBookingDriverAdapter.ViewHolder> {

    private ClientProvider mClientProvider;
    private Context mContext;

    public HistoryBookingDriverAdapter( FirebaseRecyclerOptions<HistoryBooking> options, Context context) {
        super(options);
        mClientProvider = new ClientProvider();
        mContext = context;
    }

    @Override
    protected void onBindViewHolder( HistoryBookingDriverAdapter.ViewHolder holder, int position,  HistoryBooking model) {
        String id = getRef(position).getKey();
        holder.textViewOrigin.setText(model.getOrigin());
        holder.textViewDestination.setText(model.getDestination());
        mClientProvider.getClient(model.getIdClient()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String name = snapshot.child("name").getValue().toString();
                    holder.textViewName.setText(name);
                    if (snapshot.hasChild("image")){
                        String image = snapshot.child("image").getValue().toString();
                        Picasso.with(mContext).load(image).into(holder.imageViewHistoryBooking);
                    }
                }
            }
            @Override
            public void onCancelled( DatabaseError error) {

            }
        });

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext,HistoryBookingDetailDriverActivity.class);
                intent.putExtra("idHistoryBooking", id);
                mContext.startActivity(intent);
            }
        });

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_history_booking, parent, false);
        return new ViewHolder(view);

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
         View mView;
        TextView textViewName;
        TextView textViewOrigin;
        TextView textViewDestination;
        TextView textViewCalification;
        ImageView imageViewHistoryBooking;

        public ViewHolder( View view) {
            super(view);
            mView = view;
            textViewName = view.findViewById(R.id.textViewName);
            textViewOrigin = view.findViewById(R.id.textViewOrigin);
            textViewDestination = view.findViewById(R.id.textViewDestination);
            imageViewHistoryBooking = view.findViewById(R.id.imageviewHistoryBooking);
        }
    }
}
