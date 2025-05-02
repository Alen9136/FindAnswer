package com.example.findanswer;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {

    private static List<Question> questions;

    public QuestionAdapter(List<Question> questions) {
        QuestionAdapter.questions = questions;
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
        Question q = questions.get(position);
        holder.titleTextView.setText(q.title);
        holder.descriptionTextView.setText(q.description);
        holder.subjecTextView.setText(q.subject + " • " + q.grade + " • " + q.coins + " Coins");
        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(v.getContext(), QuestionAnswerActivity.class);
            intent.putExtra("title", q.title);
            intent.putExtra("description", q.description);
            intent.putExtra("deadline", q.deadline);
            intent.putExtra("subject", q.subject);
            intent.putExtra("grade", q.grade);
            intent.putExtra("grade", q.coins);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    public void updateList(List<Question> filteredList) {
    }

    public class QuestionViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, descriptionTextView, subjecTextView;

        public QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            subjecTextView = itemView.findViewById(R.id.subjectTextView);
        }
        @SuppressLint("NotifyDataSetChanged")
        public void updateList(List<Question> newList) {
            questions.clear();
            questions.addAll(newList);
            notifyDataSetChanged();
        }

    }
}
