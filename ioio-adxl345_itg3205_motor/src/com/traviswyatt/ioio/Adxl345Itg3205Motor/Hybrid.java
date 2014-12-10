package com.traviswyatt.ioio.Adxl345Itg3205Motor;

import com.traviswyatt.ioio.Adxl345Itg3205Motor.ADXL345.ADXL345Listener;

import ioio.lib.api.IOIO;
import ioio.lib.api.TwiMaster;
import ioio.lib.api.TwiMaster.Rate;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.IOIOLooper;

public class Hybrid implements IOIOLooper {
	private ADXL345 adxl345;
	private ITG3205 itg3205;
	private TwiMaster i2c;
	private int twiNum;
	private Rate rate;
	
	private byte deviceID;
	
	private float Acc_x;
	private float Acc_y;
	private float Acc_z;
	private double Acc_magnitude;
	
	private float Gyro_x;
	private float Gyro_y;
	private float Gyro_z;
	private float Gyro_temperature;
	
	public interface HybridListener {
		public void onDeviceId(byte deviceId);
		public void onData(
				float  acc_x,
				float  acc_y,
				float  acc_z,
				double acc_magnitude,
				float gyro_x,
				float gyro_y,
				float gyro_z,
				float gyro_temperature
				);
	}
	
	private HybridListener listener;
	
	public Hybrid(int twiNum, Rate rate) {
		this.twiNum = twiNum;
		this.rate = rate;
		this.adxl345 = new ADXL345(twiNum, rate);
		this.itg3205 = new ITG3205(twiNum, rate);
	}
	
	public Hybrid setListener(HybridListener listener) {
		this.listener = listener;
		return this;
	}
	
	@Override
	public void setup(IOIO ioio) throws ConnectionLostException, InterruptedException {
		i2c = ioio.openTwiMaster(twiNum, rate, false /* smbus */);
		adxl345.setI2C(i2c);
		itg3205.setI2C(i2c);
		
		final Hybrid hybrid = this;
		adxl345.setListener(new ADXL345.ADXL345Listener() {
			@Override
			public void onDeviceId(byte deviceId) {
				hybrid.deviceID = deviceId;
				if (hybrid.listener != null) {
					hybrid.listener.onDeviceId(deviceId);
				}
			}
			@Override
			public void onData(int x, int y, int z) {
				hybrid.Acc_x = x * adxl345.getMultiplier() * 9.8f;
				hybrid.Acc_y = y * adxl345.getMultiplier() * 9.8f;
				hybrid.Acc_z = z * adxl345.getMultiplier() * 9.8f;
				hybrid.Acc_magnitude = Math.sqrt(hybrid.Acc_x * hybrid.Acc_x + hybrid.Acc_y * hybrid.Acc_y + hybrid.Acc_z * hybrid.Acc_z);
			}
			@Override
			public void onError(String message) {
				// TODO Auto-generated method stub
			}
		});
		
		itg3205.setListener(new ITG3205.ITG3205Listener() {
			public void onDeviceId(byte deviceId) {

			}			@Override
			public void onData(int x, int y, int z, int temperature) {
				hybrid.Gyro_x = (float) (((float) x / 14.375f)+2.838);  //2.838 is the compensation of offset
				hybrid.Gyro_y = (float) (((float) y / 14.375f)+2.814);  //2.814 is the compensation of offset
				hybrid.Gyro_z = (float) (((float) z / 14.375f)+0.416);  //0.416 is the compensation of offset
				hybrid.Gyro_temperature = (35f + (float) (temperature + 13200) / 280f);
			}
			@Override
			public void onError(String message) {
				// TODO Auto-generated method stub
			}
		});
		
		adxl345.setup(ioio);
		itg3205.setup(ioio);
		
	}

	@Override
	public void loop() throws ConnectionLostException, InterruptedException {
		try {
			adxl345.loop();
			itg3205.loop();
		} catch (InterruptedException e){
			return;
		}
		if (listener != null) {
			listener.onData(
					Acc_x,
					Acc_y,
					Acc_z,
					Acc_magnitude,
					Gyro_x,
					Gyro_y,
					Gyro_z,
					Gyro_temperature
					);
		}
	}

	@Override
	public void disconnected() {
		adxl345.disconnected();
		itg3205.disconnected();
		// TODO Auto-generated method stub
	}

	@Override
	public void incompatible() {
		adxl345.incompatible();
		itg3205.incompatible();
		// TODO Auto-generated method stub
	}
}
