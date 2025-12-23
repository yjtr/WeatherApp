package com.example.weatherapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.adapters.CityAdapter;
import com.example.weatherapp.database.AppDatabase;
import com.example.weatherapp.database.CityDao;
import com.example.weatherapp.models.City;

import java.util.List;

/**
 * 城市列表Activity
 * 显示已保存的城市列表，点击城市可跳转到主页面查看天气
 */
public class SecondActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CityDao cityDao;
    private CityAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        cityDao = AppDatabase.getInstance(this).cityDao();
        recyclerView = findViewById(R.id.city_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Button addBtn = findViewById(R.id.add_button);
        Button deleteBtn = findViewById(R.id.delete_button);

        addBtn.setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
        deleteBtn.setOnClickListener(v -> startActivity(new Intent(this, DeleteCityActivity.class)));

        loadCities();
    }

    /**
     * 加载城市列表
     */
    private void loadCities() {
        List<City> cities = cityDao.getAllCities();
        adapter = new CityAdapter(this, cities);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCities();
    }
}
