package pl.cntrpl.beatsaverdl;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.CheckBox;

public class MainActivity extends Activity {

    CheckBox openLanCheckbox = null;

    final String PREF_OPEN_TO_LAN = "bsdl.open_lan";

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the content view for the activity
        setContentView(R.layout.activity_main);
        final SharedPreferences prefs = getSharedPreferences(getPackageName() + ".prefs", Context.MODE_PRIVATE);

        openLanCheckbox = findViewById(R.id.chkbox_openlan);
        openLanCheckbox.setChecked(prefs.getBoolean(PREF_OPEN_TO_LAN, false));

        openLanCheckbox.setOnCheckedChangeListener((a,b) -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(PREF_OPEN_TO_LAN, b);
            editor.apply();
        });

    }
}
