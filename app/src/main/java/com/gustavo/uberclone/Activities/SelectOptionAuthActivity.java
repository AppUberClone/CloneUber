package com.gustavo.uberclone.Activities;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import  androidx.appcompat.widget.Toolbar;

import com.gustavo.uberclone.Activities.Client.RegisterActivity;
import com.gustavo.uberclone.Activities.Driver.RegisterDriverActivity;
import com.gustavo.uberclone.R;

public class SelectOptionAuthActivity extends AppCompatActivity {

     SharedPreferences mPref;
    Toolbar mToolbar;

    Button mButtonGoToLogin;
    Button mButtonGoToRegister;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_option_auth);

        mPref = getApplicationContext().getSharedPreferences("typeUser", MODE_PRIVATE);

        // add Toolbar in the activity
        mToolbar = findViewById(R.id.toolbar);
       setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("seleccionar una opcion");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mButtonGoToLogin = findViewById(R.id.btnGoToLogin);
        mButtonGoToRegister = findViewById(R.id.btnGoToRegister);

        mButtonGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 goToLogin();
            }
        });
        mButtonGoToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRegister();
            }
        });
    }

    private void goToLogin(){
        Intent intent =  new Intent( this , LoginActivity.class);
        startActivity(intent);
    }

    private void goToRegister(){

        String selectUser = mPref.getString("user", "");

        if(selectUser.equals("client")){
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, RegisterDriverActivity.class);
            startActivity(intent);
            }

    }
}