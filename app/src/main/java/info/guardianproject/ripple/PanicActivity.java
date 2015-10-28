package info.guardianproject.ripple;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
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
    private ImageView mSymbol;
    private TextView mTextHint;
    private int mTextColorBlack;
    private int mTextColorWhite;
    private int mTextColorRed;

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
        mSymbol = (ImageView) findViewById(R.id.radioactiveSymbolView);
        mSymbol.setOnTouchListener(this);

        View btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PanicActivity.this.finish();
            }
        });

        mTextColorBlack = getResources().getColor(android.R.color.black);
        mTextColorWhite = getResources().getColor(android.R.color.white);
        mTextColorRed = getResources().getColor(R.color.red);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (view == mSymbol) {
            final int Y = (int) event.getRawY();
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    mSymbol.setColorFilter(mTextColorRed);
                    RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                    yOriginal = lParams.topMargin;
                    yDelta = Y - lParams.topMargin;
                    mReleaseWillTrigger = false;

                    mArrowRect = new Rect();
                    if (!mArrow.getGlobalVisibleRect(mArrowRect)) {
                        mArrowRect = null;
                    } else {
                        Rect symbolRect = new Rect();
                        if (mSymbol.getGlobalVisibleRect(symbolRect)) {
                            yMaxTranslation = mArrowRect.bottom - symbolRect.bottom;
                            yTranslationArrow = mArrowRect.top - symbolRect.bottom;
                        }
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    mSymbol.setColorFilter(null);
                    if (mReleaseWillTrigger) {
                        AnimationHelpers.scale(mSymbol, 1.0f, 0, 200, new Runnable() {
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
                        AnimationHelpers.translateY(mSymbol, yCurrentTranslation, 0, 200);
                        mFrameRoot.setBackgroundColor(mTextColorBlack);
                    }
                    mReleaseWillTrigger = false;
                    break;

                case MotionEvent.ACTION_POINTER_DOWN:
                    break;

                case MotionEvent.ACTION_POINTER_UP:
                    break;

                case MotionEvent.ACTION_MOVE:
                    yCurrentTranslation = Math.max(0, Math.min(Y - yDelta, yMaxTranslation));
                    AnimationHelpers.translateY(mSymbol, yCurrentTranslation, yCurrentTranslation, 0);

                    int v = (int) ((float) yCurrentTranslation / yMaxTranslation * 255.0);
                    mFrameRoot.setBackgroundColor(0xff000000 + (v << 16) + (v << 8) + v);
                    if (yCurrentTranslation == yMaxTranslation) {
                        mReleaseWillTrigger = true;
                        mTextHint.setText(R.string.release_to_trigger);
                        mTextHint.setTextColor(mTextColorBlack);
                        if (Build.VERSION.SDK_INT >= 14)
                            mTextHint.setAllCaps(true);
                    } else {
                        mReleaseWillTrigger = false;
                        mTextHint.setText(R.string.swipe_down_to_trigger);
                        mTextHint.setTextColor(mTextColorWhite);
                        if (Build.VERSION.SDK_INT >= 14)
                            mTextHint.setAllCaps(false);
                    }
                    break;
            }
            view.invalidate();
            return true;
        }
        return false;
    }
}
