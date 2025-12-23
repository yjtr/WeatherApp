package com.example.weatherapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefsUtils {

    private static final String PREF_NAME = "weather_prefs";
    private static final String KEY_DEFAULT_CITY = "default_city";

    public static void saveDefaultCity(Context context, String cityName) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_DEFAULT_CITY, cityName).apply();
    }

    public static String loadDefaultCity(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sp.getString(KEY_DEFAULT_CITY, null);
    }
}
