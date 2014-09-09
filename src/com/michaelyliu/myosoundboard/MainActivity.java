package com.michaelyliu.myosoundboard;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.thalmic.myo.Arm;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.XDirection;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;

import android.app.Activity;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private TextView connectionStatus;
	private MediaPlayer mp = new MediaPlayer();
	
	private ImageView gesture;
	private ImageView gesture2;
	
	private Map<Integer, ArrayList<Float[]>> map;
	private ArrayList<Float> compare = new ArrayList<Float>();
	
	private int curPos;
	private int counter = 0;
	private int gestureCounter = 0;
	private ArrayList<Float[]> curVal = new ArrayList<Float[]>();
	
	private boolean myoConnected = false;
	private int step = 0;
	//Step 0 = Not gesturing
	//Step 1 = Selected gesturing
	//Step 2 = Listening for gesture (write)
	//Step 3 = Listening for gestures (read)

	private final File directory = Environment.getExternalStorageDirectory();

	private List<String> myList;

	private DeviceListener mListener = new AbstractDeviceListener() {
		private Arm mArm = Arm.UNKNOWN;
		private XDirection mXDirection = XDirection.UNKNOWN;

		@Override
		public void onConnect(Myo myo, long timestamp) {
			Toast.makeText(MainActivity.this, "Myo found", Toast.LENGTH_LONG)
					.show();
			connectionStatus.setTextColor(Color.BLACK);
			connectionStatus.setText("Connected.. Perform setup");
		}

		@Override
		public void onDisconnect(Myo myo, long timestamp) {
			Toast.makeText(MainActivity.this, "Myo disconnected",
					Toast.LENGTH_LONG).show();
			myoConnected = false;
			connectionStatus.setTextColor(Color.RED);
			connectionStatus.setText("Not Connected");
		}

		@Override
		public void onArmRecognized(Myo myo, long timestamp, Arm arm,
				XDirection xDirection) {
			connectionStatus.setTextColor(Color.BLUE);
			connectionStatus.setText("Ready!");
			myoConnected = true;
			mArm = arm;
			mXDirection = xDirection;
		}

		@Override
		public void onArmLost(Myo myo, long timestamp) {
			connectionStatus.setTextColor(Color.BLACK);
			connectionStatus.setText("Connected.. Perform setup");
			myoConnected = false;
			mArm = Arm.UNKNOWN;
			mXDirection = XDirection.UNKNOWN;
		}

		@Override
		public void onOrientationData(Myo myo, long timestamp,
				Quaternion rotation) {
			// Calculate Euler angles (roll, pitch, and yaw) from the
			// quaternion.
			float roll = (float) Math.toDegrees(Quaternion.roll(rotation));
			float pitch = (float) Math.toDegrees(Quaternion.pitch(rotation));
			float yaw = (float) Math.toDegrees(Quaternion.yaw(rotation));
			// Adjust roll and pitch for the orientation of the Myo on the arm.
			if (mXDirection == XDirection.TOWARD_ELBOW) {
				roll *= -1;
				pitch *= -1;
			}
			// Next, we apply a rotation to the text view using the roll, pitch,
			// and yaw.
			if (step == 2) {
				curVal.add(new Float[]{roll, pitch, yaw});
			} else if (step == 3) {
				int index = 0;
				for (Map.Entry<Integer, ArrayList<Float[]>> entry : map.entrySet()){
					if (counter == entry.getValue().size()){
						Log.d("COUNTER", Integer.toString(counter));
						Log.d("COUNTER SIZE", Float.toString(compare.get(index)/(3*counter)));
						
						if(counter != 0 && compare.get(index)/(3*counter) < 1.75) {
							playSong(directory.toString() + "/test/" + myList.get(entry.getKey()));
							connectionStatus.setText("Ready");
							step = 0;
							counter = 0;
							compare.clear();
							break;
						}
					} else if (counter < entry.getValue().size()) {
						float temp1 = entry.getValue().get(counter)[0];
						float temp2 = entry.getValue().get(counter)[1];
						float temp3 = entry.getValue().get(counter)[2];
						float temp = Math.abs(temp1 - roll)/Math.abs(temp1);
						temp += Math.abs(temp2 - pitch)/Math.abs(temp2);
						temp += Math.abs(temp3 - yaw)/Math.abs(temp3);
						if(counter == 0){
							compare.add(index, temp);					
						} else {
							Float placeholder = compare.remove(index);
							compare.add(index, placeholder + temp);						
						}
					}
					index++;
				}
				counter++;
			}
		}

		@Override
		public void onPose(Myo myo, long timestamp, Pose pose) {
			if (pose == Pose.FINGERS_SPREAD){
				if (step == 1) {
					gesture.setVisibility(View.INVISIBLE);
					gesture2.setVisibility(View.VISIBLE);
					connectionStatus.setText("Listening");
					step++;
				} else if (step == 2) {
					step = 0;
					gesture2.setVisibility(View.INVISIBLE);
					connectionStatus.setText("Ready");
					map.put(curPos, (ArrayList<Float[]>) curVal.clone());
					curVal.clear();
					curPos = -1;
				}
			}
			if (pose == Pose.THUMB_TO_PINKY){
				if (step == 3) {
					connectionStatus.setText("Ready");
					step = 0;
					counter = 0;
					compare.clear();
				} else if (step == 0) {
					step = 3;
					counter = 0;
					connectionStatus.setText("Gesture!");
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		connectionStatus = (TextView) findViewById(R.id.connectionStatus);

		ListView listView = (ListView) findViewById(R.id.listView);
		gesture = (ImageView) findViewById(R.id.gesture);
		gesture2 = (ImageView) findViewById(R.id.gesture2);
				
		myList = new ArrayList<String>();
		
		File file = new File(directory + "/test");
		File list[] = file.listFiles();

		for (int i = 0; i < list.length; i++) {
			myList.add(list[i].getName());
		}
		
		Log.d("LIST", myList.toString());
		
		map = new HashMap<Integer, ArrayList<Float[]>>();

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, android.R.id.text1, myList);
		listView.setAdapter(adapter);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				playSong(directory.toString() + "/test/" + myList.get(position));
				if (myoConnected && step == 0) {
					curPos = position;
					gesture.setVisibility(View.VISIBLE);
					step = 1;
				}
			}
		});

		Hub hub = Hub.getInstance();
		if (!hub.init(this)) {
			Toast.makeText(this, "Couldn't initialize Hub", Toast.LENGTH_SHORT)
					.show();
			finish();
			return;
		}

		hub.addListener(mListener);

		Toast.makeText(this, "Searching for Myo...", Toast.LENGTH_LONG).show();
		hub.pairWithAnyMyo();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mp.release();
	}

	private void playSong(String songPath) {
		try {
			mp.reset();
			mp.setDataSource(songPath);
			mp.prepare();
			mp.start();
		} catch (IOException e) {
			Log.v(getString(R.string.app_name), e.getMessage());
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK){
			switch(step){
				case 1:
					step = 0;
					gesture.setVisibility(View.INVISIBLE);
					break;
				case 2:
					step = 0;
					gestureCounter = 0;
					gesture2.setVisibility(View.INVISIBLE);
					curVal.clear();
					break;
				case 3:
					step = 0;
					connectionStatus.setText("Ready");
					curVal.clear();
					compare.clear();
					counter = 0;
					break;
				default:
					break;
			}
			return true;
		} else {
			return super.onKeyDown(keyCode,  event);
		}
	}
}
