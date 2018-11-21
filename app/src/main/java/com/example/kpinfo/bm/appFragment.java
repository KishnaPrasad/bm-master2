package com.example.kpinfo.bm;

import android.app.Activity;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class appFragment extends Fragment implements View.OnClickListener {
    ArrayList<parentApp> dataModels;
    ListView listView;
    private static CustomAdapter adapter;
    private static final String TAG = appFragment.class.getSimpleName();
    DatabaseReference databaseReference;
    Button btnRefresh, btnEnableLock, btnDisableLock;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_app, container, false);

        String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final String path = "userDatabase" + "/" + currentUser + "/" + "Apps";
        btnRefresh = view.findViewById(R.id.firebaseRefresh);


        btnRefresh.setOnClickListener(this);



        dataModels = new ArrayList<>();


        try {

            databaseReference = FirebaseDatabase.getInstance().getReference(path);

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
                    Toast.makeText(getContext(), "Failed to read value.", Toast.LENGTH_LONG).show();
                }
            };
            databaseReference.addChildEventListener(childEventListener);
        } catch (Exception e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return view;
    }


    private void setUsageApps(DataSnapshot dataSnapshot) {
        try {
            String pkg, lst, tot, childStats;
            pkg = dataSnapshot.getKey();
            lst = dataSnapshot.child("LastTimeUsed").getValue().toString();
            tot = dataSnapshot.child("TotalTimeInForeground").getValue().toString();

            dataModels.add(new parentApp(pkg, lst, tot));
            adapter.notifyDataSetChanged();

        } catch (Exception e) {
//            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onClick(View v) {

        Toast.makeText(getContext(), "Please Wait", Toast.LENGTH_LONG).show();
        View view = getView();

        listView = (ListView) view.findViewById(R.id.ParentPkgList);
        if (dataModels.size() != 0)
            adapter = new CustomAdapter(dataModels, getContext());
        listView.setAdapter(adapter);


    }
}


