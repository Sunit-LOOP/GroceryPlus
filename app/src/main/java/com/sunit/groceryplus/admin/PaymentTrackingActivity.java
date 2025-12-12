package com.sunit.groceryplus.admin;

import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.sunit.groceryplus.DatabaseContract;
import com.sunit.groceryplus.DatabaseHelper;
import com.sunit.groceryplus.R;
import com.sunit.groceryplus.adapters.AdminPaymentAdapter;
import com.sunit.groceryplus.models.Payment;

import java.util.ArrayList;
import java.util.List;

public class PaymentTrackingActivity extends AppCompatActivity {

    private RecyclerView paymentsRv;
    private AdminPaymentAdapter adapter;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_tracking);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        dbHelper = new DatabaseHelper(this);
        paymentsRv = findViewById(R.id.paymentsRv);

        setupRecyclerView();
        loadPayments();
    }

    private void setupRecyclerView() {
        paymentsRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminPaymentAdapter(this, new ArrayList<>());
        paymentsRv.setAdapter(adapter);
    }

    private void loadPayments() {
        List<Payment> payments = new ArrayList<>();
        Cursor cursor = dbHelper.getAllPayments();
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.PaymentEntry.COLUMN_NAME_PAYMENT_ID));
                int orderId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.PaymentEntry.COLUMN_NAME_ORDER_ID));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.PaymentEntry.COLUMN_NAME_AMOUNT));
                String method = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.PaymentEntry.COLUMN_NAME_PAYMENT_METHOD));
                String txnId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.PaymentEntry.COLUMN_NAME_TRANSACTION_ID));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.PaymentEntry.COLUMN_NAME_PAYMENT_DATE));
                
                payments.add(new Payment(id, orderId, amount, method, txnId, date));
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        adapter.updatePayments(payments);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
