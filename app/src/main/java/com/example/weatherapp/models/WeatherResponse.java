package com.example.weatherapp.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WeatherResponse {
    @SerializedName("code")
    private String code;

    @SerializedName("now")
    private Now now;

    @SerializedName("daily")
    private List<Daily> daily;

    @SerializedName("hourly")
    private List<Hourly> hourly;

    // 用于存储城市名称（从请求中获取）
    private transient String cityName;

    // Getters and Setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Now getNow() {
        return now;
    }

    public void setNow(Now now) {
        this.now = now;
    }

    public List<Daily> getDaily() {
        return daily;
    }

    public void setDaily(List<Daily> daily) {
        this.daily = daily;
    }

    public List<Hourly> getHourly() {
        return hourly;
    }

    public void setHourly(List<Hourly> hourly) {
        this.hourly = hourly;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    // 便捷方法，用于兼容原有代码
    public String getCity() {
        return cityName != null ? cityName : "";
    }

    public String getTemperature() {
        return now != null ? now.getTemp() : "";
    }

    public String getWeather() {
        return now != null ? now.getText() : "";
    }

    public String getLowTemp() {
        if (daily != null && !daily.isEmpty() && daily.get(0) != null) {
            return daily.get(0).getTempMin();
        }
        return "";
    }

    public String getHighTemp() {
        if (daily != null && !daily.isEmpty() && daily.get(0) != null) {
            return daily.get(0).getTempMax();
        }
        return "";
    }

    // 内部类：实时天气数据
    public static class Now {
        @SerializedName("temp")
        private String temp;

        @SerializedName("text")
        private String text;

        @SerializedName("feelsLike")
        private String feelsLike;

        @SerializedName("icon")
        private String icon;

        @SerializedName("humidity")
        private String humidity;

        @SerializedName("windDir")
        private String windDir;

        @SerializedName("windScale")
        private String windScale;

        @SerializedName("windSpeed")
        private String windSpeed;

        public String getTemp() {
            return temp;
        }

        public void setTemp(String temp) {
            this.temp = temp;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getFeelsLike() {
            return feelsLike;
        }

        public void setFeelsLike(String feelsLike) {
            this.feelsLike = feelsLike;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public String getHumidity() {
            return humidity;
        }

        public void setHumidity(String humidity) {
            this.humidity = humidity;
        }

        public String getWindDir() {
            return windDir;
        }

        public void setWindDir(String windDir) {
            this.windDir = windDir;
        }

        public String getWindScale() {
            return windScale;
        }

        public void setWindScale(String windScale) {
            this.windScale = windScale;
        }

        public String getWindSpeed() {
            return windSpeed;
        }

        public void setWindSpeed(String windSpeed) {
            this.windSpeed = windSpeed;
        }
    }

    // 内部类：每日天气数据
    public static class Daily {
        @SerializedName("tempMax")
        private String tempMax;

        @SerializedName("tempMin")
        private String tempMin;

        @SerializedName("textDay")
        private String textDay;

        @SerializedName("iconDay")
        private String iconDay;

        @SerializedName("fxDate")
        private String fxDate;

        public String getTempMax() {
            return tempMax;
        }

        public void setTempMax(String tempMax) {
            this.tempMax = tempMax;
        }

        public String getTempMin() {
            return tempMin;
        }

        public void setTempMin(String tempMin) {
            this.tempMin = tempMin;
        }

        public String getTextDay() {
            return textDay;
        }

        public void setTextDay(String textDay) {
            this.textDay = textDay;
        }

        public String getIconDay() {
            return iconDay;
        }

        public void setIconDay(String iconDay) {
            this.iconDay = iconDay;
        }

        public String getFxDate() {
            return fxDate;
        }

        public void setFxDate(String fxDate) {
            this.fxDate = fxDate;
        }
    }

    // 内部类：24小时预报数据
    public static class Hourly {
        @SerializedName("fxTime")
        private String fxTime;

        @SerializedName("temp")
        private String temp;

        @SerializedName("text")
        private String text;

        @SerializedName("icon")
        private String icon;

        @SerializedName("windDir")
        private String windDir;

        @SerializedName("windScale")
        private String windScale;

        public String getFxTime() {
            return fxTime;
        }

        public void setFxTime(String fxTime) {
            this.fxTime = fxTime;
        }

        public String getTemp() {
            return temp;
        }

        public void setTemp(String temp) {
            this.temp = temp;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public String getWindDir() {
            return windDir;
        }

        public void setWindDir(String windDir) {
            this.windDir = windDir;
        }

        public String getWindScale() {
            return windScale;
        }

        public void setWindScale(String windScale) {
            this.windScale = windScale;
        }
    }
}
