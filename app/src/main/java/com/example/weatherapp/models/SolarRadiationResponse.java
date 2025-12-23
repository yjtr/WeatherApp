package com.example.weatherapp.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * 太阳辐射预报API响应
 * 根据文档：https://dev.qweather.com/docs/api/solar-radiation/solar-radiation-forecast/
 */
public class SolarRadiationResponse {
    @SerializedName("metadata")
    private Metadata metadata;

    @SerializedName("forecasts")
    private List<Forecast> forecasts;

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public List<Forecast> getForecasts() {
        return forecasts;
    }

    public void setForecasts(List<Forecast> forecasts) {
        this.forecasts = forecasts;
    }

    public static class Metadata {
        @SerializedName("tag")
        private String tag;

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }
    }

    public static class Forecast {
        @SerializedName("forecastTime")
        private String forecastTime;

        @SerializedName("solarAngle")
        private SolarAngle solarAngle;

        @SerializedName("dni")
        private RadiationValue dni;

        @SerializedName("dhi")
        private RadiationValue dhi;

        @SerializedName("ghi")
        private RadiationValue ghi;

        @SerializedName("weather")
        private Weather weather;

        public String getForecastTime() {
            return forecastTime;
        }

        public void setForecastTime(String forecastTime) {
            this.forecastTime = forecastTime;
        }

        public SolarAngle getSolarAngle() {
            return solarAngle;
        }

        public void setSolarAngle(SolarAngle solarAngle) {
            this.solarAngle = solarAngle;
        }

        public RadiationValue getDni() {
            return dni;
        }

        public void setDni(RadiationValue dni) {
            this.dni = dni;
        }

        public RadiationValue getDhi() {
            return dhi;
        }

        public void setDhi(RadiationValue dhi) {
            this.dhi = dhi;
        }

        public RadiationValue getGhi() {
            return ghi;
        }

        public void setGhi(RadiationValue ghi) {
            this.ghi = ghi;
        }

        public Weather getWeather() {
            return weather;
        }

        public void setWeather(Weather weather) {
            this.weather = weather;
        }
    }

    public static class SolarAngle {
        @SerializedName("azimuth")
        private Double azimuth;

        @SerializedName("elevation")
        private Double elevation;

        public Double getAzimuth() {
            return azimuth;
        }

        public void setAzimuth(Double azimuth) {
            this.azimuth = azimuth;
        }

        public Double getElevation() {
            return elevation;
        }

        public void setElevation(Double elevation) {
            this.elevation = elevation;
        }
    }

    public static class RadiationValue {
        @SerializedName("value")
        private Double value;

        @SerializedName("unit")
        private String unit;

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }
    }

    public static class Weather {
        @SerializedName("temperature")
        private TemperatureValue temperature;

        @SerializedName("windSpeed")
        private WindSpeedValue windSpeed;

        @SerializedName("humidity")
        private Integer humidity;

        public TemperatureValue getTemperature() {
            return temperature;
        }

        public void setTemperature(TemperatureValue temperature) {
            this.temperature = temperature;
        }

        public WindSpeedValue getWindSpeed() {
            return windSpeed;
        }

        public void setWindSpeed(WindSpeedValue windSpeed) {
            this.windSpeed = windSpeed;
        }

        public Integer getHumidity() {
            return humidity;
        }

        public void setHumidity(Integer humidity) {
            this.humidity = humidity;
        }
    }

    public static class TemperatureValue {
        @SerializedName("value")
        private Double value;

        @SerializedName("unit")
        private String unit;

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }
    }

    public static class WindSpeedValue {
        @SerializedName("value")
        private Double value;

        @SerializedName("unit")
        private String unit;

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }
    }
}

