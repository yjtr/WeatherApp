package com.example.weatherapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.adapters.CityAdapter;
import com.example.weatherapp.database.AppDatabase;
import com.example.weatherapp.database.CityDao;
import com.example.weatherapp.models.City;

import java.util.List;

/**
 * 删除城市Activity
 * 显示城市列表，点击城市可删除
 */
public class DeleteCityActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_city);

        CityDao cityDao = AppDatabase.getInstance(this).cityDao();
        RecyclerView recyclerView = findViewById(R.id.delete_city_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<City> cities = cityDao.getAllCities();
        CityAdapter adapter = new CityAdapter(this, cities, true);
        recyclerView.setAdapter(adapter);
    }
}
