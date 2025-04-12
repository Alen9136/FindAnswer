package com.example.findanswer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;



import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.app.DatePickerDialog;
import android.widget.ArrayAdapter;
import androidx.annotation.Nullable;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CreateFragment extends Fragment {

    private static final int PICK_FILE_REQUEST = 1;

    private Spinner classSpinner, subjectSpinner;
    private EditText titleEditText, descriptionEditText, deadlineEditText, coinsEditText;
    private Button createButton;


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

        attachFileButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*"); //
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, "Attach file"), PICK_FILE_REQUEST);
        });

        createButton = view.findViewById(R.id.createButton);

        setupSpinners();
        setupDatePicker();
        setupCreateButton();

        return view;
    }

    private void setupSpinners() {
        String[] classes = {
                "— School —",
                "1th grade", "2th grade", "3th grade", "4th grade", "5th grade", "6th grade", "7th grade", "8th grade", "9th grade", "10th grade", "11th grade", "12th grade",
                "— University —",
                "1 course", "2 course", "3 course", "4 course",
                "— College —",
                "1st year", "2nd year", "3rd year", "4th year"
        };

        List<String> subjects = Arrays.asList(
                "— Exact sciences —",
                "Algebra", "Geometry", "Physics", "Informatics", "Chemistry", "Biology",
                "— Linguistics —",
                "English", "Russian", "German", "Spanish",
                "— Humanities —",
                "History", "Philosophy", "Literature"
        );

        ArrayAdapter<String> classAdapter = getStringArrayAdapter(classes);
        classSpinner.setAdapter(classAdapter);
        int defaultClassPosition = classAdapter.getPosition("1th grade");
        classSpinner.setSelection(defaultClassPosition);

        ArrayAdapter<String> subjectAdapter = getStringArrayAdapter(subjects);
        subjectSpinner.setAdapter(subjectAdapter);
        int defaultSubjectPosition = subjectAdapter.getPosition("Algebra");
        subjectSpinner.setSelection(defaultSubjectPosition);
    }

    @NonNull
    private ArrayAdapter<String> getStringArrayAdapter(List<String> subjects) {
        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<String>(requireContext(),
                android.R.layout.simple_spinner_item, subjects) {
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

                if (item != null && item.startsWith("—")) {
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }

                return view;
            }
        };

        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return subjectAdapter;
    }

    @NonNull
    private ArrayAdapter<String> getStringArrayAdapter(String[] classes) {
        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, classes) {
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

                if (item != null && item.startsWith("—")) {
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }

                return view;
            }
        };

        classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return classAdapter;
    }


    private void setupDatePicker() {
        deadlineEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view1, selectedYear, selectedMonth, selectedDay) -> {
                        @SuppressLint("DefaultLocale") String selectedDate = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);

                        deadlineEditText.setText(selectedDate);
                    }, year, month, day);

            datePickerDialog.show();
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
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String formattedDate = sdf.format(new Date(creationDate));
            boolean valid = true;

            if (deadline.isEmpty()) {
                deadlineEditText.setError("Complete field");
                valid = false;
            } else {
                deadlineEditText.setError(null);
            }

            if (title.isEmpty()) {
                titleEditText.setError("Complete field");
                valid = false;
            }else if(title.length() > 50){
                titleEditText.setError("Title is too long (max 50 characters)");
                valid = false;
            }else {
                titleEditText.setError(null);
            }

            if (coinsStr.isEmpty()) {
                coinsEditText.setError("Complete field");
                valid = false;
            } else {
                coinsEditText.setError(null);
            }
            if(description.length() > 300) {
                descriptionEditText.setError("Description is too long (max 300 characters)");
                valid = false;
            }
            if (valid) {
                int coins = Integer.parseInt(coinsStr);

                Question question = new Question(grade, subject, title, description, deadline, coins, formattedDate);
                questionsRef.push().setValue(question)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Question uploaded!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST) {
            getActivity();
            if (resultCode == Activity.RESULT_OK && data != null) {
                Uri selectedFileUri = data.getData();
                if (selectedFileUri != null) {
                    String fileName = selectedFileUri.getLastPathSegment();
                    Toast.makeText(getContext(), "File attached: " + fileName, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}
