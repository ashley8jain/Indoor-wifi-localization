package umd.mindlab.objects;

import java.io.IOException;
import java.util.*;

import umd.mindlab.main.LocateMeActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.util.Log;

public class WifiReceiver extends BroadcastReceiver {
	public String TAG = "WifiReceiver";
	public LocateMeActivity find;
	public Location currentLocation;

	public WifiReceiver(LocateMeActivity LocateMeActivity){
		super();
		find = LocateMeActivity;
	}

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		String xml = "<?xml version=\"1.0\"?><deviceid>"+find.deviceID+"</deviceid><data>";
		if(currentLocation != null){
			xml = xml + "<currentlocation>";
			xml = xml + "<lat>" + currentLocation.getLatitude() + "</lat>";
			xml = xml + "<lon>" + currentLocation.getLongitude() + "</lon>";
			xml = xml + "<alt>" + currentLocation.getAltitude() + "</alt>";
			xml = xml + "</currentlocation>";
		}

		List<ScanResult> results = find.wifi.getScanResults();
		
		System.out.println("Number of signals detected:" + results.size() + "\n");
		
		//Store unique MAC address alongwith the maximum signal strength received for that MAC address 
		HashMap listAccessPoints = new HashMap();
		HashMap listAccessPointsSSID = new HashMap();
		HashMap listAccessPointsFreq = new HashMap();
		
		System.out.println("MAC addresses before filtering: ");
		for (ScanResult result : results) {
			String mac = result.BSSID;
			String name = result.SSID;
			int signalStrength = result.level;
			int freq = result.frequency;
			// textStatus.append("\n\n" + result.toString());
			System.out.println(mac + "," + name+","+signalStrength + "," + freq);			
		}
		
		xml = xml + "<accesspoints>";
		
		for (ScanResult result : results) {
			String mac = result.BSSID.trim();
			String name = result.SSID.trim();
			int signalStrength = result.level;
			int freq = result.frequency;
			xml = xml + "<accesspoint>";
			xml = xml + "<name>" + name + "</name>";
			xml = xml + "<mac>" + mac + "</mac>";
			xml = xml + "<signal>" + signalStrength + "</signal>";
			xml = xml + "<freq>" + freq + "</freq>";
			xml = xml + "</accesspoint>";
			// textStatus.append("\n\n" + result.toString());
			/*if (!listAccessPoints.containsKey(mac)) {
				listAccessPoints.put(mac, signalStrength);
				listAccessPointsSSID.put(mac,name);
				System.out.println(mac + " " + name + " added to accesspoint lists");
			} else {
				if(signalStrength > Integer.parseInt(listAccessPoints.get(mac).toString())) {
					System.out.println(signalStrength + " > " + listAccessPoints.get(mac).toString());
					listAccessPoints.put(mac, signalStrength);
					listAccessPointsSSID.put(mac, name);
					System.out.println(mac + " " + signalStrength);
				}				
			}*/
			
		}
		xml = xml + "</accesspoints></data>";
				
		//System.out.println("Number of unique MAC addresses: " + listAccessPoints.size());
		
		/*Iterator it = listAccessPoints.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			String mackey  = pairs.getKey().toString();
			xml = xml + "<accesspoint>\n";
			xml = xml + "<name>" + listAccessPointsSSID.get(mackey) + "</name>\n";
			xml = xml + "<mac>" + mackey+"0" + "</mac>\n";
			xml = xml + "<signal>" + pairs.getValue() + "</signal>\n";
			xml = xml + "</accesspoint>\n";				
		}
		xml = xml + "</accesspoints>\n</data>\n";*/

		Calendar rightNow = Calendar.getInstance();
		String str="";
		str+=find.dateFormat.format(rightNow.getTime());
		str+=","+xml+"\n";
		find.xml = xml;
		if(find.xml_W!=null){
			try {
				find.xml_W.write(str);
				find.xml_W.flush();
			} catch (IOException e) {
			}
		}
//		(new SendWifiInfoTask(find)).execute(xml);
		Log.v(TAG, xml);
		if(LocateMeActivity.count > 0){
			find.wifi.startScan(); 
			LocateMeActivity.count--;
			Log.v(TAG, "COUNT: " + LocateMeActivity.count);
		}
		System.out.println(xml);
		//(new LogWifiInfoTask()).execute(xml);
	}

}
