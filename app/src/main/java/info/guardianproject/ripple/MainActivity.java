package info.guardianproject.ripple;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import info.guardianproject.panic.Panic;
import info.guardianproject.panic.PanicTrigger;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";

    static final String FIRST_RUN_PREF = "firstRun";

    private static final String GOT_IT_PREF = "gotItPref";
    private static final int CONNECT_RESULT = 0x01;

    private String responders[];
    private Set<String> enabledResponders;
    private Set<String> respondersThatCanConnect;
    private ArrayList<CharSequence> appLabelList;
    private ArrayList<Drawable> iconList;

    private SharedPreferences prefs;

    private String requestPackageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (PanicTrigger.checkForConnectIntent(this)
                || PanicTrigger.checkForDisconnectIntent(this)) {
            finish();
            return;
        }

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        View panicButton = findViewById(R.id.panic_button);
        panicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, PanicActivity.class));
            }
        });

        final Button gotItButton = (Button) findViewById(R.id.got_it);
        final View introInstructions = findViewById(R.id.intro_instructions);

        if (prefs.getBoolean(GOT_IT_PREF, false)) {
            gotItButton.setVisibility(View.GONE);
            introInstructions.setVisibility(View.GONE);
        } else {
            gotItButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    gotItButton.setVisibility(View.GONE);
                    introInstructions.setVisibility(View.GONE);
                    prefs.edit().putBoolean(GOT_IT_PREF, true).apply();
                }
            });
        }

        if (prefs.getBoolean(FIRST_RUN_PREF, true)) {
            // delay the intro instructions so people see the main screen first
            panicButton.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(MainActivity.this, TestActivity.class));
                }
            }, 2000);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        enabledResponders = PanicTrigger.getEnabledResponders(this);
        respondersThatCanConnect = PanicTrigger.getRespondersThatCanConnect(this);

        // sort enabled first, then disabled
        LinkedHashSet<String> a = new LinkedHashSet<String>(enabledResponders);
        LinkedHashSet<String> b = new LinkedHashSet<String>(PanicTrigger.getAllResponders(this));
        b.removeAll(enabledResponders);
        a.addAll(b);
        responders = a.toArray(new String[a.size()]);

        PackageManager pm = getPackageManager();
        appLabelList = new ArrayList<CharSequence>(responders.length);
        iconList = new ArrayList<Drawable>(responders.length);
        for (String packageName : responders) {
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
                String packageName = responders[position];
                boolean canConnect = respondersThatCanConnect.contains(packageName);
                holder.setupForApp(
                        packageName,
                        iconList.get(position),
                        appLabelList.get(position),
                        canConnect);
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
                Intent intent = new Intent(this, TestActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == CONNECT_RESULT) {
            PanicTrigger.addConnectedResponder(this, requestPackageName);
        }
    }

    class AppRowHolder extends RecyclerView.ViewHolder {

        private final View.OnClickListener onClickListener;
        private final SwitchCompat onSwitch;
        private final TextView editableLabel;
        private final ImageView iconView;
        private final TextView appLabelView;
        private String rowPackageName;

        AppRowHolder(final View row) {
            super(row);

            iconView = (ImageView) row.findViewById(R.id.iconView);
            appLabelView = (TextView) row.findViewById(R.id.appLabel);
            editableLabel = (TextView) row.findViewById(R.id.editableLabel);
            onSwitch = (SwitchCompat) row.findViewById(R.id.on_switch);
            onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    requestPackageName = rowPackageName;
                    Intent intent = new Intent(Panic.ACTION_CONNECT);
                    intent.setPackage(requestPackageName);
                    // TODO add TrustedIntents here
                    startActivityForResult(intent, CONNECT_RESULT);
                }
            };

            onSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean enabled) {
                    setEnabled(enabled);
                    if (enabled) {
                        PanicTrigger.enableResponder(getBaseContext(), rowPackageName);
                    } else {
                        PanicTrigger.disableResponder(getBaseContext(), rowPackageName);
                    }
                }
            });
        }

        void setEnabled(boolean enabled) {
            if (enabled) {
                editableLabel.setVisibility(View.VISIBLE);
                appLabelView.setEnabled(true);
                iconView.setEnabled(true);
                iconView.setColorFilter(null);
            } else {
                editableLabel.setVisibility(View.GONE);
                appLabelView.setEnabled(false);
                iconView.setEnabled(false);
                // grey out app icon when disabled
                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation(0);
                ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
                iconView.setColorFilter(filter);
            }
        }

        void setupForApp(String packageName, Drawable icon, CharSequence appLabel, boolean editable) {
            this.rowPackageName = packageName;
            iconView.setImageDrawable(icon);
            appLabelView.setText(appLabel);
            if (editable) {
                iconView.setOnClickListener(onClickListener);
                appLabelView.setOnClickListener(onClickListener);
                editableLabel.setOnClickListener(onClickListener);
                editableLabel.setText(R.string.edit);
                editableLabel.setTypeface(null, Typeface.BOLD);
                if (Build.VERSION.SDK_INT >= 14)
                    editableLabel.setAllCaps(true);
            } else {
                iconView.setOnClickListener(null);
                appLabelView.setOnClickListener(null);
                editableLabel.setOnClickListener(null);
                editableLabel.setText(R.string.app_hides);
                editableLabel.setTypeface(null, Typeface.NORMAL);
                if (Build.VERSION.SDK_INT >= 14)
                    editableLabel.setAllCaps(false);
            }
            boolean enabled = enabledResponders.contains(packageName);
            onSwitch.setChecked(enabled);
            setEnabled(enabled);
        }
    }
}
