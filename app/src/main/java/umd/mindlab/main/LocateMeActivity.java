package umd.mindlab.main;

import umd.mindlab.objects.WifiReceiver;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class LocateMeActivity extends Activity implements SensorEventListener{
	private static final String TAG = "LocateMeActivity";
	public WifiManager wifi;
	BroadcastReceiver receiver;
	public String xml;
	public String address;
	private SensorManager mSensorManager;
	private Sensor mSensor;
	TextView textStatus;
	TextView accStatus;
	Button update;
	//Button verify;

	LocationManager locman;
	LocationListener loclist;
	Location currentLocation;

	public static int count = 0;

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
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSensorManager.registerListener(this,mSensor,SensorManager.SENSOR_DELAY_NORMAL);
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
		accStatus = (TextView) findViewById(R.id.acc);
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
		});		*/
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
		super.onDestroy();
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

	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		accStatus.setText("X:"+sensorEvent.values[0]+" Y:"+sensorEvent.values[1]+" Z:"+sensorEvent.values[2]);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int i) {

	}
}