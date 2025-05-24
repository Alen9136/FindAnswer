package com.example.findanswer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {

    public interface OnUserProfileClickListener {
        void onUserProfileClick(String userId);
    }

    private List<Question> displayedQuestions;
    private List<Question> allQuestions;
    private final OnUserProfileClickListener userProfileClickListener;

    public QuestionAdapter(List<Question> questions, OnUserProfileClickListener listener) {
        this.allQuestions = new ArrayList<>(questions);
        this.displayedQuestions = new ArrayList<>(questions);
        this.userProfileClickListener = listener;
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_question, parent, false);
        return new QuestionViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        Question q = displayedQuestions.get(position);
        holder.titleTextView.setText(q.title);
        holder.descriptionTextView.setText(q.description);
        holder.subjectTextView.setText(q.subject + " • " + q.grade + " • " + q.coins + " Coins");

        // Загрузка аватарки
        if (q.getUserId() != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(q.getUserId()).child("avatar");

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String avatarUrl = snapshot.getValue(String.class);
                    if (avatarUrl != null && !avatarUrl.isEmpty()) {
                        Glide.with(holder.profileImageView.getContext())
                                .load(avatarUrl)
                                .placeholder(R.drawable.nav_profile)
                                .circleCrop()
                                .into(holder.profileImageView);

                    } else {
                        holder.profileImageView.setImageResource(R.drawable.nav_profile);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    holder.profileImageView.setImageResource(R.drawable.nav_profile);
                }
            });
        } else {
            holder.profileImageView.setImageResource(R.drawable.nav_profile);
        }

        // Клик по аватарке
        holder.profileImageView.setOnClickListener(v -> {
            if (userProfileClickListener != null && q.getUserId() != null) {
                userProfileClickListener.onUserProfileClick(q.getUserId());
            }
        });

        // Клик по названию
        holder.titleTextView.setOnClickListener(v -> {
            if (userProfileClickListener != null && q.getUserId() != null) {
                userProfileClickListener.onUserProfileClick(q.getUserId());
            }
        });

        // Переход к деталям
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), QuestionDetailActivity.class);
            intent.putExtra("title", q.title);
            intent.putExtra("description", q.description);
            intent.putExtra("deadline", q.deadline);
            intent.putExtra("subject", q.subject);
            intent.putExtra("grade", q.grade);
            intent.putExtra("coins", q.coins);
            intent.putExtra("fileUrl", q.getFileUrl());
            intent.putExtra("id", q.getId());
            intent.putExtra("name", q.name);
            v.getContext().startActivity(intent);
        });

        // Кнопка "Ответить"
        holder.answerButton.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), QuestionAnswerActivity.class);
            intent.putExtra("title", q.title);
            intent.putExtra("description", q.description);
            intent.putExtra("deadline", q.deadline);
            intent.putExtra("subject", q.subject);
            intent.putExtra("grade", q.grade);
            intent.putExtra("coins", q.coins);
            intent.putExtra("fileUrl", q.getFileUrl());
            intent.putExtra("id", q.getId());
            intent.putExtra("name", q.name);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return displayedQuestions.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filter(String query) {
        displayedQuestions.clear();

        if (query.isEmpty()) {
            displayedQuestions.addAll(allQuestions);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Question q : allQuestions) {
                if (matchesQuery(q, lowerCaseQuery)) {
                    displayedQuestions.add(q);
                }
            }
        }
        notifyDataSetChanged();
    }

    private boolean matchesQuery(Question question, String query) {
        return (question.title != null && question.title.toLowerCase().contains(query)) ||
                (question.description != null && question.description.toLowerCase().contains(query)) ||
                (question.subject != null && question.subject.toLowerCase().contains(query));
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateAllQuestions(List<Question> newQuestions) {
        allQuestions = new ArrayList<>(newQuestions);
        displayedQuestions = new ArrayList<>(newQuestions);
        notifyDataSetChanged();
    }

    public static class QuestionViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, descriptionTextView, subjectTextView;
        Button answerButton;
        ImageView profileImageView;

        public QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            subjectTextView = itemView.findViewById(R.id.subjectTextView);
            answerButton = itemView.findViewById(R.id.answerButton);
            profileImageView = itemView.findViewById(R.id.userAvatar);
        }
    }
}
