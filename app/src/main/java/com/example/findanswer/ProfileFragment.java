package com.example.findanswer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    private TextView answersCountTextView, likesCountTextView, profileUsername;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        answersCountTextView = view.findViewById(R.id.answersCount);
        likesCountTextView = view.findViewById(R.id.likesCount);
        profileUsername = view.findViewById(R.id.profileUsername);
        Button editProfileButton = view.findViewById(R.id.editProfileButton);
        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });
        loadUserStats();

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
                .child("avatarUrl");

        avatarRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String avatarUrl = snapshot.getValue(String.class);
                if (avatarUrl != null && isAdded()) {
                    ImageView profileAvatar = requireView().findViewById(R.id.profileAvatar);
                    Glide.with(requireContext())
                            .load(avatarUrl)
                            .placeholder(R.drawable.nav_profile)
                            .circleCrop()
                            .into(profileAvatar);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void loadUserStats() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // Пользователь не залогинен — можно скрыть статистику или показать дефолт
            answersCountTextView.setText("0");
            likesCountTextView.setText("0");
            return;
        }

        String currentUserId = currentUser.getUid();


        {
            String userId = currentUser.getUid();

            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userId)
                    .child("name");

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String name = snapshot.getValue(String.class);
                    if (name != null) {
                        // например, установить имя в TextView
                        profileUsername.setText(name);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // обработка ошибки
                }
            });
        }

        DatabaseReference answersRef = FirebaseDatabase.getInstance().getReference("answers");

        answersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalAnswers = 0;
                int totalLikes = 0;

                // Перебираем все вопросы и их ответы
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

                // Обновляем UI
                answersCountTextView.setText(String.valueOf(totalAnswers));
                likesCountTextView.setText(String.valueOf(totalLikes));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Можно обработать ошибку, например, показать тост
            }
        });
    }
}
