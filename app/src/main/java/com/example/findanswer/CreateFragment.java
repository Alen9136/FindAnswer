package com.example.findanswer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Calendar;
import java.util.Objects;

public class CreateFragment extends Fragment {

    private static final int PICK_FILE_REQUEST = 1;

    private EditText questionInput;
    private Button submitButton;
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
            startActivityForResult(Intent.createChooser(intent, "Выбери файл"), PICK_FILE_REQUEST);
        });

        createButton = view.findViewById(R.id.createButton);

        setupSpinners();
        setupDatePicker();
        setupCreateButton();

        return view;
    }

    private void setupSpinners() {
        String[] classes = {"1", "2", "3"};
        String[] subjects = {"Math", "English"};

        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, classes);
        classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        classSpinner.setAdapter(classAdapter);

        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, subjects);
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subjectSpinner.setAdapter(subjectAdapter);
    }

    private void setupDatePicker() {
        deadlineEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view, year1, month1, dayOfMonth) -> {
                        String selectedDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                        deadlineEditText.setText(selectedDate);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });
    }

    private void setupCreateButton() {
        createButton.setOnClickListener(v -> {
            String selectedClass = classSpinner.getSelectedItem().toString();
            String selectedSubject = subjectSpinner.getSelectedItem().toString();
            String title = titleEditText.getText().toString().trim();
            String description = descriptionEditText.getText().toString().trim();
            String deadline = deadlineEditText.getText().toString().trim();
            String coins = coinsEditText.getText().toString().trim();

            if (title.isEmpty() || description.isEmpty() || deadline.isEmpty() || coins.isEmpty()) {
                Toast.makeText(getContext(), "Complete all fields", Toast.LENGTH_SHORT).show();
                return;
            }


            Toast.makeText(getContext(), "Question Created!", Toast.LENGTH_SHORT).show();

            requireActivity().getSupportFragmentManager().popBackStack();
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
