package com.example.weatherapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.weatherapp.adapters.DailyForecastAdapter;
import com.example.weatherapp.views.TemperatureChartView;
import com.example.weatherapp.views.PrecipChartView;
import android.widget.HorizontalScrollView;
import com.example.weatherapp.database.AppDatabase;
import com.example.weatherapp.database.CityDao;
import com.example.weatherapp.models.AirQualityResponse;
import com.example.weatherapp.models.City;
import com.example.weatherapp.models.GeoCityResponse;
import com.example.weatherapp.models.MinutelyResponse;
import com.example.weatherapp.models.SolarRadiationResponse;
import com.example.weatherapp.models.SunResponse;
import com.example.weatherapp.models.WeatherResponse;
import com.example.weatherapp.network.ApiClient;
import com.example.weatherapp.network.WeatherService;
import com.example.weatherapp.utils.Constants;
import com.example.weatherapp.utils.DateUtils;
import com.example.weatherapp.utils.SharedPrefsUtils;
import com.example.weatherapp.utils.WeatherIconUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 主页面Activity
 * 显示当前城市的天气信息，包括实时天气、24小时预报、4天预报和空气质量
 */
public class MainActivity extends AppCompatActivity {

    // ==================== UI组件 ====================
    // 基础信息显示
    private TextView tvLowTemp, tvTemperature, tvHighTemp, tvCity, tvDayOfWeek, tvWeather;
    private ImageView imageView;
    private TextView tvAirQuality, tvTempRange;
    private Button btnAirQuality;

    // 24小时预报
    private LinearLayout hourlyForecastContainer;
    private HorizontalScrollView temperatureChartScroll;
    private HorizontalScrollView hourlyIconsScroll;
    private TemperatureChartView temperatureLineChart;

    // 15天预报
    private RecyclerView dailyForecastRecycler;
    private DailyForecastAdapter dailyForecastAdapter;

    // 分钟级降水预报
    private LinearLayout minutelyPrecipSection;
    private TextView minutelySummary;
    private PrecipChartView precipitationBarChart;

    // 紫外线强度和日出日落
    private TextView uvIndexValue;
    private TextView uvIndexDesc;
    private TextView uvIndexTime;
    private TextView sunriseTime;
    private TextView sunsetTime;

    // 下拉刷新
    private SwipeRefreshLayout swipeRefreshLayout;

    // ==================== 数据源和状态 ====================
    private WeatherService weatherService;
    private CityDao cityDao;
    private String currentLocationId;
    private String currentCityName;
    private String airQualityHealthEffect = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        initDataSources();
        handleIntent();
        setupNavigationButton();
    }

    /**
     * 初始化数据源
     */
    private void initDataSources() {
        cityDao = AppDatabase.getInstance(this).cityDao();
        weatherService = ApiClient.getClient().create(WeatherService.class);
    }

    /**
     * 处理Intent参数，决定加载哪个城市的天气
     */
    private void handleIntent() {
        String cityName = getIntent().getStringExtra("city_name");
        String locationId = getIntent().getStringExtra("location_id");

        if (cityName != null && !cityName.isEmpty()) {
            tvCity.setText(cityName);
            SharedPrefsUtils.saveDefaultCity(this, cityName);
            if (locationId != null && !locationId.isEmpty()) {
                fetchWeatherData(locationId, cityName);
            } else {
                loadCityAndFetchData(cityName);
            }
        } else {
            loadDefaultCity();
        }
    }

    /**
     * 设置导航按钮
     */
    private void setupNavigationButton() {
        ImageButton btn = findViewById(R.id.btn);
        btn.setOnClickListener(v -> startActivity(new Intent(this, SecondActivity.class)));
    }

    // ==================== 第一部分：工具方法 ====================

    /**
     * 设置折线图和图标容器的同步滑动
     */
    private void setupSynchronizedScrolling() {
        if (temperatureChartScroll == null || hourlyIconsScroll == null) {
            return;
        }

        // 使用自定义ScrollView监听器实现同步滑动
        temperatureChartScroll.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (hourlyIconsScroll != null && hourlyIconsScroll.getScrollX() != scrollX) {
                hourlyIconsScroll.scrollTo(scrollX, 0);
            }
        });

        hourlyIconsScroll.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (temperatureChartScroll != null && temperatureChartScroll.getScrollX() != scrollX) {
                temperatureChartScroll.scrollTo(scrollX, 0);
            }
        });
    }

    // ==================== 第二部分：数据库相关代码 (约35行) ====================

    /**
     * 加载默认城市
     */
    private void loadDefaultCity() {
        String city = SharedPrefsUtils.loadDefaultCity(this);
        if (city != null && !city.isEmpty()) {
            loadCityAndFetchData(city);
        } else {
            loadFirstCityFromDatabase();
        }
    }

    /**
     * 从数据库加载第一个城市
     */
    private void loadFirstCityFromDatabase() {
        List<City> cities = cityDao.getAllCities();
        if (cities != null && !cities.isEmpty()) {
            loadCityAndFetchData(cities.get(0).getName());
        } else {
            tvCity.setText("暂无城市");
        }
    }

    /**
     * 根据城市名加载城市信息并获取天气数据
     */
    private void loadCityAndFetchData(String cityName) {
        City city = cityDao.getCityByName(cityName);
        if (city != null && city.getLocationId() != null) {
            fetchWeatherData(city.getLocationId(), cityName);
        } else {
            tvCity.setText("城市信息未找到");
        }
    }

    // ==================== 第三部分：API相关代码 (约530行) ====================

    /**
     * 请求天气数据（实时天气、15天预报、24小时预报、空气质量、分钟级降水）
     */
    private void fetchWeatherData(String locationId, String cityName) {
        if (Constants.QWEATHER_API_KEY == null || Constants.QWEATHER_API_KEY.isEmpty()) {
            tvCity.setText("请配置 API Key");
            stopRefreshAnimation();
            return;
        }

        // 保存当前城市信息，用于下拉刷新
        currentLocationId = locationId;
        currentCityName = cityName;

        final WeatherResponse weatherResult = new WeatherResponse();
        weatherResult.setCityName(cityName);
        final AirQualityResponse[] airResult = {new AirQualityResponse()};
        final boolean[] nowOk = {false};
        final boolean[] dailyOk = {false};
        final boolean[] airOk = {false};

        Runnable updateUI = () -> {
            if (nowOk[0] && dailyOk[0]) {
                runOnUiThread(() -> {
                    updateWeatherUI(weatherResult, airResult[0]);
                    stopRefreshAnimation();
                });
            }
        };

        Runnable updateAirQuality = () -> {
            if (airOk[0]) {
                runOnUiThread(() -> updateAirQualityUI(airResult[0]));
            }
        };

        fetchRealTimeWeather(locationId, weatherResult, nowOk, updateUI);
        fetchDailyForecast(locationId, weatherResult, dailyOk, updateUI);
        fetchHourlyForecast(locationId);
        fetchAirQuality(locationId, airResult, airOk, updateAirQuality);
        fetchSunriseSunset(cityName);
        fetchSolarRadiation(cityName);
        fetchMinutelyPrecipitation(locationId);
    }

    /**
     * 获取实时天气
     */
    private void fetchRealTimeWeather(String locationId, final WeatherResponse weatherResult,
                                      final boolean[] nowOk, final Runnable updateUI) {
        weatherService.getWeatherNow(locationId, Constants.QWEATHER_API_KEY)
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                        if (response.isSuccessful() && response.body() != null &&
                                "200".equals(response.body().getCode())) {
                            weatherResult.setNow(response.body().getNow());
                            nowOk[0] = true;
                            updateUI.run();
                        } else {
                            runOnUiThread(() -> stopRefreshAnimation());
                        }
                    }

                    @Override
                    public void onFailure(Call<WeatherResponse> call, Throwable t) {
                        runOnUiThread(() -> stopRefreshAnimation());
                    }
                });
    }

    /**
     * 获取15天预报
     * 优化：如果主API失败，尝试使用格点API作为备用方案
     */
    private void fetchDailyForecast(String locationId, final WeatherResponse weatherResult,
                                    final boolean[] dailyOk, final Runnable updateUI) {
        weatherService.getWeather15d(locationId, Constants.QWEATHER_API_KEY)
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                        if (response.isSuccessful() && response.body() != null &&
                                "200".equals(response.body().getCode())) {
                            weatherResult.setDaily(response.body().getDaily());
                            dailyOk[0] = true;
                            updateUI.run();
                        } else {
                            // 主API失败，尝试使用格点API作为备用
                            tryGridDailyForecast(locationId, weatherResult, dailyOk, updateUI);
                        }
                    }

                    @Override
                    public void onFailure(Call<WeatherResponse> call, Throwable t) {
                        // 主API失败，尝试使用格点API作为备用
                        tryGridDailyForecast(locationId, weatherResult, dailyOk, updateUI);
                    }
                });
    }

    /**
     * 使用格点API获取15天预报（备用方案）
     */
    private void tryGridDailyForecast(String locationId, final WeatherResponse weatherResult,
                                      final boolean[] dailyOk, final Runnable updateUI) {
        // 尝试从数据库获取经纬度
        City city = cityDao.getCityByLocationId(locationId);
        if (city != null && city.getLatitude() != null && city.getLongitude() != null &&
                !city.getLatitude().isEmpty() && !city.getLongitude().isEmpty()) {
            String location = city.getLatitude() + "," + city.getLongitude();
            weatherService.getGridWeatherDaily(location, Constants.QWEATHER_API_KEY)
                    .enqueue(new Callback<WeatherResponse>() {
                        @Override
                        public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                            if (response.isSuccessful() && response.body() != null &&
                                    "200".equals(response.body().getCode())) {
                                weatherResult.setDaily(response.body().getDaily());
                                dailyOk[0] = true;
                                updateUI.run();
                            }
                        }

                        @Override
                        public void onFailure(Call<WeatherResponse> call, Throwable t) {
                            // 格点API也失败，保持dailyOk为false
                        }
                    });
        }
    }

    /**
     * 获取24小时预报
     * 优化：如果主API失败，尝试使用格点API作为备用方案
     */
    private void fetchHourlyForecast(String locationId) {
        weatherService.getWeather24h(locationId, Constants.QWEATHER_API_KEY)
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                        if (response.isSuccessful() && response.body() != null &&
                                "200".equals(response.body().getCode())) {
                            runOnUiThread(() -> updateHourlyForecast(response.body().getHourly()));
                        } else {
                            // 主API失败，尝试使用格点API作为备用
                            tryGridHourlyForecast(locationId);
                        }
                    }

                    @Override
                    public void onFailure(Call<WeatherResponse> call, Throwable t) {
                        // 主API失败，尝试使用格点API作为备用
                        tryGridHourlyForecast(locationId);
                    }
                });
    }

    /**
     * 使用格点API获取24小时预报（备用方案）
     */
    private void tryGridHourlyForecast(String locationId) {
        // 尝试从数据库获取经纬度
        City city = cityDao.getCityByLocationId(locationId);
        if (city != null && city.getLatitude() != null && city.getLongitude() != null &&
                !city.getLatitude().isEmpty() && !city.getLongitude().isEmpty()) {
            String location = city.getLatitude() + "," + city.getLongitude();
            weatherService.getGridWeatherHourly(location, Constants.QWEATHER_API_KEY)
                    .enqueue(new Callback<WeatherResponse>() {
                        @Override
                        public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                            if (response.isSuccessful() && response.body() != null &&
                                    "200".equals(response.body().getCode())) {
                                runOnUiThread(() -> updateHourlyForecast(response.body().getHourly()));
                            }
                        }

                        @Override
                        public void onFailure(Call<WeatherResponse> call, Throwable t) {
                            // 格点API也失败，不更新UI
                        }
                    });
        }
    }

    /**
     * 获取空气质量
     */
    private void fetchAirQuality(String locationId, final AirQualityResponse[] airResult,
                                 final boolean[] airOk, final Runnable updateAirQuality) {
        weatherService.getAirQuality(locationId, Constants.QWEATHER_API_KEY)
                .enqueue(new Callback<AirQualityResponse>() {
                    @Override
                    public void onResponse(Call<AirQualityResponse> call, Response<AirQualityResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            AirQualityResponse body = response.body();
                            if ("200".equals(body.getCode())) {
                                if (body.getIndexes() != null && !body.getIndexes().isEmpty()) {
                                    airResult[0] = body;
                                } else if (body.getNow() != null) {
                                    airResult[0] = body;
                                }
                                airOk[0] = true;
                                updateAirQuality.run();
                            } else {
                                fetchAirQualityByCoordinates(locationId, airResult, airOk, updateAirQuality);
                            }
                        } else {
                            fetchAirQualityByCoordinates(locationId, airResult, airOk, updateAirQuality);
                        }
                    }

                    @Override
                    public void onFailure(Call<AirQualityResponse> call, Throwable t) {
                        fetchAirQualityByCoordinates(locationId, airResult, airOk, updateAirQuality);
                    }
                });
    }

    /**
     * 通过经纬度获取空气质量（备用方案）
     * 优化：优先使用存储的经纬度，避免额外的API调用
     */
    private void fetchAirQualityByCoordinates(String locationId, final AirQualityResponse[] airResult,
                                              final boolean[] airOk, final Runnable updateAirQuality) {
        // 尝试从数据库获取存储的经纬度
        City city = cityDao.getCityByLocationId(locationId);
        if (city != null && city.getLatitude() != null && city.getLongitude() != null &&
                !city.getLatitude().isEmpty() && !city.getLongitude().isEmpty()) {
            // 直接使用存储的经纬度
            weatherService.getAirQualityByCoordinates(city.getLatitude(), city.getLongitude(),
                            Constants.QWEATHER_API_KEY, "zh")
                    .enqueue(new Callback<AirQualityResponse>() {
                        @Override
                        public void onResponse(Call<AirQualityResponse> call,
                                               Response<AirQualityResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                airResult[0] = response.body();
                            }
                            airOk[0] = true;
                            updateAirQuality.run();
                        }

                        @Override
                        public void onFailure(Call<AirQualityResponse> call, Throwable t) {
                            airOk[0] = true;
                            updateAirQuality.run();
                        }
                    });
            return;
        }

        // 如果没有存储的经纬度，则通过API获取（原有逻辑）
        WeatherService geoApi = ApiClient.getClient().create(WeatherService.class);
        geoApi.searchCity(locationId, Constants.QWEATHER_API_KEY, null, null, 1, "zh")
                .enqueue(new Callback<GeoCityResponse>() {
                    @Override
                    public void onResponse(Call<GeoCityResponse> call, Response<GeoCityResponse> response) {
                        if (response.isSuccessful() && response.body() != null &&
                                "200".equals(response.body().code) &&
                                response.body().location != null &&
                                !response.body().location.isEmpty()) {
                            GeoCityResponse.Location loc = response.body().location.get(0);
                            String latitude = loc.lat;
                            String longitude = loc.lon;

                            if (latitude != null && longitude != null &&
                                    !latitude.isEmpty() && !longitude.isEmpty()) {
                                weatherService.getAirQualityByCoordinates(latitude, longitude,
                                                Constants.QWEATHER_API_KEY, "zh")
                                        .enqueue(new Callback<AirQualityResponse>() {
                                            @Override
                                            public void onResponse(Call<AirQualityResponse> call,
                                                                   Response<AirQualityResponse> response) {
                                                if (response.isSuccessful() && response.body() != null) {
                                                    airResult[0] = response.body();
                                                }
                                                airOk[0] = true;
                                                updateAirQuality.run();
                                            }

                                            @Override
                                            public void onFailure(Call<AirQualityResponse> call, Throwable t) {
                                                airOk[0] = true;
                                                updateAirQuality.run();
                                            }
                                        });
                            } else {
                                airOk[0] = true;
                                updateAirQuality.run();
                            }
                        } else {
                            airOk[0] = true;
                            updateAirQuality.run();
                        }
                    }

                    @Override
                    public void onFailure(Call<GeoCityResponse> call, Throwable t) {
                        airOk[0] = true;
                        updateAirQuality.run();
                    }
                });
    }

    /**
     * 获取分钟级降水预报
     */
    private void fetchMinutelyPrecipitation(String locationId) {
        if (locationId == null || locationId.isEmpty()) {
            return;
        }

        // 尝试从数据库获取经纬度
        City city = cityDao.getCityByLocationId(locationId);
        String location = null;

        if (city != null && city.getLatitude() != null && city.getLongitude() != null &&
                !city.getLatitude().isEmpty() && !city.getLongitude().isEmpty()) {
            // 数据库中有经纬度，直接使用
            // 注意：和风天气API要求格式为"经度,纬度"（lon,lat），不是"纬度,经度"
            location = city.getLongitude() + "," + city.getLatitude();
            fetchMinutelyPrecipitationByLocation(location);
        } else {
            // 如果没有经纬度，尝试通过API获取
            // 注意：searchCity API的location参数可以是城市名称或locationId
            WeatherService geoApi = ApiClient.getClient().create(WeatherService.class);
            geoApi.searchCity(locationId, Constants.QWEATHER_API_KEY, null, null, 1, "zh")
                    .enqueue(new Callback<GeoCityResponse>() {
                        @Override
                        public void onResponse(Call<GeoCityResponse> call, Response<GeoCityResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                String code = response.body().code;
                                if ("200".equals(code) &&
                                        response.body().location != null &&
                                        !response.body().location.isEmpty()) {
                                    GeoCityResponse.Location loc = response.body().location.get(0);
                                    if (loc.lat != null && loc.lon != null &&
                                            !loc.lat.isEmpty() && !loc.lon.isEmpty()) {
                                        // 注意：和风天气API要求格式为"经度,纬度"（lon,lat），不是"纬度,经度"
                                        String locStr = loc.lon + "," + loc.lat;

                                        // 更新数据库中的经纬度信息（如果城市存在）
                                        City city = cityDao.getCityByLocationId(locationId);
                                        if (city != null) {
                                            city.latitude = loc.lat;
                                            city.longitude = loc.lon;
                                            cityDao.update(city);
                                        }

                                        // 调用分钟级降水API
                                        fetchMinutelyPrecipitationByLocation(locStr);
                                    } else {
                                        // API返回的数据中没有经纬度，不显示错误
                                    }
                                } else {
                                    // API返回错误码，不显示错误
                                }
                            } else {
                                // HTTP请求失败，不显示错误
                            }
                        }

                        @Override
                        public void onFailure(Call<GeoCityResponse> call, Throwable t) {
                            // 获取经纬度失败，不显示错误
                        }
                    });
        }
    }

    /**
     * 通过经纬度获取分钟级降水预报
     * 根据和风天气API文档：v7/minutely/5m
     * 参数：location（经纬度坐标 "lon,lat" 即"经度,纬度"）、key、lang
     */
    private void fetchMinutelyPrecipitationByLocation(String location) {
        // 验证location格式（应该是 "lon,lat" 即"经度,纬度"）
        if (location == null || location.isEmpty() || !location.contains(",")) {
            return;
        }

        weatherService.getMinutelyPrecipitation(location, Constants.QWEATHER_API_KEY, "zh")
                .enqueue(new Callback<MinutelyResponse>() {
                    @Override
                    public void onResponse(Call<MinutelyResponse> call, Response<MinutelyResponse> response) {
                        runOnUiThread(() -> {
                            if (response.isSuccessful() && response.body() != null) {
                                String code = response.body().getCode();
                                if ("200".equals(code)) {
                                    // API调用成功，更新UI
                                    updateMinutelyPrecipitation(response.body());
                                } else {
                                    // API返回错误码，创建错误响应
                                    MinutelyResponse errorResponse = new MinutelyResponse();
                                    errorResponse.setCode(code);
                                    updateMinutelyPrecipitation(errorResponse);
                                }
                            } else {
                                // HTTP请求失败，创建错误响应
                                MinutelyResponse errorResponse = new MinutelyResponse();
                                errorResponse.setCode(String.valueOf(response.code()));
                                updateMinutelyPrecipitation(errorResponse);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<MinutelyResponse> call, Throwable t) {
                        // API调用失败，不更新数据
                    }
                });
    }

    // ==================== 第四部分：其他代码（UI更新、业务逻辑、工具方法）(约250行) ====================

    /**
     * 更新天气UI
     */
    private void updateWeatherUI(WeatherResponse weather, AirQualityResponse airQuality) {
        String cityName = weather.getCityName();
        if (cityName != null && !cityName.isEmpty()) {
            tvCity.setText(cityName);
        }

        String temp = weather.getTemperature();
        tvTemperature.setText(temp != null && !temp.isEmpty() ? temp + "°C" : "--°C");

        String weatherText = weather.getWeather();
        tvWeather.setText(weatherText != null && !weatherText.isEmpty() ? weatherText : "--");

        if (weather.getNow() != null && weather.getNow().getIcon() != null) {
            WeatherIconUtils.setWeatherIcon(imageView, weather.getNow().getIcon());
        }

        String lowTemp = weather.getLowTemp();
        tvLowTemp.setText(lowTemp != null && !lowTemp.isEmpty() ? "最低" + lowTemp + "°C" : "最低--°C");

        String highTemp = weather.getHighTemp();
        tvHighTemp.setText(highTemp != null && !highTemp.isEmpty() ? "最高" + highTemp + "°C" : "最高--°C");

        Date currentDate = DateUtils.getCurrentDate();

        // 显示风力风向（根据文档：now.windDir 风向，now.windScale 风力等级，now.windSpeed 风速）
        if (weather.getNow() != null) {
            String windDir = weather.getNow().getWindDir();
            String windScale = weather.getNow().getWindScale();
            String windSpeed = weather.getNow().getWindSpeed();

            StringBuilder windInfo = new StringBuilder();
            if (windDir != null && !windDir.isEmpty()) {
                windInfo.append(windDir);
            }
            if (windScale != null && !windScale.isEmpty()) {
                if (windInfo.length() > 0) windInfo.append(" ");
                windInfo.append(windScale).append("级");
            }
            if (windSpeed != null && !windSpeed.isEmpty()) {
                if (windInfo.length() > 0) windInfo.append(" ");
                windInfo.append(windSpeed).append("km/h");
            }

            if (windInfo.length() > 0) {
                tvDayOfWeek.setText(windInfo.toString());
            } else {
                tvDayOfWeek.setText("--");
            }
        } else {
            tvDayOfWeek.setText("--");
        }

        // 显示当前湿度（根据文档：now.humidity 相对湿度，百分比数值）
        if (weather.getNow() != null && weather.getNow().getHumidity() != null && !weather.getNow().getHumidity().isEmpty()) {
            tvTempRange.setText("当前湿度: " + weather.getNow().getHumidity() + "%");
        } else {
            tvTempRange.setText("当前湿度: --");
        }

        if (weather.getDaily() != null && !weather.getDaily().isEmpty()) {
            updateDailyForecast(weather.getDaily(), currentDate);
        }
    }

    /**
     * 更新15天天气预报
     */
    private void updateDailyForecast(List<WeatherResponse.Daily> dailyList, Date currentDate) {
        if (dailyForecastAdapter != null && dailyList != null && !dailyList.isEmpty()) {
            // 显示所有可用的天数（最多15天）
            int daysToShow = Math.min(dailyList.size(), 15);
            List<WeatherResponse.Daily> limitedList = dailyList.subList(0, daysToShow);
            dailyForecastAdapter.updateData(limitedList, currentDate);
        }
    }

    /**
     * 更新分钟级降水预报UI
     * 根据和风天气API文档，返回数据包含：
     * - code: 状态码
     * - updateTime: 数据更新时间
     * - summary: 摘要信息
     * - minutely: 分钟级降水数据数组（未来2小时，每5分钟一个数据点）
     */
    private void updateMinutelyPrecipitation(MinutelyResponse response) {
        if (minutelyPrecipSection == null || minutelySummary == null || precipitationBarChart == null) {
            return;
        }

        // 验证数据
        if (response == null) {
            minutelyPrecipSection.setVisibility(View.GONE);
            return;
        }

        // 检查错误码
        String code = response.getCode();
        if (code != null && !"200".equals(code)) {
            String errorMsg = "暂无分钟级降水数据";
            if ("204".equals(code)) {
                errorMsg = "暂无分钟级降水数据\n（该地区暂不支持分钟级降水预报）";
            } else if ("400".equals(code)) {
                errorMsg = "暂无分钟级降水数据\n（该地区暂不支持或需要高级订阅）";
            } else if ("401".equals(code)) {
                errorMsg = "暂无分钟级降水数据\n（API密钥错误）";
            } else if ("404".equals(code)) {
                errorMsg = "暂无分钟级降水数据\n（数据不存在）";
            } else {
                errorMsg = "暂无分钟级降水数据\n（API错误码：" + code + "）";
            }
            minutelySummary.setText(errorMsg);
            precipitationBarChart.setPrecipitations(new ArrayList<>(), new ArrayList<>());
            minutelyPrecipSection.setVisibility(View.VISIBLE);
            return;
        }

        List<MinutelyResponse.Minutely> minutelyList = response.getMinutely();
        if (minutelyList == null || minutelyList.isEmpty()) {
            // 如果没有数据，显示提示信息
            minutelySummary.setText("暂无分钟级降水数据\n（该地区未来2小时无降水预报）");
            precipitationBarChart.setPrecipitations(new ArrayList<>(), new ArrayList<>());
            minutelyPrecipSection.setVisibility(View.VISIBLE);
            return;
        }

        // 显示摘要信息（API返回的summary字段）
        if (response.getSummary() != null && !response.getSummary().isEmpty()) {
            minutelySummary.setText(response.getSummary());
        } else {
            minutelySummary.setText("未来2小时降水预报（每1分钟一个数据点）");
        }

        // 准备柱状图数据（1分钟一个柱子，2小时=120个柱子）
        // API返回的是每5分钟一个数据点，我们需要将其扩展为每1分钟一个柱子
        List<Float> precipValues = new ArrayList<>();
        List<String> timeLabels = new ArrayList<>();
        int apiDataCount = Math.min(minutelyList.size(), 24); // API返回的数据点（每5分钟一个）

        // 将每5分钟的数据扩展为每1分钟的数据（每个数据点重复5次）
        for (int i = 0; i < apiDataCount; i++) {
            try {
                MinutelyResponse.Minutely minutely = minutelyList.get(i);
                float precipValue = 0;
                String timeStr = "";

                if (minutely != null) {
                    // 获取降雨量
                    try {
                        String precipStr = minutely.getPrecip();
                        if (precipStr != null && !precipStr.isEmpty()) {
                            precipValue = Float.parseFloat(precipStr);
                            // 确保降雨量不为负数
                            if (precipValue < 0) {
                                precipValue = 0;
                            }
                        }
                    } catch (NumberFormatException e) {
                        precipValue = 0;
                    }
                    String fxTime = minutely.getFxTime();
                    timeStr = fxTime != null ? DateUtils.formatMinutelyTime(fxTime) : "";
                }

                // 每个API数据点扩展为5个柱子（代表5分钟，每个柱子代表1分钟）
                for (int j = 0; j < 5; j++) {
                    precipValues.add(precipValue);
                    // 每10分钟（每10个柱子）显示一次时间标签
                    // 只在每个API数据点的第一个柱子（j==0）且是10的倍数时显示
                    int totalIndex = i * 5 + j;
                    if (totalIndex % 10 == 0 && j == 0 && !timeStr.isEmpty()) {
                        timeLabels.add(timeStr);
                    } else {
                        timeLabels.add("");
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                // 防止数组越界，填充默认值
                for (int j = 0; j < 5; j++) {
                    precipValues.add(0f);
                    timeLabels.add("");
                }
            }
        }

        // 确保总共120个柱子（2小时=120分钟）
        while (precipValues.size() < 120) {
            precipValues.add(0f);
            timeLabels.add("");
        }
        // 如果超过120个，截取前120个
        if (precipValues.size() > 120) {
            precipValues = precipValues.subList(0, 120);
            timeLabels = timeLabels.subList(0, 120);
        }

        // 更新柱状图
        precipitationBarChart.setPrecipitations(precipValues, timeLabels);
        minutelyPrecipSection.setVisibility(View.VISIBLE);
    }

    /**
     * 获取日出日落时间
     */
    private void fetchSunriseSunset(String cityName) {
        City city = cityDao.getCityByName(cityName);
        if (city == null || city.getLatitude() == null || city.getLongitude() == null ||
                city.getLatitude().isEmpty() || city.getLongitude().isEmpty()) {
            return;
        }

        // 获取今天的日期（格式：yyyyMMdd）
        String date = DateUtils.formatDateYYYYMMDD(DateUtils.getCurrentDate());

        // 注意：和风天气API要求格式为"经度,纬度"（lon,lat）
        String location = city.getLongitude() + "," + city.getLatitude();

        weatherService.getSunriseSunset(location, Constants.QWEATHER_API_KEY, date)
                .enqueue(new Callback<SunResponse>() {
                    @Override
                    public void onResponse(Call<SunResponse> call, Response<SunResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            SunResponse sunResponse = response.body();
                            if ("200".equals(sunResponse.getCode())) {
                                runOnUiThread(() -> updateSunriseSunsetUI(sunResponse));
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<SunResponse> call, Throwable t) {
                        // 失败时静默处理
                    }
                });
    }

    /**
     * 获取太阳辐射预报
     * 注意：此API返回的是太阳辐射数据（辐照度），不是紫外线指数
     * 如果需要紫外线指数，应使用天气指数API
     */
    private void fetchSolarRadiation(String cityName) {
        City city = cityDao.getCityByName(cityName);
        if (city == null || city.getLatitude() == null || city.getLongitude() == null ||
                city.getLatitude().isEmpty() || city.getLongitude().isEmpty()) {
            return;
        }

        // API使用路径参数：latitude和longitude
        // 查询参数：hours=1（获取1小时数据），interval=60（60分钟间隔，即下一个小时）
        weatherService.getSolarRadiation(city.getLatitude(), city.getLongitude(), 1, 60, Constants.QWEATHER_API_KEY)
                .enqueue(new Callback<SolarRadiationResponse>() {
                    @Override
                    public void onResponse(Call<SolarRadiationResponse> call, Response<SolarRadiationResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            SolarRadiationResponse solarResponse = response.body();
                            if (solarResponse.getForecasts() != null && !solarResponse.getForecasts().isEmpty()) {
                                // 获取下一个小时的数据（第一个数据点）
                                SolarRadiationResponse.Forecast forecast = solarResponse.getForecasts().get(0);
                                runOnUiThread(() -> updateSolarRadiationUI(forecast));
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<SolarRadiationResponse> call, Throwable t) {
                        // 失败时静默处理
                    }
                });
    }

    /**
     * 更新日出日落UI
     */
    private void updateSunriseSunsetUI(SunResponse response) {
        if (sunriseTime == null || sunsetTime == null) {
            return;
        }

        String sunrise = response.getSunrise();
        String sunset = response.getSunset();

        if (sunrise != null && !sunrise.isEmpty()) {
            sunriseTime.setText("日出: " + sunrise);
        } else {
            sunriseTime.setText("日出: --:--");
        }

        if (sunset != null && !sunset.isEmpty()) {
            sunsetTime.setText("日落: " + sunset);
        } else {
            sunsetTime.setText("日落: --:--");
        }
    }

    /**
     * 更新太阳辐射UI
     * 显示API返回的太阳辐射数据（总水平面辐照度GHI）
     */
    private void updateSolarRadiationUI(SolarRadiationResponse.Forecast forecast) {
        if (uvIndexValue == null || uvIndexDesc == null || uvIndexTime == null) {
            return;
        }

        // 显示总水平面辐照度（GHI）
        if (forecast.getGhi() != null && forecast.getGhi().getValue() != null) {
            String ghiValue = String.format("%.1f", forecast.getGhi().getValue());
            String unit = forecast.getGhi().getUnit() != null ? forecast.getGhi().getUnit() : "W/m²";
            uvIndexValue.setText(ghiValue + " " + unit);
        } else {
            uvIndexValue.setText("--");
        }

        // 显示太阳高度角
        if (forecast.getSolarAngle() != null && forecast.getSolarAngle().getElevation() != null) {
            String elevation = String.format("%.1f°", forecast.getSolarAngle().getElevation());
            uvIndexDesc.setText("高度角: " + elevation);
        } else {
            uvIndexDesc.setText("--");
        }

        // 显示预报时间
        if (forecast.getForecastTime() != null && !forecast.getForecastTime().isEmpty()) {
            // 格式化时间显示（从ISO8601格式提取时间部分）
            String timeStr = forecast.getForecastTime();
            if (timeStr.contains("T")) {
                timeStr = timeStr.split("T")[1].replace("Z", "");
                if (timeStr.length() >= 5) {
                    timeStr = timeStr.substring(0, 5); // 只显示HH:mm
                }
            }
            uvIndexTime.setText(timeStr);
        } else {
            uvIndexTime.setText("--");
        }
    }

    /**
     * 停止下拉刷新动画
     */
    private void stopRefreshAnimation() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * 更新空气质量UI
     */
    private void updateAirQualityUI(AirQualityResponse airResult) {
        if (airResult == null) {
            tvAirQuality.setText("--");
            airQualityHealthEffect = "暂无数据";
            return;
        }

        if (airResult.getIndexes() != null && !airResult.getIndexes().isEmpty()) {
            AirQualityResponse.Index index = airResult.getFirstIndex();
            if (index != null) {
                String aqiDisplay = index.getAqiDisplay();
                String category = index.getCategory();
                if (aqiDisplay != null && !aqiDisplay.isEmpty()) {
                    tvAirQuality.setText("AQI: " + aqiDisplay + (category != null && !category.isEmpty() ? " (" + category + ")" : ""));
                } else if (category != null && !category.isEmpty()) {
                    tvAirQuality.setText(category);
                } else {
                    tvAirQuality.setText("--");
                }

                if (index.getHealth() != null && index.getHealth().getEffect() != null &&
                        !index.getHealth().getEffect().isEmpty()) {
                    airQualityHealthEffect = index.getHealth().getEffect();
                } else {
                    airQualityHealthEffect = "暂无数据";
                }
            } else {
                tvAirQuality.setText("--");
                airQualityHealthEffect = "暂无数据";
            }
        } else if (airResult.getNow() != null) {
            AirQualityResponse.AirNow airNow = airResult.getNow();
            String aqi = airNow.getAqi();
            String category = airNow.getCategory();
            if (aqi != null && !aqi.isEmpty()) {
                tvAirQuality.setText("AQI: " + aqi + (category != null ? " (" + category + ")" : ""));
            } else if (category != null) {
                tvAirQuality.setText(category);
            } else {
                tvAirQuality.setText("--");
            }

            String level = airNow.getLevel();
            if (level != null && !level.isEmpty()) {
                airQualityHealthEffect = "空气质量等级: " + level;
            } else if (category != null) {
                airQualityHealthEffect = "空气质量类别: " + category;
            } else {
                airQualityHealthEffect = "暂无数据";
            }
        } else {
            tvAirQuality.setText("--");
            airQualityHealthEffect = "暂无数据";
        }
    }

    /**
     * 显示空气质量对人体影响的对话框
     */
    private void showAirQualityDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("空气质量对人体影响");
        String message = airQualityHealthEffect != null && !airQualityHealthEffect.isEmpty()
                ? airQualityHealthEffect : "暂无数据";
        builder.setMessage(message);
        builder.setPositiveButton("确定", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }


    // ==================== 第五部分：布局相关代码 (约180行) ====================

    /**
     * 绑定所有视图控件
     */
    private void bindViews() {
        tvLowTemp = findViewById(R.id.a);
        tvTemperature = findViewById(R.id.b);
        tvHighTemp = findViewById(R.id.c);
        tvCity = findViewById(R.id.d);
        tvDayOfWeek = findViewById(R.id.e);
        tvWeather = findViewById(R.id.f);
        imageView = findViewById(R.id.imageView);
        tvAirQuality = findViewById(R.id.g);
        btnAirQuality = findViewById(R.id.h);
        tvTempRange = findViewById(R.id.i);
        btnAirQuality.setOnClickListener(v -> showAirQualityDialog());

        hourlyForecastContainer = findViewById(R.id.hourly_forecast_container);
        temperatureLineChart = findViewById(R.id.temperature_line_chart);
        temperatureChartScroll = findViewById(R.id.temperature_chart_scroll);
        hourlyIconsScroll = findViewById(R.id.hourly_icons_scroll);

        // 同步折线图和图标容器的滑动
        setupSynchronizedScrolling();

        // 初始化15天天气预报RecyclerView
        dailyForecastRecycler = findViewById(R.id.daily_forecast_recycler);
        dailyForecastRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        dailyForecastAdapter = new DailyForecastAdapter(this);
        dailyForecastRecycler.setAdapter(dailyForecastAdapter);

        // 初始化分钟级降水预报视图
        minutelyPrecipSection = findViewById(R.id.minutely_precip_section);
        minutelySummary = findViewById(R.id.minutely_summary);
        precipitationBarChart = findViewById(R.id.precipitation_bar_chart);

        // 初始化紫外线强度和日出日落视图
        uvIndexValue = findViewById(R.id.uv_index_value);
        uvIndexDesc = findViewById(R.id.uv_index_desc);
        uvIndexTime = findViewById(R.id.uv_index_time);
        sunriseTime = findViewById(R.id.sunrise_time);
        sunsetTime = findViewById(R.id.sunset_time);

        // 初始化下拉刷新
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (currentLocationId != null && currentCityName != null) {
                fetchWeatherData(currentLocationId, currentCityName);
            } else {
                stopRefreshAnimation();
            }
        });
    }

    /**
     * 更新24小时预报显示
     */
    private void updateHourlyForecast(List<WeatherResponse.Hourly> hourlyList) {
        if (hourlyList == null || hourlyList.isEmpty()) {
            return;
        }

        // 更新折线图（包含温度和时间标签）
        if (temperatureLineChart != null) {
            List<Integer> temperatures = new ArrayList<>();
            List<String> timeLabels = new ArrayList<>();
            int count = Math.min(12, hourlyList.size());
            for (int i = 0; i < count; i++) {
                try {
                    WeatherResponse.Hourly hourly = hourlyList.get(i);
                    if (hourly != null) {
                        // 添加温度
                        if (hourly.getTemp() != null) {
                            try {
                                temperatures.add(Integer.parseInt(hourly.getTemp()));
                            } catch (NumberFormatException e) {
                                temperatures.add(0);
                            }
                        } else {
                            temperatures.add(0);
                        }
                        // 添加时间标签
                        String fxTime = hourly.getFxTime();
                        timeLabels.add(fxTime != null ? DateUtils.formatHourlyTime(fxTime) : "--");
                    } else {
                        temperatures.add(0);
                        timeLabels.add("--");
                    }
                } catch (IndexOutOfBoundsException e) {
                    // 防止数组越界
                    temperatures.add(0);
                    timeLabels.add("--");
                }
            }
            temperatureLineChart.setTemperatures(temperatures, timeLabels);
        }

        // 更新天气图标和文字（宽度与折线图一致）
        if (hourlyForecastContainer != null) {
            hourlyForecastContainer.removeAllViews();
            int count = Math.min(12, hourlyList.size());
            for (int i = 0; i < count; i++) {
                try {
                    WeatherResponse.Hourly hourly = hourlyList.get(i);
                    if (hourly != null) {
                        hourlyForecastContainer.addView(createHourlyItemView(hourly));
                    }
                } catch (IndexOutOfBoundsException e) {
                    // 防止数组越界，跳过该项
                } catch (Exception e) {
                    // 其他异常，跳过该项
                }
            }
        }
    }

    /**
     * 创建单个小时预报项视图（仅显示天气图标和时间，不显示箭头）
     * 宽度与折线图一致（100dp）
     */
    private View createHourlyItemView(WeatherResponse.Hourly hourly) {
        if (hourly == null) {
            // 返回一个空的占位视图
            LinearLayout emptyLayout = new LinearLayout(this);
            emptyLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    dpToPx(100), LinearLayout.LayoutParams.WRAP_CONTENT));
            return emptyLayout;
        }

        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.VERTICAL);
        itemLayout.setGravity(android.view.Gravity.CENTER);
        itemLayout.setPadding(16, 8, 16, 8);
        itemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                dpToPx(100), LinearLayout.LayoutParams.WRAP_CONTENT)); // 与折线图宽度一致

        ImageView iconView = new ImageView(this);
        iconView.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(32), dpToPx(32)));
        iconView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        String icon = hourly.getIcon();
        if (icon != null) {
            WeatherIconUtils.setWeatherIcon(iconView, icon);
        }
        itemLayout.addView(iconView);

        TextView timeText = new TextView(this);
        String fxTime = hourly.getFxTime();
        timeText.setText(fxTime != null ? DateUtils.formatHourlyTime(fxTime) : "--");
        try {
            timeText.setTextColor(getResources().getColor(android.R.color.black));
        } catch (Exception e) {
            // 如果资源获取失败，使用默认颜色
            timeText.setTextColor(0xFF000000);
        }
        timeText.setTextSize(12);
        timeText.setGravity(android.view.Gravity.CENTER);
        timeText.setPadding(0, dpToPx(4), 0, 0);
        itemLayout.addView(timeText);

        return itemLayout;
    }

    /**
     * 将dp转换为px
     */
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
