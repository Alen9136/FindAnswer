package com.example.findanswer.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.findanswer.R;
import com.example.findanswer.models.Answer;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.List;

public class AnswerAdapter extends RecyclerView.Adapter<AnswerAdapter.AnswerViewHolder> {

    List<Answer> answers;

    public AnswerAdapter(List<Answer> answers) {
        this.answers = answers;
    }

    @NonNull
    @Override
    public AnswerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_answer_block2, parent, false);
        return new AnswerViewHolder(v);
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    @Override
    public void onBindViewHolder(@NonNull AnswerViewHolder holder, int position) {
        Answer a = answers.get(position);
        holder.username.setText(a.getUsername());
        holder.answerText.setText(a.getAnswer());
        holder.likeCount.setText("THANKS " + a.getLikes());

        // Показываем корону
        if (a.isBestAnswer()) {
            holder.crownIcon.setImageResource(R.drawable.ic_crown_filled);
        } else {
            holder.crownIcon.setImageResource(R.drawable.ic_crown_outline);
        }

        // Установка лучшего ответа
        holder.crownIcon.setOnClickListener(v -> {
            FirebaseDatabase.getInstance()
                    .getReference("answers")
                    .child(a.getQuestionId())
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String answerId = ds.getKey();
                            assert answerId != null;
                            boolean isCurrent = answerId.equals(a.getId());

                            ds.getRef().child("bestAnswer").setValue(isCurrent);

                            if (isCurrent) {
                                // Увеличиваем счетчик best в профиле пользователя
                                FirebaseDatabase.getInstance()
                                        .getReference("users")
                                        .child(a.getUserId())
                                        .child("best")
                                        .setValue(ServerValue.increment(1));
                            }
                        }
                        notifyDataSetChanged();
                    });
        });

        // Удаление ответа
        holder.deleteButton.setOnClickListener(view -> {
            FirebaseDatabase.getInstance()
                    .getReference("answers")
                    .child(a.getQuestionId())
                    .child(a.getId())
                    .removeValue()
                    .addOnSuccessListener(unused -> {
                        answers.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, answers.size());
                        Toast.makeText(view.getContext(), "Deleted", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    public int getItemCount() {
        return answers.size();
    }

    static class AnswerViewHolder extends RecyclerView.ViewHolder {
        TextView username, answerText, likeCount;
        ImageButton deleteButton;
        ImageView crownIcon;

        public AnswerViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.userTextView);
            answerText = itemView.findViewById(R.id.answerText);
            likeCount = itemView.findViewById(R.id.thankCountText);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            crownIcon = itemView.findViewById(R.id.crownIcon);
        }
    }
}
