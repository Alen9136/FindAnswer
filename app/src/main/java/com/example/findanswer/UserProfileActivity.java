    package com.example.findanswer;

    import android.annotation.SuppressLint;
    import android.content.Intent;
    import android.os.Bundle;
    import android.widget.Button;
    import android.widget.ImageView;
    import android.widget.TextView;
    import androidx.annotation.NonNull;
    import androidx.appcompat.app.AppCompatActivity;

    import com.bumptech.glide.Glide;
    import com.bumptech.glide.load.engine.DiskCacheStrategy;
    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.database.ValueEventListener;

    import java.util.Objects;

    public class UserProfileActivity extends AppCompatActivity {

        private TextView answersCountTextView, likesCountTextView, profileUsername, coinsCountTextView;
        private ImageView profileAvatar;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_user_profile);
            Button viewQuestionsButton = findViewById(R.id.viewUserQuestionsButton);
            answersCountTextView = findViewById(R.id.answersCount);
            likesCountTextView = findViewById(R.id.likesCount);
            profileUsername = findViewById(R.id.profileUsername);
            profileAvatar = findViewById(R.id.profileAvatar);
            coinsCountTextView = findViewById(R.id.coinsCount);

            String userId = getIntent().getStringExtra("userId");
            if (userId != null) {
                loadUserStats(userId);
                loadAvatar(userId);
            }
            viewQuestionsButton.setOnClickListener(v -> {
                Intent intent = new Intent(UserProfileActivity.this, UserQuestionsActivity.class);
                intent.putExtra("userId", userId); // передаем ID текущего чужого пользователя
                startActivity(intent);
            });
        }

        private void loadAvatar(String userId) {
            DatabaseReference avatarRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userId)
                    .child("avatar");

            avatarRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String avatarUrl = snapshot.getValue(String.class);
                    if (avatarUrl != null && !isFinishing()) {
                        Glide.with(UserProfileActivity.this)
                                .load(avatarUrl)
                                .placeholder(R.drawable.nav_profile)
                                .error(R.drawable.nav_profile)
                                .circleCrop()
                                .diskCacheStrategy(DiskCacheStrategy.NONE) // Отключаем кэш
                                .skipMemoryCache(true)
                                .into(profileAvatar);
                    } else {
                        profileAvatar.setImageResource(R.drawable.nav_profile);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }

        @SuppressLint("SetTextI18n")
        private void loadUserStats(String userId) {
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userId);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String name = snapshot.child("name").getValue(String.class);
                    Long coinsLong = snapshot.child("coins").getValue(Long.class);

                    profileUsername.setText(Objects.requireNonNullElse(name, "User"));
                    int coins = coinsLong != null ? coinsLong.intValue() : 0;
                    coinsCountTextView.setText("Coins: " + coins);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });

            DatabaseReference answersRef = FirebaseDatabase.getInstance().getReference("answers");

            answersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int totalAnswers = 0;
                    int totalLikes = 0;

                    for (DataSnapshot questionSnapshot : snapshot.getChildren()) {
                        for (DataSnapshot answerSnapshot : questionSnapshot.getChildren()) {
                            String answerUserId = answerSnapshot.child("userId").getValue(String.class);
                            if (answerUserId != null && answerUserId.equals(userId)) {
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
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }
