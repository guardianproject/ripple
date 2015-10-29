package info.guardianproject.ripple;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import info.guardianproject.panic.PanicTrigger;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (PanicTrigger.checkForConnectIntent(this)
                || PanicTrigger.checkForDisconnectIntent(this)) {
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_trigger:
                startActivity(new Intent(this, PanicActivity.class));
                return true;
            case R.id.action_responders:
                return true;
            case R.id.action_test_run:
                Intent intent = new Intent(this, PanicActivity.class);
                intent.putExtra(PanicActivity.EXTRA_TEST_RUN, true);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
