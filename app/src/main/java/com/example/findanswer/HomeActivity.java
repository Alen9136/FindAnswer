package com.example.findanswer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Текстовый элемент для отображения текста
        TextView centerText = findViewById(R.id.center_text); // Инициализируем TextView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Загружаем главный фрагмент при запуске
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
            centerText.setText("Home Fragment"); // Устанавливаем начальный текст
        }

        // Обработка нажатий на элементы нижнего меню
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                    centerText.setText("Home Fragment");
                }else if(itemId == R.id.nav_search){
                    selectedFragment = new DiscoverFragment();
                    centerText.setText("Discover Fragment");
                }else if(itemId == R.id.nav_create){
                    selectedFragment = new CreateFragment();
                    centerText.setText("Create Fragment");
                }else if(itemId == R.id.nav_profile){
                    selectedFragment = new ProfileFragment();
                    centerText.setText("Profile Fragment");
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                }

                return true;
            }
        });
    }
}




