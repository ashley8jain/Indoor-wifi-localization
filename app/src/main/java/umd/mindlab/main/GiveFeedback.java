package umd.mindlab.main;

import umd.mindlab.objects.LogWifiInfoTask;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class GiveFeedback extends Activity implements OnClickListener {
	private static final String TAG = "GiveFeedback";

	public Button submit;

	public String building;
	public String room;
	public String wing;
	public String xCoordinate;
	public String yCoordinate;
	public String nearestAP;
	
	public String xml;

	public CheckBox buildingBox;
	public CheckBox wingBox;
	public CheckBox roomBox;
	public CheckBox xCoordBox;
	public CheckBox yCoordBox;
	public CheckBox nearestAPBox;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_two);

		xml = getIntent().getExtras().getString("xml");
		
		submit = (Button) findViewById(R.id.submit);
		submit.setOnClickListener(this);

		room = new String();
		wing = new String();
		building = new String();

		final EditText buildingText = (EditText) findViewById(R.id.buildingText);
		buildingText.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// If the event is a key-down event on the "enter" button
				if ((event.getAction() == KeyEvent.ACTION_DOWN)
						&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
					// Perform action on key press
					building = buildingText.getText().toString();
					Toast.makeText(GiveFeedback.this, building,
							Toast.LENGTH_LONG).show();

					return true;
				}
				return false;
			}
		});

		final EditText wingText = (EditText) findViewById(R.id.wingText);
		wingText.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// If the event is a key-down event on the "enter" button
				if ((event.getAction() == KeyEvent.ACTION_DOWN)
						&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
					// Perform action on key press
					wing = wingText.getText().toString();
					Toast.makeText(GiveFeedback.this, wing, Toast.LENGTH_LONG)
							.show();

					return true;
				}
				return false;
			}
		});

		final EditText roomText = (EditText) findViewById(R.id.roomText);
		roomText.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// If the event is a key-down event on the "enter" button
				if ((event.getAction() == KeyEvent.ACTION_DOWN)
						&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
					// Perform action on key press
					room = roomText.getText().toString();
					Toast.makeText(GiveFeedback.this, room, Toast.LENGTH_LONG)
							.show();

					return true;
				}
				return false;
			}
		});		
		
		final EditText xText = (EditText) findViewById(R.id.xcoordText);
		xText.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// If the event is a key-down event on the "enter" button
				if ((event.getAction() == KeyEvent.ACTION_DOWN)
						&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
					// Perform action on key press
					xCoordinate = xText.getText().toString();
					Toast.makeText(GiveFeedback.this, xCoordinate, Toast.LENGTH_LONG)
							.show();

					return true;
				}
				return false;
			}
		});
		
		final EditText yText = (EditText) findViewById(R.id.ycoordText);
		yText.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// If the event is a key-down event on the "enter" button
				if ((event.getAction() == KeyEvent.ACTION_DOWN)
						&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
					// Perform action on key press
					yCoordinate = yText.getText().toString();
					Toast.makeText(GiveFeedback.this, yCoordinate, Toast.LENGTH_LONG)
							.show();

					return true;
				}
				return false;
			}
		});
		
		final EditText nearestAPText = (EditText) findViewById(R.id.apText);
		nearestAPText.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// If the event is a key-down event on the "enter" button
				if ((event.getAction() == KeyEvent.ACTION_DOWN)
						&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
					// Perform action on key press
					nearestAP = nearestAPText.getText().toString();
					Toast.makeText(GiveFeedback.this, nearestAP, Toast.LENGTH_LONG)
							.show();

					return true;
				}
				return false;
			}
		});		

		buildingBox = (CheckBox) findViewById(R.id.checkBuilding);
		wingBox = (CheckBox) findViewById(R.id.checkWing);
		roomBox = (CheckBox) findViewById(R.id.checkRoom);
		xCoordBox = (CheckBox) findViewById(R.id.checkx);
		yCoordBox = (CheckBox) findViewById(R.id.checky);
		nearestAPBox = (CheckBox) findViewById(R.id.checkAP);
	}

	public void onClick(View v) {
		String str = "<user-input>\n<room>";
		if (roomBox.isChecked()) {
			if (room == null || room.length() < 1) {
				str = str + "unknown room";
			} else {
				str = str + room.trim();
			}
		}
		str = str + "</room>\n";
		str = str + "<wing>";
		if (wingBox.isChecked()) {
			if (wing == null || wing.length() < 1) {
				str = str + "unknown wing";
			} else {
				str = str + wing.trim();
			}
		}
		str = str + "</wing>\n";
		str = str + "<building>";
		if (buildingBox.isChecked()) {
			if (building == null || building.length() < 1) {
				str = str + "unknown building";
			} else {
				str = str + building.trim();
			}
		}
		str = str + "</building>\n";
		str = str + "<xcoordinate>";
		if (xCoordBox.isChecked()) {
			if (xCoordinate == null || xCoordinate.length() < 1) {
				str = str + "unknown x value";
			} else {
				str = str + xCoordinate.trim();
			}
		}
		str = str + "</xcoordinate>\n";
		str = str + "<ycoordinate>";
		if (yCoordBox.isChecked()) {
			if (yCoordinate == null || yCoordinate.length() < 1) {
				str = str + "unknown y value";
			} else {
				str = str + yCoordinate.trim();
			}
		}
		str = str + "</ycoordinate>\n";
		str = str + "<ap>";
		if (nearestAPBox.isChecked()) {
			if (nearestAP == null || nearestAP.length() < 1) {
				str = str + "unknown AP";
			} else {
				str = str + nearestAP.trim();
			}
		}
		str = str + "</ap>\n</user-input>\n";

		str = str + xml; 
		Toast.makeText(this, "Logging feedback and wifi information", Toast.LENGTH_LONG).show();
		Log.v(TAG, str);
		//(new LogWifiInfoTask()).execute(str);

		Intent intent = new Intent();
		setResult(RESULT_OK, intent);
		finish();
	}
}
