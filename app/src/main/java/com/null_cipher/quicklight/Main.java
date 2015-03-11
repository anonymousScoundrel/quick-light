package com.null_cipher.quicklight;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


public class Main extends Activity {

    /** The Camera object. */
    private Camera camera = null;

    /** The Parameters object which holds the Camera object's parameters. */
    private Parameters params = null;

    /** Flag which holds the state of the Camera's light. */
    private boolean isLightOn = false;

    /** Flag which holds the state of the handler which gets the strobeRunner callback. */
    private boolean isStrobeOn = false;

    /** The callback method used to turn on and off the Camera's light. */
    private Runnable strobeRunner = null;

    /** The handler which calls the strobeRunner callback at a user specified interval. */
    private Handler handler = null;

    /** The default time, in milliseconds, between each invocation of the strobeRunner method. */
    public int interval;

    /** The maximum selectable interval for the NumberPicker object to display. */
    public int maxInterval;

    /** The scrollable object allowing users to select a different default interval value. */
    private NumberPicker picker = null;

    /** The object allowing users to turn on and off the Camera's light. */
    private ToggleButton torchToggle = null;

    /** The object allowing users to turn on and off the strobe effect. */
    private ToggleButton strobeToggle = null;

    /** The object where application preferences are set and gotten from. */
    private SharedPreferences prefs = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getLightPrefs();
        setEnv();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent intent;
        switch (id) {
            case (R.id.action_about):
                intent = new Intent(Main.this, About.class);
                startActivity(intent);
                break;
            case (R.id.action_settings):
                intent = new Intent(Main.this, Settings.class);
                startActivity(intent);
                break;
            default:

        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onPause() {
        super.onPause();
        if (Main.this.camera != null) {
            if (Main.this.isStrobeOn) {
                Main.this.killStrobe();
            }
            Main.this.camera.release();
            Main.this.camera = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getLightPrefs();
        if (Main.this.camera == null) {
            setEnv();
        }
    }

//    This method can be used to handle any actions needed on orientation transition
//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        setContentView(R.layout.activity_main);
//    }

    /**
     * Tries to invoke the Camera object's open method. Releases the Camera and returns true if it
     * doesn't crash. Catches and returns false otherwise.
     * @return True or false.
     */
    public boolean isCameraReady() {
        Camera camera = null;
        try {
            camera = Camera.open();
        } catch (RuntimeException err) {
            return false;
        } finally {
            if (camera != null) {
                camera.release();
            }
        }
        return true;
    }

    public void getLightPrefs() {
        if (Main.this.prefs == null) {
            Main.this.prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        }
        Main.this.interval = Main.this.prefs.getInt("interval", 75);
        Main.this.maxInterval = Main.this.prefs.getInt("maxInterval", 3000);
    }

    public void setEnv() {
        if (Main.this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            if (isCameraReady()) {
                Main.this.camera = Camera.open();
                Main.this.params = Main.this.camera.getParameters();
                Main.this.torchToggle = (ToggleButton) findViewById(R.id.quick_light_torch_button);
                Main.this.strobeToggle = (ToggleButton) findViewById(R.id.quick_light_strobe_button);
            } else {
                TextView warning = (TextView) findViewById(R.id.warnings);
                warning.setText("Light is unavailable.\nCamera is engaged by another application.");
            }
        }
        if (Main.this.picker == null) {
            Main.this.picker = (NumberPicker) findViewById(R.id.strobe_interval_picker);
            Main.this.picker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker pick, int oldVal, int newVal) {
                    Main.this.interval = newVal;
                }
            });
        }
        Main.this.picker.setMaxValue(Main.this.maxInterval);
        Main.this.picker.setMinValue(1);
        Main.this.picker.setValue(Main.this.interval);

    }

    public void onTorchToggle(View view) {
        if (Main.this.isStrobeOn) {
            Main.this.killStrobe();
        }
        if (Main.this.isLightOn) {
            Main.this.isLightOn = false;
            Main.this.params.setFlashMode(Parameters.FLASH_MODE_OFF);
            Main.this.camera.setParameters(Main.this.params);
        } else {
            Main.this.isLightOn = true;
            Main.this.params.setFlashMode(Parameters.FLASH_MODE_TORCH);
            Main.this.camera.setParameters(Main.this.params);
        }
    }

    public void strobeHelper() {
        if (Main.this.isLightOn) {
            Main.this.isLightOn = false;
            Main.this.params.setFlashMode(Parameters.FLASH_MODE_OFF);
            Main.this.camera.setParameters(Main.this.params);
        } else {
            Main.this.isLightOn = true;
            Main.this.params.setFlashMode(Parameters.FLASH_MODE_TORCH);
            Main.this.camera.setParameters(Main.this.params);
        }
    }

    private void killStrobe() {
        Main.this.isStrobeOn = false;
        Main.this.handler.removeCallbacks(Main.this.strobeRunner);
        Main.this.strobeToggle.setChecked(false);
        Main.this.params.setFlashMode(Parameters.FLASH_MODE_OFF);
        Main.this.camera.setParameters(Main.this.params);
        Main.this.isLightOn = false;
    }

    public void onStrobeToggle(View view) {
        if (Main.this.isStrobeOn) {
            Main.this.killStrobe();
        } else {
            Main.this.isStrobeOn = true;
            Main.this.torchToggle.setChecked(false);
            if (Main.this.strobeRunner == null) {
                Main.this.strobeRunner = new Runnable() {
                    @Override
                    public void run() {
                        Main.this.strobeHelper();
                        Main.this.handler.postDelayed(Main.this.strobeRunner, Main.this.interval);
                    }
                };
                if (Main.this.handler == null) {
                    Main.this.handler = new Handler();
                }
                Main.this.handler.postDelayed(Main.this.strobeRunner, Main.this.interval);
            } else {
                Main.this.handler.postDelayed(Main.this.strobeRunner, Main.this.interval);
            }
        }
    }

    /**
     *
     * @param str The string which the Toast will display.
     */
    private void log(String str) {
        Toast.makeText(Main.this, str, Toast.LENGTH_SHORT).show();
    }
}

/*
 * TODO:
 *  Find out if android already has a todo file
 *  Clean up and comment code base
 *  Build website to allow donations (and look decent)
 *  How to allow donations, see previous
 *  Add help activity with better documentation
 *  Add light as a service so user can open other applications while light is active
 *  Morse code feature?
 *  Move number picker to dialog box?
 *  Add support back to G2?
 */