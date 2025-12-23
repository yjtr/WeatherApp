package com.example.weatherapp.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * 分钟级降水预报响应模型
 */
public class MinutelyResponse {
    @SerializedName("code")
    private String code;

    @SerializedName("updateTime")
    private String updateTime;

    @SerializedName("summary")
    private String summary;

    @SerializedName("minutely")
    private List<Minutely> minutely;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<Minutely> getMinutely() {
        return minutely;
    }

    public void setMinutely(List<Minutely> minutely) {
        this.minutely = minutely;
    }

    /**
     * 分钟级降水数据
     */
    public static class Minutely {
        @SerializedName("fxTime")
        private String fxTime;

        @SerializedName("precip")
        private String precip;

        @SerializedName("type")
        private String type;

        public String getFxTime() {
            return fxTime;
        }

        public void setFxTime(String fxTime) {
            this.fxTime = fxTime;
        }

        public String getPrecip() {
            return precip;
        }

        public void setPrecip(String precip) {
            this.precip = precip;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}

