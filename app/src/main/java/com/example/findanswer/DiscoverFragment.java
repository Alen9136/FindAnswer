package com.example.findanswer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class DiscoverFragment extends Fragment {
    private QuestionAdapter adapter;
    private EditText searchEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discover, container, false);

        searchEditText = view.findViewById(R.id.searchEditText);
        ImageButton searchButton = view.findViewById(R.id.searchButton);
        RecyclerView recyclerView = view.findViewById(R.id.discoverRecyclerView);
        adapter = new QuestionAdapter(new ArrayList<>(), userId -> {
            // Пока ничего не делаем, или логируем
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        loadQuestions();

        searchButton.setOnClickListener(v -> performSearch());

        // Поиск при нажатии Enter
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            performSearch();
            return true;
        });

        return view;
    }

    private void loadQuestions() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("questions");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Question> questions = new ArrayList<>();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Question q = snap.getValue(Question.class);
                    if (q != null) questions.add(q);
                }
                adapter.updateAllQuestions(questions);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error loading questions", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performSearch() {
        String query = searchEditText.getText().toString().trim();
        adapter.filter(query);
    }
}