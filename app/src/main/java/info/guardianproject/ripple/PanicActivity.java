package info.guardianproject.ripple;

import android.app.Activity;
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

import info.guardianproject.securereaderinterface.uiutil.AnimationHelpers;

public class PanicActivity extends Activity implements OnTouchListener {
    public static final String TAG = "PanicActivity";

    public int yMaxTranslation;
    public int yTranslationArrow;
    public int yCurrentTranslation;
    public int yDelta;
    public int yOriginal;
    public Rect mArrowRect;
    public boolean mIsOverArrow = false;
    private View mArrow;
    private ImageView mSymbol;
	private boolean mOnlyTesting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.setBackgroundDrawable(null);
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_panic);

        mOnlyTesting = getIntent().getBooleanExtra("testing", false);

        mArrow = findViewById(R.id.arrowSymbolView);

        mSymbol = (ImageView) findViewById(R.id.radioactiveSymbolView);
        mSymbol.setOnTouchListener(this);

        View btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PanicActivity.this.finish();
            }
        });
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (view == mSymbol) {
            final int X = (int) event.getRawX();
            final int Y = (int) event.getRawY();
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                    yOriginal = lParams.topMargin;
                    yDelta = Y - lParams.topMargin;
                    mIsOverArrow = false;

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
                case MotionEvent.ACTION_UP: {
                    mSymbol.setColorFilter(null);
                    if (mIsOverArrow) {
                        AnimationHelpers.scale(mSymbol, 1.0f, 0, 200, new Runnable() {
                            @Override
                            public void run() {
                                // TODO implement trigger broadcast
                            }
                        });
                    } else {
                        AnimationHelpers.translateY(mSymbol, yCurrentTranslation, 0, 200);
                    }
                    mIsOverArrow = false;
                    break;
                }

                case MotionEvent.ACTION_POINTER_DOWN:
                    break;
                case MotionEvent.ACTION_POINTER_UP:

                    break;
                case MotionEvent.ACTION_MOVE: {
                    yCurrentTranslation = Math.max(0, Math.min(Y - yDelta, yMaxTranslation));
                    AnimationHelpers.translateY(mSymbol, yCurrentTranslation, yCurrentTranslation, 0);

                    if (yCurrentTranslation >= yTranslationArrow)
                        mIsOverArrow = true;
                    else
                        mIsOverArrow = false;
                    setSymbolColor(mIsOverArrow);
                    break;
                }
            }
            view.invalidate();
            return true;
        }
        return false;
    }

    private void setSymbolColor(boolean isOverArrow) {
        if (isOverArrow)
            mSymbol.setColorFilter(0xffff0000);
        else
            mSymbol.setColorFilter(null);
    }
}
