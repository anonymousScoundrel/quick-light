package com.null_cipher.quicklight;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Set;

public class Settings extends Activity {

    private SharedPreferences prefs;
    private EditText interval;
    private EditText maxInterval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Settings.this.interval = (EditText) findViewById(R.id.def_strobe_interval_val);
        Settings.this.maxInterval = (EditText) findViewById(R.id.def_strobe_max_val);
        Settings.this.prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Settings.this.interval.setText(Integer.toString(Settings.this.prefs.getInt("interval", 75)));
        Settings.this.maxInterval.setText(Integer.toString(Settings.this.prefs.getInt("maxInterval", 3000)));
        Settings.this.interval.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "99999")});
        Settings.this.maxInterval.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "99999")});
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = Settings.this.prefs.edit();
        editor.putInt("interval", Integer.valueOf(Settings.this.interval.getText().toString()));
        editor.putInt("maxInterval", Integer.valueOf(Settings.this.maxInterval.getText().toString()));
        editor.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(Settings.this, About.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    public void savePrefs(View view) {
        int def = Integer.valueOf(Settings.this.interval.getText().toString());
        int max = Integer.valueOf(Settings.this.maxInterval.getText().toString());
        boolean incorrectValues = def > max;

        if (incorrectValues) {
            Toast.makeText(Settings.this, R.string.incorrect_interval_assignment, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(Settings.this, R.string.save_button_click, Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
