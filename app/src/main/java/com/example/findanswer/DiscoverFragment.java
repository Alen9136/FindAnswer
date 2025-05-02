package com.example.findanswer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DiscoverFragment extends Fragment {

    private EditText searchEditText;
    private QuestionAdapter adapter;
    private List<Question> fullQuestionList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discover, container, false);

        searchEditText = view.findViewById(R.id.searchEditText);
        ImageButton searchButton = view.findViewById(R.id.searchButton);
        RecyclerView recyclerView = view.findViewById(R.id.discoverRecyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        fullQuestionList = new ArrayList<>();
        adapter = new QuestionAdapter(fullQuestionList);
        recyclerView.setAdapter(adapter);

        loadQuestions();

        searchButton.setOnClickListener(v -> {
            String query = searchEditText.getText().toString().trim();
            filterQuestions(query);
        });

        return view;
    }

    private void loadQuestions() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("questions");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                fullQuestionList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Question q = snap.getValue(Question.class);
                    if (q != null) {
                        fullQuestionList.add(q);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void filterQuestions(String query) {
        if (TextUtils.isEmpty(query)) {
            adapter.updateList(fullQuestionList);
            return;
        }

        List<Question> filteredList = new ArrayList<>();
        for (Question q : fullQuestionList) {
            if ((q.title != null && q.title.toLowerCase().contains(query.toLowerCase())) ||
                    (q.description != null && q.description.toLowerCase().contains(query.toLowerCase())) ||
                    (q.subject != null && q.subject.toLowerCase().contains(query.toLowerCase())) ||
                    (q.grade != null && q.grade.toLowerCase().contains(query.toLowerCase()))) {
                filteredList.add(q);
            }
        }
        adapter.updateList(filteredList);
    }
}
