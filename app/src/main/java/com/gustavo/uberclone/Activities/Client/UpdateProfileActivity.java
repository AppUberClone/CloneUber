package com.gustavo.uberclone.Activities.Client;

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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gustavo.uberclone.Providers.AuthProvider;
import com.gustavo.uberclone.Providers.ClientProvider;
import com.gustavo.uberclone.R;
import com.gustavo.uberclone.includes.MyToolbar;
import com.gustavo.uberclone.models.Client;
import com.gustavo.uberclone.utils.CompressorBitmapImage;
import com.gustavo.uberclone.utils.FileUtil;
import com.squareup.picasso.Picasso;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateProfileActivity extends AppCompatActivity {

    private ImageView  mImageViewProfile;
    private TextView mTextViewName;
    private Button mButtonUpdate;
    private CircleImageView mCircleImageBack;

    private ClientProvider mClientProvider;
    private AuthProvider mAuthProvider;

    private File mImageFile;
    private String mImage;

    private final int GALLERY_REQUEST = 1;

    private ProgressDialog mProgressDialog;
    private  String mName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
       // MyToolbar.show(this, "Actualizar perfil",true);
        mImageViewProfile = findViewById(R.id.imageViewProfile);
        mTextViewName = findViewById(R.id.textInputName);
        mButtonUpdate = findViewById(R.id.btnUpdateProfile);
        mCircleImageBack = findViewById(R.id.circleImageBackHistoryBooking);
        mClientProvider = new ClientProvider();
        mAuthProvider = new AuthProvider();
        mProgressDialog = new ProgressDialog(this);


        getClientInfo();

        mImageViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        mButtonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });

        mCircleImageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
         galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_REQUEST );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable  Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK ){
            try {
               mImageFile =  FileUtil.from(this,data.getData());
               mImageViewProfile.setImageBitmap(BitmapFactory.decodeFile(mImageFile.getAbsolutePath()));
            } catch ( Exception e){
                Log.d( "ERROR", "Mensaje:" +e.getMessage());
              }
        }
    }

    private void getClientInfo(){
        mClientProvider.getClient(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull  DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String name = snapshot.child("name").getValue().toString();
                    String image = "";
                    if (snapshot.hasChild("image")){
                        image = snapshot.child("image").getValue().toString();
                        Picasso.with(UpdateProfileActivity.this).load(image).into(mImageViewProfile);
                    }
                    mTextViewName.setText(name);
                }
            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });
    }

    private void updateProfile() {
         mName = mTextViewName.getText().toString();
        if (!mName.equals("") && mImageFile != null){
            mProgressDialog.setMessage("Espere un momento...");
            mProgressDialog.show();
            saveImage();

        } else {
            Toast.makeText(this, "Ingrese la imagen y el nombre", Toast.LENGTH_SHORT).show();
          }
    }

    public void saveImage(){
        byte[] imageByte = CompressorBitmapImage.getImage(this, mImageFile.getPath(), 500, 500);
        StorageReference storage = FirebaseStorage.getInstance().getReference().child("client_images").child(mAuthProvider.getId() + ".jpg");
        UploadTask uploadTask = storage.putBytes(imageByte);
        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull  Task<UploadTask.TaskSnapshot> task) {
                 if (task.isSuccessful()){
                     storage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                         @Override
                         public void onSuccess(Uri uri) {
                              String image = uri.toString();
                              Client client = new Client();
                              client.setImage(image);
                              client.setName(mName);
                              client.setId(mAuthProvider.getId());
                              mClientProvider.update(client).addOnSuccessListener(new OnSuccessListener<Void>() {
                                  @Override
                                  public void onSuccess(Void unused) {
                                      mProgressDialog.dismiss();
                                      Toast.makeText(UpdateProfileActivity.this, "Su informacion se actualizo correctamente", Toast.LENGTH_SHORT).show();
                                  }
                              });
                         }
                     });
                 } else {
                     Toast.makeText(UpdateProfileActivity.this, "Hubo un error al subir la imagen", Toast.LENGTH_SHORT).show();
                   }
            }
        });
    }
}