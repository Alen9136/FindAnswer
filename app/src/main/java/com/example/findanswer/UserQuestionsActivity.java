package com.example.findanswer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class UserQuestionsActivity extends AppCompatActivity {

    private QuestionAdapter adapter;
    private List<Question> questionList;
    private String userId;

    private TextView questionsTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_questions);

        RecyclerView recyclerView = findViewById(R.id.questionsRecyclerView);
        questionsTitle = findViewById(R.id.questionsTitle);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        questionList = new ArrayList<>();

        // Получаем userId из Intent
        userId = getIntent().getStringExtra("userId");

        adapter = new QuestionAdapter(questionList, question -> {
            // Обработка клика на вопрос (если нужно)
        });
        recyclerView.setAdapter(adapter);

        // Загружаем имя пользователя и отображаем в questionsTitle
        loadUserName();

        // Загружаем вопросы пользователя
        loadUserQuestions();
    }

    private void loadUserName() {
        if (userId == null) return;

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                if (name != null && !name.isEmpty()) {
                    questionsTitle.setText(name + "'s Questions");
                } else {
                    questionsTitle.setText("User's Questions");
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                questionsTitle.setText("User's Questions");
            }
        });
    }

    private void loadUserQuestions() {
        if (userId == null) return;

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("questions");

        // Важно: для orderByChild и equalTo поле должно существовать и быть правильно заполнено
        // В твоей модели вопроса должен быть userId (или authorId) для фильтрации по нему
        ref.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        questionList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Question q = ds.getValue(Question.class);
                            if (q != null) {
                                questionList.add(q);
                            }
                        }
                        adapter.updateAllQuestions(questionList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Можно показать ошибку, если нужно
                    }
                });
    }
}
