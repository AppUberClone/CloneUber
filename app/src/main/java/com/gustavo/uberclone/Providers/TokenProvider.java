package com.gustavo.uberclone.Providers;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.gustavo.uberclone.models.Token;

public class TokenProvider {

    DatabaseReference mDatabase;

    public  TokenProvider(){
         mDatabase = FirebaseDatabase.getInstance().getReference().child("Tokens");
    }

    public  void  create(final  String idUser) {
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                Token token = new Token(instanceIdResult.getToken());
                mDatabase.child(idUser).setValue(token);
            }
        });
    }

    public DatabaseReference getToken(String idUser){
        return  mDatabase.child(idUser);
    }
}
