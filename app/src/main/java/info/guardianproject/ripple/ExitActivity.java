
package info.guardianproject.ripple;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

public class ExitActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21) {
            finishAndRemoveTask();
        } else {
            finish();
        }

        System.exit(0);
    }

    public static void exitAndRemoveFromRecentApps(Activity activity) {
        Intent intent = new Intent(activity, ExitActivity.class);

        if (Build.VERSION.SDK_INT < 11) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                    | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK
                    | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                    | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        }

        activity.startActivity(intent);
    }
}
