package com.zahid.love.adapters;

import android.os.CountDownTimer;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.MatchViewHolder> {

    private List<Tournament> matchList;
    private OnMatchClickListener listener;
    private DecimalFormat decimalFormat;
    private Map<String, CountDownTimer> countDownTimers;

    public interface OnMatchClickListener {
        void onMatchClick(Tournament tournament);
    }

    public interface OnJoinClickListener {
        void onJoinClick(Tournament tournament);
    }

    public interface OnRoomInfoClickListener {
        void onRoomInfoClick(Tournament tournament);
    }

    public MatchAdapter(List<Tournament> matchList, OnMatchClickListener listener) {
        this.matchList = matchList;
        this.listener = listener;
        this.decimalFormat = new DecimalFormat("#,##0.00");
        this.countDownTimers = new HashMap<>();
    }

    private OnJoinClickListener joinClickListener;
    private OnRoomInfoClickListener roomInfoClickListener;

    public void setOnJoinClickListener(OnJoinClickListener listener) {
        this.joinClickListener = listener;
    }

    public void setOnRoomInfoClickListener(OnRoomInfoClickListener listener) {
        this.roomInfoClickListener = listener;
    }

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_match_card, parent, false);
        return new MatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        Tournament tournament = matchList.get(position);

        // Basic match info
        holder.titleTextView.setText(tournament.getTitle());
        holder.typeTextView.setText(tournament.getMode());
        holder.entryFeeTextView.setText("Entry: ৳" + decimalFormat.format(tournament.getEntryFee()));
        holder.prizePoolTextView.setText("Prize: ৳" + decimalFormat.format(tournament.getPrizePool()));

        // Players info
        holder.playersTextView.setText(tournament.getParticipantsCount() + "/" + tournament.getMaxParticipants() + " players");

        // Status and time
        holder.dateTimeTextView.setText(tournament.getTimeUntilStart());
        holder.countdownTextView.setText(tournament.getTimeUntilStart());

        // Load game banner
        if (holder.bannerImageView != null) {
            int gameIcon = GameUtils.getGameIcon(tournament.getGame());
            holder.bannerImageView.setImageResource(gameIcon);
        }

        // Setup buttons
        setupButtons(tournament, holder);

        // Click listeners
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMatchClick(tournament);
            }
        });

        holder.joinButton.setOnClickListener(v -> {
            if (joinClickListener != null) {
                joinClickListener.onJoinClick(tournament);
            } else if (listener != null) {
                listener.onMatchClick(tournament);
            }
        });

        holder.roomInfoButton.setOnClickListener(v -> {
            if (roomInfoClickListener != null) {
                roomInfoClickListener.onRoomInfoClick(tournament);
            }
        });
    }

    private void setupButtons(Tournament tournament, MatchViewHolder holder) {
        boolean isMatchFull = tournament.getParticipantsCount() >= tournament.getMaxParticipants();
        boolean isJoinable = tournament.getStatus().equals("Open") && !isMatchFull;

        // Join button logic
        if (isMatchFull) {
            holder.joinButton.setText("MATCH FULL");
            holder.joinButton.setEnabled(false);
            holder.joinButton.setBackgroundTintList(
                    holder.itemView.getContext().getResources().getColorStateList(R.color.text_secondary)
            );
        } else if (!isJoinable || !tournament.getStatus().equals("Open")) {
            holder.joinButton.setText("NOT AVAILABLE");
            holder.joinButton.setEnabled(false);
            holder.joinButton.setBackgroundTintList(
                    holder.itemView.getContext().getResources().getColorStateList(R.color.text_secondary)
            );
        } else {
            holder.joinButton.setText("JOIN NOW");
            holder.joinButton.setEnabled(true);
            holder.joinButton.setBackgroundTintList(
                    holder.itemView.getContext().getResources().getColorStateList(R.color.neon_blue)
            );
        }

        // Room info button
        if (tournament.getRoomId() != null && !tournament.getRoomId().isEmpty()) {
            holder.roomInfoButton.setText("VIEW ROOM INFO");
            holder.roomInfoButton.setEnabled(true);
            holder.roomInfoButton.setVisibility(android.view.View.VISIBLE);
        } else {
            holder.roomInfoButton.setVisibility(android.view.View.GONE);
        }

        // Status indicator
        if (isMatchFull) {
            holder.statusIndicator.setBackgroundColor(
                    holder.itemView.getContext().getResources().getColor(R.color.text_secondary)
            );
        } else {
            holder.statusIndicator.setBackgroundColor(
                    holder.itemView.getContext().getResources().getColor(R.color.neon_green)
            );
        }
    }

    @Override
    public int getItemCount() {
        return matchList.size();
    }

    @Override
    public void onViewRecycled(@NonNull MatchViewHolder holder) {
        super.onViewRecycled(holder);
        // Cancel countdown timer when view is recycled
        int position = holder.getAdapterPosition();
        if (position != RecyclerView.NO_POSITION && position < matchList.size()) {
            String matchId = matchList.get(position).getId();
            CountDownTimer timer = countDownTimers.get(matchId);
            if (timer != null) {
                timer.cancel();
                countDownTimers.remove(matchId);
            }
        }
    }

    static class MatchViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        ImageView bannerImageView;
        View statusIndicator;
        TextView titleTextView, typeTextView, mapTextView;
        TextView entryFeeTextView, prizePoolTextView;
        TextView playersTextView, dateTimeTextView, countdownTextView;
        MaterialButton joinButton, roomInfoButton;

        public MatchViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            bannerImageView = itemView.findViewById(R.id.bannerImageView);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            typeTextView = itemView.findViewById(R.id.typeTextView);
            entryFeeTextView = itemView.findViewById(R.id.entryFeeTextView);
            prizePoolTextView = itemView.findViewById(R.id.prizePoolTextView);
            playersTextView = itemView.findViewById(R.id.playersTextView);
            dateTimeTextView = itemView.findViewById(R.id.dateTimeTextView);
            countdownTextView = itemView.findViewById(R.id.countdownTextView);
            joinButton = itemView.findViewById(R.id.joinButton);
            roomInfoButton = itemView.findViewById(R.id.roomInfoButton);
        }
    }
}