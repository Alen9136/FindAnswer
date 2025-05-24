package com.example.findanswer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class ProfileFragment extends Fragment {

    private TextView answersCountTextView, likesCountTextView, profileUsername;
    private ImageView profileAvatar;
    private TextView coinsCountTextView;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        answersCountTextView = view.findViewById(R.id.answersCount);
        likesCountTextView = view.findViewById(R.id.likesCount);

        profileUsername = view.findViewById(R.id.profileUsername);
        profileAvatar = view.findViewById(R.id.profileAvatar);
        coinsCountTextView = view.findViewById(R.id.coinsCount);


        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView editProfileText = view.findViewById(R.id.editText);
        editProfileText.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView questionText = view.findViewById(R.id.questionText);
        questionText.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), QuestionsActivity.class);
            startActivity(intent);
        });
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView answerTexts = view.findViewById(R.id.answersText);
        answerTexts.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AnswerActivity.class);
            startActivity(intent);
        });

        loadUserStats();
        loadAvatar();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserStats(); // обновит имя, лайки и ответы
        loadAvatar();    // обновит аватарку
    }

    private void loadAvatar() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        DatabaseReference avatarRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("avatar");

        avatarRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String avatarUrl = snapshot.getValue(String.class);
                if (avatarUrl != null && isAdded()) {
                    Glide.with(requireContext())
                            .load(avatarUrl)
                            .placeholder(R.drawable.nav_profile)
                            .error(R.drawable.nav_profile)
                            .circleCrop()
                            .diskCacheStrategy(DiskCacheStrategy.NONE) // Отключаем кэш
                            .skipMemoryCache(true)
                            .into(profileAvatar);
                } else {
                    profileAvatar.setImageResource(R.drawable.nav_profile); // дефолтный аватар
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Ошибка загрузки аватара — можно обработать
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void loadUserStats() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            answersCountTextView.setText("0");
            likesCountTextView.setText("0");
            coinsCountTextView.setText("Coins: 0");
            profileUsername.setText("Guest");
            return;
        }

        String currentUserId = currentUser.getUid();

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUserId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                Long coinsLong = snapshot.child("coins").getValue(Long.class);

                profileUsername.setText(name != null ? name : "User");

                int coins = coinsLong != null ? coinsLong.intValue() : 0;
                coinsCountTextView.setText("Coins: " + coins);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Логирование при необходимости
            }
        });

        DatabaseReference answersRef = FirebaseDatabase.getInstance().getReference("answers");

        answersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalAnswers = 0;
                int totalLikes = 0;

                for (DataSnapshot questionSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot answerSnapshot : questionSnapshot.getChildren()) {
                        String userId = answerSnapshot.child("userId").getValue(String.class);
                        if (userId != null && userId.equals(currentUserId)) {
                            totalAnswers++;
                            Integer likes = answerSnapshot.child("thankCount").getValue(Integer.class);
                            if (likes != null) {
                                totalLikes += likes;
                            }
                        }
                    }
                }

                answersCountTextView.setText(String.valueOf(totalAnswers));
                likesCountTextView.setText(String.valueOf(totalLikes));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Логирование при необходимости
            }
        });
    }

}
