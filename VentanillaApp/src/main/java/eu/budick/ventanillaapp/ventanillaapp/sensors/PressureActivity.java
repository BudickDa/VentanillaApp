package eu.budick.ventanillaapp.ventanillaapp.sensors;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import eu.budick.ventanillaapp.ventanillaapp.R;
import eu.budick.ventanillaapp.ventanillaapp.sensors.SensorService.SensorLocalBinder;

public class PressureActivity extends Activity {

    private TextView pressureId;
    private TextView altitudeId;

    private Handler marshalHandler = new Handler();
    private ScheduledExecutorService scheduleTaskExecutor = Executors.newScheduledThreadPool(5);

    private SensorService sensorService;
    boolean isBound = false;

    /*Bind to service*/
    private ServiceConnection  myConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            try {
                SensorLocalBinder b = (SensorLocalBinder) service;
                sensorService = b.getService();
                isBound = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pressure);

        altitudeId = ((TextView) findViewById(R.id.altitude));
        pressureId = ((TextView) findViewById(R.id.airpressure));

        try {
            Intent intent = new Intent(this, SensorService.class);
            bindService(intent, myConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onStart() {
        super.onStart();
    }

    protected void onResume() {
        super.onResume();
        refreshPressure();
    }

    public void refreshPressure()
    {
        scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                try {
                    if (sensorService != null) {
                        marshalHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    pressureId.setText((int)sensorService.getSensorValues(Sensor.TYPE_PRESSURE)[0]+" hPa");
                                    altitudeId.setText((int)sensorService.getSensorValues(Sensor.TYPE_PRESSURE)[1]+" m");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }else{
                        marshalHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    pressureId.setText("no Values");
                                    altitudeId.setText("no Values");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                    }
                } catch(Exception e) {
                    System.out.println(e);
                }
            }
        }, 0, 2, TimeUnit.SECONDS);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.pressure, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    /*Helper*/
    private String getAltitude(float pressure) {
        float alt = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressure);
        return Float.toString(alt);
    }

    private String getPressure(float pressure){
        return Float.toString(pressure);
    }



}
