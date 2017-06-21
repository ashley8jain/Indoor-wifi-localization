package umd.mindlab.main;

import umd.mindlab.objects.SendWifiInfoTask;
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
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.http.AndroidHttpClient;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
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
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class LocateMeActivity extends AppCompatActivity implements SensorEventListener{

   final long maxSizeFile = 4*1024*1024;
   long wifiInterval = 20*1000;
   long gpsInterval = 300*1000;
   int pedo_steps = 3;

   private static final String TAG = "LocateMeActivity";
   private static final int BUFFER_SIZE = 10*1024*1024;
   public static WifiManager wifi;
   BroadcastReceiver receiver;
   public TelephonyManager telephonyManager;
   public static String deviceID;
   public String xml;
   public String address;
   private SensorManager SM;
   private Sensor laccSensor,gyroSensor,gravSensor,acceleroSensor,magneticSensor,baroSensor,step_counter;
   private SensorEventListener SEL;
   String strr;
   TextView textStatus,timerText;
   TextView laccStatus,gpsloc,gyroStatus,gravStatus,acceleroStatus,magStatus,baroStatus;
   Button update;
   ImageButton mapp;
   Button start;
   Context context;
   Handler handler;
   int Hours, Seconds, Minutes, MilliSeconds;
   long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L ;
   boolean start_wifiscan=false;
   //Button verify;

   LocationManager locationManager;
   LocationListener loclist;

   public static int count = 0;
   private Timer myTimer,fileSizeTimer;

   String path=Environment.getExternalStorageDirectory().getPath()+"/datas/csv";
   File accelero_file,lacc_file,gyro_file,grav_file,magn_file,baro_file;
   File gps_file,xml_file;
   FileWriter accelero_W,lacc_W,gyro_W,grav_W,magn_W,baro_W;
   FileWriter gps_W;
   public FileWriter xml_W;

   public DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

   public void onCreate(Bundle savedInstanceState) {

      count = 0;
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);
      Log.v("OnCreate","OnCreate");

      context = this;
      SEL = this;
      telephonyManager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);

      start = (Button) findViewById(R.id.start);
      start.setText("Start");

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
      step_counter = SM.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
      SM.registerListener(this,laccSensor,SensorManager.SENSOR_DELAY_NORMAL);
      SM.registerListener(this,gyroSensor,SensorManager.SENSOR_DELAY_NORMAL);
      SM.registerListener(this,gravSensor,SensorManager.SENSOR_DELAY_NORMAL);
      SM.registerListener(this,acceleroSensor,SensorManager.SENSOR_DELAY_NORMAL);
      SM.registerListener(this,magneticSensor,SensorManager.SENSOR_DELAY_NORMAL);
      SM.registerListener(this,baroSensor,SensorManager.SENSOR_DELAY_NORMAL);
      SM.registerListener(this,step_counter,SensorManager.SENSOR_DELAY_NORMAL);
//      gpsloc = (TextView) findViewById(R.id.gpslocation);
      // Acquire a reference to the system Location Manager
      locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

      if ( !locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
         buildAlertMessageNoGps();
      }

//      myTimer = new Timer();
//      myTimer.schedule(new TimerTask() {
//         @Override
//         public void run() {
//            try {
//               sendGPS();
//               Log.v(TAG,"timer");
//            } catch (IOException e) {
//               e.printStackTrace();
//            }
//         }
//
//      }, 0, 600000);

      // Define a listener that responds to location updates
      loclist = new LocationListener() {
         public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location provider.
            Log.v("gps","gpssss");
            Calendar rightNow = Calendar.getInstance();
//            String str="";
            strr="";

            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(context, Locale.getDefault());
            strr+=dateFormat.format(rightNow.getTime());

//            str+="Lat: "+location.getLatitude()+"\n Long: "+location.getLongitude();
            strr+=","+location.getLatitude()+","+location.getLongitude();
//            str+="\n Altitude: "+location.getAltitude();
            strr+=","+location.getAltitude()+",";
            try{
               addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
               String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

               if(address!=null){
//                  str+="\n Address: "+address;
                  strr+=address+" ";
               }

               String city = addresses.get(0).getLocality();
//               str+="\n City: "+city;
               String state = addresses.get(0).getAdminArea();
//               str+="\n State: "+state;
               String country = addresses.get(0).getCountryName();
//               str+="\n Country: "+country;
               String postalCode = addresses.get(0).getPostalCode();
//               str+="\n Postal code: "+postalCode;

               strr+=city+" "+state+" "+country+" "+postalCode+"\n";

            } catch (IOException e) {
               e.printStackTrace();
            }

            //gpsloc.setText(str);

            try {
               if(gps_W!=null){
                  gps_W.append(strr);
                  gps_W.flush();
               }
            } catch (IOException e) {
            }
         }

         public void onStatusChanged(String provider, int status, Bundle extras) {}

         public void onProviderEnabled(String provider) {}

         public void onProviderDisabled(String provider) {}
      };

      locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,gpsInterval,0,loclist);

      mapp = (ImageButton) findViewById(R.id.other);

      deviceID = telephonyManager.getDeviceId();
      timerText = (TextView) findViewById(R.id.timer);

      File dir = new File(path);
      dir.mkdirs();
      accelero_file = new File(path,"Accelero_data.csv");
      lacc_file = new File(path,"linear_acc_data.csv");
      gyro_file = new File(path,"gyro_data.csv");
      magn_file = new File(path,"magn_data.csv");
      grav_file = new File(path,"grav_data.csv");
      baro_file = new File(path,"baro_data.csv");
      xml_file = new File(path,"xml.csv");
      gps_file = new File(path,"gps.csv");

      mapp.setOnClickListener(new View.OnClickListener(){
         @Override
         public void onClick(View v) {
            Intent intent = new Intent(context,map.class);
            startActivity(intent);
         }
      });

      handler = new Handler();

      final boolean[] start_B = {false};
      start.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            if(!start_B[0]) {
               start_B[0] =true;
               start_wifiscan=true;
               start.setText("Stop");

               StartTime = SystemClock.uptimeMillis();
               handler.postDelayed(runnable, 0);

               final File dir = new File(Environment.getExternalStorageDirectory().getPath()+"/datas");

               try {
                  accelero_W = new FileWriter(accelero_file);
                  lacc_W = new FileWriter(lacc_file);
                  gyro_W = new FileWriter(gyro_file);
                  magn_W = new FileWriter(magn_file);
                  grav_W = new FileWriter(grav_file);
                  baro_W = new FileWriter(baro_file);
                  gps_W = new FileWriter(gps_file);
                  xml_W = new FileWriter(xml_file);

                  myTimer = new Timer();
                  myTimer.schedule(new TimerTask() {
                     @Override
                     public void run(){
                        Log.v(TAG,"wifiscan");
                        setUp();
                     }
                  }, 0, wifiInterval);

                  accelero_W.append("date_time,X,Y,Z\n");
                  lacc_W.append("date_time,X,Y,Z\n");
                  gyro_W.append("date_time,X,Y,Z\n");
                  magn_W.append("date_time,X,Y,Z\n");
                  grav_W.append("date_time,X,Y,Z\n");
                  baro_W.append("date_time,air humidity(%)\n");
                  gps_W.append("Time,Lat,Long,Altitude,Address\n");
                  xml_W.append("Time,XML_data\n");

                  accelero_W.flush();
                  lacc_W.flush();
                  gyro_W.flush();
                  magn_W.flush();
                  grav_W.flush();
                  gps_W.flush();
                  xml_W.flush();

               } catch (IOException e) {
                  e.printStackTrace();
               }

               fileSizeTimer = new Timer();
               fileSizeTimer.schedule(new TimerTask() {
                  @Override
                  public void run(){

                     Log.v("file size: ",""+dirSize(dir));

                     if(dirSize(dir)>maxSizeFile){
                        try{
                           accelero_W.close();
                           lacc_W.close();
                           magn_W.close();
                           gyro_W.close();
                           grav_W.close();
                           baro_W.close();
                           gps_W.close();
                           xml_W.close();

                           FileOutputStream fileOutputStream;
                           ZipOutputStream zipOutputStream =  null;
                           Log.v(TAG,Environment.getExternalStorageDirectory().getPath());

                           String destination = Environment.getExternalStorageDirectory().getPath()+ "/datas/"+deviceID+".zip";

                           File file = new File(destination);
                           if(!file.exists())
                              file.createNewFile();

                           fileOutputStream = new FileOutputStream(file);
                           zipOutputStream =  new ZipOutputStream(new BufferedOutputStream(fileOutputStream));
                           zipFile(zipOutputStream, path+"/");

                     }catch(IOException e){
                        e.printStackTrace();
                     }

                     //send file
                     (new SendWifiInfoTask(LocateMeActivity.this)).execute(xml);

                        try {
                           accelero_W = new FileWriter(accelero_file);
                           lacc_W = new FileWriter(lacc_file);
                           gyro_W = new FileWriter(gyro_file);
                           magn_W = new FileWriter(magn_file);
                           grav_W = new FileWriter(grav_file);
                           baro_W = new FileWriter(baro_file);
                           gps_W = new FileWriter(gps_file);
                           xml_W = new FileWriter(xml_file);
                           accelero_W.append("date_time,X,Y,Z\n");
                           lacc_W.append("date_time,X,Y,Z\n");
                           gyro_W.append("date_time,X,Y,Z\n");
                           magn_W.append("date_time,X,Y,Z\n");
                           grav_W.append("date_time,X,Y,Z\n");
                           baro_W.append("date_time,air humidity(%)\n");
                           gps_W.append("Time,Lat,Long,Altitude,Address\n");
                           xml_W.append("Time,XML_data\n");
                           accelero_W.flush();
                           lacc_W.flush();
                           gyro_W.flush();
                           magn_W.flush();
                           grav_W.flush();
                           gps_W.flush();
                           xml_W.flush();

                        } catch (IOException e) {
                           e.printStackTrace();
                        }
                     }
                  }
               }, 0, 1200000);
            }
            else{
               start_B[0]=false;
               start_wifiscan=false;

               TimeBuff += MillisecondTime;
               handler.removeCallbacks(runnable);

               myTimer.cancel();fileSizeTimer.cancel();

               MillisecondTime = 0L ;
               StartTime = 0L ;
               TimeBuff = 0L ;
               UpdateTime = 0L ;
               Seconds = 0 ;
               Minutes = 0 ;
               MilliSeconds = 0 ;

               //sensor datas zip
               try{
                  accelero_W.close();
                  lacc_W.close();
                  magn_W.close();
                  gyro_W.close();
                  grav_W.close();
                  baro_W.close();
                  gps_W.close();
                  xml_W.close();

                  FileOutputStream fileOutputStream;
                  ZipOutputStream zipOutputStream =  null;
                  Log.v(TAG,Environment.getExternalStorageDirectory().getPath());

                  String destination = Environment.getExternalStorageDirectory().getPath()+ "/datas/"+deviceID+".zip";

                  File file = new File(destination);
                  if(!file.exists())
                     file.createNewFile();

                  fileOutputStream = new FileOutputStream(file);
                  zipOutputStream =  new ZipOutputStream(new BufferedOutputStream(fileOutputStream));
                  zipFile(zipOutputStream, path+"/");

               }catch(IOException e){
                  e.printStackTrace();
               }

               //send file
               (new SendWifiInfoTask(LocateMeActivity.this)).execute(xml);
//               setUp();
               start.setText("Start");
            }

         }
      });

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
      Log.v("OnStart","OnStart");

      if (receiver == null) {
         wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
         receiver = new WifiReceiver(this);
         registerReceiver(receiver, new IntentFilter(
               WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
      }
//      laccStatus = (TextView) findViewById(R.id.accCoord);
//      gyroStatus = (TextView) findViewById(R.id.gyroCoord);
//      gravStatus = (TextView) findViewById(R.id.gravStatus);
//      magStatus = (TextView) findViewById(R.id.magStatus);
//      acceleroStatus = (TextView) findViewById(R.id.accelero);
//      baroStatus = (TextView) findViewById(R.id.barStatus);
//      update = (Button) findViewById(R.id.update);


//      update.setOnClickListener(new View.OnClickListener() {
//         public void onClick(View v) {
//
//            if (count > 0) {
//               count++;
//               Toast.makeText(
//                     LocateMeActivity.this,
//                     "Currently, on " + count
//                           + " iteration, cannot start another scan",
//                     Toast.LENGTH_LONG).show();
//            } else {
//                  Toast.makeText(LocateMeActivity.this,
//                          "Scan in progress...", Toast.LENGTH_LONG).show();
////                  SM.unregisterListener(SEL,acceleroSensor);
////                  SM.unregisterListener(SEL,gyroSensor);
////                  SM.unregisterListener(SEL,gravSensor);
////                  SM.unregisterListener(SEL,laccSensor);
////                  SM.unregisterListener(SEL,magneticSensor);
////                  SM.unregisterListener(SEL,baroSensor);
//
//
////                  accelero_W.close();
////                  lacc_W.close();
////                  magn_W.close();
////                  gyro_W.close();
////                  grav_W.close();
////                  baro_W.close();
////
////                  FileOutputStream fileOutputStream;
////                  ZipOutputStream zipOutputStream =  null;
////                  Log.v(TAG,Environment.getExternalStorageDirectory().getPath());
////
////                  String destination = Environment.getExternalStorageDirectory().getPath()+ "/"+deviceID+".zip";
////
////                  File file = new File(destination);
////                  if (!file.exists())
////                     file.createNewFile();
////
////                  fileOutputStream = new FileOutputStream(file);
////                  zipOutputStream =  new ZipOutputStream(new BufferedOutputStream(fileOutputStream));
////
////                  zipFile(zipOutputStream, path+"/");
//
//               System.out.println("here....\n\n");
//               wifi.startScan();
//
//            }
//         }
//      });



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
      Log.v("OnResume","OnResume");

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
      Log.v("OnStop","OnStop");
   }

   public void onDestroy() {
      super.onDestroy();
      Log.v("OnDestroy","OnDestroy");

      if (receiver != null) {
         unregisterReceiver(receiver);
      }
      SM.unregisterListener(this,acceleroSensor);
      SM.unregisterListener(this,gyroSensor);
      SM.unregisterListener(this,gravSensor);
      SM.unregisterListener(this,laccSensor);
      SM.unregisterListener(this,magneticSensor);
      SM.unregisterListener(this,baroSensor);
      locationManager.removeUpdates(loclist);

      try {
         if(accelero_W!=null) {
            accelero_W.close();
            lacc_W.close();
            magn_W.close();
            gyro_W.close();
            grav_W.close();
            baro_W.close();
            gps_W.close();
            xml_W.close();
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   public void onPause() {
      count = 0;
      super.onPause();
      Log.v("OnPause","OnPause");

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
//         Toast.makeText(
//               context,
//               "You are not connected to the internet, adjust your settings!",
//               Toast.LENGTH_LONG).show();
         finish();
      } else {
         wifi.startScan();
//         //Log.v(TAG, currentLocation + "");
//         Toast.makeText(context, "Click locate to refresh results",
//               Toast.LENGTH_LONG).show();
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
      return networkInfo == null?false:networkInfo.isConnected();
   }

   float old_steps=0,new_steps=0;
   float olaccX=0,nlaccX=0,olaccY=0,nlaccY=0,olaccZ=0,nlaccZ=0;
   float oaccX=0,naccX=0,oaccY=0,naccY=0,oaccZ=0,naccZ=0;
   float ogyroX=0,ngyroX=0,ogyroY=0,ngyroY=0,ogyroZ=0,ngyroZ=0;
   float ogravX=0,ngravX=0,ogravY=0,ngravY=0,ogravZ=0,ngravZ=0;
   float omagnX=0,nmagnX=0,omagnY=0,nmagnY=0,omagnZ=0,nmagnZ=0;

   @Override
   public void onSensorChanged(SensorEvent sensorEvent){

     Calendar rightNow = Calendar.getInstance();

      switch(sensorEvent.sensor.getType()){
         case Sensor.TYPE_STEP_COUNTER:
//            laccStatus.setText("Step_counter:"+sensorEvent.values[0]);
            new_steps = sensorEvent.values[0];
            if(Math.abs(new_steps-old_steps)>=pedo_steps&&start_wifiscan){
               old_steps = new_steps;
               Log.v(TAG,"wifiscan");
               setUp();
            }
            break;
         case Sensor.TYPE_ACCELEROMETER:
            naccX = sensorEvent.values[0];
            naccY = sensorEvent.values[1];
            naccZ = sensorEvent.values[2];
            if((Math.abs(naccX-oaccX))>1||(Math.abs(naccY-oaccY))>1||(Math.abs(naccZ-oaccZ))>1) {
//           acceleroStatus.setText("Accelerometer (m/s^2)\nX:" + String.format("%.1f", sensorEvent.values[0]) +
//                   "\nY:" + String.format("%.1f", sensorEvent.values[1]) +
//                   "\nZ:" + String.format("%.1f", sensorEvent.values[2]));

               String string = dateFormat.format(rightNow.getTime()) +
                       "," + String.format("%.1f", sensorEvent.values[0]) +
                       "," + String.format("%.1f", sensorEvent.values[1]) +
                       "," + String.format("%.1f", sensorEvent.values[2]) + "\n";

               //Log.v(TAG,string);
//         Toast.makeText(this, dateFormat.format(rightNow.getTimeInMillis())+"",
//                 Toast.LENGTH_LONG).show();

               try {
                  if (accelero_W != null) {
                     accelero_W.append(string);
                     accelero_W.flush();
                  }

               } catch (Exception e) {
               }
               oaccX = naccX;
               oaccY = naccY;
               oaccZ = naccZ;
            }
            break;
         case Sensor.TYPE_LINEAR_ACCELERATION:
            nlaccX = sensorEvent.values[0];
            nlaccY = sensorEvent.values[1];
            nlaccZ = sensorEvent.values[2];
            if((Math.abs(nlaccX-olaccX))>1||(Math.abs(nlaccY-olaccY))>1||(Math.abs(nlaccZ-olaccZ))>1) {
//           laccStatus.setText("Linear Acceleration (m/s^2)\nX:" + String.format("%.1f", sensorEvent.values[0]) +
//                   "\nY:" + String.format("%.1f", sensorEvent.values[1]) +
//                   "\nZ:" + String.format("%.1f", sensorEvent.values[2]));

               String string = dateFormat.format(rightNow.getTime()) +
                       "," + String.format("%.1f", sensorEvent.values[0]) +
                       "," + String.format("%.1f", sensorEvent.values[1]) +
                       "," + String.format("%.1f", sensorEvent.values[2]) + "\n";

               try {
                  if (lacc_W != null) {
                     lacc_W.append(string);
                     lacc_W.flush();
                  }

               } catch (Exception e) {
               }
               olaccX = nlaccX;
               olaccY = nlaccY;
               olaccZ = nlaccZ;
            }
            break;
         case Sensor.TYPE_GYROSCOPE:
            ngyroX = sensorEvent.values[0];
            ngyroY = sensorEvent.values[1];
            ngyroZ = sensorEvent.values[2];

            if((Math.abs(ngyroX-ogyroX))>1||(Math.abs(ngyroY-ogyroY))>1||(Math.abs(ngyroZ-ogyroZ))>1) {
//         gyroStatus.setText("Rotation rate (rad/s)\nX:" + String.format("%.1f", sensorEvent.values[0])+
//               "\nY:" + String.format("%.1f", sensorEvent.values[1])+
//               "\nZ:" + String.format("%.1f", sensorEvent.values[2]));

               String string = dateFormat.format(rightNow.getTime())+
                       ","+String.format("%.1f", sensorEvent.values[0])+
                       "," + String.format("%.1f", sensorEvent.values[1])+
                       "," + String.format("%.1f", sensorEvent.values[2])+"\n";

               try {
                  if(gyro_W!=null) {
                     gyro_W.append(string);
                     gyro_W.flush();
                  }

               } catch (Exception e) {
               }
               ogyroX = ngyroX;
               ogyroY = ngyroY;
               ogyroZ = ngyroZ;
            }
            break;
         case Sensor.TYPE_GRAVITY:
            ngravX = sensorEvent.values[0];
            ngravY = sensorEvent.values[1];
            ngravZ = sensorEvent.values[2];
            if((Math.abs(ngravX-ogravX))>1||(Math.abs(ngravY-ogravY))>1||(Math.abs(ngravZ-ogravZ))>1) {
//         gravStatus.setText("Gravity (m/s^2)\nX:" + String.format("%.1f", sensorEvent.values[0])+
//               "\nY:" + String.format("%.1f", sensorEvent.values[1])+
//               "\nZ:" + String.format("%.1f", sensorEvent.values[2]));

               String string = dateFormat.format(rightNow.getTime())+
                       ","+String.format("%.1f", sensorEvent.values[0])+
                       "," + String.format("%.1f", sensorEvent.values[1])+
                       "," + String.format("%.1f", sensorEvent.values[2])+"\n";

               try {
                  if(grav_W!=null) {
                     grav_W.append(string);
                     grav_W.flush();
                  }

               } catch (Exception e) {
               }
               ogravX = ngravX;
               ogravY = ngravY;
               ogravZ = ngravZ;
            }
            break;
         case Sensor.TYPE_MAGNETIC_FIELD:
            nmagnX = sensorEvent.values[0];
            nmagnY = sensorEvent.values[1];
            nmagnZ = sensorEvent.values[2];

            if((Math.abs(nmagnX-omagnX))>10||(Math.abs(nmagnY-omagnY))>10||(Math.abs(nmagnZ-omagnZ))>10) {

//           gravStatus.setText("Magnetic field (uT)\nX:" + String.format("%.1f", sensorEvent.values[0]) +
//                   "\nY:" + String.format("%.1f", sensorEvent.values[1]) +
//                   "\nZ:" + String.format("%.1f", sensorEvent.values[2]));

               String string = dateFormat.format(rightNow.getTime()) +
                       "," + String.format("%.1f", sensorEvent.values[0]) +
                       "," + String.format("%.1f", sensorEvent.values[1]) +
                       "," + String.format("%.1f", sensorEvent.values[2]) + "\n";

               try {
                     if (magn_W != null) {
                        magn_W.append(string);
                        magn_W.flush();
                     }
                  } catch (Exception e){
               }
               omagnX = nmagnX;
               omagnY = nmagnY;
               omagnZ = nmagnZ;
            }
            break;
         case Sensor.TYPE_RELATIVE_HUMIDITY:
//            baroStatus.setText("Air humidity (%)\n" + String.format("%.1f", sensorEvent.values[0]));
            String string = dateFormat.format(rightNow.getTime()) +","+ String.format("%.2f", sensorEvent.values[0]);
            try {
               if(baro_W != null){
                  baro_W.append(string);
                  baro_W.flush();
               }
            }
            catch(Exception e){
            }
            break;
         default:
      }

   }

   @Override
   public void onAccuracyChanged(Sensor sensor, int i) {

   }

   void sendGPS() throws IOException {
      if(gps_W!=null) {
         gps_W.close();
         FileOutputStream fileOutputStream;
         ZipOutputStream zipOutputStream =  null;
         //Log.v(TAG,Environment.getExternalStorageDirectory().getPath());

         String destination = Environment.getExternalStorageDirectory().getPath()+ "datas/"+deviceID+".zip";

         File file2 = new File(destination);
         if (!file2.exists())
            file2.createNewFile();

         fileOutputStream = new FileOutputStream(file2);
         zipOutputStream =  new ZipOutputStream(new BufferedOutputStream(fileOutputStream));

         zipFile(zipOutputStream,path+"/");

         //send file
         String URI = "http://rovermind.cs.umd.edu:8080/LocationServer/FindLocation?type=ap";
         AndroidHttpClient client = AndroidHttpClient.newInstance("user agent");
         String displayString = "";

         HttpPost post = new HttpPost(URI);

         Log.v(TAG, post.getMethod());
         Log.v(TAG, post.getURI().toASCIIString());

         Log.v(TAG,"send");
         File file = new File(Environment.getExternalStorageDirectory().getPath(),deviceID+".zip");

           Log.v(TAG,file.exists()+" hereeeeee");
           InputStreamEntity reqEntity = null;
           try {
               reqEntity = new InputStreamEntity(
                       new FileInputStream(file), -1);
               reqEntity.setContentType("binary/octet-stream");
               reqEntity.setChunked(true); // Send in multiple parts if needed
               post.setEntity(reqEntity);
               Log.v(TAG, "sending info");
               HttpResponse response = client.execute(post);
               Log.v(TAG,"responseee: "+response.toString());
               Log.v(TAG, "post aborted: " + post.isAborted());
               BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));


               StringBuilder builder = new StringBuilder();
               String line = "\n";
               line = line + "\n";
               while ((line = reader.readLine()) != null) {
                   builder.append(line);
                   builder.append("\n");
                   Log.v(TAG, line + "\n");
               }

               String serverResponse = builder.toString();
               Log.v(TAG, "server response: " + serverResponse);
               displayString = serverResponse;
           } catch (FileNotFoundException e) {
               e.printStackTrace();
           } catch (IOException e) {
               e.printStackTrace();
           }
           finally {
               client.close();
               Log.v(TAG, "right before the return");
               Log.v(TAG, displayString);
           }

            //delete file
            file.delete();
            gps_W = new FileWriter(gps_file);
         }
   }

   private static void zipFile(ZipOutputStream zipOutputStream, String sourcePath) throws  IOException{
      Log.v(TAG,sourcePath);
      java.io.File files = new java.io.File(sourcePath);
      java.io.File[] fileList = files.listFiles();

      String entryPath="";
      BufferedInputStream input;
      for (java.io.File file : fileList) {
         if (file.isDirectory()) {
            zipFile(zipOutputStream, file.getPath());
         } else {
            byte data[] = new byte[BUFFER_SIZE];
            FileInputStream fileInputStream = new FileInputStream(file.getPath());
            input = new BufferedInputStream(fileInputStream, BUFFER_SIZE);
            Log.v(TAG,file.getAbsolutePath());
            Log.v(TAG,file.getAbsolutePath().replace(sourcePath,""));
            entryPath=file.getAbsolutePath().replace( sourcePath,"");

            ZipEntry entry = new ZipEntry(entryPath);
            zipOutputStream.putNextEntry(entry);

            int count;
            while ((count = input.read(data, 0, BUFFER_SIZE)) != -1) {
               zipOutputStream.write(data, 0, count);
            }
            input.close();
         }
      }
      zipOutputStream.close();

   }

   public Runnable runnable = new Runnable() {

      public void run() {

         MillisecondTime = SystemClock.uptimeMillis() - StartTime;

         UpdateTime = TimeBuff + MillisecondTime;

         Seconds = (int) (UpdateTime / 1000);

         Minutes = Seconds / 60;

         Hours = Minutes / 60;

         Seconds = Seconds % 60;

         MilliSeconds = (int) (UpdateTime % 1000);

         timerText.setText(String.format("%02d", Hours)+":"+ String.format("%02d", (Minutes%60)) + ":"
                 + String.format("%02d", Seconds));

         handler.postDelayed(this, 0);
      }

   };

   long dirSize(File dir) {

      if (dir.exists()) {
         long result = 0;
         File[] fileList = dir.listFiles();
         for(int i = 0; i < fileList.length; i++) {
            // Recursive call if it's a directory
            if(fileList[i].isDirectory()) {
               result += dirSize(fileList [i]);
            } else {
               // Sum the file size in bytes
               result += fileList[i].length();
            }
         }
         return result; // return the file size
      }
      return 0;
   }


}
