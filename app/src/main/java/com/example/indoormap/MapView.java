package com.example.indoormap;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

public class MapView extends View {

    private final Context mContext;
    private final ScaleGestureDetector scaleGestureDetector;
    private float scaleFactor = 1.0f;  // Default scale factor (1 = no zoom)

    // Image's original dimensions
    private final float imageWidth = 1000f;  // Image width in pixels
    private final float imageHeight = 1000f; // Image height in pixels

    // Translation (Panning) variables
    private float translateX = 0f;  // Horizontal translation
    private float translateY = 0f;  // Vertical translation
    private float prevX, prevY;     // Previous touch positions

    // Bounds for panning
    private float minTranslateX, maxTranslateX;
    private float minTranslateY, maxTranslateY;

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Resources res = mContext.getResources();

        // Load the map image
        Drawable myImage = ResourcesCompat.getDrawable(res, R.drawable.office, null);
        if (myImage == null) return;

        // Image's scaled dimensions
        float scaledWidth = imageWidth * scaleFactor;
        float scaledHeight = imageHeight * scaleFactor;

        // Calculate panning bounds to ensure the map doesn't move out of view
        minTranslateX = Math.min(0, getWidth() - scaledWidth);
        maxTranslateX = 0;
        minTranslateY = Math.min(0, getHeight() - scaledHeight);
        maxTranslateY = 0;

        // Clamp translations to prevent moving the map out of bounds
        translateX = Math.max(minTranslateX, Math.min(translateX, maxTranslateX));
        translateY = Math.max(minTranslateY, Math.min(translateY, maxTranslateY));

        // Apply scaling and translation
        canvas.save();
        canvas.translate(translateX, translateY);
        canvas.scale(scaleFactor, scaleFactor);

        // Set the bounds of the image and draw it
        myImage.setBounds(0, 0, (int) imageWidth, (int) imageHeight);
        myImage.draw(canvas);
        canvas.restore();

        // Draw an example marker on the map
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        float markerX = 700f; // Example marker position (image coordinates)
        float markerY = 500f;
        float scaledMarkerX = markerX * scaleFactor + translateX;
        float scaledMarkerY = markerY * scaleFactor + translateY;
        canvas.drawCircle(scaledMarkerX, scaledMarkerY, 10 * scaleFactor, paint); // Scale marker size with zoom
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Handle pinch-to-zoom
        scaleGestureDetector.onTouchEvent(event);

        // Handle drag gestures
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                prevX = event.getX();
                prevY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - prevX;
                float dy = event.getY() - prevY;

                // Update translations for dragging
                translateX += dx;
                translateY += dy;

                // Update the previous touch position
                prevX = event.getX();
                prevY = event.getY();

                invalidate(); // Redraw the view
                break;
        }

        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            // Adjust the scale factor based on the pinch gesture
            scaleFactor *= detector.getScaleFactor();

            // Clamp the scale factor to prevent excessive zoom
            scaleFactor = Math.max(0.5f, Math.min(scaleFactor, 3.0f));

            invalidate(); // Redraw the view
            return true;
        }
    }
}
