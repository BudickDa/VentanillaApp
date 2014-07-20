package eu.budick.ventanillaapp.ventanillaapp.sensors;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class SensorService extends Service {
    private SensorManager mSensorManager = null;
    private ServiceHandler mServiceHandler;
    private final IBinder mBinder = new SensorLocalBinder();
    int mStartMode;
    boolean mAllowRebind;

    /*Sensordata*/
    private HashMap values = new HashMap();

    public SensorService() {
    }


    @Override
    public void onCreate() {
        /*try {
            mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
            sensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        mServiceHandler = new ServiceHandler(thread.getLooper());


        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        regSensorListener(Sensor.TYPE_PRESSURE);
        regSensorListener(Sensor.TYPE_AMBIENT_TEMPERATURE);
        regSensorListener(Sensor.TYPE_RELATIVE_HUMIDITY);
        regSensorListener(Sensor.TYPE_LIGHT);
        regSensorListener(Sensor.TYPE_GRAVITY);
        regSensorListener(Sensor.TYPE_MAGNETIC_FIELD);
    }

    private void regSensorListener(int sensor){
        if (mSensorManager.getDefaultSensor(sensor) != null) {
            try {
                mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(sensor), SensorManager.SENSOR_DELAY_NORMAL);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        // Unregister listener
        this.mSensorManager.unregisterListener(mSensorListener);
    }


    private SensorEventListener mSensorListener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            Sensor sensor = event.sensor;
            if (sensor.getType() == Sensor.TYPE_PRESSURE) {
                try {
                    values.put(Sensor.TYPE_PRESSURE, event.values);
                    sendToServer(getSensorValues(Sensor.TYPE_PRESSURE)[0],"Pressure");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if (sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                try {
                    values.put(Sensor.TYPE_AMBIENT_TEMPERATURE, event.values);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else if (sensor.getType() == Sensor.TYPE_RELATIVE_HUMIDITY) {
                try {
                    values.put(Sensor.TYPE_RELATIVE_HUMIDITY, event.values);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if (sensor.getType() == Sensor.TYPE_GRAVITY) {
                try {
                    values.put(Sensor.TYPE_GRAVITY, event.values);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if (sensor.getType() == Sensor.TYPE_LIGHT) {
                try {
                    values.put(Sensor.TYPE_LIGHT, event.values);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                try {
                    values.put(Sensor.TYPE_MAGNETIC_FIELD, event.values);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };



    public  float[] getSensorValues(int sensor) {
        try {
           return (float[]) values.get(sensor);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new float[]{0};
    }

    /*
    //Mittleren Fehler finden und elminieren
    public void elError(float[] values){
        for(int index = 0; index<= values.length; index++){
            if(tmpError[index].length<=15){
                tmpError[index][tmpError[index].length] = values[index];
            }
            else{
                float f = 0;
                int l = tmpError[index].length;
                for(int i = 0; i<=l; i++){
                    f+=tmpError[index][i];
                }
                try {
                    tmpError[index] = new float[15];
                    values[index] = f/l;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        sensorValues = values;
    }
    */

    /*Servicebinding stuff...*/

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        // Register listener

        return mBinder;
    }

    public boolean onUnbind(Intent intent) {
        // All clients have unbound with unbindService()
        return mAllowRebind;
    }

    /*@Override
    public void onRebind(Intent intent) {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
    }*/



    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {


            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            stopSelf(msg.arg1);
        }
    }

    public class SensorLocalBinder extends Binder {
        SensorService getService() {
            return SensorService.this;
        }
    }

    public void sendToServer(float value, String name){
        //postData(name,value);
        try {
            JSONObject data = new JSONObject();
            data.put("name", name);
            data.put("value", (long) value);
            data.put("key", 1234);
            JSONObject header = new JSONObject();
            header.put("deviceType","Android"); // Device type
            header.put("deviceVersion","2.0"); // Device OS version
            header.put("language", "es-es");	// Language of the Android client
            data.put("header", header);
            String dataString = data.toString();
            new SendData().execute(dataString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

class SendData extends AsyncTask<String, Void, Boolean> {
    @Override
    protected Boolean doInBackground(String... data) {
        try {
            HttpClient httpclient = new DefaultHttpClient();
            //Laptop
            HttpPost httppost = new HttpPost("http://192.168.178.39:3000/api");
            //Win7 PC
            //HttpPost httppost = new HttpPost("http://192.168.178.28:3000/api");

            try {
                StringEntity se = new StringEntity(data[0]);
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                httppost.setEntity(se);
                HttpResponse response = httpclient.execute(httppost);

                /*Checking response */
                if(response!=null){
                    InputStream in = response.getEntity().getContent(); //Get the data in the entity
                }

            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
            } catch (IOException e) {
                // TODO Auto-generated catch block
            }
        } catch (Exception e) {

        }
        return true;
    }
}
