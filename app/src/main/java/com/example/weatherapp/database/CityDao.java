package com.example.weatherapp.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.weatherapp.models.City;

import java.util.List;

/**
 * 城市数据访问对象
 * 提供城市数据的增删查改操作
 */
@Dao
public interface CityDao {

    @Insert
    void insert(City city);

    @Update
    void update(City city);

    @Query("SELECT * FROM city")
    List<City> getAllCities();

    @Query("SELECT * FROM city WHERE name = :cityName LIMIT 1")
    City getCityByName(String cityName);

    @Query("DELETE FROM city WHERE name = :cityName")
    void deleteCity(String cityName);

    @Query("SELECT COUNT(*) FROM city WHERE locationId = :locationId")
    int countByLocationId(String locationId);

    @Query("SELECT * FROM city WHERE locationId = :locationId LIMIT 1")
    City getCityByLocationId(String locationId);
}
