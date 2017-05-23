package umd.mindlab.objects;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;

import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;

public class LogWifiInfoTask extends AsyncTask<String, Long, String> {
	public final String URI = "http://mind6.cs.umd.edu:8080/SimpleLogger/LogInput";
	public String TAG = "LogWifiInfoTask";

/*	public LogWifiInfoTask(){
		//fma = find;
	}*/

	//@SuppressWarnings("finally")
	@Override
	protected String doInBackground(String... params) {
		AndroidHttpClient client = AndroidHttpClient.newInstance("user agent");
		String displayString = "";

		HttpPost post = new HttpPost(URI);

		Log.v(TAG, params[0].length() + "");
		Log.v(TAG, post.getMethod());
		Log.v(TAG, post.getURI().toASCIIString());

		MultipartEntity entity = new MultipartEntity();
		try {
			entity.addPart("data", new InputStreamBody(
					new ByteArrayInputStream(params[0].getBytes()), "text/xml",
					"ap"));

/*			String hello = "hello";
			entity.addPart("data", new InputStreamBody(
					new ByteArrayInputStream(hello.getBytes()), "text/xml",
					"ap"));*/
			post.setEntity(entity);

			Log.v(TAG, "sending info");
			Log.v(TAG, params[0]);
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

		} catch (UnsupportedEncodingException e) {
			Log.v(TAG, e.getMessage());
		} catch (IOException e) {
			Log.v(TAG, e.getMessage());
		} finally {
			client.close();
			Log.v(TAG, "right before the return");
			Log.v(TAG, displayString);
		}

		return displayString;

	}

	protected void onPostExecute(String result) {
		// Post results back to UI thread
		//Toast.makeText(fma, "results logged!", Toast.LENGTH_LONG).show();

	}

}
