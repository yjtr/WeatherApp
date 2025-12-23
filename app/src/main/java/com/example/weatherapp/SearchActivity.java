package com.example.weatherapp;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.adapters.CityAdapter;
import com.example.weatherapp.database.AppDatabase;
import com.example.weatherapp.database.CityDao;
import com.example.weatherapp.models.City;
import com.example.weatherapp.models.GeoCityResponse;
import com.example.weatherapp.network.ApiClient;
import com.example.weatherapp.network.WeatherService;
import com.example.weatherapp.utils.Constants;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 城市搜索Activity
 * 提供城市搜索功能，将搜索到的城市添加到数据库
 */
public class SearchActivity extends AppCompatActivity {

    private EditText searchBar;
    private ImageButton searchButton;
    private Button btnAddCity;
    private CityDao cityDao;
    private WeatherService api;
    private RecyclerView searchResultsRecycler;
    private ScrollView cityGridScroll;
    private CityAdapter searchResultAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchBar = findViewById(R.id.search_bar);
        searchButton = findViewById(R.id.search_button);
        btnAddCity = findViewById(R.id.add_button);
        searchResultsRecycler = findViewById(R.id.search_results_recycler);
        cityGridScroll = findViewById(R.id.city_grid_scroll);
        cityDao = AppDatabase.getInstance(this).cityDao();
        api = ApiClient.getClient().create(WeatherService.class);

        // 初始化搜索结果RecyclerView
        searchResultsRecycler.setLayoutManager(new LinearLayoutManager(this));
        searchResultAdapter = new CityAdapter(this, null, location -> addLocationToDatabase(location));
        searchResultsRecycler.setAdapter(searchResultAdapter);

        bindCityButtons();
        setupSearchFunctionality();
        btnAddCity.setOnClickListener(v -> addCityByName());
        setupBackPressedHandler();
    }

    /**
     * 设置返回键处理（使用返回栈机制）
     */
    private void setupBackPressedHandler() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 如果正在显示搜索结果，返回时显示快捷城市按钮
                if (searchResultsRecycler.getVisibility() == View.VISIBLE) {
                    searchResultsRecycler.setVisibility(View.GONE);
                    cityGridScroll.setVisibility(View.VISIBLE);
                    searchBar.setText("");
                } else {
                    // 如果没有搜索结果，禁用回调并让系统处理返回
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    /**
     * 设置搜索功能
     */
    private void setupSearchFunctionality() {
        searchButton.setOnClickListener(v -> addCityByName());
        searchBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                addCityByName();
                return true;
            }
            return false;
        });
    }

    /**
     * 根据输入的城市名称搜索城市
     * 显示所有搜索结果（包括城市及其下属各区）
     */
    private void addCityByName() {
        final String cityName = searchBar.getText().toString().trim();

        if (cityName.isEmpty()) {
            Toast.makeText(this, "请输入城市名称", Toast.LENGTH_SHORT).show();
            return;
        }

        // 增加返回数量，以便显示城市下的各区
        api.searchCity(cityName, Constants.QWEATHER_API_KEY, null, null, 20, "zh")
                .enqueue(new Callback<GeoCityResponse>() {
                    @Override
                    public void onResponse(Call<GeoCityResponse> call, Response<GeoCityResponse> response) {
                        if (response.isSuccessful() && response.body() != null &&
                                "200".equals(response.body().code) &&
                                response.body().location != null &&
                                !response.body().location.isEmpty()) {

                            // 显示搜索结果列表
                            searchResultAdapter = new CityAdapter(
                                    SearchActivity.this,
                                    response.body().location,
                                    location -> addLocationToDatabase(location)
                            );
                            searchResultsRecycler.setAdapter(searchResultAdapter);

                            // 切换显示：隐藏快捷城市按钮，显示搜索结果
                            cityGridScroll.setVisibility(View.GONE);
                            searchResultsRecycler.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(SearchActivity.this, "未找到该城市：" + cityName, Toast.LENGTH_SHORT).show();
                            // 如果没有搜索结果，显示快捷城市按钮
                            cityGridScroll.setVisibility(View.VISIBLE);
                            searchResultsRecycler.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<GeoCityResponse> call, Throwable t) {
                        Toast.makeText(SearchActivity.this, "网络错误：" + t.getMessage(), Toast.LENGTH_SHORT).show();
                        // 网络错误时显示快捷城市按钮
                        cityGridScroll.setVisibility(View.VISIBLE);
                        searchResultsRecycler.setVisibility(View.GONE);
                    }
                });
    }

    /**
     * 将选中的位置添加到数据库
     */
    private void addLocationToDatabase(GeoCityResponse.Location location) {
        if (cityDao.countByLocationId(location.id) > 0) {
            Toast.makeText(this, "城市已存在：" + location.name, Toast.LENGTH_SHORT).show();
            return;
        }

        // 保存经纬度信息，用于格点API和空气质量API
        City city = new City(location.name, location.id, location.lat, location.lon);
        cityDao.insert(city);
        Toast.makeText(this, "已添加城市：" + location.name, Toast.LENGTH_SHORT).show();
        finish();
    }

    /**
     * 绑定城市快捷按钮
     */
    private void bindCityButtons() {
        GridLayout gridLayout = findViewById(R.id.city_grid);
        bindButtonsRecursive(gridLayout);
    }

    /**
     * 递归绑定所有按钮
     */
    private void bindButtonsRecursive(ViewGroup parent) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child instanceof Button) {
                Button btn = (Button) child;
                btn.setOnClickListener(v -> {
                    searchBar.setText(btn.getText().toString());
                    // 点击快捷按钮后自动搜索
                    addCityByName();
                });
            } else if (child instanceof ViewGroup) {
                bindButtonsRecursive((ViewGroup) child);
            }
        }
    }

}
