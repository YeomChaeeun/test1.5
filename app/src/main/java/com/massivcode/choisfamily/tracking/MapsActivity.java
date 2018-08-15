package com.massivcode.choisfamily.tracking;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.massivcode.choisfamily.tracking.models.Coord;
import com.massivcode.choisfamily.tracking.models.TrackingHistory;
import com.massivcode.choisfamily.tracking.models.TrackingHistoryDAO;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationChangeListener {

    private TrackingHistoryDAO mTrackingHistoryDAO;
    private boolean mIsInitialized = false;
    private boolean mIsTracking = false;
    private TextView mDistanceTextView, mSpeedTextView, mTimeTextView;
    private GoogleMap mMap;
    private ProgressDialog mProgressDialog, mSaveDialog;
    private FloatingActionButton mToggleButton, mStopButton;
    private LinkedList<Location> mHistory = new LinkedList<>();
    private double mCurrentSpeed;
    private long mTime;
    private float mDistance;
    private Location mCurrentLocation;
    private Timer mTimer;
    private PolylineOptions mPolyLineOptions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        initDialog();

        mTrackingHistoryDAO = new TrackingHistoryDAO(getApplicationContext());
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mPolyLineOptions = new PolylineOptions();
        mPolyLineOptions.color(Color.parseColor("#8BC34A"));
        mPolyLineOptions.width(5);

        mTimer = new Timer();
        initViews();

    }


    private void initDialog() {
        mProgressDialog = new ProgressDialog(MapsActivity.this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setTitle("위치 로딩");
        mProgressDialog.setMessage("현재 위치를 불러오고 있습니다.");
        mProgressDialog.show();

        mSaveDialog = new ProgressDialog(MapsActivity.this);
        mSaveDialog.setCancelable(false);
        mSaveDialog.setTitle("트래킹 기록 저장");
        mSaveDialog.setMessage("경로 데이터를 저장하고 있습니다.");
    }

    private void initViews() {
        mDistanceTextView = (TextView) findViewById(R.id.distance_tv);
        mSpeedTextView = (TextView) findViewById(R.id.speed_tv);
        mTimeTextView = (TextView) findViewById(R.id.time_tv);

        mToggleButton = (FloatingActionButton) findViewById(R.id.toggle_fab);
        mToggleButton.setOnClickListener(mOnToggleButtonClickListener);
        mStopButton = (FloatingActionButton) findViewById(R.id.stop_fab);
        mStopButton.setOnClickListener(mOnStopButtonClickListener);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        System.out.println("onMapReady");
        mMap = googleMap;
        mMap.setOnMyLocationChangeListener(this);
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
    }

    @Override
    public void onMyLocationChange(Location location) {
        if (!mIsInitialized) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 18));
            mIsInitialized = true;

            mProgressDialog.dismiss();
        }

        if (mIsTracking) {
            mCurrentLocation = location;
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mCurrentSpeed = mCurrentLocation.getSpeed() * 3.6;
                    if (mCurrentSpeed > 0) {
                        mHistory.add(mCurrentLocation);
                        mPolyLineOptions.add(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
                        int size = mHistory.size();
                        if (size > 1) {
                            Location previousLocation = mHistory.get(size - 2);
                            // 누적 이동거리 (미터)
                            mDistance += mCurrentLocation.distanceTo(previousLocation);
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mDistanceTextView.setText(FormatUtil.getDouble(mDistance) + " m");
                                mSpeedTextView.setText(FormatUtil.getDouble(mCurrentSpeed) + " km/h");
                                mMap.clear();
                                mMap.addPolyline(mPolyLineOptions);
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), 18));
                            }
                        });
                    }

                    mTime += 500;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTimeTextView.setText(FormatUtil.getTime(mTime));
                        }
                    });
                }
            }, 500);

        }

    }

    private View.OnClickListener mOnToggleButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mIsTracking) {
                mIsTracking = false;
                mToggleButton.setImageResource(R.drawable.ic_start);
            } else {
                mIsTracking = true;
                mToggleButton.setImageResource(R.drawable.ic_pause);

            }
        }
    };

    private View.OnClickListener mOnStopButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mIsTracking) {
                mOnToggleButtonClickListener.onClick(mToggleButton);
                List<LatLng> recordedPath = mPolyLineOptions.getPoints();
                int size = recordedPath.size();

                if (size > 1) {
                    mSaveDialog.show();
                    List<Coord> coords = new ArrayList<>();
                    for (LatLng each : recordedPath) {
                        coords.add(new Coord(each.latitude, each.longitude));
                    }

                    String pathJson = new Gson().toJson(coords);
                    double time = mTime / 1000.0;
                    double averageSpeed = mDistance / time;
                    final TrackingHistory trackingHistory = new TrackingHistory(mTime, averageSpeed, mDistance, pathJson, System.currentTimeMillis());

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mTrackingHistoryDAO.save(trackingHistory);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mSaveDialog.dismiss();
                                    Toast.makeText(MapsActivity.this, "트래킹 기록이 저장되었습니다!", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            });
                        }
                    }).start();

                } else {
                    Toast.makeText(MapsActivity.this, "움직임이 적어 저장하지 않습니다!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                finish();
            }
        }
    };

}
