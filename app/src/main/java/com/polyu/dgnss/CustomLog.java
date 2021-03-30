package com.polyu.dgnss;

import java.io.File;
import java.io.FileOutputStream;

import android.os.Environment;

public class CustomLog {

	static public void recordRCTM(String FILENAMEFORRECORD, String outText) {
		if (!FILENAMEFORRECORD.isEmpty()) {
			if (outText.length() > 0) {
				try {
					String state = Environment.getExternalStorageState();
					if (Environment.MEDIA_MOUNTED.equals(state)) {
						File sdcard = Environment.getExternalStorageDirectory();
						File dir = new File(sdcard.getAbsolutePath()
								+ "/NTRIP/");
						dir.mkdirs();

						String filename = "RTCM-" + FILENAMEFORRECORD + ".txt";
						File file = new File(dir, filename);
						FileOutputStream f = new FileOutputStream(file, true);
						outText += "\n";
						byte[] byteForStore = outText.getBytes("UTF-8");
						f.write(byteForStore, 0, byteForStore.length);
						f.flush();
						f.close();

					}
				} catch (Exception e) {
					// Log.d("SaveRawDataChunk", e.getMessage());
				}
			}
		}
	}

	static public void recordNMEA(String FILENAMEFORRECORD, String outText) {
		if (outText.length() > 0) {
			try {
				String state = Environment.getExternalStorageState();
				if (Environment.MEDIA_MOUNTED.equals(state)) {
					File sdcard = Environment.getExternalStorageDirectory();
					File dir = new File(sdcard.getAbsolutePath() + "/NTRIP/");
					dir.mkdirs();

					String filename = "NMEA-" + FILENAMEFORRECORD + ".txt";
					File file = new File(dir, filename);
					FileOutputStream f = new FileOutputStream(file, true);
					outText += "\n";
					byte[] byteForStore = outText.getBytes("UTF-8");
					f.write(byteForStore, 0, byteForStore.length);
					f.flush();
					f.close();

				}
			} catch (Exception e) {
				// Log.d("SaveRawDataChunk", e.getMessage());
			}
		}
	}

	static public void recordResult(String FILENAMEFORRECORD, String outText) {

		if (!FILENAMEFORRECORD.isEmpty()) {
			if (outText.length() > 0) {
				try {
					String state = Environment.getExternalStorageState();
					if (Environment.MEDIA_MOUNTED.equals(state)) {
						File sdcard = Environment.getExternalStorageDirectory();
						File dir = new File(sdcard.getAbsolutePath()
								+ "/NTRIP/");
						dir.mkdirs();

						String filename = "RESULT-" + FILENAMEFORRECORD
								+ ".txt";
						File file = new File(dir, filename);
						FileOutputStream f = new FileOutputStream(file, true);
						outText += "\n";
						byte[] byteForStore = outText.getBytes("UTF-8");
						f.write(byteForStore, 0, byteForStore.length);
						f.flush();
						f.close();

					}
				} catch (Exception e) {
					// Log.d("SaveRawDataChunk", e.getMessage());
				}
			}
		}
	}
}
