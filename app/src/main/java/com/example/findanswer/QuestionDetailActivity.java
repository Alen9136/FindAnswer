package com.example.findanswer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class QuestionDetailActivity extends AppCompatActivity {

    TextView titleTextView, descriptionTextView, subjectTextView, usernameTextView;
    Button answerButton;
    EditText askAuthorEditText;
    LinearLayout answersContainer;

    String questionId;

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        titleTextView = findViewById(R.id.titleTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        subjectTextView = findViewById(R.id.subjectTextView);
        usernameTextView = findViewById(R.id.usernameTextView);
        answerButton = findViewById(R.id.answerButton2);
        askAuthorEditText = findViewById(R.id.askAuthorEditText);
        answersContainer = findViewById(R.id.answersContainer);

        // Получаем данные из Intent
        String title = getIntent().getStringExtra("title");
        String description = getIntent().getStringExtra("description");
        String subject = getIntent().getStringExtra("subject");
        String grade = getIntent().getStringExtra("grade");
        int coins = getIntent().getIntExtra("coins", 0);
        String deadline = getIntent().getStringExtra("deadline");
        String fileUrl = getIntent().getStringExtra("fileUrl");
        questionId = getIntent().getStringExtra("id");
        String username = getIntent().getStringExtra("name");

        // Устанавливаем данные в элементы UI
        titleTextView.setText(title);
        descriptionTextView.setText(description);
        subjectTextView.setText(subject + " • " + grade);
        usernameTextView.setText(username);
        answerButton.setText("+ ANSWER THE QUESTION " + coins + " C");

        // Кнопка для перехода к экрану ответа
        answerButton.setOnClickListener(v -> {
            Intent intent = new Intent(QuestionDetailActivity.this, QuestionAnswerActivity.class);
            intent.putExtra("title", title);
            intent.putExtra("description", description);
            intent.putExtra("subject", subject);
            intent.putExtra("grade", grade);
            intent.putExtra("coins", coins);
            intent.putExtra("deadline", deadline);
            intent.putExtra("fileUrl", fileUrl);
            intent.putExtra("id", questionId);
            startActivity(intent);
        });

        loadAnswers();
    }

    private void loadAnswers() {
        if (questionId == null) return;

        DatabaseReference answersRef = FirebaseDatabase.getInstance()
                .getReference("answers")
                .child(questionId);

        answersRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                answersContainer.removeAllViews();
                String currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

                for (DataSnapshot answerSnapshot : snapshot.getChildren()) {
                    String answerId = answerSnapshot.getKey();
                    String answerText = answerSnapshot.child("answer").getValue(String.class);
                    String answerUsername = answerSnapshot.child("username").getValue(String.class);
                    Long thankCount = answerSnapshot.child("thankCount").getValue(Long.class);
                    if (thankCount == null) thankCount = 0L;

                    LayoutInflater inflater = LayoutInflater.from(QuestionDetailActivity.this);
                    LinearLayout answerView = (LinearLayout) inflater.inflate(R.layout.item_answer_block, answersContainer, false);

                    TextView answerUsernameTextView = answerView.findViewById(R.id.answerUsername);
                    TextView answerTextView = answerView.findViewById(R.id.answerText);
                    TextView thankCountTextView = answerView.findViewById(R.id.thankCountText);
                    ImageView heartIcon = answerView.findViewById(R.id.heartIcon);

                    answerUsernameTextView.setText(answerUsername);
                    answerTextView.setText(answerText);
                    thankCountTextView.setText("THANKS " + thankCount);

                    boolean likedByMe = answerSnapshot.child("likedBy").hasChild(currentUserId);
                    heartIcon.setImageResource(likedByMe ? R.drawable.ic_heart_filled_red : R.drawable.ic_heart_outline_red);

                    heartIcon.setOnClickListener(v -> {
                        assert answerId != null;
                        DatabaseReference likeRef = FirebaseDatabase.getInstance()
                                .getReference("answers")
                                .child(questionId)
                                .child(answerId);

                        likeRef.child("likedBy").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (!snapshot.exists()) {
                                    likeRef.child("thankCount").runTransaction(new Transaction.Handler() {
                                        @NonNull
                                        @Override
                                        public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                                            Long currentCount = currentData.getValue(Long.class);
                                            if (currentCount == null) currentData.setValue(1);
                                            else currentData.setValue(currentCount + 1);
                                            return Transaction.success(currentData);
                                        }

                                        @Override
                                        public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                                            if (committed) {
                                                likeRef.child("likedBy").child(currentUserId).setValue(true);
                                                heartIcon.setImageResource(R.drawable.ic_heart_filled_red);
                                            }
                                        }
                                    });
                                } else {
                                    Toast.makeText(QuestionDetailActivity.this, "You already liked this answer.", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(QuestionDetailActivity.this, "Failed to like answer.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });

                    answersContainer.addView(answerView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(QuestionDetailActivity.this, "Failed to load answers.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
