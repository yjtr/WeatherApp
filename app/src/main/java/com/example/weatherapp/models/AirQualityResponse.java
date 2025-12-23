package com.example.weatherapp.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AirQualityResponse {
    @SerializedName("code")
    private String code;

    @SerializedName("metadata")
    private Metadata metadata;

    @SerializedName("indexes")
    private List<Index> indexes;

    // 旧API格式支持（v7/air/now）
    @SerializedName("now")
    private AirNow now;

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public List<Index> getIndexes() {
        return indexes;
    }

    public void setIndexes(List<Index> indexes) {
        this.indexes = indexes;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public AirNow getNow() {
        return now;
    }

    public void setNow(AirNow now) {
        this.now = now;
    }

    // 获取第一个空气质量指数（通常使用QAQI或US-EPA）
    public Index getFirstIndex() {
        if (indexes != null && !indexes.isEmpty()) {
            // 优先返回QAQI，如果没有则返回第一个
            for (Index index : indexes) {
                if ("qaqi".equals(index.getCode())) {
                    return index;
                }
            }
            return indexes.get(0);
        }
        return null;
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

    public static class Index {
        @SerializedName("code")
        private String code;

        @SerializedName("name")
        private String name;

        @SerializedName("aqi")
        private Integer aqi;

        @SerializedName("aqiDisplay")
        private String aqiDisplay;

        @SerializedName("level")
        private String level;

        @SerializedName("category")
        private String category;

        @SerializedName("health")
        private Health health;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAqi() {
            return aqi;
        }

        public void setAqi(Integer aqi) {
            this.aqi = aqi;
        }

        public String getAqiDisplay() {
            return aqiDisplay;
        }

        public void setAqiDisplay(String aqiDisplay) {
            this.aqiDisplay = aqiDisplay;
        }

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public Health getHealth() {
            return health;
        }

        public void setHealth(Health health) {
            this.health = health;
        }
    }

    public static class Health {
        @SerializedName("effect")
        private String effect;

        @SerializedName("advice")
        private Advice advice;

        public String getEffect() {
            return effect;
        }

        public void setEffect(String effect) {
            this.effect = effect;
        }

        public Advice getAdvice() {
            return advice;
        }

        public void setAdvice(Advice advice) {
            this.advice = advice;
        }
    }

    public static class Advice {
        @SerializedName("generalPopulation")
        private String generalPopulation;

        @SerializedName("sensitivePopulation")
        private String sensitivePopulation;

        public String getGeneralPopulation() {
            return generalPopulation;
        }

        public void setGeneralPopulation(String generalPopulation) {
            this.generalPopulation = generalPopulation;
        }

        public String getSensitivePopulation() {
            return sensitivePopulation;
        }

        public void setSensitivePopulation(String sensitivePopulation) {
            this.sensitivePopulation = sensitivePopulation;
        }
    }

    // 旧API格式支持（v7/air/now）
    public static class AirNow {
        @SerializedName("aqi")
        private String aqi;

        @SerializedName("level")
        private String level;

        @SerializedName("category")
        private String category;

        @SerializedName("primary")
        private String primary;

        public String getAqi() {
            return aqi;
        }

        public void setAqi(String aqi) {
            this.aqi = aqi;
        }

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getPrimary() {
            return primary;
        }

        public void setPrimary(String primary) {
            this.primary = primary;
        }
    }
}
