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

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

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
                fileTextLink.setText("Открыть файл");
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
    }
}
