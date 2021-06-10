package com.gustavo.uberclone.Activities.Client;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.gustavo.uberclone.Providers.AuthProvider;
import com.gustavo.uberclone.Providers.ClientProvider;
import com.gustavo.uberclone.R;
import com.gustavo.uberclone.includes.MyToolbar;

public class UpdateProfileActivity extends AppCompatActivity {

    private ImageView  mImageViewProfile;
    private TextView mTextViewName;
    private Button mButtonUpdate;

    private ClientProvider mClientProvider;
    private AuthProvider mAuthProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        MyToolbar.show(this, "Actualizar perfil",true);
        mImageViewProfile = findViewById(R.id.imageViewProfile);
        mTextViewName = findViewById(R.id.textInputName);
        mButtonUpdate = findViewById(R.id.btnUpdateProfile);

        mClientProvider = new ClientProvider();
        mAuthProvider = new AuthProvider();

        getClientInfo();

        mButtonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });
    }

    private void getClientInfo(){
        mClientProvider.getClient(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull  DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String name = snapshot.child("name").getValue().toString();
                    mTextViewName.setText(name);
                }
            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });
    }

    private void updateProfile() {
    }
}