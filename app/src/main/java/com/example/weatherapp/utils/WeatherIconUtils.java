package com.example.weatherapp.utils;

import android.content.Context;
import android.widget.ImageView;

/**
 * 天气图标工具类
 * 根据和风天气API返回的图标代码，设置对应的图标资源
 */
public class WeatherIconUtils {

    /**
     * 为ImageView设置天气图标
     */
    public static void setWeatherIcon(ImageView imageView, String iconCode) {
        if (imageView != null && imageView.getContext() != null) {
            int resourceId = getIconResourceId(imageView.getContext(), iconCode);
            imageView.setImageResource(resourceId);
        }
    }

    /**
     * 根据图标代码获取drawable资源ID
     */
    private static int getIconResourceId(Context context, String iconCode) {
        if (iconCode == null || iconCode.isEmpty()) {
            return getDefaultIcon(context);
        }

        try {
            int code = Integer.parseInt(iconCode);
            return getIconResourceId(context, code);
        } catch (NumberFormatException e) {
            return getDefaultIcon(context);
        }
    }

    /**
     * 根据图标代码获取drawable资源ID
     */
    private static int getIconResourceId(Context context, int iconCode) {
        String resourceName = "ic_weather_" + iconCode;
        int resourceId = context.getResources().getIdentifier(
                resourceName, "drawable", context.getPackageName());

        if (resourceId != 0) {
            return resourceId;
        }

        return getFallbackIcon(context, iconCode);
    }

    /**
     * 获取备用图标（当找不到精确匹配时）
     */
    private static int getFallbackIcon(Context context, int iconCode) {
        if (iconCode >= 100 && iconCode < 200) {
            return getResourceId(context, "ic_weather_100", getDefaultIcon(context));
        } else if (iconCode >= 300 && iconCode < 400) {
            return getResourceId(context, "ic_weather_305", getDefaultIcon(context));
        } else if (iconCode >= 400 && iconCode < 500) {
            return getResourceId(context, "ic_weather_400", getDefaultIcon(context));
        } else if (iconCode >= 500 && iconCode < 600) {
            return getResourceId(context, "ic_weather_501", getDefaultIcon(context));
        }
        return getDefaultIcon(context);
    }

    /**
     * 获取资源ID（如果资源不存在则返回默认值）
     */
    private static int getResourceId(Context context, String resourceName, int defaultId) {
        int resourceId = context.getResources().getIdentifier(
                resourceName, "drawable", context.getPackageName());
        return resourceId != 0 ? resourceId : defaultId;
    }

    /**
     * 获取默认图标
     */
    private static int getDefaultIcon(Context context) {
        int sunnyIconId = context.getResources().getIdentifier(
                "sunny_icon", "drawable", context.getPackageName());
        if (sunnyIconId != 0) {
            return sunnyIconId;
        }
        return android.R.drawable.ic_menu_view;
    }
}
