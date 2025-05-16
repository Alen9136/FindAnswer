package com.example.findanswer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    ImageView profileImageView;
    EditText nameEditText;
    Button saveButton;

    Uri imageUri;
    String userId;

    DatabaseReference userRef;
    StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        profileImageView = findViewById(R.id.profileImageView);
        nameEditText = findViewById(R.id.nameEditText);
        saveButton = findViewById(R.id.saveButton);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
            storageRef = FirebaseStorage.getInstance().getReference("avatars").child(userId + ".jpg");

            // Подгружаем текущие данные
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String name = snapshot.child("name").getValue(String.class);
                    String avatarUrl = snapshot.child("avatar").getValue(String.class);

                    nameEditText.setText(name);

                    if (avatarUrl != null) {
                        Glide.with(EditProfileActivity.this).load(avatarUrl).into(profileImageView);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }

        // Выбор новой аватарки
        profileImageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        // Сохранение
        saveButton.setOnClickListener(v -> {
            String newName = nameEditText.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(this, "Enter your name", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("name", newName);

            if (imageUri != null) {
                storageRef.putFile(imageUri)
                        .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            updates.put("avatar", uri.toString());
                            userRef.updateChildren(updates);
                            Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                            finish();
                        }));
            } else {
                userRef.updateChildren(updates);
                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
        }
    }
}
