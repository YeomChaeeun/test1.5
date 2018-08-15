package com.massivcode.choisfamily.tracking;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.massivcode.choisfamily.tracking.adapter.TrackingHistoryAdapter;
import com.massivcode.choisfamily.tracking.models.TrackingHistory;
import com.massivcode.choisfamily.tracking.models.TrackingHistoryDAO;

@SuppressWarnings("ConstantConditions")
public class MainActivity extends AppCompatActivity implements AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {
    private TrackingHistoryDAO mTrackingHistoryDAO;
    private TextView mNotifyTextView;
    private ListView mListView;
    private TrackingHistoryAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTrackingHistoryDAO = new TrackingHistoryDAO(getApplicationContext());
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Cursor cursor = mTrackingHistoryDAO.findAll();
        System.out.println(cursor.getCount());
        if (mAdapter == null) {
            mAdapter = new TrackingHistoryAdapter(getApplicationContext(), cursor);
            mListView.setAdapter(mAdapter);
        } else {
            mAdapter.swapCursor(cursor);
        }

        if (cursor == null) {
            mNotifyTextView.setVisibility(View.VISIBLE);
        } else {
            mNotifyTextView.setVisibility(View.GONE);
        }
    }

    private void initViews() {
        mNotifyTextView = (TextView) findViewById(R.id.notify_tv);

        mListView = (ListView) findViewById(R.id.lv);
        mListView.setOnItemLongClickListener(this);
        mListView.setOnItemClickListener(this);
        findViewById(R.id.fab).setOnClickListener(mOnTrackingStartButtonClickListener);
    }

    private View.OnClickListener mOnTrackingStartButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(MainActivity.this, MapsActivity.class));
        }
    };

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, final long id) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("기록 삭제")
                .setMessage("기록을 삭제하시겠습니까?")
                .setCancelable(false)
                .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mTrackingHistoryDAO.delete((int) id);
                        mAdapter.swapCursor(mTrackingHistoryDAO.findAll());
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TrackingHistory data = mAdapter.get(position);
        startActivity(new Intent(MainActivity.this, ViewerActivity.class).putExtra("data", data));
    }
}
