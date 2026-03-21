package com.example.apsitcanteen.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Custom View to draw a Bar Chart using Android Canvas.
 */
public class BarChartView extends View {

    private LinkedHashMap<String, Double> data;
    private Paint barPaint, textPaint, axisPaint, gridPaint;
    private float padding = dp(20);
    private float labelHeight = dp(20);
    private float yAxisWidth = dp(40);

    public BarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barPaint.setColor(0xFF2D6A4F); // Medium Green

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFF1A1A1A);
        textPaint.setTextSize(dp(10));
        textPaint.setTextAlign(Paint.Align.CENTER);

        axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisPaint.setColor(0xFF1B4332);
        axisPaint.setStrokeWidth(dp(2));

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(0xFFE0E0E0);
        gridPaint.setStrokeWidth(dp(1));
    }

    public void setData(LinkedHashMap<String, Double> data) {
        this.data = data;
        invalidate(); // Redraw
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = (int) dp(220); // Fixed height as requested
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (data == null || data.isEmpty()) return;

        float width = getWidth();
        float height = getHeight();
        float chartWidth = width - padding * 2 - yAxisWidth;
        float chartHeight = height - padding * 2 - labelHeight;

        // Find Max Value for scaling
        double maxValue = 0;
        for (double val : data.values()) {
            if (val > maxValue) maxValue = val;
        }
        // Round up maxValue for grid lines
        maxValue = Math.ceil(maxValue / 500.0) * 500.0;

        // Draw Y-Axis Grid Lines and Labels
        for (int i = 0; i <= 5; i++) {
            float y = padding + chartHeight - (chartHeight * i / 5);
            canvas.drawLine(padding + yAxisWidth, y, width - padding, y, gridPaint);
            
            String label = "₹" + (int)(maxValue * i / 5);
            textPaint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(label, padding + yAxisWidth - dp(5), y + dp(4), textPaint);
        }

        // Draw X-Axis Line
        canvas.drawLine(padding + yAxisWidth, padding + chartHeight, width - padding, padding + chartHeight, axisPaint);

        // Draw Bars
        int numBars = data.size();
        float barGap = dp(8);
        float totalGaps = barGap * (numBars + 1);
        float barWidth = (chartWidth - totalGaps) / numBars;

        int index = 0;
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            float x = padding + yAxisWidth + barGap + (index * (barWidth + barGap));
            float barHeight = (float) (entry.getValue() / maxValue * chartHeight);
            float y = padding + chartHeight - barHeight;

            // Highlight highest bar in Gold
            if (entry.getValue() == getMaxValueInMap()) {
                barPaint.setColor(0xFFD4A017);
            } else {
                barPaint.setColor(0xFF2D6A4F);
            }

            canvas.drawRect(x, y, x + barWidth, padding + chartHeight, barPaint);

            // Labels
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(entry.getKey(), x + barWidth / 2, padding + chartHeight + labelHeight, textPaint);
            
            // Value Label
            float valueY = y - dp(4);
            canvas.drawText("₹" + entry.getValue().intValue(), x + barWidth / 2, valueY, textPaint);

            index++;
        }
    }

    private double getMaxValueInMap() {
        double max = 0;
        for (double v : data.values()) if (v > max) max = v;
        return max;
    }

    private float dp(float d) {
        return d * getResources().getDisplayMetrics().density;
    }
}
