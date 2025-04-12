package com.example.findanswer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class QuestionAnswerActivity extends AppCompatActivity {

    TextView questionTitle, questionDescription, questionDeadline, answerBox;

    @SuppressLint({"MissingInflatedId", "SetTextI18n", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_answer);

        questionTitle = findViewById(R.id.qa_title);
        questionDescription = findViewById(R.id.qa_description);
        questionDeadline = findViewById(R.id.qa_deadline);
        answerBox = findViewById(R.id.qa_fullAnswer);
        ImageView backButton = findViewById(R.id.nav_return);


        // Обработка нажатия
        backButton.setOnClickListener(view -> finish()); // возвращаемся назад

        // Получаем данные из Intent
        String title = getIntent().getStringExtra("title");
        String description = getIntent().getStringExtra("description");
        String deadline = getIntent().getStringExtra("deadline");

        questionTitle.setText("Question");
        questionDescription.setText(description);
        questionDeadline.setText("Deadline: " + deadline);
        answerBox.setText("Answer:\n\nExplanation:");
    }
}
