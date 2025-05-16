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

        questionTitle = findViewById(R.id.qa_title);
        questionDescription = findViewById(R.id.qa_description);
        questionDeadline = findViewById(R.id.qa_deadline);
        answerBox = findViewById(R.id.qa_fullAnswer);
        fileTextLink = findViewById(R.id.qa_fileLink);
        questionImageView = findViewById(R.id.qa_imageView);
        backButton = findViewById(R.id.nav_return);

        // Назад
        backButton.setOnClickListener(view -> finish());

        // Получение данных
        String title = getIntent().getStringExtra("title");
        String description = getIntent().getStringExtra("description");
        String deadline = getIntent().getStringExtra("deadline");
        String fileUrl = getIntent().getStringExtra("fileUrl");


        // Установка текста
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
        Button sendAnswerButton = findViewById(R.id.submitButton);

        sendAnswerButton.setOnClickListener(v -> {
            String userAnswer = answerBox.getText().toString().trim();
            if (!userAnswer.isEmpty()) {
                String questionId = getIntent().getStringExtra("id");
                if (questionId == null) {
                    // Логируем или показываем ошибку
                    Toast.makeText(this, "Error: question ID is missing", Toast.LENGTH_LONG).show();
                    finish();  // Закрываем активность, потому что дальше работать не с чем
                    return;
                }
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    String userId = user.getUid();

                    DatabaseReference userRef = FirebaseDatabase.getInstance()
                            .getReference("users").child(userId).child("name");

                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String name = snapshot.getValue(String.class);
                            DatabaseReference answersRef = FirebaseDatabase.getInstance().getReference("answers").child(questionId);
                            String answerId = answersRef.push().getKey();

                            Map<String, Object> answerData = new HashMap<>();
                            answerData.put("username", name);
                            answerData.put("answer", userAnswer);
                            answerData.put("likes", 0);
                            answerData.put(answerId, answerId);
                            answerData.put("userId", userId);

                            if (answerId != null) {
                                answersRef.child(answerId).setValue(answerData).addOnSuccessListener(unused -> {
                                    finish();
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Ошибка получения имени
                        }
                    });
                }


                // Пример: сохраняем в Firebase (в узел answers)

            }
        });

    }

}
