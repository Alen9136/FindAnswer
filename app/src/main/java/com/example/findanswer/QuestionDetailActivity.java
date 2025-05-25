package com.example.findanswer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
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

    private static final String TAG = "QuestionDetailActivity";

    TextView titleTextView, descriptionTextView, subjectTextView, usernameTextView;
    Button answerButton;
    EditText askAuthorEditText;
    LinearLayout answersContainer;
    TextView clarificationsTitle;
    private String authorUserId;
    LinearLayout clarificationsContainer;
    String questionId;
    TextView deadlineTextView;

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        // Инициализация всех View
        initViews();

        questionId = getIntent().getStringExtra("id");
        if (questionId == null) {
            Toast.makeText(this, "Question ID не передан", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupSendClarificationButton();
        loadQuestionDetails();
        loadAnswers();
        loadClarifications();
    }

    private void initViews() {
        titleTextView = findViewById(R.id.titleTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        subjectTextView = findViewById(R.id.subjectTextView);
        usernameTextView = findViewById(R.id.usernameTextView);
        answerButton = findViewById(R.id.answerButton2);
        askAuthorEditText = findViewById(R.id.askAuthorEditText);
        Button sendClarificationButton = findViewById(R.id.sendClarificationButton);
        clarificationsTitle = findViewById(R.id.clarificationsTitle);
        clarificationsContainer = findViewById(R.id.clarificationsContainer);
        answersContainer = findViewById(R.id.answersContainer);
        deadlineTextView = findViewById(R.id.deadlineTextView);
    }

    private void setupSendClarificationButton() {
        Button sendClarificationButton = findViewById(R.id.sendClarificationButton);
        sendClarificationButton.setOnClickListener(v -> {
            String message = askAuthorEditText.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
                return;
            }

            String senderId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
            String clarificationId = FirebaseDatabase.getInstance().getReference()
                    .child("clarifications").child(questionId).push().getKey();

            if (clarificationId == null) {
                Toast.makeText(this, "Failed to generate clarification ID", Toast.LENGTH_SHORT).show();
                return;
            }

            Clarification clarification = new Clarification(senderId, message, System.currentTimeMillis());

            FirebaseDatabase.getInstance().getReference()
                    .child("clarifications")
                    .child(questionId)
                    .child(clarificationId)
                    .setValue(clarification)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show();
                        askAuthorEditText.setText("");
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to send message: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });
    }

    private void loadClarifications() {
        DatabaseReference clarificationsRef = FirebaseDatabase.getInstance()
                .getReference("clarifications")
                .child(questionId);

        clarificationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (clarificationsContainer == null) return;

                clarificationsContainer.removeAllViews();

                if (!snapshot.exists()) {
                    if (clarificationsTitle != null) {
                        clarificationsTitle.setVisibility(View.GONE);
                    }
                    return;
                }

                if (clarificationsTitle != null) {
                    clarificationsTitle.setVisibility(View.VISIBLE);
                }

                LayoutInflater inflater = LayoutInflater.from(QuestionDetailActivity.this);

                for (DataSnapshot clarificationSnapshot : snapshot.getChildren()) {
                    Clarification clarification = clarificationSnapshot.getValue(Clarification.class);
                    if (clarification == null) continue;

                    String senderId = clarification.getSenderId();
                    String message = clarification.getMessage();

                    FirebaseDatabase.getInstance().getReference("users").child(senderId).get()
                            .addOnSuccessListener(userSnapshot -> {
                                String username = "Unknown user";
                                String photoUrl = null;

                                if (userSnapshot.exists()) {
                                    String nameFromDb = userSnapshot.child("name").getValue(String.class);
                                    if (nameFromDb != null) username = nameFromDb;
                                    photoUrl = userSnapshot.child("avatar").getValue(String.class);
                                }

                                View clarificationView = inflater.inflate(R.layout.item_clarification, clarificationsContainer, false);

                                TextView usernameView = clarificationView.findViewById(R.id.clarificationUsername);
                                TextView messageView = clarificationView.findViewById(R.id.clarificationMessage);
                                ImageView photoView = clarificationView.findViewById(R.id.clarificationUserPhoto);

                                if (usernameView != null) usernameView.setText(username);
                                if (messageView != null) messageView.setText(message);

                                if (photoView != null) {
                                    if (photoUrl != null && !photoUrl.isEmpty()) {
                                        Glide.with(QuestionDetailActivity.this)
                                                .load(photoUrl)
                                                .placeholder(R.drawable.nav_profile)
                                                .circleCrop()
                                                .into(photoView);
                                    } else {
                                        photoView.setImageResource(R.drawable.nav_profile);
                                    }

                                    photoView.setOnClickListener(v -> {
                                        Intent intent = new Intent(QuestionDetailActivity.this, UserProfileActivity.class);
                                        intent.putExtra("userId", senderId);
                                        startActivity(intent);
                                    });
                                }

                                if (clarificationsContainer != null) {
                                    clarificationsContainer.addView(clarificationView);
                                }
                            })
                            .addOnFailureListener(e -> Log.e(TAG, "Failed to load user data", e));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(QuestionDetailActivity.this, "Ошибка загрузки уточнений: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadQuestionDetails() {
        DatabaseReference questionRef = FirebaseDatabase.getInstance()
                .getReference("questions")
                .child(questionId);

        questionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Question question = snapshot.getValue(Question.class);
                if (question == null) {
                    Toast.makeText(QuestionDetailActivity.this, "Question data is null", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                if (deadlineTextView != null) {
                    deadlineTextView.setText("Deadline: " + question.getDeadline());
                }

                if (titleTextView != null) titleTextView.setText(question.title);
                if (descriptionTextView != null) descriptionTextView.setText(question.description);
                if (subjectTextView != null) subjectTextView.setText(question.subject + " • " + question.grade);
                if (usernameTextView != null) usernameTextView.setText(question.getName());
                if (answerButton != null) answerButton.setText("+ ANSWER THE QUESTION " + question.coins/2 + " C");

                authorUserId = question.getUserId();

                if (usernameTextView != null) {
                    usernameTextView.setOnClickListener(v -> {
                        if (authorUserId != null && !authorUserId.isEmpty()) {
                            Intent intent = new Intent(QuestionDetailActivity.this, UserProfileActivity.class);
                            intent.putExtra("userId", authorUserId);
                            startActivity(intent);
                        } else {
                            Toast.makeText(QuestionDetailActivity.this, "User ID не доступен", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                if (answerButton != null) {
                    answerButton.setOnClickListener(v -> {
                        Intent intent = new Intent(QuestionDetailActivity.this, QuestionAnswerActivity.class);
                        intent.putExtra("title", question.title);
                        intent.putExtra("description", question.description);
                        intent.putExtra("subject", question.subject);
                        intent.putExtra("grade", question.grade);
                        intent.putExtra("coins", question.coins);
                        intent.putExtra("deadline", question.deadline);
                        intent.putExtra("fileUrl", question.getFileUrl());
                        intent.putExtra("id", questionId);
                        intent.putExtra("authorUserId", authorUserId);
                        startActivity(intent);
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(QuestionDetailActivity.this, "Ошибка загрузки вопроса: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void loadAnswers() {
        if (questionId == null || answersContainer == null) return;

        DatabaseReference answersRef = FirebaseDatabase.getInstance()
                .getReference("answers")
                .child(questionId);

        answersRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                answersContainer.removeAllViews();

                FirebaseAuth auth = FirebaseAuth.getInstance();
                if (auth.getCurrentUser() == null) return;

                String currentUserId = auth.getCurrentUser().getUid();

                for (DataSnapshot answerSnapshot : snapshot.getChildren()) {
                    String answerId = answerSnapshot.getKey();
                    String answerText = answerSnapshot.child("answer").getValue(String.class);
                    String answerUsername = answerSnapshot.child("username").getValue(String.class);
                    String answerUserId = answerSnapshot.child("userId").getValue(String.class);
                    Long thankCount = answerSnapshot.child("thankCount").getValue(Long.class);

                    // Проверка на null значений
                    if (answerText == null) answerText = "";
                    if (answerUsername == null) answerUsername = "Anonymous";
                    if (thankCount == null) thankCount = 0L;

                    LayoutInflater inflater = LayoutInflater.from(QuestionDetailActivity.this);
                    View answerView = inflater.inflate(R.layout.item_answer_block, answersContainer, false);

                    @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView answerUsernameTextView = answerView.findViewById(R.id.usernameTextView);
                    @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView answerTextView = answerView.findViewById(R.id.answerText);
                    TextView thankCountTextView = answerView.findViewById(R.id.thankCountText);
                    ImageView heartIcon = answerView.findViewById(R.id.heartIcon);

                    // Проверка на null перед установкой текста
                    if (answerUsernameTextView != null) answerUsernameTextView.setText(answerUsername);
                    if (answerTextView != null) answerTextView.setText(answerText);
                    if (thankCountTextView != null) thankCountTextView.setText("THANKS " + thankCount);

                    if (heartIcon != null) {
                        boolean likedByMe = answerSnapshot.child("likedBy").hasChild(currentUserId);
                        heartIcon.setImageResource(likedByMe ? R.drawable.ic_heart_filled_red : R.drawable.ic_heart_outline_red);

                        heartIcon.setOnClickListener(v -> {
                            if (answerId == null) return;
                            if (answerUserId != null && answerUserId.equals(currentUserId)) {
                                Toast.makeText(QuestionDetailActivity.this, "You can't like your own answer.", Toast.LENGTH_SHORT).show();
                                return;
                            }

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
                                                if (committed && heartIcon != null) {
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
                                    Toast.makeText(QuestionDetailActivity.this, "Failed to like answer: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        });
                    }

                    answersContainer.addView(answerView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(QuestionDetailActivity.this, "Failed to load answers: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}