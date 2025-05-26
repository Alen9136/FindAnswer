package com.example.findanswer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
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

public class HomeFragment extends Fragment {
    private QuestionAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        adapter = new QuestionAdapter(new ArrayList<>(), userId -> {
            Intent intent = new Intent(getContext(), UserProfileActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        Button askQuestionButton = view.findViewById(R.id.ask_question_button);
        askQuestionButton.setOnClickListener(v -> {
            CreateFragment createFragment = new CreateFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, createFragment)
                    .addToBackStack(null)
                    .commit();

            // Обновляем выделение в навигации
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).updateNavigationSelection(R.id.nav_create);
            }
        });

        loadQuestions();
        return view;
    }

    private void loadQuestions() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("questions");
        ref.addValueEventListener(new ValueEventListener() {
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
                Toast.makeText(getContext(), "Failed to load questions", Toast.LENGTH_SHORT).show();
            }
        });
    }
}