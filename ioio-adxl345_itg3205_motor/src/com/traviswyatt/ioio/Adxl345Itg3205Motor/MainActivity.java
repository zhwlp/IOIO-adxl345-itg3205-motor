package com.traviswyatt.ioio.Adxl345Itg3205Motor;


import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.TwiMaster;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;





import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.traviswyatt.ioio.adxl345_itg3205_DataSaving.R;

public class MainActivity extends IOIOActivity {

	private TextView ioioStatusText;
	private TextView deviceIdText;
	private TextView Acc_xAxisText;
	private TextView Acc_yAxisText;
	private TextView Acc_zAxisText;
	private TextView Acc_magnitudeText;

	private TextView Gyro_xAxisText;
	private TextView Gyro_yAxisText;
	private TextView Gyro_zAxisText;
	private TextView Gyro_temperatureText;
	public static final int Motor1 = 37;

	// Record start
	TextView txtMessage;
	Button btnStart;
	Button btnStop;
	TextRecorder recorder;
	Thread writeThread;
	boolean started;
	PwmOutput pwm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		ioioStatusText = (TextView) findViewById(R.id.ioio_status);
		deviceIdText = (TextView) findViewById(R.id.device_id);
		Acc_xAxisText = (TextView) findViewById(R.id.x_axis);
		Acc_yAxisText = (TextView) findViewById(R.id.y_axis);
		Acc_zAxisText = (TextView) findViewById(R.id.z_axis);
		Acc_magnitudeText = (TextView) findViewById(R.id.magnitude);
		Gyro_xAxisText = (TextView) findViewById(R.id.Gyro_x_axis);
		Gyro_yAxisText = (TextView) findViewById(R.id.Gyro_y_axis);
		Gyro_zAxisText = (TextView) findViewById(R.id.Gyro_z_axis);
		Gyro_temperatureText = (TextView) findViewById(R.id.temperature);
		
		// record start
        txtMessage = (TextView)this.findViewById(R.id.textViewMessage);
        btnStart = (Button)this.findViewById(R.id.buttonStart);
        btnStop = (Button)this.findViewById(R.id.buttonStop);
        
        final MainActivity activity = this;
        
        btnStart.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				activity.start();
				
				try {
					pwm.setDutyCycle(0.5f);
				} catch (ConnectionLostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
        
        btnStop.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				activity.stop();
				try {
					pwm.setDutyCycle(0.0f);
				} catch (ConnectionLostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
        
        recorder = new TextRecorder(this.getApplicationContext(), "/storage/sdcard0");
	}  // record end
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/**
	 * A method to create our IOIO thread.
	 * 
	 * @see ioio.lib.util.AbstractIOIOActivity#createIOIOThread()
	 */
	

	
	@Override
	protected IOIOLooper createIOIOLooper() {
		int twiNum = 0; // IOIO pin 1 = SDA, pin 2 = SCL
		final Hybrid hybrid = new Hybrid(twiNum, TwiMaster.Rate.RATE_100KHz);
		
		hybrid.setListener(new Hybrid.HybridListener() {
			
			@Override
			public void onDeviceId(byte deviceId) {
				updateTextView(deviceIdText, "Device ID: " + (int) (deviceId & 0xFF));
			}
			
			@Override
			public void onData(float acc_x, float acc_y, float acc_z,
					double acc_magnitude, float gyro_x, float gyro_y, float gyro_z,
					float gyro_temperature) {
				
				Date date = new Date();
				String now = new SimpleDateFormat("MMddHHmmssSSSS").format(date);
				
				StringBuilder builder = new StringBuilder();
				builder.append(now)
					.append(",")
					.append(acc_x)
					.append(",")
					.append(acc_y)
					.append(",")
					.append(acc_z)
					.append(",")
					.append(acc_magnitude)
					.append(",")
					.append(gyro_x)
					.append(",")
					.append(gyro_y)
					.append(",")
					.append(gyro_z)
					.append(",")
					.append(gyro_temperature);
				
				recorder.writeLine(builder.toString());
				
				updateTextView(Acc_xAxisText, "Acc_X = " + acc_x);
				updateTextView(Acc_yAxisText, "Acc_Y = " + acc_y);
				updateTextView(Acc_zAxisText, "Acc_Z = " + acc_z);
				updateTextView(Acc_magnitudeText, "Magnitude = " + acc_magnitude);
				
				updateTextView(Gyro_xAxisText, "Gyro_X = " + gyro_x + " deg/s");
				updateTextView(Gyro_yAxisText, "Gyro_Y = " + gyro_y + " deg/s");
				updateTextView(Gyro_zAxisText, "Gyro_Z = " + gyro_z + " deg/s");
				updateTextView(Gyro_temperatureText, "Temperature = " + gyro_temperature + " C");
				

			}
		});
				
		return new DeviceLooper(hybrid);
	}
	

	
	public MainActivity() {
		this.started = false;

	}

    
    private void start() {
    	if (started) {
    		return;
    	}
    	
    	txtMessage.setText("Started");
    	recorder.start();
    	started = true;
    	recorder.writeLine("Time,Acc_X,Acc_Y,Acc_Z,Magnitude,Gyro_X,Gyro_Y,Gyro_Z,Temperature");
    }
    
    private void stop() {
    	if (!started) {
    		return;
    	}
    	
    	started = false;
		recorder.stop();
    	txtMessage.setText("Stopped");
    }
	
    // record data end
	

	private void updateTextView(final TextView textView, final String text) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				textView.setText(text);
			}
		});
	}
	
	/**
	 * This is the thread on which all the IOIO activity happens. It will be run
	 * every time the application is resumed and aborted when it is paused. The
	 * method setup() will be called right after a connection with the IOIO has
	 * been established (which might happen several times!). Then, loop() will
	 * be called repetitively until the IOIO gets disconnected.
	 */
	class DeviceLooper implements IOIOLooper {
		
		/**
		 * Duration to sleep after each loop.
		 */
		private static final long THREAD_SLEEP = 1L; // milliseconds
		
		private IOIOLooper device;

		public DeviceLooper(IOIOLooper device) {
			this.device = device;
		}
		
		@Override
		public void setup(IOIO ioio) throws ConnectionLostException, InterruptedException {
			device.setup(ioio);
			pwm = ioio.openPwmOutput(Motor1, 100);
			updateTextView(ioioStatusText, "IOIO Connected");
		}

		/**
		 * Called repetitively while the IOIO is connected.
		 * 
		 * @throws ConnectionLostException
		 *             When IOIO connection is lost.
		 * @throws InterruptedException 
		 * 
		 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#loop()
		 */
		@Override
		public void loop() throws ConnectionLostException, InterruptedException {
			device.loop();
			Thread.sleep(THREAD_SLEEP);
		}

		@Override
		public void disconnected() {
			device.disconnected();
			updateTextView(ioioStatusText, "IOIO Disconnected");
		}

		@Override
		public void incompatible() {
			device.incompatible();
			updateTextView(ioioStatusText, "IOIO Incompatible");
		}
	}
  
	
}
