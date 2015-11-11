package info.guardianproject.ripple;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import info.guardianproject.securereaderinterface.uiutil.AnimationHelpers;

public class PanicActivity extends Activity implements OnTouchListener {
    public static final String TAG = "PanicActivity";

    public static final String EXTRA_TEST_RUN = "info.guardianproject.ripple.extra.TEST_RUN";

    public int yMaxTranslation;
    public int yTranslationArrow;
    public int yCurrentTranslation;
    public int yDelta;
    public int yOriginal;
    public Rect mArrowRect;
    public boolean mReleaseWillTrigger = false;
    private RelativeLayout mFrameRoot;
    private View mArrow;
    private ImageView mPanicSwipeButton;
    private TextView mTextHint;
    private int mColorWhite;
    private int mColorRipple;
    private int mColorTriggered;
    private int mColorTriggeredText;
    private int mColorRippleOutside;
    private int mRedStart;
    private int mGreenStart;
    private int mBlueStart;
    private int mRedEnd;
    private int mGreenEnd;
    private int mBlueEnd;
    private int mRedDelta;
    private int mGreenDelta;
    private int mBlueDelta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.setBackgroundDrawable(null);
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_panic);

        mArrow = findViewById(R.id.arrowSymbolView);

        mFrameRoot = (RelativeLayout) findViewById(R.id.frameRoot);
        mTextHint = (TextView) findViewById(R.id.textHint);
        mPanicSwipeButton = (ImageView) findViewById(R.id.panic_swipe_button);
        mPanicSwipeButton.setOnTouchListener(this);

        View btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PanicActivity.this.finish();
            }
        });

        Resources r = getResources();
        mColorWhite = r.getColor(android.R.color.white);
        mColorRipple = r.getColor(R.color.ripple);
        mColorTriggered = r.getColor(R.color.triggered);
        mColorTriggeredText = r.getColor(R.color.triggered_text);
        mColorRippleOutside = r.getColor(R.color.ripple_outside);
        mRedStart = (mColorRipple & 0x00ff0000) >> 16;
        mGreenStart = (mColorRipple & 0x0000ff00) >> 8;
        mBlueStart = mColorRipple & 0x000000ff;
        mRedEnd = (mColorTriggered & 0x00ff0000) >> 16;
        mGreenEnd = (mColorTriggered & 0x0000ff00) >> 8;
        mBlueEnd = mColorTriggered & 0x000000ff;
        mRedDelta = mRedEnd - mRedStart;
        mGreenDelta = mGreenEnd - mGreenStart;
        mBlueDelta = mBlueEnd - mBlueStart;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (view == mPanicSwipeButton) {
            final int Y = (int) event.getRawY();
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    mPanicSwipeButton.setPressed(true);

                    RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                    yOriginal = lParams.topMargin;
                    yDelta = Y - lParams.topMargin;
                    mReleaseWillTrigger = false;

                    mArrowRect = new Rect();
                    if (!mArrow.getGlobalVisibleRect(mArrowRect)) {
                        mArrowRect = null;
                    } else {
                        Rect symbolRect = new Rect();
                        if (mPanicSwipeButton.getGlobalVisibleRect(symbolRect)) {
                            yMaxTranslation = mArrowRect.bottom - symbolRect.bottom;
                            yTranslationArrow = mArrowRect.top - symbolRect.bottom;
                        }
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    mPanicSwipeButton.setPressed(false);

                    if (mReleaseWillTrigger) {
                        AnimationHelpers.scale(mPanicSwipeButton, 1.0f, 0, 200, new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(getBaseContext(), CountDownActivity.class);
                                intent.putExtra(EXTRA_TEST_RUN,
                                        getIntent().getBooleanExtra(EXTRA_TEST_RUN, false));
                                startActivity(intent);
                                finish();
                            }
                        });
                    } else {
                        AnimationHelpers.translateY(mPanicSwipeButton, yCurrentTranslation, 0, 200);
                        mFrameRoot.setBackgroundColor(mColorRipple);
                    }
                    mReleaseWillTrigger = false;
                    break;

                case MotionEvent.ACTION_POINTER_DOWN:
                    break;

                case MotionEvent.ACTION_POINTER_UP:
                    break;

                case MotionEvent.ACTION_MOVE:
                    yCurrentTranslation = Math.max(0, Math.min(Y - yDelta, yMaxTranslation));
                    AnimationHelpers.translateY(mPanicSwipeButton, yCurrentTranslation, yCurrentTranslation, 0);

                    float v = (float) yCurrentTranslation / yMaxTranslation;
                    mFrameRoot.setBackgroundColor((int) (0xff000000 + ((mRedStart + ((int) (mRedDelta * v))) << 16)
                            + ((mGreenStart + ((int) (mGreenDelta * v))) << 8)
                            + (mBlueStart + (mBlueDelta * v))));
                    if (yCurrentTranslation == yMaxTranslation) {
                        mReleaseWillTrigger = true;
                        mTextHint.setText(R.string.release_to_confirm);
                        mTextHint.setTextColor(mColorTriggeredText);
                    } else {
                        mReleaseWillTrigger = false;
                        mTextHint.setText(R.string.swipe_down_to_trigger);
                        mTextHint.setTextColor(mColorWhite);
                    }
                    break;
            }
            view.invalidate();
            return true;
        }
        return false;
    }
}
