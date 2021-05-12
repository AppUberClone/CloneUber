package com.gustavo.uberclone.Activities.Driver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.gustavo.uberclone.Providers.AuthProvider;
import com.gustavo.uberclone.Providers.DriverProvider;
import com.gustavo.uberclone.R;
import com.gustavo.uberclone.includes.MyToolbar;
import com.gustavo.uberclone.models.Driver;

import dmax.dialog.SpotsDialog;

public class RegisterDriverActivity extends AppCompatActivity {


    Button mButtonRegister;

    AuthProvider mAuthProvider;
    DriverProvider mDriverProvider;

    AlertDialog mDialog;

    TextInputEditText mTextInputName;
    TextInputEditText mTextInputEmail;
    TextInputEditText mTextInputBrand;
    TextInputEditText mTextInputPlate;
    TextInputEditText mTextInputPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_driver);

        MyToolbar.show(this,"Registrar conductor", true);

        mButtonRegister = findViewById(R.id.btnRegister);
        mTextInputName = findViewById(R.id.textInputName);
        mTextInputEmail = findViewById(R.id.textInputEmail);
        mTextInputBrand = findViewById(R.id.textInputBrand);
        mTextInputPlate = findViewById(R.id.textInputPlate);
        mTextInputPassword = findViewById(R.id.textInputPassword);

        mAuthProvider = new AuthProvider();
        mDriverProvider = new DriverProvider();

        mDialog = new SpotsDialog.Builder().setContext(RegisterDriverActivity.this).setMessage("Espere un momento ").build();


        mButtonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                  clickRegister();
            }
        });
    }

    public void clickRegister(){
         String name = mTextInputName.getText().toString();
         String email = mTextInputEmail.getText().toString();
         String vehicleBrand = mTextInputBrand.getText().toString();
         String vehiclePlate = mTextInputPlate.getText().toString();
         String password = mTextInputPassword.getText().toString();

         if (!name.isEmpty() && !email.isEmpty() && !vehicleBrand.isEmpty() && !vehiclePlate.isEmpty() && !password.isEmpty()){
             if(password.length() >= 6){
                 mDialog.show();
                 register(name, email, vehicleBrand, vehiclePlate, password);

             } else {
                 Toast.makeText(this, "La contraseña debe tener mas de 6 caracteres", Toast.LENGTH_SHORT).show();
             }
         } else {
             Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            }

    }
    public void register( String name, String email, String vehicleBrand, String  vehiclePlate,  String password){


          mAuthProvider.register(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
              @Override
              public void onComplete(@NonNull Task<AuthResult> task) {
                         mDialog.hide();
                        if (task.isSuccessful()){

                            String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            Driver driver = new Driver(id, name, email,vehicleBrand,vehiclePlate);
                            create(driver);
                        }   else {
                            Toast.makeText(RegisterDriverActivity.this, "No se pudo registrar el usuario", Toast.LENGTH_SHORT).show();
                            }
              }
          });
    }

    public void  create(Driver driver){

        mDriverProvider.create(driver).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    //Toast.makeText(RegisterDriverActivity.this, "El registro se realizo exitosamente", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterDriverActivity.this, MapDriverActivity.class);
                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                     startActivity(intent);
                }   else {
                    Toast.makeText(RegisterDriverActivity.this, "Error al registrar el conductor", Toast.LENGTH_SHORT).show();
                    }
            }
        });
    }
}