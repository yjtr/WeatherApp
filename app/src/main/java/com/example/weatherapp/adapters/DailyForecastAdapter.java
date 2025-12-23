package com.example.weatherapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.R;
import com.example.weatherapp.models.WeatherResponse;
import com.example.weatherapp.utils.DateUtils;
import com.example.weatherapp.utils.WeatherIconUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 每日天气预报适配器
 * 显示15天天气预报列表
 */
public class DailyForecastAdapter extends RecyclerView.Adapter<DailyForecastAdapter.DailyForecastViewHolder> {

    private final Context context;
    private List<WeatherResponse.Daily> dailyList;
    private Date startDate;

    public DailyForecastAdapter(Context context) {
        this.context = context;
    }

    /**
     * 更新数据
     */
    public void updateData(List<WeatherResponse.Daily> dailyList, Date startDate) {
        this.dailyList = dailyList;
        this.startDate = startDate;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DailyForecastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_daily_forecast, parent, false);
        return new DailyForecastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DailyForecastViewHolder holder, int position) {
        if (dailyList == null || position >= dailyList.size()) {
            return;
        }

        WeatherResponse.Daily daily = dailyList.get(position);
        if (daily == null) {
            return;
        }

        // 优先使用API返回的fxDate字段，如果没有则手动计算
        String dateText = null;
        if (daily.getFxDate() != null && !daily.getFxDate().isEmpty()) {
            // 尝试解析fxDate字段（格式可能是 "2025-12-22" 或 "12/22"）
            String fxDate = daily.getFxDate();
            if (fxDate.contains("/")) {
                // 如果已经是 MM/dd 格式，直接使用
                dateText = fxDate;
            } else if (fxDate.contains("-")) {
                // 如果是 "2025-12-22" 格式，提取 MM/dd
                try {
                    String[] parts = fxDate.split("-");
                    if (parts.length >= 2) {
                        dateText = parts[1] + "/" + parts[2];
                    }
                } catch (Exception e) {
                    // 解析失败，使用手动计算
                }
            }
        }

        // 如果API没有提供日期或解析失败，则手动计算
        if (dateText == null || dateText.isEmpty()) {
            Calendar calendar = Calendar.getInstance();
            if (startDate != null) {
                calendar.setTime(startDate);
            } else {
                calendar.setTime(new Date());
            }
            calendar.add(Calendar.DAY_OF_MONTH, position);
            Date date = calendar.getTime();
            dateText = DateUtils.formatDateMMdd(date);
        }

        holder.tvDate.setText(dateText);

        // 设置天气描述
        String weatherText = daily.getTextDay();
        holder.tvWeather.setText(weatherText != null && !weatherText.isEmpty() ? weatherText : "--");

        // 设置温度范围
        String tempMax = daily.getTempMax();
        String tempMin = daily.getTempMin();
        if (tempMax != null && tempMin != null && !tempMax.isEmpty() && !tempMin.isEmpty()) {
            holder.tvTemp.setText("高: " + tempMax + "°C / 低: " + tempMin + "°C");
        } else {
            holder.tvTemp.setText("--");
        }

        // 设置天气图标
        if (daily.getIconDay() != null) {
            WeatherIconUtils.setWeatherIcon(holder.icon, daily.getIconDay());
        }
    }

    @Override
    public int getItemCount() {
        return dailyList == null ? 0 : dailyList.size();
    }

    static class DailyForecastViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView tvDate;
        TextView tvWeather;
        TextView tvTemp;

        DailyForecastViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon_daily);
            tvDate = itemView.findViewById(R.id.tv_date_daily);
            tvWeather = itemView.findViewById(R.id.tv_weather_daily);
            tvTemp = itemView.findViewById(R.id.tv_temp_daily);
        }
    }
}

