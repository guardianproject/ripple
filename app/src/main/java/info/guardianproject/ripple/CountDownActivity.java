package info.guardianproject.ripple;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

import info.guardianproject.panic.PanicTrigger;

public class CountDownActivity extends Activity {
    private static final String TAG = "CountDownActivity";

    CountDownAsyncTask mCountDownAsyncTask;
    private TextView mCountDownNumber;
    private boolean mTestRun;

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

        mCountDownAsyncTask = new CountDownAsyncTask();
        mCountDownAsyncTask.execute();


        RelativeLayout frameRoot = (RelativeLayout) findViewById(R.id.frameRoot);
        frameRoot.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.i(TAG, "onTouch ");
                cancel();
                return true;
            }
        });

        if (Build.VERSION.SDK_INT >= 11) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN);
            frameRoot.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    Log.i(TAG, "onSystemUiVisibilityChange " + visibility);
                    /* If the nav bar comes back while the countdown is active,
                       that means the user clicked on the screen. Showing the
                       test dialog also triggers this, so filter on countdown */
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0
                            && mCountDownAsyncTask.countdown > 0) {
                        cancel();
                    }
                }
            });
        }
    }

    private void cancel() {
        Log.i(TAG, "cancel");
        mCountDownAsyncTask.cancel(true);
        finish();
    }

    private class CountDownAsyncTask extends AsyncTask<Void, Integer, Void> {

        private int countdown = 5;

        @Override
        protected void onProgressUpdate(Integer... values) {
            mCountDownNumber.setText(String.valueOf(values[0]));
        }

        @Override
        protected Void doInBackground(Void... voids) {
            do {
                publishProgress(countdown);
                countdown--;
                if (isCancelled()) {
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            } while (countdown > 0);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            final Activity activity = CountDownActivity.this;

            if (mTestRun) {
                new AlertDialog.Builder(activity)
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.panic_test_successful)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                CountDownActivity.this.finish();
                            }
                        }).show();
            } else {
                // TODO implement trigger broadcast
                Toast.makeText(activity, R.string.done, Toast.LENGTH_LONG).show();

                Set<String> receivers = PanicTrigger.getReceiverPackageNames(activity);
                for (String s : receivers) {
                    Toast.makeText(activity, "trigger " + s, Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        }

    }
}
