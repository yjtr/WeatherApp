package com.example.weatherapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.MainActivity;
import com.example.weatherapp.database.AppDatabase;
import com.example.weatherapp.database.CityDao;
import com.example.weatherapp.models.City;
import com.example.weatherapp.models.GeoCityResponse;

import java.util.List;

/**
 * 统一的城市列表适配器
 * 支持显示城市列表和搜索结果列表
 */
public class CityAdapter extends RecyclerView.Adapter<CityAdapter.CityViewHolder> {

    public static final int MODE_CITY_LIST = 0;        // 城市列表模式
    public static final int MODE_DELETE = 1;            // 删除模式
    public static final int MODE_SEARCH_RESULT = 2;    // 搜索结果模式

    private final Context context;
    private final int mode;
    private List<City> cities;
    private List<GeoCityResponse.Location> locations;
    private final CityDao cityDao;
    private OnLocationClickListener locationClickListener;

    /**
     * 搜索结果点击监听器
     */
    public interface OnLocationClickListener {
        void onLocationClick(GeoCityResponse.Location location);
    }

    /**
     * 城市列表模式构造函数
     */
    public CityAdapter(Context context, List<City> cities) {
        this(context, cities, MODE_CITY_LIST);
    }

    /**
     * 城市列表模式构造函数（支持删除模式）
     */
    public CityAdapter(Context context, List<City> cities, boolean deleteMode) {
        this(context, cities, deleteMode ? MODE_DELETE : MODE_CITY_LIST);
    }

    /**
     * 城市列表模式构造函数（指定模式）
     */
    public CityAdapter(Context context, List<City> cities, int mode) {
        this.context = context;
        this.cities = cities;
        this.mode = mode;
        this.cityDao = AppDatabase.getInstance(context).cityDao();
    }

    /**
     * 搜索结果模式构造函数
     */
    public CityAdapter(Context context, List<GeoCityResponse.Location> locations, OnLocationClickListener listener) {
        this.context = context;
        this.locations = locations;
        this.mode = MODE_SEARCH_RESULT;
        this.cityDao = AppDatabase.getInstance(context).cityDao();
        this.locationClickListener = listener;
    }

    @NonNull
    @Override
    public CityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setLayoutParams(new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        container.setPadding(0, 0, 0, 12);

        TextView tv = new TextView(context);
        tv.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        tv.setPadding(24, 20, 24, 20);
        tv.setTextSize(mode == MODE_SEARCH_RESULT ? 16 : 18);
        tv.setTextColor(Color.WHITE);
        tv.setBackgroundColor(Color.parseColor("#80FFFFFF"));
        tv.setElevation(2f);
        tv.setGravity(android.view.Gravity.CENTER_VERTICAL);
        if (mode == MODE_SEARCH_RESULT) {
            tv.setMinHeight(56);
        }

        container.addView(tv);
        return new CityViewHolder(container, tv);
    }

    @Override
    public void onBindViewHolder(@NonNull CityViewHolder holder, int position) {
        if (mode == MODE_SEARCH_RESULT) {
            // 搜索结果模式
            GeoCityResponse.Location location = locations.get(position);

            // 显示格式：城市名称 (省/市)
            String displayText = location.name;
            if (location.adm1 != null && !location.adm1.isEmpty()) {
                displayText += " (" + location.adm1;
                if (location.adm2 != null && !location.adm2.isEmpty() && !location.adm2.equals(location.name)) {
                    displayText += " · " + location.adm2;
                }
                displayText += ")";
            }

            holder.textView.setText(displayText);

            holder.itemView.setOnClickListener(v -> {
                if (locationClickListener != null) {
                    locationClickListener.onLocationClick(location);
                }
            });
        } else {
            // 城市列表模式
            City city = cities.get(position);
            holder.textView.setText(city.getName());

            holder.itemView.setOnClickListener(v -> {
                if (mode == MODE_DELETE) {
                    // 删除模式
                    cityDao.deleteCity(city.getName());
                    cities.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, cities.size());
                } else {
                    // 普通列表模式，跳转到主页面
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra("city_name", city.getName());
                    intent.putExtra("location_id", city.getLocationId());
                    context.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if (mode == MODE_SEARCH_RESULT) {
            return locations == null ? 0 : locations.size();
        } else {
            return cities == null ? 0 : cities.size();
        }
    }

    /**
     * 更新搜索结果数据
     */
    public void updateSearchResults(List<GeoCityResponse.Location> newLocations) {
        if (mode == MODE_SEARCH_RESULT) {
            this.locations = newLocations;
            notifyDataSetChanged();
        }
    }

    static class CityViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        CityViewHolder(@NonNull android.view.View itemView, TextView textView) {
            super(itemView);
            this.textView = textView;
        }
    }
}
