package com.example.kpinfo.bm;

import android.app.ActivityManager;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class AppLockService extends Service {
    public AppLockService() {
    }
    String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
    final String path = "userDatabase" + "/" + currentUser +"/" +"ChildLock";
    DatabaseReference databaseReference;
    ArrayList<String> apps;
    String c;

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceIntent = new Intent(getApplicationContext(),this.getClass());
        restartServiceIntent.setPackage(getPackageName());
        startService(restartServiceIntent);
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            databaseReference = FirebaseDatabase.getInstance().getReference(path);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        int i = 0;
                        for(DataSnapshot d : dataSnapshot.getChildren()) {
                            apps.add(d.getKey());
                            i++;
                        }
                    }
                }//onDataChange

                @Override
                public void onCancelled(DatabaseError error) {

                }//onCancelled
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List l = am.getRecentTasks(1, ActivityManager.RECENT_WITH_EXCLUDED);
        Iterator i = l.iterator();
        PackageManager pm = this.getPackageManager();
        while (i.hasNext()) {
            ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo)(i.next());
            try {
                 c =(String) pm.getApplicationLabel(pm.getApplicationInfo(
                        info.processName, PackageManager.GET_META_DATA));
                Log.w("LABEL", c);
            } catch (Exception e) {
// Name Not FOund Exception
            }
        }LockApp(c);
    }

    private void LockApp(String c) {
        if(apps.contains(c)){
            final AlertDialog.Builder builder = new AlertDialog.Builder((this));
            builder.setMessage("Are you sure ,you want to Logout?");
            builder.setCancelable(true);
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onTaskRemoved(intent);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
