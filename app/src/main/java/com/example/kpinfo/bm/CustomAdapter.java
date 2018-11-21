package com.example.kpinfo.bm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;


class CustomAdapter extends ArrayAdapter<parentApp> implements View.OnClickListener{



        private ArrayList<parentApp> dataSet;
        Context mContext;

        // View lookup cache
        private static class ViewHolder {
            TextView pkgName;
            TextView lastUsed;
            TextView totTimeBgd;
            Switch childStats;
        }

        public CustomAdapter(ArrayList<parentApp> data, Context context) {
            super(context, R.layout.parent_usage_stats_item, data);
            this.dataSet = data;
            this.mContext=context;

        }

        @Override
        public void onClick(View v) {

            int position=(Integer) v.getTag();
            Object object= getItem(position);
            parentApp dataModel=(parentApp) object;
           // final parentApp da = dataSet.get(position);


            switch (v.getId())
            {
 //              case R.id.:
//                    Snackbar.make(v, "Release date " +dataModel.getFeature(), Snackbar.LENGTH_LONG)
//                            .setAction("No action", null).show();
//                    break;
            }
        }

        private int lastPosition = -1;

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            final parentApp dataModel = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            final ViewHolder viewHolder; // view lookup cache stored in tag

            final View result;

            if (convertView == null) {

                viewHolder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.parent_usage_stats_item, parent, false);
                viewHolder.pkgName = (TextView) convertView.findViewById(R.id.parent_package_name);
                viewHolder.lastUsed = (TextView) convertView.findViewById(R.id.parent_last_time_used);
                viewHolder.totTimeBgd = (TextView) convertView.findViewById(R.id.parent_usage_time);
                viewHolder.childStats = (Switch) convertView.findViewById(R.id.switch_childModeStats);

                result=convertView;

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();

                result=convertView;
            }

         //   Animation animation = AnimationUtils.loadAnimation(mContext, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
            //result.startAnimation(animation);
            lastPosition = position;

            viewHolder.pkgName.setText(dataModel.getPkgName());
            viewHolder.lastUsed.setText(dataModel.getLastTimeUsed());
            viewHolder.totTimeBgd.setText(dataModel.getTotalTimeInForeground());
            viewHolder.childStats.setTag(position);

            viewHolder.childStats.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int position = (Integer) buttonView.getTag();
if(isChecked){
//    viewHolder.childStats.setChecked(true);
    String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
    final String path = "userDatabase" + "/" + currentUser + "/" + "ChildLock";
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(path);
    databaseReference.child(viewHolder.pkgName.getText().toString()).setValue("true");
    Toast.makeText(getContext(),""+viewHolder.pkgName.getText()+position,Toast.LENGTH_LONG).show();

}


                }
            });


//            viewHolder.info.setOnClickListener(this);
//            viewHolder.info.setTag(position);
            // Return the completed view to render on screen
            return convertView;
        }


}



