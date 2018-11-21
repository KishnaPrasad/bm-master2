package com.example.kpinfo.bm;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class smsFragment extends Fragment implements View.OnClickListener{
ListView listView;
private static final  int PERMISSIONs_REQUEST_READ_CONTACTS = 100;
ArrayList<String> smsList;
    ArrayAdapter<String> adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_sms, container, false);
        Button refresh = view.findViewById(R.id.btnSmsRefresh);
        refresh.setOnClickListener(this);
        try {
            int permissionCheck = ContextCompat.checkSelfPermission(getContext(),Manifest.permission.READ_SMS);

            if(permissionCheck == PackageManager.PERMISSION_GRANTED){
                showContacts();
            }
            else{
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_SMS},
                        PERMISSIONs_REQUEST_READ_CONTACTS);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_LONG).show();
        }
        return inflater.inflate(R.layout.fragment_sms, container, false);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONs_REQUEST_READ_CONTACTS){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                showContacts();
            }
            else {
                Toast.makeText(getContext(),"Until you grant permission Names cannot be Displayed",Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showContacts() {
        try {
            Uri inboxURI = Uri.parse(("context://sms/inbox"));
            smsList = new ArrayList<String>();
            ContentResolver cr = getContext().getContentResolver();

            Cursor c = cr.query(inboxURI,null,null,null,null);

            while (c.moveToNext()){
                String Number = c.getString(c.getColumnIndexOrThrow("Address")).toString();
                String Body = c.getString(c.getColumnIndexOrThrow("Body")).toString();
                smsList.add("Number: " +Number +"\n"+"Body "+Body);
            }
            c.close();

        } catch (IllegalArgumentException e) {
            Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onClick(View v) {
        try {
            listView = v.findViewById(R.id.msgList);
            if(smsList!=null)
                adapter = new ArrayAdapter<String>(getContext(),R.layout.simple_list_item_1,smsList);
            listView.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
