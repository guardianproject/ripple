package info.guardianproject.ripple;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import info.guardianproject.panic.PanicTrigger;

public class CountDownActivity extends Activity {
    private static final String TAG = "CountDownActivity";

    private static final String KEY_COUNT_DOWN_DONE = "keyCountDownDone";

    private CountDownAsyncTask mCountDownAsyncTask;
    private TextView mCountDownNumber;
    private TextView mTouchToCancel;
    private ImageView mCancelButton;
    private int mCountDown = 0xff;
    private boolean mTestRun;

    // lint is failing to see that setOnSystemUiVisibilityChangeListener is wrapped in
    // if (Build.VERSION.SDK_INT >= 11).
    @TargetApi(11)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTestRun = getIntent().getBooleanExtra(PanicActivity.EXTRA_TEST_RUN, false);

        Window window = getWindow();
        window.setBackgroundDrawable(null);
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_count_down);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int scale;
        if (displayMetrics.heightPixels > displayMetrics.widthPixels) {
            scale = displayMetrics.heightPixels;
        } else {
            scale = displayMetrics.widthPixels;
        }
        mCountDownNumber = (TextView) findViewById(R.id.countDownNumber);
        mCountDownNumber.setTextSize(((float) scale) / 5);

        mTouchToCancel = (TextView) findViewById(R.id.tap_anywhere_to_cancel);
        mCancelButton = (ImageView) findViewById(R.id.cancelButton);

        mCountDownAsyncTask = new CountDownAsyncTask();

        if (savedInstanceState != null && savedInstanceState.getBoolean(KEY_COUNT_DOWN_DONE, false)) {
            showDoneScreen();
        } else {
            mCountDownAsyncTask.execute();
        }

        RelativeLayout frameRoot = (RelativeLayout) findViewById(R.id.frameRoot);
        frameRoot.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                cancel();
                return true;
            }
        });

        if (Build.VERSION.SDK_INT >= 16) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
        } else if (Build.VERSION.SDK_INT >= 14) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }

        if (Build.VERSION.SDK_INT >= 11) {
            frameRoot.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    /* If the nav bar comes back while the countdown is active,
                       that means the user clicked on the screen. Showing the
                       test dialog also triggers this, so filter on countdown */
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0 && mCountDown > 0) {
                        cancel();
                    }
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_COUNT_DOWN_DONE, mCountDown == 0);
        if (mCountDown > 0) {
            // cancel the countdown, it'll get restarted when the Activity comes back
            mCountDownAsyncTask.cancel(true);
        }
    }

    private void cancel() {
        mCountDownAsyncTask.cancel(true);
        finish();
    }

    private void showDoneScreen() {
        mTouchToCancel.setText(R.string.done);
        mTouchToCancel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 64);
        mCancelButton.setVisibility(View.GONE);
        mCountDownNumber.setVisibility(View.GONE);
    }

    private class CountDownAsyncTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onProgressUpdate(Integer... values) {
            mCountDown = values[0];
            if (values[0] > 0) {
                mCountDownNumber.setText(String.valueOf(values[0]));
            } else {
                showDoneScreen();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                int countdown = 5;
                while (countdown >= 0) {
                    publishProgress(countdown);
                    countdown--;
                    Thread.sleep(1000);
                    if (isCancelled()) {
                        break;
                    }
                }
            } catch (InterruptedException e) {
                // ignored
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            final Activity activity = CountDownActivity.this;

            if (mTestRun) {
                new AlertDialog.Builder(activity)
                        .setTitle(R.string.test_dialog_title)
                        .setMessage(R.string.panic_test_successful)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                CountDownActivity.this.finish();
                            }
                        }).show();
            } else {
                PanicTrigger.sendTrigger(activity);
                Toast.makeText(activity, R.string.done, Toast.LENGTH_LONG).show();
                ExitActivity.exitAndRemoveFromRecentApps(activity);
            }
        }
    }
}
