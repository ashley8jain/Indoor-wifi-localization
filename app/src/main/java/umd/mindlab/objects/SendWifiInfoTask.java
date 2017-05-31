package umd.mindlab.objects;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;

import umd.mindlab.main.LocateMeActivity;
import umd.mindlab.main.R;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class SendWifiInfoTask extends AsyncTask<String, Long, String> {

	public String TAG = "SendWifiInfoTask";
	public final String URI = "http://rovermind.cs.umd.edu:8080/LocationServer/FindLocation?type=ap";

	public LocateMeActivity fma;
	
	public SendWifiInfoTask(LocateMeActivity find){
		fma = find;
	}

	//@SuppressWarnings("finally")
	@Override
	protected String doInBackground(String... params) {
		AndroidHttpClient client = AndroidHttpClient.newInstance("user agent");
		String displayString = "";

		HttpPost post = new HttpPost(URI);

		Log.v(TAG, post.getMethod());
		Log.v(TAG, post.getURI().toASCIIString());

		MultipartEntity entity = new MultipartEntity();
		try {
			entity.addPart("data", new InputStreamBody(
					new ByteArrayInputStream(params[0].getBytes()), "text/xml",
					"ap"));

			post.setEntity(entity);

			Log.v(TAG, "sending info");
			HttpResponse response = client.execute(post);
			Log.v(TAG, "post aborted: " + post.isAborted());
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));

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
			//System.out.println("Got the server response" + displayString);

		} catch (UnsupportedEncodingException e) {
			Log.v(TAG, e.getMessage());
		} catch (IOException e) {
			Log.v(TAG, e.getMessage());
		} finally {
			client.close();
			Log.v(TAG, "right before the return");
			Log.v(TAG, displayString);
		}




//		Log.v(TAG,"device: "+fma.deviceID);

//		File file = new File(Environment.getExternalStorageDirectory().getPath(),
//				fma.deviceID+".zip");
//
//		Log.v(TAG,file.exists()+" hereeeeee");
//		InputStreamEntity reqEntity = null;
//		try {
//			reqEntity = new InputStreamEntity(
//                    new FileInputStream(file), -1);
//			reqEntity.setContentType("binary/octet-stream");
//			reqEntity.setChunked(true); // Send in multiple parts if needed
//			post.setEntity(reqEntity);
//			Log.v(TAG, "sending info");
//			HttpResponse response = client.execute(post);
//			Log.v(TAG,"responseee: "+response.toString());
//			Log.v(TAG, "post aborted: " + post.isAborted());
//			BufferedReader reader = new BufferedReader(new InputStreamReader(
//					response.getEntity().getContent()));
//
//
//			StringBuilder builder = new StringBuilder();
//			String line = "\n";
//			line = line + "\n";
//			while ((line = reader.readLine()) != null) {
//				builder.append(line);
//				builder.append("\n");
//				Log.v(TAG, line + "\n");
//			}
//
//			String serverResponse = builder.toString();
//			Log.v(TAG, "server response: " + serverResponse);
//			displayString = serverResponse;
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		finally {
//			client.close();
//			Log.v(TAG, "right before the return");
//			Log.v(TAG, displayString);
//		}
		return displayString;

	}
	
	protected void onPostExecute(String result) {
		// Post results back to UI thread
		TextView tv = (TextView) fma.findViewById(R.id.textStatus);
		tv.setText(result);
	}

}
