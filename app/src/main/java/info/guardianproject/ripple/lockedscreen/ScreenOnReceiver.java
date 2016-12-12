package info.guardianproject.ripple.lockedscreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import info.guardianproject.ripple.PanicActivity;

public class ScreenOnReceiver extends BroadcastReceiver {
    public ScreenOnReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
            if (PasswordFailsReceiver.triggerRipple) {
                context.startActivity(new Intent(context, PanicActivity.class));
                PasswordFailsReceiver.triggerRipple = false;
            }
        }
    }
}
