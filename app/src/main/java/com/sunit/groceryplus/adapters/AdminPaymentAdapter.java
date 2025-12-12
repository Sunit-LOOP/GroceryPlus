package com.sunit.groceryplus.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sunit.groceryplus.R;
import com.sunit.groceryplus.models.Payment;

import java.util.List;
import java.util.Locale;

public class AdminPaymentAdapter extends RecyclerView.Adapter<AdminPaymentAdapter.ViewHolder> {

    private Context context;
    private List<Payment> payments;

    public AdminPaymentAdapter(Context context, List<Payment> payments) {
        this.context = context;
        this.payments = payments;
    }

    public void updatePayments(List<Payment> newPayments) {
        this.payments = newPayments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_admin_payment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Payment payment = payments.get(position);
        holder.orderIdTv.setText("Order #" + payment.getOrderId());
        holder.amountTv.setText(String.format(Locale.US, "$%.2f", payment.getAmount()));
        holder.methodTv.setText(payment.getPaymentMethod());
        holder.dateTv.setText(payment.getPaymentDate());
        holder.txnIdTv.setText("TXN: " + payment.getTransactionId());
    }

    @Override
    public int getItemCount() {
        return payments.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView orderIdTv, amountTv, methodTv, dateTv, txnIdTv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdTv = itemView.findViewById(R.id.paymentOrderIdTv);
            amountTv = itemView.findViewById(R.id.paymentAmountTv);
            methodTv = itemView.findViewById(R.id.paymentMethodTv);
            dateTv = itemView.findViewById(R.id.paymentDateTv);
            txnIdTv = itemView.findViewById(R.id.paymentTransactionIdTv);
        }
    }
}
