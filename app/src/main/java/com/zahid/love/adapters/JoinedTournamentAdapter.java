package com.zahid.love.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.zahid.love.R;
import com.zahid.love.models.Tournament;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class JoinedTournamentAdapter extends RecyclerView.Adapter<JoinedTournamentAdapter.JoinedTournamentViewHolder> {

    private List<Tournament> tournamentList;
    private DecimalFormat decimalFormat;
    private SimpleDateFormat dateFormat;

    public JoinedTournamentAdapter(List<Tournament> tournamentList) {
        this.tournamentList = tournamentList;
        this.decimalFormat = new DecimalFormat("#,##0.00");
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public JoinedTournamentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_joined_tournament, parent, false);
        return new JoinedTournamentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JoinedTournamentViewHolder holder, int position) {
        Tournament tournament = tournamentList.get(position);
        
        holder.titleTextView.setText(tournament.getTitle());
        holder.gameTextView.setText(tournament.getGame());
        holder.statusTextView.setText(tournament.getStatus());
        holder.entryFeeTextView.setText("৳" + decimalFormat.format(tournament.getEntryFee()));
        
        // Parse date from Laravel API response
        try {
            if (tournament.getStartsAt() != null) {
                java.text.SimpleDateFormat isoFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", java.util.Locale.getDefault());
                Date date = isoFormat.parse(tournament.getStartsAt());
                holder.dateTextView.setText(dateFormat.format(date));
            } else {
                holder.dateTextView.setText(dateFormat.format(new Date()));
            }
        } catch (Exception e) {
            holder.dateTextView.setText(tournament.getTimeUntilStart());
        }
        
        // Load game icon
        if (tournament.getGame().equals("Free Fire")) {
            holder.gameIconImageView.setImageResource(R.drawable.free_fire_logo);
        } else if (tournament.getGame().equals("PUBG Mobile")) {
            holder.gameIconImageView.setImageResource(R.drawable.app_logo);
        } else if (tournament.getGame().equals("Call of Duty Mobile")) {
            holder.gameIconImageView.setImageResource(R.drawable.app_logo);
        } else if (tournament.getGame().toLowerCase().contains("ludo")) {
            holder.gameIconImageView.setImageResource(R.drawable.ludo_logo);
        } else {
            holder.gameIconImageView.setImageResource(R.drawable.app_logo);
        }
        
        // Set status color
        int statusColor;
        switch (tournament.getStatus()) {
            case "Open":
                statusColor = holder.itemView.getContext().getResources().getColor(R.color.neon_blue);
                break;
            case "Closed":
                statusColor = holder.itemView.getContext().getResources().getColor(R.color.neon_green);
                break;
            case "Completed":
                statusColor = holder.itemView.getContext().getResources().getColor(R.color.text_secondary);
                break;
            default:
                statusColor = holder.itemView.getContext().getResources().getColor(R.color.text_primary);
                break;
        }
        holder.statusTextView.setTextColor(statusColor);
    }

    @Override
    public int getItemCount() {
        return tournamentList.size();
    }

    static class JoinedTournamentViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        ImageView gameIconImageView;
        TextView titleTextView, gameTextView, statusTextView, entryFeeTextView, dateTextView;

        public JoinedTournamentViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            gameIconImageView = itemView.findViewById(R.id.gameIconImageView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            gameTextView = itemView.findViewById(R.id.gameTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            entryFeeTextView = itemView.findViewById(R.id.entryFeeTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
        }
    }
}