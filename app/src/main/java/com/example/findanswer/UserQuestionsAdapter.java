package com.example.findanswer;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserQuestionsAdapter extends RecyclerView.Adapter<UserQuestionsAdapter.UserQuestionViewHolder> {

    public interface OnQuestionDeleteListener {
        void onDelete(Question question, int position);
    }

    public interface OnQuestionClickListener {
        void onQuestionClick(Question question);
    }

    private List<Question> questions;
    private final OnQuestionDeleteListener deleteListener;
    private final OnQuestionClickListener clickListener;

    public UserQuestionsAdapter(List<Question> questions,
                                OnQuestionDeleteListener deleteListener,
                                OnQuestionClickListener clickListener) {
        this.questions = questions;
        this.deleteListener = deleteListener;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public UserQuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_question2, parent, false);
        return new UserQuestionViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull UserQuestionViewHolder holder, int position) {
        Question q = questions.get(position);

        holder.titleTextView.setText(q.title);
        holder.descriptionTextView.setText(q.description);
        holder.subjectTextView.setText(q.subject + " • " + q.grade);

        // Клик по всей карточке вопроса
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onQuestionClick(q);
            }
        });

        // Клик по кнопке удаления (корзине)
        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(q, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateQuestions(List<Question> newQuestions) {
        this.questions = newQuestions;
        notifyDataSetChanged();
    }

    public static class UserQuestionViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, descriptionTextView, subjectTextView;
        ImageButton deleteButton;

        public UserQuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            subjectTextView = itemView.findViewById(R.id.subjectTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}

