package info.guardianproject.ripple;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import info.guardianproject.panic.PanicTrigger;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";

    ArrayList<CharSequence> appLabelList;
    ArrayList<Drawable> iconList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (PanicTrigger.checkForConnectIntent(this)
                || PanicTrigger.checkForDisconnectIntent(this)) {
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        int ripple = getResources().getColor(R.color.ripple);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ripple));

        View panicButton = findViewById(R.id.panic_button);
        panicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, PanicActivity.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        final String receivers[] = PanicTrigger.getAllResponders(this).toArray(new String[0]);
        PackageManager pm = getPackageManager();
        appLabelList = new ArrayList<CharSequence>(receivers.length);
        iconList = new ArrayList<Drawable>(receivers.length);
        for (String packageName : receivers) {
            try {
                appLabelList.add(pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)));
                iconList.add(pm.getApplicationIcon(packageName));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getApplicationContext()));
        recyclerView.setHasFixedSize(true); // does not change, except in onResume()
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new RecyclerView.Adapter<AppRowHolder>() {
            @Override
            public AppRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return (new AppRowHolder(getLayoutInflater().inflate(R.layout.row, parent, false)));
            }

            @Override
            public void onBindViewHolder(AppRowHolder holder, int position) {
                holder.setIcon(iconList.get(position));
                holder.setText(appLabelList.get(position));
            }

            @Override
            public int getItemCount() {
                return appLabelList.size();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_test_run:
                Intent intent = new Intent(this, PanicActivity.class);
                intent.putExtra(PanicActivity.EXTRA_TEST_RUN, true);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    static class AppRowHolder extends RecyclerView.ViewHolder {
        SwitchCompat onSwitch = null;

        AppRowHolder(View row) {
            super(row);
            onSwitch = (SwitchCompat) row.findViewById(R.id.on_switch);
        }

        void setIcon(Drawable drawable) {
            onSwitch.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            onSwitch.setCompoundDrawablePadding(20);
        }

        void setText(CharSequence text) {
            onSwitch.setText(text);
        }

        void setOnSwitch(boolean on) {
            onSwitch.setChecked(on);
        }
    }
}
