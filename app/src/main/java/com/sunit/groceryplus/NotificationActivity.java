package com.sunit.groceryplus;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sunit.groceryplus.adapters.NotificationAdapter;
import com.sunit.groceryplus.models.Notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView emptyView;
    private NotificationAdapter adapter;
    private List<Notification> notificationList;
    private DatabaseHelper dbHelper;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Notifications");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize Views
        recyclerView = findViewById(R.id.notificationRecyclerView);
        emptyView = findViewById(R.id.emptyView);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(this, notificationList);
        recyclerView.setAdapter(adapter);

        dbHelper = new DatabaseHelper(this);

        // Get User ID
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = sharedPreferences.getInt("userId", -1);

        if (userId != -1) {
            loadNotifications();
        } else {
            Toast.makeText(this, "Please login to view notifications", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadNotifications() {
        notificationList.clear();
        Cursor cursor = dbHelper.getUserNotifications(userId);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                try {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.NotificationEntry.COLUMN_NAME_NOTIFICATION_ID));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.NotificationEntry.COLUMN_NAME_TITLE));
                    String message = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.NotificationEntry.COLUMN_NAME_MESSAGE));
                    String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.NotificationEntry.COLUMN_NAME_CREATED_AT));
                    int isReadInt = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.NotificationEntry.COLUMN_NAME_IS_READ));
                    boolean isRead = (isReadInt == 1);

                    notificationList.add(new Notification(id, userId, title, message, date, isRead));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
            cursor.close();
        }

        if (notificationList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
