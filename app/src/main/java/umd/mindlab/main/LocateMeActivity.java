package umd.mindlab.main;

import umd.mindlab.objects.WifiReceiver;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LocateMeActivity extends Activity implements SensorEventListener{
   private static final String TAG = "LocateMeActivity";
   public WifiManager wifi;
   BroadcastReceiver receiver;
   public String xml;
   public String address;
   private SensorManager SM;
   private Sensor laccSensor;
   private Sensor gyroSensor;
   private Sensor gravSensor,acceleroSensor,magneticSensor,baroSensor;
   TextView textStatus;
   TextView laccStatus,gpsloc,gyroStatus,gravStatus,acceleroStatus,magStatus,baroStatus;
   Button update;
   //Button verify;

   LocationManager locman;
   LocationListener loclist;
   Location currentLocation;

   public static int count = 0;

   String path=Environment.getExternalStorageDirectory().getPath()+"/datas";
   File accelero_file,lacc_file,gyro_file,grav_file,magn_file,baro_file;
   FileWriter accelero_W,lacc_W,gyro_W,grav_W,magn_W,baro_W;

   public void onCreate(Bundle savedInstanceState) {
      count = 0;
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);
      if (receiver == null) {
         wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
         receiver = new WifiReceiver(this);
         registerReceiver(receiver, new IntentFilter(
               WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
      }
      SM = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
      laccSensor = SM.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
      gyroSensor = SM.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
      gravSensor = SM.getDefaultSensor(Sensor.TYPE_GRAVITY);
      acceleroSensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
      magneticSensor = SM.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
      baroSensor = SM.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
      SM.registerListener(this,laccSensor,SensorManager.SENSOR_DELAY_NORMAL);
      SM.registerListener(this,gyroSensor,SensorManager.SENSOR_DELAY_NORMAL);
      SM.registerListener(this,gravSensor,SensorManager.SENSOR_DELAY_NORMAL);
      SM.registerListener(this,acceleroSensor,SensorManager.SENSOR_DELAY_NORMAL);
      SM.registerListener(this,magneticSensor,SensorManager.SENSOR_DELAY_NORMAL);
      SM.registerListener(this,baroSensor,SensorManager.SENSOR_DELAY_NORMAL);
      gpsloc = (TextView) findViewById(R.id.gpslocation);
      // Acquire a reference to the system Location Manager
      LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

      if ( !locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
         buildAlertMessageNoGps();
      }

      // Define a listener that responds to location updates
      loclist = new LocationListener() {
         public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location provider.
//          Toast.makeText(
//                LocateMeActivity.this,
//                location.toString(),
//                Toast.LENGTH_LONG).show();
            gpsloc.setText("Lat: "+location.getLatitude()+"\n Long: "+location.getLongitude());
         }

         public void onStatusChanged(String provider, int status, Bundle extras) {}

         public void onProviderEnabled(String provider) {}

         public void onProviderDisabled(String provider) {}
      };

// Register the listener with the Location Manager to receive location updates
      locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, loclist);
   }

   private void buildAlertMessageNoGps() {
      final AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setMessage("GPS is disabled, please enable gps")
            .setCancelable(false)
            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
               public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                  startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
               }
            });
      final AlertDialog alert = builder.create();
      alert.show();
   }

   /** Called when the activity is first created. */
   @Override
   public void onStart() {
      super.onStart();
      if (receiver == null) {
         wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
         receiver = new WifiReceiver(this);
         registerReceiver(receiver, new IntentFilter(
               WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
      }

      laccStatus = (TextView) findViewById(R.id.accCoord);
      gyroStatus = (TextView) findViewById(R.id.gyroCoord);
      gravStatus = (TextView) findViewById(R.id.gravStatus);
      magStatus = (TextView) findViewById(R.id.magStatus);
      acceleroStatus = (TextView) findViewById(R.id.accelero);
      baroStatus = (TextView) findViewById(R.id.barStatus);
      update = (Button) findViewById(R.id.update);
      update.setOnClickListener(new View.OnClickListener() {
         public void onClick(View v) {
            if (count > 0) {
               count++;
               Toast.makeText(
                     LocateMeActivity.this,
                     "Currently, on " + count
                           + " iteration, cannot start another scan",
                     Toast.LENGTH_LONG).show();
            } else {
               Toast.makeText(LocateMeActivity.this,
                     "Scan in progress...", Toast.LENGTH_LONG).show();
               System.out.println("here....\n\n");
               setUp();
            }
         }
      });

      File dir = new File(path);
      dir.mkdirs();
      accelero_file = new File(path,"Accelero_data.csv");
      lacc_file = new File(path,"linear_acc_data.csv");
      gyro_file = new File(path,"gyro_data.csv");
      magn_file = new File(path,"magn_data.csv");
      grav_file = new File(path,"grav_data.csv");
      baro_file = new File(path,"baro_data.csv");

      try {
         accelero_W = new FileWriter(accelero_file);
         lacc_W = new FileWriter(lacc_file);
         gyro_W = new FileWriter(gyro_file);
         magn_W = new FileWriter(magn_file);
         grav_W = new FileWriter(grav_file);
         baro_W = new FileWriter(baro_file);

         accelero_W.append("date_time,X,Y,Z\n");
         lacc_W.append("date_time,X,Y,Z\n");
         gyro_W.append("date_time,X,Y,Z\n");
         magn_W.append("date_time,X,Y,Z\n");
         grav_W.append("date_time,X,Y,Z\n");

         accelero_W.flush();
         lacc_W.flush();
         gyro_W.flush();
         magn_W.flush();
         grav_W.flush();
      } catch (IOException e) {
         e.printStackTrace();
      }



      /*verify = (Button) findViewById(R.id.changeActivity);
      verify.setOnClickListener(new View.OnClickListener() {

         public void onClick(View v) {
            if (count > 0) {
               count++;
               Toast.makeText(
                     LocateMeActivity.this,
                     "Currently, on "
                           + count
                           + " iteration, wait for scanning to complete...",
                     Toast.LENGTH_SHORT).show();
            } else {
               Toast.makeText(LocateMeActivity.this, "Switching Activity",
                     Toast.LENGTH_SHORT).show();
               Intent myIntent = new Intent(v.getContext(),
                     GiveFeedback.class);
               Bundle xmlBundle = new Bundle();
               xmlBundle.putString("xml", xml);
               myIntent.putExtras(xmlBundle);
               startActivityForResult(myIntent, 0);
            }
         }
      });       */




   }

   protected void onResume() {
      super.onResume();
      if (receiver == null) {
         wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
         receiver = new WifiReceiver(this);
         registerReceiver(receiver, new IntentFilter(
               WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
      }
   }

   @Override
   public void onStop() {
      count = 0;
      super.onStop();
   }

   public void onDestroy() {
      if (receiver != null) {
         unregisterReceiver(receiver);
      }
      //SM.unregisterListener((SensorEventListener) accSensor);
      //SM.unregisterListener((SensorEventListener) gyroSensor);
      super.onDestroy();
      try {
         accelero_W.close();
         lacc_W.close();
         magn_W.close();
         gyro_W.close();
         grav_W.close();
         baro_W.close();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   public void onPause() {
      count = 0;
      super.onPause();
   }

   protected void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
   }

   /*protected void onRestoreInstanceState(Bundle savedInstanceState) {
      System.out.println("In restore");
      onCreate(savedInstanceState);
   }*/

   private void setUp() {
      if (!isConnected(getApplicationContext())) {
         Toast.makeText(
               this,
               "You are not connected to the internet, adjust your settings!",
               Toast.LENGTH_LONG).show();
         finish();
      } else {
         wifi.startScan();
         //Log.v(TAG, currentLocation + "");
         Toast.makeText(this, "Click locate to refresh results",
               Toast.LENGTH_LONG).show();
      }
   }

   private static boolean isConnected(Context context) {
      ConnectivityManager connectivityManager = (ConnectivityManager) context
            .getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo networkInfo = null;
      if (connectivityManager != null) {
         networkInfo = connectivityManager
               .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
      }
      return networkInfo == null ? false : networkInfo.isConnected();
   }

   DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

   @Override
   public void onSensorChanged(SensorEvent sensorEvent) {

      Calendar rightNow = Calendar.getInstance();

      if(sensorEvent.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
         acceleroStatus.setText("Accelerometer (m/s^2)\nX:" + String.format("%.1f", sensorEvent.values[0])+
                 "\nY:" + String.format("%.1f", sensorEvent.values[1])+
                 "\nZ:" + String.format("%.1f", sensorEvent.values[2]));

         String string = dateFormat.format(rightNow.getTime())+
               ","+String.format("%.1f", sensorEvent.values[0])+
               "," + String.format("%.1f", sensorEvent.values[1])+
               "," + String.format("%.1f", sensorEvent.values[2])+"\n";

//         Toast.makeText(this, dateFormat.format(rightNow.getTimeInMillis())+"",
//                 Toast.LENGTH_LONG).show();

         try {

            accelero_W.append(string);
            accelero_W.flush();

         } catch (Exception e) {
            e.printStackTrace();
         }
      }
      if(sensorEvent.sensor.getType()==Sensor.TYPE_LINEAR_ACCELERATION) {
         laccStatus.setText("Linear Acceleration (m/s^2)\nX:" + String.format("%.1f", sensorEvent.values[0])+
               "\nY:" + String.format("%.1f", sensorEvent.values[1])+
               "\nZ:" + String.format("%.1f", sensorEvent.values[2]));

         String string = dateFormat.format(rightNow.getTime())+
                 ","+String.format("%.1f", sensorEvent.values[0])+
                 "," + String.format("%.1f", sensorEvent.values[1])+
                 "," + String.format("%.1f", sensorEvent.values[2])+"\n";

         try {

            lacc_W.append(string);
            lacc_W.flush();

         } catch (Exception e) {
            e.printStackTrace();
         }
      }
      if(sensorEvent.sensor.getType()==Sensor.TYPE_GYROSCOPE){
         gyroStatus.setText("Rotation rate (rad/s)\nX:" + String.format("%.1f", sensorEvent.values[0])+
               "\nY:" + String.format("%.1f", sensorEvent.values[1])+
               "\nZ:" + String.format("%.1f", sensorEvent.values[2]));

         String string = dateFormat.format(rightNow.getTime())+
                 ","+String.format("%.1f", sensorEvent.values[0])+
                 "," + String.format("%.1f", sensorEvent.values[1])+
                 "," + String.format("%.1f", sensorEvent.values[2])+"\n";

         try {

            gyro_W.append(string);
            gyro_W.flush();

         } catch (Exception e) {
            e.printStackTrace();
         }
      }
      if(sensorEvent.sensor.getType()==Sensor.TYPE_GRAVITY){
         gravStatus.setText("Gravity (m/s^2)\nX:" + String.format("%.1f", sensorEvent.values[0])+
               "\nY:" + String.format("%.1f", sensorEvent.values[1])+
               "\nZ:" + String.format("%.1f", sensorEvent.values[2]));

         String string = dateFormat.format(rightNow.getTime())+
                 ","+String.format("%.1f", sensorEvent.values[0])+
                 "," + String.format("%.1f", sensorEvent.values[1])+
                 "," + String.format("%.1f", sensorEvent.values[2])+"\n";

         try {

            grav_W.append(string);
            grav_W.flush();

         } catch (Exception e) {
            e.printStackTrace();
         }
      }
      if(sensorEvent.sensor.getType()==Sensor.TYPE_MAGNETIC_FIELD) {
         magStatus.setText("Magnetic Field (uT)\nX:" + String.format("%.1f", sensorEvent.values[0]) +
               "\nY:" + String.format("%.1f", sensorEvent.values[1]) +
               "\nZ:" + String.format("%.1f", sensorEvent.values[2]));

         String string = dateFormat.format(rightNow.getTime())+
                 ","+String.format("%.1f", sensorEvent.values[0])+
                 "," + String.format("%.1f", sensorEvent.values[1])+
                 "," + String.format("%.1f", sensorEvent.values[2])+"\n";

         try {

            magn_W.append(string);
            magn_W.flush();

         } catch (Exception e) {
            e.printStackTrace();
         }
      }
      if(sensorEvent.sensor.getType()==Sensor.TYPE_RELATIVE_HUMIDITY){
         baroStatus.setText("Air humidity (%)\n" + String.format("%.1f", sensorEvent.values[0]));
      }

   }

   @Override
   public void onAccuracyChanged(Sensor sensor, int i) {

   }
}
