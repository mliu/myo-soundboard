package com.michaelyliu.myosoundboard;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {
	
	private TextView connectionStatus;
	private Boolean recording = false;
	private MediaPlayer mPlayer;
	private List<String> myList;
	File file;
	
	private DeviceListener mListener = new AbstractDeviceListener() {
		private Arm mArm = Arm.UNKNOWN;
		private XDirection mXDirection = XDirection.UNKNOWN;
	
		@Override
		public void onConnect(Myo myo, long timestamp) {
			Toast.makeText(MainActivity.this, "Myo found", Toast.LENGTH_LONG).show();
			connectionStatus.setTextColor(Color.BLUE);
			connectionStatus.setText("Connected.. Perform setup");
		}
		
		@Override
		public void onDisconnect(Myo myo, long timestamp) {
			Toast.makeText(MainActivity.this, "Myo disconnected", Toast.LENGTH_LONG).show();
			connectionStatus.setTextColor(Color.RED);
			connectionStatus.setText("Not Connected");
		}
		
		@Override
		public void onArmRecognized(Myo myo, long timestamp, Arm arm, XDirection xDirection) {
			connectionStatus.setTextColor(Color.GREEN);
			connectionStatus.setText("Ready!");
			mArm = arm;
			mXDirection = xDirection;
		}
		
        @Override
        public void onArmLost(Myo myo, long timestamp) {
        	connectionStatus.setTextColor(Color.BLUE);
        	connectionStatus.setText("Connected.. Perform setup");
            mArm = Arm.UNKNOWN;
            mXDirection = XDirection.UNKNOWN;
        }
        
        @Override
        public void onOrientationData(Myo myo, long timestamp, Quaternion rotation) {
            // Calculate Euler angles (roll, pitch, and yaw) from the quaternion.
            float roll = (float) Math.toDegrees(Quaternion.roll(rotation));
            float pitch = (float) Math.toDegrees(Quaternion.pitch(rotation));
            float yaw = (float) Math.toDegrees(Quaternion.yaw(rotation));
            // Adjust roll and pitch for the orientation of the Myo on the arm.
            if (mXDirection == XDirection.TOWARD_ELBOW) {
                roll *= -1;
                pitch *= -1;
            }
            // Next, we apply a rotation to the text view using the roll, pitch, and yaw.
            mTextView.setRotation(roll);
            mTextView.setRotationX(pitch);
            mTextView.setRotationY(yaw);
        }
        
        @Override
        public void onPose(Myo myo, long timestamp, Pose pose) {
            switch (pose) {
            case FIST:
//                mTextView.setText(getString(R.string.pose_fist));
                break;
            case WAVE_IN:
//                mTextView.setText(getString(R.string.pose_wavein));
                break;
            case WAVE_OUT:
//                mTextView.setText(getString(R.string.pose_waveout));
                break;
            case FINGERS_SPREAD:
//                mTextView.setText(getString(R.string.pose_fingersspread));
                break;
            case THUMB_TO_PINKY:
//                mTextView.setText(getString(R.string.pose_thumbtopinky));
                break;
            }
        }
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		connectionStatus = (TextView) findViewById(R.id.connectionStatus);
		
//		ListView listView = (ListView) findViewById(R.id.mylist);
//		myList = new ArrayList<String>();
//		
//		File directory = Environment.getExternalStorageDirectory();
//		file = new File(directory + "/test");
//		File list[] = file.listFiles();
//		
//		for(int i = 0; i < list.length; i++){
//			myList.add(list[i].getName());
//		}
//		
//		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, myList);
//		listView.setAdapter(adapter);
//		
		Hub hub = Hub.getInstance();
		if (!hub.init(this)) {
			Toast.makeText(this, "Couldn't initialize Hub", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		
		hub.addListener(mListener);
		
		Toast.makeText(this, "Searching for Myo...", Toast.LENGTH_LONG).show();
		hub.pairWithAnyMyo();
	}
}
