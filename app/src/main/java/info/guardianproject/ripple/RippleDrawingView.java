package info.guardianproject.ripple;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

public class RippleDrawingView extends View {
    private Paint mRippleOutsidePaint;
    private Paint mRippleInsidePaint;
    private Paint mRippleCenterPaint;
    private float mSize;

    public RippleDrawingView(Context context) {
        super(context);
        init(context);
    }

    public RippleDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RippleDrawingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (Build.VERSION.SDK_INT >= 11) {
            float width = getWidth();
            float x = width / 2;
            float y = getHeight() * 0.92f;
            canvas.drawCircle(x, y, mSize, mRippleOutsidePaint);
            canvas.drawCircle(x, y, mSize - (width * 0.166666f), mRippleInsidePaint);
            canvas.drawCircle(x, y, mSize - (width * 0.333333f), mRippleCenterPaint);
        }
    }

    public void setSize(float size) {
        mSize = size;
    }

    private void init(Context context) {
        Resources r = context.getResources();
        mRippleOutsidePaint = new Paint();
        mRippleOutsidePaint.setStyle(Paint.Style.FILL);
        mRippleOutsidePaint.setColor(r.getColor(R.color.ripple_outside));
        mRippleInsidePaint = new Paint();
        mRippleInsidePaint.setStyle(Paint.Style.FILL);
        mRippleInsidePaint.setColor(r.getColor(R.color.ripple));
        mRippleCenterPaint = new Paint();
        mRippleCenterPaint.setStyle(Paint.Style.FILL);
        mRippleCenterPaint.setColor(r.getColor(R.color.triggered));
    }
}
