package com.zahid.love.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zahid.love.R;
import com.zahid.love.models.Transaction;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactionList;
    private DecimalFormat decimalFormat;
    private SimpleDateFormat dateFormat;

    public TransactionAdapter(List<Transaction> transactionList) {
        this.transactionList = transactionList;
        this.decimalFormat = new DecimalFormat("#,##0.00");
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        
        // Set transaction type and description
        String description = getTransactionDescription(transaction);
        holder.descriptionTextView.setText(description);
        
        // Set amount with proper sign
        String amountText;
        int amountColor;
        if (transaction.getType().equals("credit")) {
            amountText = "+৳" + decimalFormat.format(transaction.getAmount());
            amountColor = holder.itemView.getContext().getResources().getColor(R.color.neon_green);
        } else {
            amountText = "-৳" + decimalFormat.format(Math.abs(transaction.getAmount()));
            amountColor = holder.itemView.getContext().getResources().getColor(R.color.error_color);
        }
        holder.amountTextView.setText(amountText);
        holder.amountTextView.setTextColor(amountColor);
        
        // Set status
        String status = transaction.getStatus() != null ? transaction.getStatus() : "completed";
        holder.statusTextView.setText(status.toUpperCase());
        int statusColor;
        switch (status) {
            case "approved":
            case "completed":
                statusColor = holder.itemView.getContext().getResources().getColor(R.color.neon_green);
                break;
            case "pending":
                statusColor = holder.itemView.getContext().getResources().getColor(R.color.warning_color);
                break;
            case "rejected":
                statusColor = holder.itemView.getContext().getResources().getColor(R.color.error_color);
                break;
            default:
                statusColor = holder.itemView.getContext().getResources().getColor(R.color.text_secondary);
                break;
        }
        holder.statusTextView.setTextColor(statusColor);
        
        // Set date
        try {
            if (transaction.getCreatedAt() != null) {
                // Parse ISO date string from Laravel
                java.text.SimpleDateFormat isoFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", java.util.Locale.getDefault());
                Date date = isoFormat.parse(transaction.getCreatedAt());
                holder.dateTextView.setText(dateFormat.format(date));
            } else {
                holder.dateTextView.setText(dateFormat.format(new Date(transaction.getTimestamp())));
            }
        } catch (Exception e) {
            holder.dateTextView.setText(dateFormat.format(new Date()));
        }
        
        // Set icon based on transaction type
        int iconResource;
        switch (transaction.getType()) {
            case "credit":
                iconResource = R.drawable.ic_add_money;
                break;
            case "debit":
                iconResource = R.drawable.ic_withdraw_money;
                break;
            default:
                // Determine icon based on reason
                if (transaction.getReason() != null) {
                    if (transaction.getReason().toLowerCase().contains("prize")) {
                        iconResource = R.drawable.ic_prize;
                    } else if (transaction.getReason().toLowerCase().contains("tournament")) {
                        iconResource = R.drawable.ic_tournament;
                    } else {
                        iconResource = R.drawable.ic_transaction;
                    }
                } else {
                    iconResource = R.drawable.ic_transaction;
                }
                break;
        }
        holder.iconImageView.setImageResource(iconResource);
    }

    private String getTransactionDescription(Transaction transaction) {
        if (transaction.getReason() != null && !transaction.getReason().isEmpty()) {
            return transaction.getReason();
        } else if (transaction.getDescription() != null && !transaction.getDescription().isEmpty()) {
            return transaction.getDescription();
        } else {
            // Fallback based on type
            switch (transaction.getType()) {
                case "credit":
                    return "Money Added";
                case "debit":
                    return "Money Deducted";
                default:
                    return "Transaction";
            }
        }
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        ImageView iconImageView;
        TextView descriptionTextView, amountTextView, statusTextView, dateTextView;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImageView = itemView.findViewById(R.id.iconImageView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            amountTextView = itemView.findViewById(R.id.amountTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
        }
    }
}