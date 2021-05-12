package com.gustavo.uberclone.Activities.Client;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.gustavo.uberclone.Activities.Driver.RegisterDriverActivity;
import com.gustavo.uberclone.Providers.AuthProvider;
import com.gustavo.uberclone.Providers.ClientProvider;
import com.gustavo.uberclone.R;
import com.gustavo.uberclone.includes.MyToolbar;
import com.gustavo.uberclone.models.Client;

import dmax.dialog.SpotsDialog;

public class RegisterActivity extends AppCompatActivity {

    SharedPreferences mPref;

    AuthProvider mAuthProvider;
    ClientProvider mClientProvider;
    AlertDialog mDialog;

    Button mButtonRegister;
    TextInputEditText mTextInputName;
    TextInputEditText mTextInputEmail;
    TextInputEditText mTextInputPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

       MyToolbar.show(this, "Registrar usuario", true);

        mDialog = new SpotsDialog.Builder().setContext(RegisterActivity.this).setMessage("Espere un momento ").build();


        mButtonRegister = findViewById(R.id.btnRegister);

        mTextInputName = findViewById(R.id.textInputname);
        mTextInputEmail = findViewById(R.id.textInputEmail);
        mTextInputPassword = findViewById(R.id.textInputPassword);


        mPref = getApplicationContext().getSharedPreferences("typeUser", MODE_PRIVATE);

        mAuthProvider = new AuthProvider();
        mClientProvider = new ClientProvider();


       // String selectUser = mPref.getString("user", "");
      //  Toast.makeText(this, "El valor que selecciono es: " + selectUser , Toast.LENGTH_SHORT).show();

        mButtonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                clickRegister();
            }
        });

    }
    private void clickRegister(){

        final String  name = mTextInputName.getText().toString();
        final String email = mTextInputEmail.getText().toString();
        final String password = mTextInputPassword.getText().toString();

        if(!name.isEmpty() && !email.isEmpty() && !password.isEmpty()){
            if(password.length() >=6){
                mDialog.show();
                register(name,email,password);

            } else {
                Toast.makeText(this, "La contraseña debe tener mas de 6 caracteres", Toast.LENGTH_SHORT).show();
                }

        } else {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            }
    }
    public  void register(final String name, final String email, String password){
        mAuthProvider.register(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mDialog.hide();
                if ( task.isSuccessful()){
                    String id = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    Client client = new Client(id,name,email);
                    create(client);
                }    else {
                    Toast.makeText(RegisterActivity.this, "No se pudo  registrar el usuario", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    public void create(Client client){

        mClientProvider.create(client).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){
                    //Toast.makeText(RegisterActivity.this, "El registro se realizo exitosamente", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(RegisterActivity.this, MapClientActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                } else{
                    Toast.makeText(RegisterActivity.this, "Fallo al registrar al cliente", Toast.LENGTH_SHORT).show();
                    }
            }
        });
    }


    /*
     private void saveUser(String id, String name, String email){
         String selectUser = mPref.getString("user", "");

         User user = new User();
         user.setEmail(email);
         user.setName(name);

         if (selectUser.equals("driver")){

                mDatabase.child("Users").child("Drivers").child(id).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(RegisterActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(RegisterActivity.this, "Fallo el registro", Toast.LENGTH_SHORT).show();
                                }

                    }
                });
         } else if (selectUser.equals("client")){
                mDatabase.child("Users").child("Clients").child(id).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(RegisterActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                            }    else {
                                Toast.makeText(RegisterActivity.this, "Fallo el registro", Toast.LENGTH_SHORT).show();
                            }
                    }
                });
            }
     } */

}