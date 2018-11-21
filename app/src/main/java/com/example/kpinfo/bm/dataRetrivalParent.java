package com.example.kpinfo.bm;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;



public class dataRetrivalParent {
    private static final String TAG = dataRetrivalParent.class.getSimpleName();
    DatabaseReference database;
    DatabaseReference dbCount;
    int count;
    ArrayList<parentApp> userData;
    final Context context;
    public dataRetrivalParent(final Context context) {
        this.context = context;
        userData = new ArrayList<parentApp>();
        try {
            String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
            final String path = "userDatabase" + "/" + currentUser +"/" +"Apps";
            database = FirebaseDatabase.getInstance().getReference(path);

            //database.addChildEventListener(new ChildEventListener() {
            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    setUsageApps(dataSnapshot);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    setUsageApps(dataSnapshot);
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG, "Failed to read value.", databaseError.toException());
                    Toast.makeText(context, "Failed to read value.", Toast.LENGTH_LONG).show();
                }
            };
            database.addChildEventListener(childEventListener);
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    public ArrayList<parentApp> getUserData(){
        return userData;
    }

    private void setUsageApps(DataSnapshot dataSnapshot) {
        try {
            parentApp p = new parentApp();
            p.setPkgName(dataSnapshot.getKey());
            p.setLastTimeUsed(dataSnapshot.child("LastTimeUsed").getValue().toString());
            p.setTotalTimeInForeground(dataSnapshot.child("TotalTimeInForeground").getValue().toString());
            userData.add(p);


        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }


    }
}
