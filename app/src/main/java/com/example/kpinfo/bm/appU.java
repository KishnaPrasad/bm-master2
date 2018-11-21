package com.example.kpinfo.bm;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.app.Activity;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import android.text.format.DateUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Activity to display package usage statistics.
 */
public class appU extends Fragment implements OnItemSelectedListener {
    private static final String TAG = "UsageStatsActivity";
    private static final boolean localLOGV = false;
    private UsageStatsManager mUsageStatsManager;
    private LayoutInflater mInflater;
    private UsageStatsAdapter mAdapter;
    private PackageManager mPm;

    public static class AppNameComparator implements Comparator<UsageStats> {
        private Map<String, String> mAppLabelList;

        AppNameComparator(Map<String, String> appList) {
            mAppLabelList = appList;
        }

        @Override
        public final int compare(UsageStats a, UsageStats b) {
            String alabel = mAppLabelList.get(a.getPackageName());
            String blabel = mAppLabelList.get(b.getPackageName());
            return alabel.compareTo(blabel);
        }
    }

    public static class LastTimeUsedComparator implements Comparator<UsageStats> {
        @Override
        public final int compare(UsageStats a, UsageStats b) {
            // return by descending order
            return (int) (b.getLastTimeUsed() - a.getLastTimeUsed());
        }
    }

    public static class UsageTimeComparator implements Comparator<UsageStats> {
        @Override
        public final int compare(UsageStats a, UsageStats b) {
            return (int) (b.getTotalTimeInForeground() - a.getTotalTimeInForeground());
        }
    }

    // View Holder used when displaying views
    static class AppViewHolder {
        TextView pkgName;
        TextView lastTimeUsed;
        TextView usageTime;
        TextView lockStatus;
    }

    class UsageStatsAdapter extends BaseAdapter {
        public boolean ChildMode = false;
        // Constants defining order for display order
        private static final int _DISPLAY_ORDER_USAGE_TIME = 0;
        private static final int _DISPLAY_ORDER_LAST_TIME_USED = 1;
        private static final int _DISPLAY_ORDER_APP_NAME = 2;

        private int mDisplayOrder = _DISPLAY_ORDER_USAGE_TIME;
        private LastTimeUsedComparator mLastTimeUsedComparator = new LastTimeUsedComparator();
        private UsageTimeComparator mUsageTimeComparator = new UsageTimeComparator();
        private AppNameComparator mAppLabelComparator;
        private final ArrayMap<String, String> mAppLabelMap = new ArrayMap<>();
        private final ArrayList<UsageStats> mPackageStats = new ArrayList<>();

        private DatabaseReference database;
        private DatabaseReference mChildStatusReference;

        UsageStatsAdapter() {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -5);

            final List<UsageStats> stats =
                    mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST,
                            cal.getTimeInMillis(), System.currentTimeMillis());
            if (stats == null) {
                return;
            }

            ArrayMap<String, UsageStats> map = new ArrayMap<>();
            final int statCount = stats.size();
            for (int i = 0; i < statCount; i++) {
                final android.app.usage.UsageStats pkgStats = stats.get(i);

                // load application labels for each application
                try {
                    ApplicationInfo appInfo = mPm.getApplicationInfo(pkgStats.getPackageName(), 0);
                    String label = appInfo.loadLabel(mPm).toString();
                    mAppLabelMap.put(pkgStats.getPackageName(), label);

                    UsageStats existingStats =
                            map.get(pkgStats.getPackageName());
                    if (existingStats == null) {
                        map.put(pkgStats.getPackageName(), pkgStats);
                    } else {
                        existingStats.add(pkgStats);
                    }

                } catch (NameNotFoundException e) {
                    // This package may be gone.
                }
            }
            mPackageStats.addAll(map.values());

            // Sort list
            mAppLabelComparator = new AppNameComparator(mAppLabelMap);
            sortList();
        }

        @Override
        public int getCount() {
            return mPackageStats.size();
        }

        @Override
        public Object getItem(int position) {
            return mPackageStats.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // A ViewHolder keeps references to children views to avoid unneccessary calls
            // to findViewById() on each row.
            final AppViewHolder holder;

            // When convertView is not null, we can reuse it directly, there is no need
            // to reinflate it. We only inflate a new View when the convertView supplied
            // by ListView is null.
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.usage_stats_item, null);

                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                holder = new AppViewHolder();
                holder.pkgName = (TextView) convertView.findViewById(R.id.package_name);
                holder.lastTimeUsed = (TextView) convertView.findViewById(R.id.last_time_used);
                holder.usageTime = (TextView) convertView.findViewById(R.id.usage_time);
                holder.lockStatus = (TextView) convertView.findViewById(R.id.LockStatusValue);
                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (AppViewHolder) convertView.getTag();
            }
            String pkg = null, ltimeUsed = null, totTime = null;


            // Bind the data efficiently with the holder
            UsageStats pkgStats = mPackageStats.get(position);
            if (pkgStats != null) {
                try {
                    String label = mAppLabelMap.get(pkgStats.getPackageName());
                    holder.pkgName.setText(label);
                    holder.lastTimeUsed.setText(DateUtils.formatSameDayTime(pkgStats.getLastTimeUsed(),
                            System.currentTimeMillis(), DateFormat.MEDIUM, DateFormat.MEDIUM));
                    holder.usageTime.setText(
                            DateUtils.formatElapsedTime(pkgStats.getTotalTimeInForeground() / 1000));
                    holder.lockStatus.setText(String.valueOf(ChildMode));
                    pkg = label;
                    pkg = pkg.replace(".", "%2E");
                    ltimeUsed = String.valueOf(DateUtils.formatSameDayTime(pkgStats.getLastTimeUsed(),
                            System.currentTimeMillis(), DateFormat.MEDIUM, DateFormat.MEDIUM));
                    totTime = String.valueOf(DateUtils.formatElapsedTime(pkgStats.getTotalTimeInForeground() / 1000));
                    String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    final String path = getString(R.string.firebase_path) + "/" + currentUser + "/" + "Apps";
                    database = FirebaseDatabase.getInstance().getReference(path +"/" +pkg);
                    mChildStatusReference = FirebaseDatabase.getInstance().getReference(path + "/" +pkg + "/" +"ChildModeStatus:");

                    database.child("LastTimeUsed").setValue(ltimeUsed);
                    database.child("TotalTimeInForeground").setValue(totTime);
                    database.child("ChildModeStatus");


                    ValueEventListener postListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            holder.lockStatus.setText(String.valueOf(dataSnapshot.getValue(ChildMode)));

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                            //Toast.makeText(getContext(), "Failed to load post.",Toast.LENGTH_LONG).show();

                        }
                    };
                    mChildStatusReference.addValueEventListener(postListener);


                } catch (Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }

            } else {
                Log.w(TAG, "No usage stats info for package:" + position);
            }


            return convertView;
        }

        void sortList(int sortOrder) {
            if (mDisplayOrder == sortOrder) {
                // do nothing
                return;
            }
            mDisplayOrder = sortOrder;
            sortList();
        }

        private void sortList() {
            if (mDisplayOrder == _DISPLAY_ORDER_USAGE_TIME) {
                if (localLOGV) Log.i(TAG, "Sorting by usage time");
                Collections.sort(mPackageStats, mUsageTimeComparator);
            } else if (mDisplayOrder == _DISPLAY_ORDER_LAST_TIME_USED) {
                if (localLOGV) Log.i(TAG, "Sorting by last time used");
                Collections.sort(mPackageStats, mLastTimeUsedComparator);
            } else if (mDisplayOrder == _DISPLAY_ORDER_APP_NAME) {
                if (localLOGV) Log.i(TAG, "Sorting by application name");
                Collections.sort(mPackageStats, mAppLabelComparator);
            }
            notifyDataSetChanged();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.activity_app_u, container, false);


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        try {
            mUsageStatsManager = (UsageStatsManager) getActivity().getSystemService(Context.USAGE_STATS_SERVICE);
            mInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mPm = getActivity().getPackageManager();
            View view = getView();
            Intent serviceIntent = new Intent(getContext(), AppLockService.class);
            getContext().startService(serviceIntent);
            Spinner typeSpinner = (Spinner) view.findViewById(R.id.typeSpinner);
            typeSpinner.setOnItemSelectedListener(this);

            ListView listView = (ListView) view.findViewById(R.id.pkg_list);
            mAdapter = new UsageStatsAdapter();
            listView.setAdapter(mAdapter);
            super.onActivityCreated(savedInstanceState);
        } catch (Exception e) {
            Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Called when the activity is first created.
     */
//    @Override
//    protected void onCreate(Bundle icicle) {
//        super.onCreate(icicle);
//        setContentView(R.layout.activity_app_u);
//
//        mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
//        mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        mPm = getPackageManager();
//
//        Spinner typeSpinner = (Spinner) findViewById(R.id.typeSpinner);
//        typeSpinner.setOnItemSelectedListener(this);
//
//        ListView listView = (ListView) findViewById(R.id.pkg_list);
//        mAdapter = new UsageStatsAdapter();
//        listView.setAdapter(mAdapter);
//    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mAdapter.sortList(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // do nothing
    }
}