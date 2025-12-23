package com.example.weatherapp.network;

import com.example.weatherapp.models.AirQualityResponse;
import com.example.weatherapp.models.GeoCityResponse;
import com.example.weatherapp.models.MinutelyResponse;
import com.example.weatherapp.models.SolarRadiationResponse;
import com.example.weatherapp.models.SunResponse;
import com.example.weatherapp.models.WeatherResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * 和风天气API服务接口
 * 定义所有API请求方法
 */
public interface WeatherService {

    @GET("geo/v2/city/lookup")
    Call<GeoCityResponse> searchCity(
            @Query("location") String location,
            @Query("key") String key,
            @Query("adm") String adm,
            @Query("range") String range,
            @Query("number") Integer number,
            @Query("lang") String lang
    );

    @GET("v7/weather/now")
    Call<WeatherResponse> getWeatherNow(
            @Query("location") String locationId,
            @Query("key") String key
    );

    @GET("v7/weather/3d")
    Call<WeatherResponse> getWeather3d(
            @Query("location") String locationId,
            @Query("key") String key
    );

    @GET("v7/weather/7d")
    Call<WeatherResponse> getWeather7d(
            @Query("location") String locationId,
            @Query("key") String key
    );

    @GET("v7/weather/15d")
    Call<WeatherResponse> getWeather15d(
            @Query("location") String locationId,
            @Query("key") String key
    );

    @GET("airquality/v1/current/{latitude}/{longitude}")
    Call<AirQualityResponse> getAirQualityByCoordinates(
            @Path("latitude") String latitude,
            @Path("longitude") String longitude,
            @Query("key") String key,
            @Query("lang") String lang
    );

    @GET("v7/air/now")
    Call<AirQualityResponse> getAirQuality(
            @Query("location") String locationId,
            @Query("key") String key
    );

    @GET("v7/weather/24h")
    Call<WeatherResponse> getWeather24h(
            @Query("location") String locationId,
            @Query("key") String key
    );

    /**
     * 格点每日天气预报（基于坐标，精度3-5公里）
     * location格式: "lat,lon" 例如: "39.9042,116.4074"
     */
    @GET("v7/grid-weather/15d")
    Call<WeatherResponse> getGridWeatherDaily(
            @Query("location") String location,
            @Query("key") String key
    );

    /**
     * 格点逐小时天气预报（基于坐标，精度3-5公里）
     * location格式: "lat,lon" 例如: "39.9042,116.4074"
     */
    @GET("v7/grid-weather/72h")
    Call<WeatherResponse> getGridWeatherHourly(
            @Query("location") String location,
            @Query("key") String key
    );

    /**
     * 分钟级降水预报（未来2小时，基于坐标）
     * location格式: "lat,lon" 例如: "39.9042,116.4074"
     */
    @GET("v7/minutely/5m")
    Call<MinutelyResponse> getMinutelyPrecipitation(
            @Query("location") String location,
            @Query("key") String key,
            @Query("lang") String lang
    );

    /**
     * 日出日落时间（基于坐标）
     * location格式: "lat,lon" 例如: "39.9042,116.4074"
     */
    @GET("v7/astronomy/sun")
    Call<SunResponse> getSunriseSunset(
            @Query("location") String location,
            @Query("key") String key,
            @Query("date") String date
    );

    /**
     * 太阳辐射预报（基于坐标）
     * 路径参数：latitude, longitude
     * 查询参数：hours（可选1-60，默认24），interval（可选15/30/60分钟，默认60）
     */
    @GET("solarradiation/v1/forecast/{latitude}/{longitude}")
    Call<SolarRadiationResponse> getSolarRadiation(
            @Path("latitude") String latitude,
            @Path("longitude") String longitude,
            @Query("hours") Integer hours,
            @Query("interval") Integer interval,
            @Query("key") String key
    );
}
