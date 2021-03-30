package com.polyu.rtcm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import com.polyu.dgnss.CustomLog;

public class LoadNTRIP extends AsyncTask<String, Void, Void> {
	String nProtocol = "";
	String nServer = "";
	int nPort = 10000;
	String nMountpoint = "";
	String nUsername = "";
	String nPassword = "";
	Socket nsocket = null;
	InputStream nis = null; // Network Input Stream
	OutputStream nos = null; // Network Output Stream
	boolean inDecodingProcess = false;
	List<String> correctionMessage = new ArrayList<String>();
	int indexDecoded = 0;
	DecimalFormat format5 = new DecimalFormat("#.000000");
	DecimalFormat format2 = new DecimalFormat("#.00");
	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	int DataDGPSToSaveIndex = 0;
	private WeakReference<SatelliteRTCMList> _rtcmData;
	// private WeakReference<TIME> _NMEAtime;
	static String FILENAMEFORRECORD = "";

	public LoadNTRIP(SatelliteRTCMList rtcmData) {
		_rtcmData = new WeakReference<SatelliteRTCMList>(rtcmData);
		// _NMEAtime = new WeakReference<TIME>(NMEAtime);
	}

	static public void setFileNmeaForRecord(String filename) {
		FILENAMEFORRECORD = filename;
	}

	@SuppressWarnings("deprecation")
	static public String getCurrDate() {
		String dt;
		Date cal = Calendar.getInstance().getTime();
		dt = cal.toLocaleString();
		return dt;
	}

	@Override
	protected Void doInBackground(String... params) {
		// TODO Auto-generated method stub
		// NTRIPtest = new NetworkClient("ntripv1", "59.152.234.19",
		// 2101,"DGPS", "lsgi", "lsgi");
		while (true) {
			try {
				Log.i("NTAG", "1");
				SocketAddress sockaddr = new InetSocketAddress("59.152.234.19",
						2101);
				// SocketAddress sockaddr = new
				// InetSocketAddress("172.16.2.67",3333);
				// SocketAddress sockaddr = new
				// InetSocketAddress("103.11.89.114",3333);
				Log.i("NTAG", "2");
				nsocket = new Socket();
				Log.i("NTAG", "3");
				// nsocket.connect(sockaddr);
				nsocket.connect(sockaddr, 0); // 10 second connection
				// timeout
				Log.i("NTAG", "4");
				if (nsocket.isConnected()) {
					Log.i("NTAG", "5");
					CustomLog.recordResult(FILENAMEFORRECORD + "Server",
							"connected at " + getCurrDate());
					nsocket.setSoTimeout(0); // 20 second timeout once
												// data is flowing
					nis = nsocket.getInputStream();
					nos = nsocket.getOutputStream();
					Log.i("NTAG", "Socket created, streams assigned");
					nProtocol = "ntripv1";
					nMountpoint = "DGPS";
					nUsername = "lsgi";
					nPassword = "lsgi";
					if (nProtocol.equals("ntripv1")) {
						// Build request message
						Log.i("NTAG", "This is a NTRIP connection");
						String requestmsg = "GET /" + nMountpoint
								+ " HTTP/1.0\r\n";
						requestmsg += "User-Agent: NTRIP LefebureAndroidNTRIPClient/20121216\r\n";
						requestmsg += "Accept: */*\r\n";
						requestmsg += "Connection: close\r\n";
						if (nUsername.length() > 0) {
							requestmsg += "Authorization: Basic "
									+ ToBase64(nUsername + ":" + nPassword);
						}
						requestmsg += "\r\n";
						nos.write(requestmsg.getBytes());
						// Log.i("Request", requestmsg);
					} else {
						// Log.i(NTAG, "This is a raw TCP/IP connection");
					}

					// Log.i(NTAG, "Waiting for inital data...");
					byte[] buffer = new byte[1024];
					int read = nis.read(buffer, 0, 1024); // This is blocking
					while (read != -1) {
						byte[] tempdata = new byte[read];
						System.arraycopy(buffer, 0, tempdata, 0, read);
						// Log.i(NTAG, "Got data: " + new String(tempdata));
						// DataHandler.sendMessage(DataHandler.obtainMessage(MSG_NETWORK_GOT_DATA,
						// tempdata));
						Log.i("RTK", "DATA input");
						// ****************************************************************
						String tempStr = convertTo(tempdata);
						long t1 = System.nanoTime();
						if (inDecodingProcess == false) {

							String tempStrType1 = extractType1Message(tempStr);
							String tempStrType31 = extractType31Message(tempStr);

							correctionMessage.add(tempStrType1);
							if (tempStrType1.length() > 0) {
								DecodingRTCMnew(tempStrType1, 1);
							}
							if (tempStrType31.length() > 0) {
								DecodingRTCMnew(tempStrType31, 31);
							}
							// DecodingRTCM();

						}
						long t2 = System.nanoTime() - t1;
						Log.i("TIME", "RTCM: " + String.valueOf(t2));

						// ********************************************************************

						read = nis.read(buffer, 0, 1024); // This is blocking
					}
				}
				Log.i("NTAG", "6");

			} catch (SocketTimeoutException ex) {
				// DataHandler.sendMessage(DataHandler.obtainMessage(MSG_NETWORK_TIMEOUT));

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (nis != null)
						nis.close();
					if (nos != null)
						nos.close();
					if (nsocket != null)
						nsocket.close();
					// CloseUDPSocket();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				// Log.i(NTAG, "Finished");
				// DataHandler.sendMessage(DataHandler.obtainMessage(MSG_NETWORK_FINISHED));

			}
			CustomLog.recordResult(FILENAMEFORRECORD + "Server",
					"disconnected at " + getCurrDate());
			executeHardWork();
		}
		// return null;
	}

	private void executeHardWork() {
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private String extractType31Message(String str) {
		// TODO Auto-generated
		// method stub
		int index = -1;
		String result = "";
		int length = str.length();
		// Log.i("extractType1Message", "1");
		// //boolean hasC = str.contains("01100110000001");
		for (int i = 0; i < length - 24; i++) {
			if (str.substring(i, i + 24).equals("011001100111110001110000")) {
				index = i;
				i = length;
			}
		}
		// Log.i("extractType1Message", "2" + String.valueOf(index));

		if (index != -1) {
			String afterElimateHead = str.substring(index);
			int frameLength = Integer.parseInt(
					afterElimateHead.substring(46, 51), 2);
			int afterElimateHeadLength = (frameLength + 2) * 30;
			if (afterElimateHeadLength <= afterElimateHead.length()) {
				result = afterElimateHead.substring(0, afterElimateHeadLength);
			}

		}
		return result;
	}

	@SuppressWarnings("unused")
	private void DecodingRTCMnew(String allBinary, int type) {
		// TODO Auto-generated method stub

		int referenceId = Integer.parseInt(allBinary.substring(14, 24), 2);

		int parity = Integer.parseInt(allBinary.substring(24, 30), 2);

		double moderZ = Integer.parseInt(allBinary.substring(30, 43), 2) * 0.6;// multiply
																				// by
																				// 0.6
																				// seconds
		if (type == 31) {
			moderZ += 16;

		}

		int sequenceNumber = Integer.parseInt(allBinary.substring(43, 46), 2);

		int lengthFrame = Integer.parseInt(allBinary.substring(46, 51), 2);

		int stationHealth = Integer.parseInt(allBinary.substring(51, 54), 2);

		parity = Integer.parseInt(allBinary.substring(54, 60), 2);

		// decoding satellite by satellite
		String binaryExceptParity_Header = cutHEADandParity(allBinary,
				lengthFrame);

		int numberSatellite = (int) Math.floor(binaryExceptParity_Header
				.length() / 40.0);
		// Log.i("8_numberSatellite", String.valueOf(numberSatellite));
		// *******************************************************RTCM<<<<
		/*
		 * double[][] RTCMaMatrix = new double[numberSatellite][4]; double[]
		 * RTCMLMatrix = new double[numberSatellite]; Calendar RTCMcalendar =
		 * Calendar.getInstance(); int RTCMYear =
		 * RTCMcalendar.get(Calendar.YEAR); int RTCMMon =
		 * RTCMcalendar.get(Calendar.MONTH) + 1; int RTCMDay =
		 * RTCMcalendar.get(Calendar.DAY_OF_MONTH); int RTCMHour =
		 * RTCMcalendar.get(Calendar.HOUR_OF_DAY); int RTCMMin =
		 * RTCMcalendar.get(Calendar.MINUTE); int RTCMSec =
		 * RTCMcalendar.get(Calendar.SECOND); TIME RTCMrequestTime = new
		 * TIME(RTCMYear, RTCMMon, RTCMDay, RTCMHour, RTCMMin, RTCMSec);
		 */
		// *******************************************************RTCM>>>>

		for (int indexSV = 0; indexSV < numberSatellite; indexSV++) {
			int scaleFactor = Integer.parseInt(binaryExceptParity_Header
					.substring(indexSV * 40, indexSV * 40 + 1));

			int udre = Integer.parseInt(binaryExceptParity_Header.substring(
					indexSV * 40 + 1, indexSV * 40 + 3), 2);

			int satelliteID = Integer.parseInt(binaryExceptParity_Header
					.substring(indexSV * 40 + 3, indexSV * 40 + 8), 2);
			if (satelliteID == 0) {
				satelliteID = 32;
			}
			if (type == 31) {

				satelliteID += 64;
			}
			// *******************************************************RTCM<<<<
			/*
			 * int RTCMsatEphIndex = SatEphemeride.FindTheBestFitTime(
			 * RTCMrequestTime, satelliteID); if (RTCMsatEphIndex != -1) {
			 * 
			 * GPSTIME RTCMrequestGPSTime = SatEphemeride
			 * .TimeToGpsTime(RTCMrequestTime);
			 * 
			 * Position RTCMsatPosition = SatEphemeride
			 * .SatellitePositionWithRotation(
			 * SatEphemeride.nData.get(RTCMsatEphIndex), RTCMrequestGPSTime,
			 * RTCMreceiverPosition, RTCMreceiverClockErr); double
			 * RTCMnmeaToSatdeltX = RTCMreceiverPosition.xyz.x -
			 * RTCMsatPosition.XX; double RTCMnmeaToSatdeltY =
			 * RTCMreceiverPosition.xyz.y - RTCMsatPosition.YY; double
			 * RTCMnmeaToSatdeltZ = RTCMreceiverPosition.xyz.z -
			 * RTCMsatPosition.ZZ; double RTCMnmeaToSatDistance =
			 * Math.sqrt(Math.pow( RTCMnmeaToSatdeltX, 2) +
			 * Math.pow(RTCMnmeaToSatdeltY, 2) + Math.pow(RTCMnmeaToSatdeltZ,
			 * 2)); RTCMaMatrix[indexSV][0] = (RTCMnmeaToSatdeltX /
			 * RTCMnmeaToSatDistance); RTCMaMatrix[indexSV][1] =
			 * (RTCMnmeaToSatdeltY / RTCMnmeaToSatDistance);
			 * RTCMaMatrix[indexSV][2] = (RTCMnmeaToSatdeltZ /
			 * RTCMnmeaToSatDistance); RTCMaMatrix[indexSV][3] = 1; Log.i("",
			 * ""); }
			 */
			// *******************************************************RTCM>>>>

			int pseudorangeCorrectionInt = eliminateNegative(binaryExceptParity_Header
					.substring(indexSV * 40 + 8, indexSV * 40 + 24));
			int range_rateCorrectionInt = eliminateNegative(binaryExceptParity_Header
					.substring(indexSV * 40 + 24, indexSV * 40 + 32));

			int issueOfData = Integer.parseInt(binaryExceptParity_Header
					.substring(indexSV * 40 + 32, indexSV * 40 + 40), 2);
			if (type == 31) {
				issueOfData = Integer.parseInt(binaryExceptParity_Header
						.substring(indexSV * 40 + 33, indexSV * 40 + 40), 2);
			}
			int indexSVinList = _rtcmData.get().Match(satelliteID);

			double pseudorangeCorrection = -1.0;
			double range_rateCorrection = -1.0;
			if (scaleFactor == 0) {
				pseudorangeCorrection = pseudorangeCorrectionInt * 0.02;
				range_rateCorrection = range_rateCorrectionInt * 0.002;

			} else if (scaleFactor == 1) {
				pseudorangeCorrection = pseudorangeCorrectionInt * 0.32;
				range_rateCorrection = range_rateCorrectionInt * 0.032;
			}
			pseudorangeCorrection = Double.valueOf(format2
					.format(pseudorangeCorrection));
			range_rateCorrection = Double.valueOf(format5
					.format(range_rateCorrection));
			/*
			 * RTCMLMatrix[indexSV] = pseudorangeCorrection;
			 */

			if (Math.abs(pseudorangeCorrection) < 100) {
				if (indexSVinList == -1) {
					SatelliteRTCM svRTCM = new SatelliteRTCM(scaleFactor, udre,
							satelliteID, pseudorangeCorrection,
							range_rateCorrection, issueOfData, moderZ);
					_rtcmData.get().Add(svRTCM);

				} else {
					_rtcmData.get().dataList.get(indexSVinList).scaleFactor = scaleFactor;
					_rtcmData.get().dataList.get(indexSVinList).udre = udre;
					_rtcmData.get().dataList.get(indexSVinList).satelliteID = satelliteID;
					_rtcmData.get().dataList.get(indexSVinList).pseudorangeCorrection = pseudorangeCorrection;
					_rtcmData.get().dataList.get(indexSVinList).range_rateCorrection = range_rateCorrection;
					_rtcmData.get().dataList.get(indexSVinList).issueOfData = issueOfData;
					_rtcmData.get().dataList.get(indexSVinList).modernZ = moderZ;
				}
				String outText = formatter.format(new Date()) + ",Time, "
						+ format2.format(moderZ) + ",SatID,"
						+ String.valueOf(satelliteID) + ",PRC,"
						+ String.valueOf(pseudorangeCorrection) + ",RRC,"
						+ String.valueOf(range_rateCorrection) + ",udre,"
						+ String.valueOf(udre) + ",Scale,"
						+ String.valueOf(scaleFactor) + ",IOD,"
						+ String.valueOf(issueOfData);
				CustomLog.recordRCTM(FILENAMEFORRECORD, outText);
				Log.i("RTK", outText);
			} else {
				// Log.i("xX", "sda");
			}

		}
		// *******************************************************RTCM<<<<
		/*
		 * Matrix RTCMA = new Matrix(RTCMaMatrix); Matrix RTCML = new
		 * Matrix(RTCMLMatrix, RTCMLMatrix.length); Matrix RTCMResult =
		 * RTCMA.solve(RTCML); String RTCMDeltX =
		 * format2.format(RTCMResult.get(0, 0)); String RTCMDeltY =
		 * format2.format(RTCMResult.get(1, 0)); String RTCMDeltZ =
		 * format2.format(RTCMResult.get(2, 0)); String RTCMDeltT =
		 * format2.format(RTCMResult.get(3, 0));
		 * 
		 * String RTCMforRecord = RTCMrequestTime.display() + "," + RTCMDeltX +
		 * "," + RTCMDeltY + "," + RTCMDeltZ + "," + RTCMDeltT;
		 * RecordRTCMDelt(RTCMforRecord); RTCMDeltRecorCount += 1; //
		 * edNMEA.setText(String.valueOf(RTCMDeltRecorCount)); Log.i("RTCMTEST",
		 * String.valueOf(RTCMDeltRecorCount));
		 */
		// *******************************************************RTCM>>>>

	}

	@SuppressWarnings("unused")
	private void RecordRTCMDelt(String outText) {
		if (outText.length() > 0) {
			try {
				String state = Environment.getExternalStorageState();
				if (Environment.MEDIA_MOUNTED.equals(state)) { // We can
																// read
																// and write
																// the
																// media
					File sdcard = Environment.getExternalStorageDirectory();
					File dir = new File(sdcard.getAbsolutePath() + "/NTRIP/");
					dir.mkdirs();

					String filename = "RTCMDELT-" + FILENAMEFORRECORD + ".txt";
					File file = new File(dir, filename);
					// Log.i("FFFFFFFFF", file.getAbsolutePath());
					FileOutputStream f = new FileOutputStream(file, true);
					outText += "\n";
					byte[] byteForStore = outText.getBytes("UTF-8");
					f.write(byteForStore, 0, byteForStore.length);
					f.flush();
					f.close();

					// Log.d("SaveRawDataChunk", "Saved data to file: " +
					// filename);
				}
			} catch (Exception e) {
				// Log.d("SaveRawDataChunk", e.getMessage());
			}
			DataDGPSToSaveIndex = 0;
		}
	}

	private String cutHEADandParity(String str, int length) {
		// TODO Auto-generated method stub
		String result = "";
		String temp = str.substring(60);
		for (int i = 0; i < length; i++) {
			result += temp.substring(0, 24);
			temp = temp.substring(30);
		}

		return result;
	}

	private String extractType1Message(String str) {
		// TODO Auto-generated method stub
		int index = -1;
		String result = "";
		int length = str.length();

		for (int i = 0; i < length - 24; i++) {
			if (str.substring(i, i + 24).equals("011001100000010001110000")) {
				index = i;
				i = length;
			}
		}

		if (index != -1) {
			String afterElimateHead = str.substring(index);
			int frameLength = Integer.parseInt(
					afterElimateHead.substring(46, 51), 2);
			int afterElimateHeadLength = (frameLength + 2) * 30;
			if (afterElimateHeadLength <= afterElimateHead.length()) {
				result = afterElimateHead.substring(0, afterElimateHeadLength);
			}

		}
		return result;
	}

	private int eliminateNegative(String string) {
		// TODO Auto-generated method stub
		int result = 0;
		if (string.startsWith("1")) {
			String reverseString = "";

			for (int i = 0; i < string.length(); i++) {
				if (string.charAt(i) == '0') {
					reverseString += "1";
				} else {
					reverseString += "0";
				}
			}
			result = (Integer.parseInt(reverseString, 2) + 1) * -1;
		} else {
			result = Integer.parseInt(string, 2);
		}
		return result;

	}

	private String functionReverseString(String s) {
		// TODO Auto-generated method stub
		return new StringBuffer(s).reverse().toString();
	}

	private String convertTo(byte[] tempdata) {
		String str = null;
		try {
			str = new String(tempdata, 0, tempdata.length, "ASCII");
			String allBinary = "";
			String reversedBinary = "";
			for (int indexChar = 0; indexChar < str.length(); indexChar++) {

				int charInt = str.charAt(indexChar);
				String binaryChar = Integer.toBinaryString(charInt);

				String outBinary = String.format("%08d",
						Integer.parseInt(binaryChar));
				reversedBinary += functionReverseString(outBinary.substring(2));

				int remains = (int) Math.IEEEremainder(
						(double) indexChar + 1.0, 5);

				if (remains == 0) {
					String invertedWord = "";
					if ((str.startsWith("Y") && indexChar < 5)
							|| (indexChar > 5 && allBinary.endsWith("1")))// the
																			// first
																			// 24
																			// bits
																			// of
																			// this
																			// word
																			// will
																			// be
																			// inverted
					{
						for (int indexBit = 0; indexBit < reversedBinary
								.length(); indexBit++) {
							if (indexBit < 24) {
								if (reversedBinary.charAt(indexBit) == '0') {
									invertedWord += "1";
								} else
									invertedWord += "0";
							} else {
								invertedWord += reversedBinary.charAt(indexBit);
							}
						}
					}
					if ((str.startsWith("f") && indexChar < 5)
							|| (indexChar > 5 && allBinary.endsWith("0"))) {
						invertedWord += reversedBinary;
					}
					allBinary += invertedWord;

					reversedBinary = "";
				}
			}
			str = allBinary;

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return str;
	}

	private String ToBase64(String in) {
		return Base64.encodeToString(in.getBytes(), 4);
	}
}
