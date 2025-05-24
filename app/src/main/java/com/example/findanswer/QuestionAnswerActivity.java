package com.example.findanswer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class QuestionAnswerActivity extends AppCompatActivity {

    TextView questionTitle, questionDescription, questionDeadline, fileTextLink;
    EditText answerBox;
    ImageView questionImageView;
    ImageButton backButton;

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_answer);

        // Инициализация
        questionTitle = findViewById(R.id.qa_title);
        questionDescription = findViewById(R.id.qa_description);
        questionDeadline = findViewById(R.id.qa_deadline);
        answerBox = findViewById(R.id.qa_fullAnswer);
        fileTextLink = findViewById(R.id.qa_fileLink);
        questionImageView = findViewById(R.id.qa_imageView);
        backButton = findViewById(R.id.nav_return);
        Button sendAnswerButton = findViewById(R.id.submitButton);

        // Назад
        backButton.setOnClickListener(view -> finish());

        // Получение данных вопроса из Intent
        String title = getIntent().getStringExtra("title");
        String description = getIntent().getStringExtra("description");
        String deadline = getIntent().getStringExtra("deadline");
        String fileUrl = getIntent().getStringExtra("fileUrl");

        // Получаем ID автора вопроса из Intent (нужно передавать его при открытии)
        String authorUserId = getIntent().getStringExtra("authorUserId");

        // Получаем текущего пользователя
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = (currentUser != null) ? currentUser.getUid() : null;

        // Запретить отвечать самому себе
        if (authorUserId != null && authorUserId.equals(currentUserId)) {
            sendAnswerButton.setEnabled(false);
            sendAnswerButton.setAlpha(0.5f);
            sendAnswerButton.setText("You cannot answer your own question");
            Toast.makeText(this, "You cannot answer your own question", Toast.LENGTH_LONG).show();
        }

        // Установка текста вопроса
        questionTitle.setText(title);
        questionDescription.setText(description);
        questionDeadline.setText("Deadline: " + deadline);
        answerBox.setText("Answer:\n\nExplanation:");

        // Обработка прикрепленного файла
        if (fileUrl != null && !fileUrl.isEmpty()) {
            if (fileUrl.endsWith(".jpg") || fileUrl.endsWith(".png") || fileUrl.endsWith(".jpeg")) {
                questionImageView.setVisibility(View.VISIBLE);
                fileTextLink.setVisibility(View.GONE);
                Glide.with(this).load(fileUrl).into(questionImageView);
            } else {
                questionImageView.setVisibility(View.GONE);
                fileTextLink.setVisibility(View.VISIBLE);
                fileTextLink.setText("Open file");
                fileTextLink.setOnClickListener(view -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(fileUrl));
                    startActivity(intent);
                });
            }
        } else {
            questionImageView.setVisibility(View.GONE);
            fileTextLink.setVisibility(View.GONE);
        }

        // Обработка нажатия кнопки отправки (если не заблокирована)
        sendAnswerButton.setOnClickListener(v -> {
            if (!sendAnswerButton.isEnabled()) return; // защита
            String userAnswer = answerBox.getText().toString().trim();
            if (!userAnswer.isEmpty()) {
                String questionId = getIntent().getStringExtra("id");
                if (questionId == null) {
                    Toast.makeText(this, "Error: question ID is missing", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                if (currentUser != null) {
                    String userId = currentUser.getUid();

                    DatabaseReference answersRef = FirebaseDatabase.getInstance().getReference("answers").child(questionId);

                    // Проверяем, есть ли уже ответ от этого пользователя
                    answersRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                // Ответ уже есть
                                Toast.makeText(QuestionAnswerActivity.this, "You have already answered this question", Toast.LENGTH_LONG).show();
                            } else {
                                // Нет ответа — можно добавить новый
                                DatabaseReference userRef = FirebaseDatabase.getInstance()
                                        .getReference("users").child(userId).child("name");

                                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                        String name = userSnapshot.getValue(String.class);

                                        long currentAnswerCount = snapshot.getChildrenCount();

                                        DatabaseReference questionRef = FirebaseDatabase.getInstance().getReference("questions").child(questionId);
                                        questionRef.child("coins").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot coinsSnapshot) {
                                                Long totalCoins = coinsSnapshot.getValue(Long.class);
                                                if (totalCoins == null) totalCoins = 0L;

                                                long reward;
                                                if (currentAnswerCount < 2) {
                                                    reward = totalCoins / 2;
                                                } else {
                                                    reward = 0;
                                                }

                                                DatabaseReference userCoinsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("coins");

                                                String answerId = answersRef.push().getKey();
                                                if (answerId == null) return;

                                                Map<String, Object> answerData = new HashMap<>();
                                                answerData.put("username", name);
                                                answerData.put("answer", userAnswer);
                                                answerData.put("likes", 0);
                                                answerData.put("userId", userId);
                                                answerData.put("id", answerId);
                                                answerData.put("questionId", questionId);

                                                answersRef.child(answerId).setValue(answerData).addOnSuccessListener(unused -> {
                                                    if (reward > 0) {
                                                        userCoinsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot coinsSnapshot) {
                                                                Long currentCoins = coinsSnapshot.getValue(Long.class);
                                                                if (currentCoins == null) currentCoins = 0L;

                                                                userCoinsRef.setValue(currentCoins + reward).addOnCompleteListener(task -> {
                                                                    Toast.makeText(QuestionAnswerActivity.this, "Answer submitted! +" + reward + " coins", Toast.LENGTH_SHORT).show();
                                                                    finish();
                                                                });
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError error) {
                                                                Toast.makeText(QuestionAnswerActivity.this, "Failed to update coins", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    } else {
                                                        Toast.makeText(QuestionAnswerActivity.this, "Answer submitted!", Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Toast.makeText(QuestionAnswerActivity.this, "Failed to get question reward", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(QuestionAnswerActivity.this, "Failed to check existing answers", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

    }

}
