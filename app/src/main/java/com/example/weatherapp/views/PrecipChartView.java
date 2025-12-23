package com.example.weatherapp.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * 降雨量柱状图View
 * 显示未来2小时降雨量，每5分钟一个柱子，支持横向滑动
 */
public class PrecipChartView extends View {
    private List<Float> precipitations = new ArrayList<>();
    private List<String> timeLabels = new ArrayList<>();
    private Paint barPaint;
    private Paint textPaint;
    private Paint timeTextPaint;
    private Paint gridLinePaint;
    private Paint baselinePaint;
    private int padding = 40;
    private int chartHeight;
    private int chartWidth;
    private float maxPrecip = 0;
    private int itemWidth = 20; // 每个柱子的宽度（dp转px后，更密集）
    private int barSpacing = 2; // 柱子之间的间距（dp转px后，更密集）

    public PrecipChartView(Context context) {
        super(context);
        init();
    }

    public PrecipChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PrecipChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 柱子画笔
        barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barPaint.setStyle(Paint.Style.FILL);

        // 降雨量文字画笔
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(28f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        // 时间文字画笔（增大字体）
        timeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        timeTextPaint.setColor(Color.WHITE);
        timeTextPaint.setTextSize(32f); // 增大字体
        timeTextPaint.setTextAlign(Paint.Align.CENTER);

        // 网格线画笔（用于时间刻度）
        gridLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridLinePaint.setColor(0xFFCCCCCC);
        gridLinePaint.setStyle(Paint.Style.STROKE);
        gridLinePaint.setStrokeWidth(1f);

        // 底部基线画笔（明显的黑线）
        baselinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        baselinePaint.setColor(Color.BLACK);
        baselinePaint.setStyle(Paint.Style.STROKE);
        baselinePaint.setStrokeWidth(3f); // 明显的黑线
    }

    /**
     * 设置降雨量数据和时间标签
     */
    public void setPrecipitations(List<Float> precipts, List<String> times) {
        this.precipitations = precipts != null ? new ArrayList<>(precipts) : new ArrayList<>();
        this.timeLabels = times != null ? new ArrayList<>(times) : new ArrayList<>();
        calculateMaxPrecip();
        requestLayout();
        invalidate();
    }

    /**
     * 计算最大降雨量
     */
    private void calculateMaxPrecip() {
        maxPrecip = 0;
        for (Float precip : precipitations) {
            if (precip > maxPrecip) {
                maxPrecip = precip;
            }
        }
        // 如果最大值为0，设置一个默认值用于显示
        if (maxPrecip == 0) {
            maxPrecip = 1.0f;
        } else {
            // 增加一些余量，让图表更好看
            maxPrecip = maxPrecip * 1.2f;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = dpToPx(200); // 固定高度200dp

        // 计算内容宽度（每个柱子宽度 + 间距）
        int itemWidthPx = dpToPx(itemWidth);
        int barSpacingPx = dpToPx(barSpacing);
        int contentWidth = precipitations.size() * (itemWidthPx + barSpacingPx);
        if (contentWidth < width) {
            contentWidth = width; // 至少与屏幕宽度相同
        }

        setMeasuredDimension(contentWidth, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (precipitations.isEmpty()) {
            return;
        }

        chartWidth = getWidth();
        chartHeight = getHeight() - padding * 2 - dpToPx(10); // 底部留空间显示时间

        // 绘制网格线（每10分钟一条，即每10个柱子）
        drawGridLines(canvas);

        // 绘制柱状图
        drawBars(canvas);

        // 绘制底部基线（明显的黑线）
        drawBaseline(canvas);

        // 绘制时间标签
        drawTimeLabels(canvas);
    }

    /**
     * 绘制网格线（每10分钟一条，即每10个柱子）
     */
    private void drawGridLines(Canvas canvas) {
        int itemWidthPx = dpToPx(itemWidth);
        int barSpacingPx = dpToPx(barSpacing);

        // 每10个柱子（10分钟）绘制一条竖线
        for (int i = 0; i < precipitations.size(); i += 10) {
            float x = itemWidthPx / 2f + i * (itemWidthPx + barSpacingPx);
            canvas.drawLine(x, padding, x, padding + chartHeight, gridLinePaint);
        }
    }

    /**
     * 绘制柱状图
     */
    private void drawBars(Canvas canvas) {
        int itemWidthPx = dpToPx(itemWidth);
        int barSpacingPx = dpToPx(barSpacing);
        int barWidth = itemWidthPx - barSpacingPx;

        for (int i = 0; i < precipitations.size(); i++) {
            float precip = precipitations.get(i);
            float x = i * (itemWidthPx + barSpacingPx) + barSpacingPx / 2f;

            // 计算柱子高度
            float barHeight = (precip / maxPrecip) * chartHeight;
            float barTop = padding + chartHeight - barHeight;
            float barBottom = padding + chartHeight;

            // 根据降雨强度设置颜色
            if (precip > 0.5) {
                barPaint.setColor(0xFF2196F3); // 蓝色 - 中雨
            } else if (precip > 0.1) {
                barPaint.setColor(0xFF64B5F6); // 浅蓝色 - 小雨
            } else if (precip > 0) {
                barPaint.setColor(0xFFBBDEFB); // 更浅的蓝色 - 微量
            } else {
                barPaint.setColor(0xFFE0E0E0); // 灰色 - 无雨
            }

            // 绘制柱子
            canvas.drawRect(x, barTop, x + barWidth, barBottom, barPaint);

            // 绘制降雨量文字（在柱子顶部）
            if (precip > 0) {
                textPaint.setTextSize(24f);
                textPaint.setTextAlign(Paint.Align.CENTER);
                float textY = barTop - dpToPx(4);
                if (textY < padding) {
                    textY = barBottom + dpToPx(20); // 如果空间不够，显示在柱子下方
                }
                canvas.drawText(String.format("%.1f", precip), x + barWidth / 2f, textY, textPaint);
            }
        }
    }

    /**
     * 绘制底部基线（明显的黑线）
     */
    private void drawBaseline(Canvas canvas) {
        float baselineY = padding + chartHeight;
        canvas.drawLine(0, baselineY, getWidth(), baselineY, baselinePaint);
    }

    /**
     * 绘制时间标签（每10分钟显示一次，即每10个柱子）
     * 时间标签可以占用前后两个柱子的空间
     */
    private void drawTimeLabels(Canvas canvas) {
        int itemWidthPx = dpToPx(itemWidth);
        int barSpacingPx = dpToPx(barSpacing);

        for (int i = 0; i < precipitations.size(); i += 10) {
            if (i < timeLabels.size() && timeLabels.get(i) != null && !timeLabels.get(i).isEmpty()) {
                // 计算中心位置（对应柱子的中心）
                float centerX = itemWidthPx / 2f + i * (itemWidthPx + barSpacingPx);

                // 时间标签可以占用前后两个柱子的空间，所以x位置就是中心位置
                timeTextPaint.setTextSize(32f); // 增大字体
                timeTextPaint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(timeLabels.get(i), centerX, getHeight() - dpToPx(5), timeTextPaint);
            }
        }
    }

    /**
     * dp转px
     */
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}

