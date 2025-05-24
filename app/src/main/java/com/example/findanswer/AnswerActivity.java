package com.example.findanswer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.findanswer.adapters.AnswerAdapter;
import com.example.findanswer.models.Answer;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class AnswerActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    AnswerAdapter answerAdapter;
    List<Answer> answerList;
    DatabaseReference answersRef;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer); // создай layout с RecyclerView

        recyclerView = findViewById(R.id.recyclerViewAnswers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        answerList = new ArrayList<>();
        answerAdapter = new AnswerAdapter(answerList);
        recyclerView.setAdapter(answerAdapter);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String currentUserId = currentUser.getUid();

        answersRef = FirebaseDatabase.getInstance().getReference("answers");

        // Получаем все ответы и фильтруем по userId
        answersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                answerList.clear();
                for (DataSnapshot questionSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot answerSnapshot : questionSnapshot.getChildren()) {
                        Answer answer = answerSnapshot.getValue(Answer.class);
                        if (answer != null && currentUserId.equals(answer.getUserId())) {
                            answer.setId(answerSnapshot.getKey());
                            answer.setQuestionId(questionSnapshot.getKey());
                            answerList.add(answer);
                        }
                    }
                }
                answerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AnswerActivity.this, "Failed to load answers", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
