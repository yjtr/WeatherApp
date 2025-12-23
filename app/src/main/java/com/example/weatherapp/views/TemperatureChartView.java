package com.example.weatherapp.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * 可滑动的温度折线图View
 * 显示24小时温度变化，包含0度基准线，支持横向滑动
 */
public class TemperatureChartView extends View {
    private List<Integer> temperatures = new ArrayList<>();
    private Paint linePaint;
    private Paint pointPaint;
    private Paint textPaint;
    private Paint zeroLinePaint;
    private int padding = 40;
    private int chartHeight;
    private int chartWidth;
    private int minTemp = Integer.MAX_VALUE;
    private int maxTemp = Integer.MIN_VALUE;
    private int tempRange;
    private int itemWidth = 100; // 每个数据点的宽度（dp转px后）

    public TemperatureChartView(Context context) {
        super(context);
        init();
    }

    public TemperatureChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TemperatureChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 温度折线画笔
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(0xFF4A90E2);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(4f);

        // 温度点画笔
        pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointPaint.setColor(0xFF4A90E2);
        pointPaint.setStyle(Paint.Style.FILL);

        // 温度文字画笔（增大字体）
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(40f); // 增大温度字体
        textPaint.setTextAlign(Paint.Align.CENTER);

        // 0度线画笔
        zeroLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        zeroLinePaint.setColor(0xFF999999);
        zeroLinePaint.setStyle(Paint.Style.STROKE);
        zeroLinePaint.setStrokeWidth(2f);
        zeroLinePaint.setPathEffect(new DashPathEffect(new float[]{10, 5}, 0));
    }

    /**
     * 设置温度数据（时间标签由下方天气图标区域显示，此处不再显示）
     */
    public void setTemperatures(List<Integer> temps, List<String> times) {
        this.temperatures = temps != null ? new ArrayList<>(temps) : new ArrayList<>();
        // 不再使用时间标签，时间由下方天气图标区域统一显示
        calculateRange();
        requestLayout();
        invalidate();
    }

    /**
     * 计算温度范围
     * 改进算法：确保0度线始终在可见范围内
     */
    private void calculateRange() {
        if (temperatures.isEmpty()) {
            minTemp = 0;
            maxTemp = 30;
            tempRange = 30;
            return;
        }

        // 先找到实际的最小和最大温度
        int actualMin = Integer.MAX_VALUE;
        int actualMax = Integer.MIN_VALUE;
        for (Integer temp : temperatures) {
            if (temp < actualMin) actualMin = temp;
            if (temp > actualMax) actualMax = temp;
        }

        int padding = 5; // 上下留白

        // 改进算法：根据温度分布情况智能设置范围，确保0度线始终可见且位置合理
        if (actualMin >= 0) {
            // 所有温度都 >= 0，0度线显示在底部附近
            // 如果最高温度不是很高，可以给底部留一些空间，让0度线更明显
            minTemp = 0;
            if (actualMax <= 10) {
                // 温度较低时，给底部留更多空间，让0度线更明显
                maxTemp = actualMax + Math.max(padding, 5);
            } else {
                maxTemp = actualMax + padding;
            }
        } else if (actualMax <= 0) {
            // 所有温度都 <= 0，0度线显示在顶部附近
            // 如果最低温度不是很低，可以给顶部留一些空间，让0度线更明显
            maxTemp = 0;
            if (actualMin >= -10) {
                // 温度较高时（接近0），给顶部留更多空间，让0度线更明显
                minTemp = actualMin - Math.max(padding, 5);
            } else {
                minTemp = actualMin - padding;
            }
        } else {
            // 温度跨越0度，确保0度在范围内
            minTemp = actualMin - padding;
            maxTemp = actualMax + padding;
            // 确保0度在范围内（虽然已经保证了，但再次确认）
            if (minTemp > 0) minTemp = 0;
            if (maxTemp < 0) maxTemp = 0;
        }

        tempRange = maxTemp - minTemp;

        // 防止除零错误
        if (tempRange == 0) {
            tempRange = 1;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = dpToPx(220); // 减少高度，因为不再显示时间标签

        // 计算内容宽度
        int contentWidth = temperatures.size() * dpToPx(itemWidth);
        if (contentWidth < width) {
            contentWidth = width; // 至少与屏幕宽度相同
        }

        setMeasuredDimension(contentWidth, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (temperatures.isEmpty()) {
            return;
        }

        chartWidth = getWidth();
        chartHeight = getHeight() - padding * 2 - dpToPx(40); // 底部留空间显示温度文字（不再显示时间）

        // 绘制0度基准线
        drawZeroLine(canvas);

        // 绘制温度折线
        drawTemperatureLine(canvas);

        // 绘制温度点、温度文字和时间标签
        drawTemperaturePoints(canvas);
    }

    /**
     * 绘制0度基准线
     * 改进：确保0度线始终在可见区域内显示
     */
    private void drawZeroLine(Canvas canvas) {
        float zeroY = calculateY(0);
        float topBound = padding;
        float bottomBound = getHeight() - padding - dpToPx(40);

        // 确保0度线在可见区域内
        // 如果0度线超出范围，将其限制在可见区域内
        if (zeroY < topBound) {
            zeroY = topBound;
        } else if (zeroY > bottomBound) {
            zeroY = bottomBound;
        }

        // 始终绘制0度线（因为算法已经确保0度在范围内）
        canvas.drawLine(0, zeroY, getWidth(), zeroY, zeroLinePaint);

        // 在左侧标注0度（如果空间足够）
        if (zeroY >= topBound && zeroY <= bottomBound) {
            textPaint.setTextSize(32f);
            textPaint.setTextAlign(Paint.Align.LEFT);
            float labelY = zeroY + dpToPx(10);
            // 确保标签不会超出边界
            if (labelY <= bottomBound + dpToPx(20)) {
                canvas.drawText("0°", padding - dpToPx(35), labelY, textPaint);
            }
        }
    }

    /**
     * 绘制温度折线
     */
    private void drawTemperatureLine(Canvas canvas) {
        if (temperatures.size() < 2) {
            return;
        }

        Path path = new Path();
        int itemWidthPx = dpToPx(itemWidth);

        for (int i = 0; i < temperatures.size(); i++) {
            float x = itemWidthPx / 2f + i * itemWidthPx;
            float y = calculateY(temperatures.get(i));

            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }

        canvas.drawPath(path, linePaint);
    }

    /**
     * 绘制温度点和温度文字（不再绘制时间标签，时间由下方天气图标区域统一显示）
     */
    private void drawTemperaturePoints(Canvas canvas) {
        if (temperatures.isEmpty()) {
            return;
        }

        int itemWidthPx = dpToPx(itemWidth);

        for (int i = 0; i < temperatures.size(); i++) {
            float x = itemWidthPx / 2f + i * itemWidthPx;
            float y = calculateY(temperatures.get(i));
            int temp = temperatures.get(i);

            // 绘制温度点
            canvas.drawCircle(x, y, 6f, pointPaint);

            // 绘制温度文字（在点下方，字体更大）
            textPaint.setTextSize(40f);
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(temp + "°", x, getHeight() - dpToPx(10), textPaint);
        }
    }

    /**
     * 计算温度对应的Y坐标
     */
    private float calculateY(int temperature) {
        if (tempRange == 0) {
            return padding + chartHeight / 2f;
        }
        float ratio = (float) (maxTemp - temperature) / tempRange;
        return padding + ratio * chartHeight;
    }

    /**
     * dp转px
     */
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}

