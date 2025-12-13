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

    private com.google.android.material.card.MaterialCardView cardAll, cardStripe, cardCod;
    private String currentFilter = "all"; // all, cod, stripe

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_tracking);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        dbHelper = new DatabaseHelper(this);
        paymentsRv = findViewById(R.id.paymentsRv);
        
        // Initialize filter cards
        cardAll = findViewById(R.id.cardAll);
        cardStripe = findViewById(R.id.cardStripe);
        cardCod = findViewById(R.id.cardCod);

        setupRecyclerView();
        setupFilterChips();
        loadPayments(currentFilter);
    }

    private void setupRecyclerView() {
        paymentsRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminPaymentAdapter(this, new ArrayList<>());
        paymentsRv.setAdapter(adapter);
    }

    private void setupFilterChips() {
        cardAll.setOnClickListener(v -> selectFilter("all"));
        cardStripe.setOnClickListener(v -> selectFilter("stripe"));
        cardCod.setOnClickListener(v -> selectFilter("cod"));
        
        // Initial selection
        selectFilter("all");
    }

    private void selectFilter(String filter) {
        currentFilter = filter;
        
        // Update Card Highlighting
        highlightCard(cardAll, "all".equals(filter));
        highlightCard(cardStripe, "stripe".equals(filter));
        highlightCard(cardCod, "cod".equals(filter));
        
        loadPayments(currentFilter);
    }

    private void highlightCard(com.google.android.material.card.MaterialCardView card, boolean isSelected) {
        if (isSelected) {
            card.setStrokeColor(getResources().getColor(android.R.color.holo_green_dark));
            card.setStrokeWidth(4);
        } else {
            card.setStrokeColor(0xFFE0E0E0); // Light Gray #E0E0E0
            card.setStrokeWidth(2);
        }
    }
    
    private void loadPayments(String filter) {
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
                
                // Apply filter
                if (filter.equals("all") || method.equalsIgnoreCase(filter)) {
                    payments.add(new Payment(id, orderId, amount, method, txnId, date));
                }
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
