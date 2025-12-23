package com.example.weatherapp.models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "city")
public class City {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;        // 显示用
    public String locationId;  // 和风 API 用
    public String latitude;    // 纬度（用于格点API和空气质量API）
    public String longitude;   // 经度（用于格点API和空气质量API）

    /**
     * Room使用的构造函数（无参构造函数）
     */
    public City() {
    }

    /**
     * 便捷构造函数（不包含经纬度）
     * 使用@Ignore注解，Room不会使用这个构造函数
     */
    @Ignore
    public City(String name, String locationId) {
        this.name = name;
        this.locationId = locationId;
    }

    /**
     * 便捷构造函数（包含经纬度）
     * 使用@Ignore注解，Room不会使用这个构造函数
     */
    @Ignore
    public City(String name, String locationId, String latitude, String longitude) {
        this.name = name;
        this.locationId = locationId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // ======== Getter（关键）========
    public String getName() {
        return name;
    }

    public String getLocationId() {
        return locationId;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }
}
