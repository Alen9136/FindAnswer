package com.example.findanswer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {
    private List<Question> displayedQuestions; // Текущие отображаемые вопросы
    private List<Question> allQuestions;      // Полный список вопросов

    public QuestionAdapter(List<Question> questions) {
        this.allQuestions = new ArrayList<>(questions);
        this.displayedQuestions = new ArrayList<>(questions);
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
            intent.putExtra("name", q.name); // ⚡ Добавляем username
            v.getContext().startActivity(intent);
        });

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
            intent.putExtra("name", q.name); // ⚡ Добавляем username
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return displayedQuestions.size();
    }

    // Основной метод фильтрации
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

    // Обновление полного списка вопросов
    @SuppressLint("NotifyDataSetChanged")
    public void updateAllQuestions(List<Question> newQuestions) {
        allQuestions = new ArrayList<>(newQuestions);
        displayedQuestions = new ArrayList<>(newQuestions);
        notifyDataSetChanged();
    }

    public static class QuestionViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, descriptionTextView, subjectTextView;
        Button answerButton;

        public QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            subjectTextView = itemView.findViewById(R.id.subjectTextView);
            answerButton = itemView.findViewById(R.id.answerButton);
        }
    }

}