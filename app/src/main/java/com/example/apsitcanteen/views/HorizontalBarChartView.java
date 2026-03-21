package com.example.apsitcanteen.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Custom View to draw a Horizontal Bar Chart using Android Canvas.
 */
public class HorizontalBarChartView extends View {

    private LinkedHashMap<String, Integer> data;
    private Paint barPaint, textPaint;
    private float labelAreaWidth = dp(100);
    private float barHeight = dp(24);
    private float gap = dp(16);

    public HorizontalBarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFF1A1A1A);
        textPaint.setTextSize(dp(12));
    }

    public void setData(LinkedHashMap<String, Integer> data) {
        this.data = data;
        requestLayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = 0;
        if (data != null) {
            height = (int) ((barHeight + gap) * data.size() + gap);
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (data == null || data.isEmpty()) return;

        float width = getWidth();
        float availableBarWidth = width - labelAreaWidth - dp(60);

        int maxVal = 0;
        for (int val : data.values()) if (val > maxVal) maxVal = val;

        int index = 0;
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            float y = gap + (index * (barHeight + gap));
            
            // Draw Item Name
            textPaint.setTextAlign(Paint.Align.LEFT);
            String name = entry.getKey();
            if (name.length() > 15) name = name.substring(0, 12) + "...";
            canvas.drawText(name, dp(8), y + barHeight / 1.5f, textPaint);

            // Draw Bar
            float barLen = (float) entry.getValue() / maxVal * availableBarWidth;
            if (index % 2 == 0) {
                barPaint.setColor(0xFF2D6A4F); // Green
            } else {
                barPaint.setColor(0xFFD4A017); // Gold
            }
            canvas.drawRect(labelAreaWidth, y, labelAreaWidth + barLen, y + barHeight, barPaint);

            // Draw Count at end of bar
            textPaint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(String.valueOf(entry.getValue()), labelAreaWidth + barLen + dp(8), y + barHeight / 1.5f, textPaint);

            index++;
        }
    }

    private float dp(float d) {
        return d * getResources().getDisplayMetrics().density;
    }
}
