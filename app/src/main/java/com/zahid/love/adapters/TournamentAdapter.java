package com.zahid.love.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.zahid.love.R;
import com.zahid.love.models.Tournament;
import com.zahid.love.utils.GameUtils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TournamentAdapter extends RecyclerView.Adapter<TournamentAdapter.TournamentViewHolder> {

    private List<Tournament> tournamentList;
    private OnTournamentClickListener listener;
    private DecimalFormat decimalFormat;
    private SimpleDateFormat dateFormat;

    public interface OnTournamentClickListener {
        void onTournamentClick(Tournament tournament);
    }

    public TournamentAdapter(List<Tournament> tournamentList, OnTournamentClickListener listener) {
        this.tournamentList = tournamentList;
        this.listener = listener;
        this.decimalFormat = new DecimalFormat("#,##0.00");
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public TournamentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tournament, parent, false);
        return new TournamentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TournamentViewHolder holder, int position) {
        Tournament tournament = tournamentList.get(position);
        
        holder.titleTextView.setText(tournament.getTitle());
        holder.gameTextView.setText(tournament.getGame());
        holder.typeTextView.setText(tournament.getMode());
        holder.statusTextView.setText(tournament.getStatus());
        holder.entryFeeTextView.setText("Entry: ৳" + decimalFormat.format(tournament.getEntryFee()));
        holder.prizePoolTextView.setText("Prize: ৳" + decimalFormat.format(tournament.getPrizePool()));
        holder.playersTextView.setText(tournament.getParticipantsCount() + "/" + tournament.getMaxParticipants() + " Players");
        holder.startTimeTextView.setText(tournament.getTimeUntilStart());
        
        // Load game image
        int gameIcon = GameUtils.getGameIcon(tournament.getGame());
        holder.gameImageView.setImageResource(gameIcon);
        
        // Set status color
        int statusColor;
        switch (tournament.getStatus()) {
            case "Open":
                statusColor = holder.itemView.getContext().getResources().getColor(R.color.neon_blue);
                holder.joinButton.setEnabled(true);
                holder.joinButton.setText("JOIN NOW");
                holder.joinButton.setBackgroundTintList(holder.itemView.getContext().getResources().getColorStateList(R.color.neon_blue));
                break;
            case "Closed":
                statusColor = holder.itemView.getContext().getResources().getColor(R.color.neon_green);
                holder.joinButton.setEnabled(false);
                holder.joinButton.setText("CLOSED");
                holder.joinButton.setBackgroundTintList(holder.itemView.getContext().getResources().getColorStateList(R.color.text_secondary));
                break;
            case "Completed":
                statusColor = holder.itemView.getContext().getResources().getColor(R.color.text_secondary);
                holder.joinButton.setEnabled(false);
                holder.joinButton.setText("COMPLETED");
                holder.joinButton.setBackgroundTintList(holder.itemView.getContext().getResources().getColorStateList(R.color.text_secondary));
                break;
            default:
                statusColor = holder.itemView.getContext().getResources().getColor(R.color.text_primary);
                holder.joinButton.setEnabled(false);
                holder.joinButton.setText("NOT AVAILABLE");
                holder.joinButton.setBackgroundTintList(holder.itemView.getContext().getResources().getColorStateList(R.color.text_secondary));
                break;
        }
        holder.statusTextView.setTextColor(statusColor);
        
        // Check if tournament is full
        if (tournament.getParticipantsCount() >= tournament.getMaxParticipants()) {
            holder.joinButton.setEnabled(false);
            holder.joinButton.setText("FULL");
            holder.joinButton.setBackgroundTintList(holder.itemView.getContext().getResources().getColorStateList(R.color.error_color));
        }
        
        // Click listeners
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTournamentClick(tournament);
            }
        });
        
        holder.joinButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTournamentClick(tournament);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tournamentList.size();
    }

    static class TournamentViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        ImageView gameImageView;
        TextView titleTextView, gameTextView, typeTextView, statusTextView;
        TextView entryFeeTextView, prizePoolTextView, playersTextView, startTimeTextView;
        MaterialButton joinButton;

        public TournamentViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            gameImageView = itemView.findViewById(R.id.gameImageView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            gameTextView = itemView.findViewById(R.id.gameTextView);
            typeTextView = itemView.findViewById(R.id.typeTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            entryFeeTextView = itemView.findViewById(R.id.entryFeeTextView);
            prizePoolTextView = itemView.findViewById(R.id.prizePoolTextView);
            playersTextView = itemView.findViewById(R.id.playersTextView);
            startTimeTextView = itemView.findViewById(R.id.startTimeTextView);
            joinButton = itemView.findViewById(R.id.joinButton);
        }
    }
}