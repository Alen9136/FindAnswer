package com.example.findanswer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.*;

public class CreateFragment extends Fragment {

    private static final int FILE_REQUEST_CODE = 123;

    private Spinner classSpinner, subjectSpinner;
    private EditText titleEditText, descriptionEditText, deadlineEditText, coinsEditText;
    private Button createButton;
    private String uploadedFileUrl;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create, container, false);

        classSpinner = view.findViewById(R.id.classSpinner);
        subjectSpinner = view.findViewById(R.id.subjectSpinner);
        titleEditText = view.findViewById(R.id.titleEditText);
        descriptionEditText = view.findViewById(R.id.descriptionEditText);
        deadlineEditText = view.findViewById(R.id.deadlineEditText);
        coinsEditText = view.findViewById(R.id.coinsEditText);
        Button attachFileButton = view.findViewById(R.id.attachFileButton);
        createButton = view.findViewById(R.id.createButton);

        attachFileButton.setOnClickListener(v -> requestFilePermission());

        setupSpinners();
        setupDatePicker();
        setupCreateButton();

        return view;
    }

    private void setupSpinners() {
        String[] classes = {
                "— School —", "1th grade", "2th grade", "3th grade", "4th grade", "5th grade", "6th grade", "7th grade", "8th grade", "9th grade", "10th grade", "11th grade", "12th grade",
                "— University —", "1 course", "2 course", "3 course", "4 course",
                "— College —", "1st year", "2nd year", "3rd year", "4th year"
        };

        List<String> subjects = Arrays.asList(
                "— Exact sciences —", "Algebra", "Geometry", "Physics", "Informatics", "Chemistry", "Biology",
                "— Linguistics —", "English", "Russian", "German", "Spanish",
                "— Humanities —", "History", "Philosophy", "Literature"
        );

        classSpinner.setAdapter(getDropdownAdapter(classes));
        classSpinner.setSelection(Arrays.asList(classes).indexOf("1th grade"));

        subjectSpinner.setAdapter(getDropdownAdapter(subjects));
        subjectSpinner.setSelection(subjects.indexOf("Algebra"));
    }

    private ArrayAdapter<String> getDropdownAdapter(List<String> list) {
        return new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, list) {
            @Override
            public boolean isEnabled(int position) {
                String item = getItem(position);
                return item != null && !item.startsWith("—");
            }

            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                String item = getItem(position);
                tv.setTextColor((item != null && item.startsWith("—")) ? Color.GRAY : Color.BLACK);
                return view;
            }
        };
    }

    private ArrayAdapter<String> getDropdownAdapter(String[] array) {
        return getDropdownAdapter(Arrays.asList(array));
    }

    private void setupDatePicker() {
        deadlineEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog picker = new DatePickerDialog(requireContext(), (view1, year, month, dayOfMonth) -> {
                @SuppressLint("DefaultLocale")
                String date = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year);
                deadlineEditText.setText(date);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            picker.show();
        });
    }

    private void setupCreateButton() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference questionsRef = database.getReference("questions");

        createButton.setOnClickListener(v -> {
            String grade = classSpinner.getSelectedItem().toString();
            String subject = subjectSpinner.getSelectedItem().toString();
            String title = titleEditText.getText().toString().trim();
            String description = descriptionEditText.getText().toString().trim();
            String coinsStr = coinsEditText.getText().toString().trim();
            String deadline = deadlineEditText.getText().toString().trim();
            long creationDate = System.currentTimeMillis();
            String formattedDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date(creationDate));
            boolean valid = true;

            if (deadline.isEmpty()) {
                deadlineEditText.setError("Complete field");
                valid = false;
            } else deadlineEditText.setError(null);

            if (title.isEmpty()) {
                titleEditText.setError("Complete field");
                valid = false;
            } else if (title.length() > 50) {
                titleEditText.setError("Max 50 characters");
                valid = false;
            } else titleEditText.setError(null);

            if (coinsStr.isEmpty()) {
                coinsEditText.setError("Complete field");
                valid = false;
            } else coinsEditText.setError(null);

            if (description.length() > 300) {
                descriptionEditText.setError("Max 300 characters");
                valid = false;
            }

            if (!valid) return;

            int coins = Integer.parseInt(coinsStr);

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String name = snapshot.child("name").getValue(String.class);
                    if (name == null || name.isEmpty()) {
                        Toast.makeText(getContext(), "Пожалуйста, укажите имя в профиле", Toast.LENGTH_LONG).show();
                        return;
                    }
                    String questionId = questionsRef.push().getKey(); // получаем ключ
                    if(questionId != null){
                        Question question = new Question(grade, subject, title, description, deadline, coins, formattedDate, name);
                        question.setFileUrl(uploadedFileUrl);
                        question.setId(questionId);
                        questionsRef.push().setValue(question)
                                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Question uploaded!", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Failed to load username", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void uploadFileToFirebase(Uri fileUri) {
        String fileName = UUID.randomUUID().toString();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("uploads/" + fileName);

        storageRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot ->
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            uploadedFileUrl = uri.toString();
                            Toast.makeText(getContext(), "Файл загружен!", Toast.LENGTH_SHORT).show();
                        }))
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Ошибка загрузки: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void requestFilePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        android.Manifest.permission.READ_MEDIA_IMAGES,
                        android.Manifest.permission.READ_MEDIA_VIDEO,
                        android.Manifest.permission.READ_MEDIA_AUDIO
                }, 200);
            } else {
                openFilePicker();
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 200);
            } else {
                openFilePicker();
            }
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, FILE_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 200) {
            boolean granted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }

            if (granted) {
                openFilePicker();
            } else {
                Toast.makeText(getContext(), "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedFileUri = data.getData();
            if (selectedFileUri != null) {
                uploadFileToFirebase(selectedFileUri);
            }
        }
    }
}
