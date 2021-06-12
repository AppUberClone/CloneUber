package com.gustavo.uberclone.Activities.Driver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;
import com.gustavo.uberclone.Activities.Client.UpdateProfileActivity;
import com.gustavo.uberclone.Providers.AuthProvider;
import com.gustavo.uberclone.Providers.DriverProvider;
import com.gustavo.uberclone.Providers.ImagesProvider;
import com.gustavo.uberclone.R;
import com.gustavo.uberclone.includes.MyToolbar;
import com.gustavo.uberclone.models.Driver;
import com.gustavo.uberclone.retrofit.IFCMApi;
import com.gustavo.uberclone.utils.FileUtil;
import com.squareup.picasso.Picasso;

import java.io.File;

public class UpdateProfileDriverActivity extends AppCompatActivity {

    private ImageView mImageViewProfile;
    private TextView mTextViewName;
    private TextView mTextViewVehicleBrand;
    private TextView mTExtViewVehiclePlate;
    private Button  mButtonUpdate;

    private DriverProvider mDriverProvider;
    private AuthProvider mAuthProvider;
    private ImagesProvider mImagesProvider;

    private File mImageFile;
    private String mImage;

    private final int GALLERY_REQUEST = 1;

    private ProgressDialog mProgressDialog;

    private String mName, mVehicleBrand,mVehiclePlate;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile_driver);

        MyToolbar.show(this, "Actualizar perfil", true);
        mImageViewProfile = findViewById(R.id.imageViewProfile);
        mTextViewName = findViewById(R.id.textInputName);
        mTextViewVehicleBrand = findViewById(R.id.textInputBrand);
        mTExtViewVehiclePlate = findViewById(R.id.textInputPlate);
        mButtonUpdate = findViewById(R.id.btnUpdateProfile);

        mDriverProvider = new DriverProvider();
        mAuthProvider = new AuthProvider();
        mImagesProvider = new ImagesProvider("driver_images");
        mProgressDialog = new ProgressDialog(this);

        getDriverInfo();

        mButtonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });

        mImageViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

    }

    private void openGallery() {
        Intent intentGallery = new Intent(Intent.ACTION_GET_CONTENT);
        intentGallery.setType("image/*");
        startActivityForResult(intentGallery, GALLERY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){
            try {
                mImageFile = FileUtil.from(this, data.getData());
                mImageViewProfile.setImageBitmap(BitmapFactory.decodeFile(mImageFile.getAbsolutePath()));
            } catch (Exception e){
                Log.d("ERROR", "mensaje:" + e.getMessage());
               }
        }
    }

    private void getDriverInfo(){
        mDriverProvider.getDriver(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull  DataSnapshot snapshot) {

                if (snapshot.exists()){
                    String name = snapshot.child("name").getValue().toString();
                    String vehicleBrand = snapshot.child("vehicleBrand").getValue().toString();
                    String vehiclePlate = snapshot.child("vehiclePlate").getValue().toString();
                    String image = "";
                    if (snapshot.hasChild("image")){
                        image = snapshot.child("image").getValue().toString();
                        Picasso.with(UpdateProfileDriverActivity.this).load(image).into(mImageViewProfile);
                    }
                    mTextViewName.setText(name);
                    mTextViewVehicleBrand.setText(vehicleBrand);
                    mTExtViewVehiclePlate.setText(vehiclePlate);
                }
            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });
    }

    private void updateProfile() {

        mName = mTextViewName.getText().toString();
        mVehicleBrand = mTextViewVehicleBrand.getText().toString();
         mVehiclePlate = mTExtViewVehiclePlate.getText().toString();

        if (!mName.equals("") && mImageFile!=null){
            mProgressDialog.setMessage("Espere un momento...");
            mProgressDialog.show();
            saveImage();

        } else {
            Toast.makeText(this, "Ingrese la imagen y el nombre", Toast.LENGTH_SHORT).show();
          }
    }

    private void saveImage() {

        mImagesProvider.saveImage(UpdateProfileDriverActivity.this, mImageFile, mAuthProvider.getId()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull  Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()){
                    mImagesProvider.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String image = uri.toString();
                            Driver driver = new Driver();
                            driver.setImage(image);
                            driver.setName(mName);
                            driver.setId(mAuthProvider.getId());
                            driver.setVehicleBrand(mVehicleBrand);
                            driver.setVehiclePlate(mVehiclePlate);
                            mDriverProvider.update(driver).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    mProgressDialog.dismiss();
                                    Toast.makeText(UpdateProfileDriverActivity.this, "Su informacion se actualizo correctamente", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    });
                } else {
                    Toast.makeText(UpdateProfileDriverActivity.this, "Hubo un error al subir la imagen", Toast.LENGTH_SHORT).show();
                  }

            }
        });


    }

}