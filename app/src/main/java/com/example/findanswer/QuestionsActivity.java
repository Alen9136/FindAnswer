package com.example.findanswer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class QuestionsActivity extends AppCompatActivity {
    private UserQuestionsAdapter adapter;
    private final List<Question> userQuestions = new ArrayList<>();
    private DatabaseReference questionsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.questions_activity);

        RecyclerView recyclerView = findViewById(R.id.questionsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new UserQuestionsAdapter(
                userQuestions,
                this::deleteQuestion,        // Обработчик удаления
                this::openQuestionDetail     // Обработчик клика по вопросу
        );

        recyclerView.setAdapter(adapter);

        questionsRef = FirebaseDatabase.getInstance().getReference("questions");
        loadUserQuestions();
    }

    private void loadUserQuestions() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        Log.d("QuestionsActivity", "Current User ID: " + userId);

        questionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userQuestions.clear(); // очищаем старые данные
                Log.d("QuestionsActivity", "Total questions: " + snapshot.getChildrenCount());

                for (DataSnapshot qSnap : snapshot.getChildren()) {
                    Question q = qSnap.getValue(Question.class);
                    if (q != null && userId.equals(q.getUserId())) {
                        q.setId(qSnap.getKey());
                        userQuestions.add(q);
                    }
                }
                Log.d("QuestionsActivity", "User questions count: " + userQuestions.size());
                adapter.updateQuestions(userQuestions);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("QuestionsActivity", "Database error: " + error.getMessage());
            }
        });
    }
    private void deleteQuestion(Question question, int position) {
        if (question.getId() == null) return;

        String questionId = question.getId();

        // Удаление вопроса
        questionsRef.child(questionId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    // Удаление всех ответов, связанных с вопросом
                    FirebaseDatabase.getInstance()
                            .getReference("answers")
                            .child(questionId)
                            .removeValue();

                    // Удаление из списка и обновление адаптера
                    if (position >= 0 && position < userQuestions.size()) {
                        userQuestions.remove(position);
                        adapter.notifyItemRemoved(position);
                        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                });
    }

    private void openQuestionDetail(Question question) {
        Intent intent = new Intent(this, QuestionDetailActivity.class);
        intent.putExtra("id", question.getId()); // передаём только id
        startActivity(intent);
    }



}
