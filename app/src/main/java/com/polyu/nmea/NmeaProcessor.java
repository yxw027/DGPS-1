package com.polyu.nmea;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import Jama.Matrix;
import android.os.Bundle;
import android.util.Log;

import com.polyu.GPSEphemeris.GPSEphemeris;
import com.polyu.dgnss.ConstantClass;
import com.polyu.dgnss.CustomLog;
import com.polyu.eleazi.SatelliteELEAZIList;
import com.polyu.position.BLHPosition;
import com.polyu.position.NMEAPosition;
import com.polyu.position.SatPosition;
import com.polyu.position.XYZPosition;
import com.polyu.rtcm.SatelliteRTCMList;
import com.polyu.time.GPSTIME;
import com.polyu.time.TIME;

public class NmeaProcessor {

	String resultOutput = "";
	String satPositionOutput = "";
	String ionTropOutput = "";
	String[] satlist;
	boolean FlagGPRMC = false;
	boolean FlagGPGSA = false;
	boolean FlagGPGGA = false;
	boolean FlagNMEAandRTCMSatListFullMatched = true;
	SatList NMEASatList;
	NMEAPosition nmeaPosition;
	NMEAPosition _finalResultPositionDGPSPlusIon = null;
	double NMEALatitude;
	double NMEALongitude;
	double NMEAh;
	String NMEAPDOP;
	String DGPSPDOP;
	TIME NMEAtime;
	DecimalFormat format2 = new DecimalFormat("#.00");
	SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd,HHmmss");
	String NMEASatSNR = "";
	String recordresult1 = "";
	String recordresult2 = "";
	String recordresult3 = "";
	String recordresult11 = "";
	String recordresult21 = "";
	String recordresult31 = "";
	static String FILENAMEFORRECORD = "";

	static public void setFileNmeaForRecord(String filename) {
		FILENAMEFORRECORD = filename;
	}

	public NMEAPosition getFinalResultPositionDGPSPlusIon() {
		return _finalResultPositionDGPSPlusIon;
	}

	public Bundle process(String nmea, boolean nmeaStatement,
			SatelliteRTCMList SatRTCM, GPSEphemeris GPSEphemeride,
			SatelliteELEAZIList satEleAziData) {

		Bundle resultsBundle = new Bundle();
		resultsBundle.putString("FinalLatitude", "");
		resultsBundle.putString("FinalLongitude", "");
		resultsBundle.putString("FinalAltitude", "");
		resultsBundle.putString("Latitude", "");
		resultsBundle.putString("Longitude", "");
		resultsBundle.putString("Altitude", "");
		resultsBundle.putString("Time", "");
		resultsBundle.putString("Fix", "");
		resultsBundle.putString("SatList", "");

		try {
			if (nmeaStatement) {
				String[] seperaNMEA = nmea.split("\\,");
				if (nmea.startsWith("$GPRMC")) {
	
					// String nmea101 =
					// "$GPRMC,092944.00,A,2218.38938,N,11410.78469,E,0.019,,210714,,,D*7F";
					// seperaNMEA = nmea101.split("\\,");
					// Log.e("$GPRMC", nmea);
					if (seperaNMEA[2].equals("V")) {
						// tLatitude.setText("N/A");
						// tLongitude.setText("N/A");
						resultsBundle.putString("Latitude", "N/A");
						resultsBundle.putString("Longitude", "N/A");
						FlagGPRMC = false;
					} else if (seperaNMEA[2].equals("A")) {
						String nmeatime = seperaNMEA[1];
						String nmeadate = seperaNMEA[9];
						int nmeaHH = Integer.valueOf(nmeatime.substring(0, 2));
						int nmeaMM = Integer.valueOf(nmeatime.substring(2, 4));
						int nmeaSS = Integer.valueOf(nmeatime.substring(4, 6)) + 16;
						int nmeaDAY = Integer.valueOf(nmeadate.substring(0, 2));
						int nmeaMON = Integer.valueOf(nmeadate.substring(2, 4));
						int nmeaYY = Integer.valueOf(nmeadate.substring(4)) + 2000;
						TIME nmeaTime = new TIME(nmeaYY, nmeaMON, nmeaDAY, nmeaHH,
								nmeaMM, nmeaSS);
						NMEAtime = nmeaTime;
						FlagGPRMC = true;
	
						// tTime.setText(nmeaTime.display());
						resultsBundle.putString("Time", nmeaTime.display());
					}
	
				} else if (nmea.startsWith("$GPGSA")) {
					// String nmea102 =
					// "$GPGSA,A,3,02,04,06,17,20,28,30,01,10,,,,1.33,0.79,1.08*08";
					// seperaNMEA = nmea102.split("\\,");
	
					if (seperaNMEA[2].equals("1")) {
						// no fix
						// tFix.setText("NO FIX");
						resultsBundle.putString("Fix", "NO FIX");
						FlagGPGSA = false;
	
					} else if (seperaNMEA[2].equals("2")) {
						// 2D fix
						// tFix.setText("2D FIX");
						resultsBundle.putString("Fix", "2D FIX");
						satlist = findSatList(seperaNMEA);
						if (satlist != null) {
							NMEASatList = new SatList(satlist);
							FlagGPGSA = true;
							String satListOut = "";
							for (int i = 0; i < satlist.length; i++) {
								satListOut += " " + satlist[i];
							}
							// tSatList.setText(satListOut);
							resultsBundle.putString("SatList", satListOut);
							NMEAPDOP = seperaNMEA[15];
						}
	
					} else if (seperaNMEA[2].equals("3")) {
						// 3D fix
	
						// tFix.setText("3D FIX");
						resultsBundle.putString("Fix", "3D FIX");
						satlist = findSatList(seperaNMEA);
						if (satlist != null) {
							NMEASatList = new SatList(satlist);
	
							FlagGPGSA = true;
	
							String satListOut = "";
							for (int i = 0; i < satlist.length; i++) {
								satListOut += " " + satlist[i];
							}
							// tSatList.setText(satListOut);
							resultsBundle.putString("SatList", satListOut);
							NMEAPDOP = seperaNMEA[15];
	
						}
	
					}
	
				} else if (nmea.startsWith("$GPGGA")) {
					// String nmea103 =
					// "$GPGGA,092944.00,2218.38938,N,11410.78469,E,2,12,0.79,67.7,M,-1.6,M,,0000*7D";
					// seperaNMEA = nmea103.split("\\,");
	
					if (seperaNMEA[9].length() > 0 && seperaNMEA[11].length() > 0) {
						double altitude;
						altitude = Double.valueOf(seperaNMEA[9])
								+ Double.valueOf(seperaNMEA[11]);
	
						NMEAh = altitude;
						NMEALatitude = toDecimal(seperaNMEA[2]);
						NMEALongitude = toDecimal(seperaNMEA[4]);
						if (seperaNMEA[3].equals("S")) {
							NMEALatitude = NMEALatitude * -1;
						}
						if (seperaNMEA[5].equals("W")) {
							NMEALongitude = NMEALongitude * -1;
						}
	
						FlagGPGGA = true;
	
					} else {
						FlagGPGGA = false;
					}
	
				}
	
				// edNMEA.append(nmea);
	
				if (FlagGPRMC == true && FlagGPGSA == true && FlagGPGGA == true) {
					nmeaPosition = new NMEAPosition(NMEALatitude, NMEALongitude,
							NMEAh, NMEAtime, NMEASatList);
					/*
					 * // <<<<<***************************FOR TEST SatelliteRTCMList
					 * rtcmDataTEST = new SatelliteRTCMList(); SatelliteRTCM
					 * svRTCMTEST1 = new SatelliteRTCM(0, 1, 2, -13.16, 0.000, 73,
					 * 1800); rtcmDataTEST.Add(svRTCMTEST1); SatelliteRTCM
					 * svRTCMTEST2 = new SatelliteRTCM(0, 1, 4, 0.38, 0.000, 27,
					 * 1800); rtcmDataTEST.Add(svRTCMTEST2); SatelliteRTCM
					 * svRTCMTEST3 = new SatelliteRTCM(0, 1, 6, -1.84, 0.000, 41,
					 * 1800); rtcmDataTEST.Add(svRTCMTEST3); SatelliteRTCM
					 * svRTCMTEST4 = new SatelliteRTCM(0, 1, 17, -1.76, 0.002, 11,
					 * 1800); rtcmDataTEST.Add(svRTCMTEST4); SatelliteRTCM
					 * svRTCMTEST5 = new SatelliteRTCM(0, 1, 20, -4.32, 0.000, 27,
					 * 1800); rtcmDataTEST.Add(svRTCMTEST5); SatelliteRTCM
					 * svRTCMTEST6 = new SatelliteRTCM(0, 1, 28, -0.48, 0.000, 91,
					 * 1800); rtcmDataTEST.Add(svRTCMTEST6); SatelliteRTCM
					 * svRTCMTEST7 = new SatelliteRTCM(0, 1, 30, -17.06, -0.004, 26,
					 * 1800); rtcmDataTEST.Add(svRTCMTEST7); SatelliteRTCM
					 * svRTCMTEST8 = new SatelliteRTCM(0, 1, 1, -17.98, -0.006, 0,
					 * 1800); rtcmDataTEST.Add(svRTCMTEST8); SatelliteRTCM
					 * svRTCMTEST9 = new SatelliteRTCM(0, 1, 10, -8.94, 0.000, 37,
					 * 1800); rtcmDataTEST.Add(svRTCMTEST9);
					 * 
					 * SatelliteELEAZIList satEleAziDataTest = new
					 * SatelliteELEAZIList(); SatelliteELEAZI satELETEST1 = new
					 * SatelliteELEAZI( 2, 253, 18, 41);
					 * satEleAziDataTest.Add(satELETEST1); SatelliteELEAZI
					 * satELETEST2 = new SatelliteELEAZI( 4, 340, 61, 49);
					 * satEleAziDataTest.Add(satELETEST2); SatelliteELEAZI
					 * satELETEST3 = new SatelliteELEAZI( 6, 277, 47, 47);
					 * satEleAziDataTest.Add(satELETEST3); SatelliteELEAZI
					 * satELETEST4 = new SatelliteELEAZI( 17, 000, 50, 47);
					 * satEleAziDataTest.Add(satELETEST4); SatelliteELEAZI
					 * satELETEST5 = new SatelliteELEAZI( 20, 51, 36, 44);
					 * satEleAziDataTest.Add(satELETEST5); SatelliteELEAZI
					 * satELETEST6 = new SatelliteELEAZI( 28, 159, 72, 41);
					 * satEleAziDataTest.Add(satELETEST6); SatelliteELEAZI
					 * satELETEST7 = new SatelliteELEAZI( 30, 188, 12, 32);
					 * satEleAziDataTest.Add(satELETEST7); SatelliteELEAZI
					 * satELETEST8 = new SatelliteELEAZI( 1, 47, 9, 43);
					 * satEleAziDataTest.Add(satELETEST8); SatelliteELEAZI
					 * satELETEST9 = new SatelliteELEAZI( 10, 193, 31, 41);
					 * satEleAziDataTest.Add(satELETEST9);
					 * 
					 * satEleAziData = satEleAziDataTest; SatelliteRTCMList SatRTCM
					 * = rtcmDataTEST; // ***************************FOR TEST>>>>>>
					 */
					// SatelliteRTCMList SatRTCM = rtcmData;
					// CGps GPSEphemeride = test;
					if (SatRTCM.Count() > 0 && GPSEphemeride != null
							&& satEleAziData != null) {
						String[] nList = nmeaPosition.satlist.satPRNList;
						int numberOfSat = nmeaPosition.satlist.satCount;
	
						// ***********FORM L
						// MATRIX***********************>>>>>>
						double[] lMatrix = new double[numberOfSat];
						double[] lMatrixWithIonMinus = new double[numberOfSat];
						double[] lMatrixWithIonPlus = new double[numberOfSat];
						double[] ionCorrectionOld = new double[numberOfSat];
						double[] tropCorrectionOld = new double[numberOfSat];
						String satDGPSforRecord = "";
						FlagNMEAandRTCMSatListFullMatched = true;
						for (int i = 0; i < numberOfSat; i++) {
							int rtcmIndex = SatRTCM
									.Match(Integer.valueOf(nList[i]));
							int eleIndex = satEleAziData.Match(Integer
									.valueOf(nList[i]));
							if (rtcmIndex != -1 && eleIndex != -1) {
	
								double RTCMPRC = SatRTCM.SatelliteRTCM(rtcmIndex).pseudorangeCorrection;
								double RTCMRRC = GNSSCorrection.rrcCorrection(
										nmeaPosition,
										SatRTCM.SatelliteRTCM(rtcmIndex));
								double RTCMcorrection = RTCMPRC + RTCMRRC;
	
								ionCorrectionOld[i] = GNSSCorrection.ionCorrection(
										GPSEphemeride,
										satEleAziData.SatelliteELEAZI(eleIndex),
										GPSEphemeride.nHead, nmeaPosition);
								tropCorrectionOld[i] = GNSSCorrection
										.tropCorrection(satEleAziData
												.SatelliteELEAZI(eleIndex).ele,
												nmeaPosition.h);
	
								lMatrix[i] = RTCMcorrection;
								lMatrixWithIonMinus[i] = RTCMcorrection
										+ ionCorrectionOld[i];
								lMatrixWithIonPlus[i] = RTCMcorrection
										+ ionCorrectionOld[i]
										+ tropCorrectionOld[i];
								satDGPSforRecord += "#L,"
										+ nList[i]
										+ ",RTCM,"
										+ format2.format(lMatrix[i])
										+ ",ION,"
										+ format2.format(ionCorrectionOld[i])
										+ ",TROP,"
										+ format2.format(tropCorrectionOld[i])
										+ ",ELE,"
										+ format2.format(satEleAziData
												.SatelliteELEAZI(eleIndex).ele)
										+ ",AZI,"
										+ format2.format(satEleAziData
												.SatelliteELEAZI(eleIndex).azi)
										+ ",SNR,"
										+ format2.format(satEleAziData
												.SatelliteELEAZI(eleIndex).snr)
										+ "\n";
							} else {
								FlagNMEAandRTCMSatListFullMatched = false;
							}
						}
	
						Matrix L = new Matrix(lMatrix, lMatrix.length);
						Matrix LMinusIon = new Matrix(lMatrixWithIonMinus,
								lMatrixWithIonMinus.length);
						Matrix LPlusIon = new Matrix(lMatrixWithIonPlus,
								lMatrixWithIonPlus.length);
	
						// edNMEA.append(MatchOutput + "\n");
						// ***********FORM A
						// MATRIX***********************<<<<<<
						// FlagNMEAandRTCMSatListFullMatched = true;
						if (FlagNMEAandRTCMSatListFullMatched && numberOfSat > 3) {
							// edNMEA.append("------------------SatPosition Matched-------------------\n");
	
							NMEAPosition receiverPosition = nmeaPosition;
							// *****************************************************************
							// DGPS
							// *****************************************************************
	
							NMEAPosition finalResultPositionDGPS = ReceiverPositionBasedOnDGPS(
									numberOfSat, receiverPosition, GPSEphemeride,
									nList, L, 1);
							// *****************************************************************
							// DGPS + ION
							receiverPosition = nmeaPosition;
							// *****************************************************************
							NMEAPosition finalResultPositionDGPSMinusIon = ReceiverPositionBasedOnDGPS(
									numberOfSat, receiverPosition, GPSEphemeride,
									nList, LPlusIon, 2);
							// *****************************************************************
							// DGPS + ION+TROP
							Log.i("1000", "3");
							receiverPosition = nmeaPosition;
							// *****************************************************************
							NMEAPosition finalResultPositionDGPSPlusIon = ReceiverPositionBasedOnDGPS(
									numberOfSat, receiverPosition, GPSEphemeride,
									nList, L, 3);
	
							if (finalResultPositionDGPSPlusIon != null)
								_finalResultPositionDGPSPlusIon = finalResultPositionDGPSPlusIon;
	
							resultOutput = "#DATE," + nmeaPosition.time.display()
									+ "\n" + recordresult1 + ",," + recordresult2
									+ ",," + recordresult3 + ",,NMEA,"
									+ format2.format(nmeaPosition.xyz.x) + ","
									+ format2.format(nmeaPosition.xyz.y) + ","
									+ format2.format(nmeaPosition.xyz.z) + "\n"
									+ recordresult11 + ",," + recordresult21 + ",,"
									+ recordresult31 + ",,NMEA,"
									+ format2.format(nmeaPosition.NEH.N) + ","
									+ format2.format(nmeaPosition.NEH.E) + ","
									+ format2.format(nmeaPosition.NEH.H) + "\n"
									+ "NMEAPDOP," + NMEAPDOP + ",GPSPDOP,"
									+ DGPSPDOP + "\n" + satDGPSforRecord + "\n"
									+ ionTropOutput + satPositionOutput;
							CustomLog.recordResult(FILENAMEFORRECORD, resultOutput);
	
							/*
							 * tLatitudeResult .setText(format2
							 * .format(finalResultPositionDGPSPlusIon.NEH.N));
							 * tLongitudeResult .setText(format2
							 * .format(finalResultPositionDGPSPlusIon.NEH.E));
							 * tAltitudeResult .setText(format2
							 * .format(finalResultPositionDGPSPlusIon.NEH.H));
							 * tLatitude.setText(format2
							 * .format(nmeaPosition.NEH.N));
							 * tLongitude.setText(format2
							 * .format(nmeaPosition.NEH.E));
							 * tAltitude.setText(format2
							 * .format(nmeaPosition.NEH.H));
							 */
							resultsBundle.putString("FinalLatitude", format2
									.format(finalResultPositionDGPSPlusIon.NEH.N));
							resultsBundle.putString("FinalLongitude", format2
									.format(finalResultPositionDGPSPlusIon.NEH.E));
							resultsBundle.putString("FinalAltitude", format2
									.format(finalResultPositionDGPSPlusIon.NEH.H));
							resultsBundle.putString("Latitude",
									format2.format(nmeaPosition.NEH.N));
							resultsBundle.putString("Longitude",
									format2.format(nmeaPosition.NEH.E));
							resultsBundle.putString("Altitude",
									format2.format(nmeaPosition.NEH.H));
	
						} else {
							resultOutput = "#DATE," + formatter.format(new Date())
									+ "\n" + "#NOTMARCH,NMEA,"
									+ format2.format(nmeaPosition.xyz.x) + ","
									+ format2.format(nmeaPosition.xyz.y) + ","
									+ format2.format(nmeaPosition.xyz.z) + "\n"
									+ satDGPSforRecord + "\n" + satPositionOutput;
							CustomLog.recordResult(FILENAMEFORRECORD, resultOutput);
							// * FlagNMEAandSatListFullMatched = true;
							// edNMEA.append("------------------SatPosition NOT SMatched-------------------\n");
	
						}
	
					} else {
	
						// edNMEA.append("---------------------FALSE-------------------\n");
					}
	
					FlagGPRMC = false;
					FlagGPGSA = false;
					FlagGPGGA = false;
	
				}
	
			}
		} catch (Exception e) {
			
		}
			
		return resultsBundle;
	}

	private String[] findSatList(String[] seperaNMEA) {
		// TODO Auto-generated method stub
		String[] result = null;
		int endIndex = -1;
		if (seperaNMEA[2].equals("0")) {
			return null;
		} else {

			for (int i = 3; i < 15; i++) {
				if (seperaNMEA[i].length() > 0 && seperaNMEA[i + 1].isEmpty()) {
					endIndex = i;
				}

			}
			if (seperaNMEA[14].length() > 0) {
				endIndex = 14;
			}

			if (endIndex != -1) {
				int length = endIndex - 2;
				int QNZZNum = 0;

				for (int i = 0; i < length; i++) {
					double firstSat = Double.valueOf(seperaNMEA[i + 3]);
					if (firstSat > 100) {
						QNZZNum += 1;
					}
				}

				result = new String[length - QNZZNum];
				int resultIndex = 0;
				for (int i = 0; i < length; i++) {
					double firstSat = Double.valueOf(seperaNMEA[i + 3]);
					if (firstSat > 191) {
						Log.i("QNZZ", String.valueOf(i));
					} else {
						result[resultIndex] = seperaNMEA[i + 3];
						resultIndex += 1;

					}
				}

			}

		}
		return result;

	}

	private NMEAPosition ReceiverPositionBasedOnDGPS(int numberOfSat,
			NMEAPosition receiverPosition, GPSEphemeris GPSEphemeride,
			String[] nList, Matrix L, int index) {
		// TODO Auto-generated method stub
		double receiverClockErr = 0;
		boolean FlagNMEAandSatListFullMatched = true;
		// do {
		double[][] aMatrix = new double[numberOfSat][4];
		double[] elevationAngle = new double[numberOfSat];
		double[] azimuthAngle = new double[numberOfSat];

		double[] ionCorrection = new double[numberOfSat];
		double[] tropCorrection = new double[numberOfSat];
		TIME requestTime = receiverPosition.time;
		satPositionOutput = "#SATPOS,";
		ionTropOutput = "";
		NMEAPosition resultPosition = null;
		for (int i = 0; i < numberOfSat; i++) {
			int satEphIndex = GPSEphemeride.FindTheBestFitTime(requestTime,
					Integer.valueOf(nList[i]));
			if (satEphIndex != -1) {

				GPSTIME requestGPSTime = GPSEphemeride
						.TimeToGpsTime(requestTime);

				SatPosition satPosition = GPSEphemeride
						.SatellitePositionWithRotation(
								GPSEphemeride.nData.get(satEphIndex),
								requestGPSTime, receiverPosition,
								receiverClockErr);
				double nmeaToSatdeltX = receiverPosition.xyz.x - satPosition.XX;
				double nmeaToSatdeltY = receiverPosition.xyz.y - satPosition.YY;
				double nmeaToSatdeltZ = receiverPosition.xyz.z - satPosition.ZZ;
				double nmeaToSatDistance = Math.sqrt(Math
						.pow(nmeaToSatdeltX, 2)
						+ Math.pow(nmeaToSatdeltY, 2)
						+ Math.pow(nmeaToSatdeltZ, 2));
				aMatrix[i][0] = (nmeaToSatdeltX / nmeaToSatDistance);
				aMatrix[i][1] = (nmeaToSatdeltY / nmeaToSatDistance);
				aMatrix[i][2] = (nmeaToSatdeltZ / nmeaToSatDistance);
				aMatrix[i][3] = 1;

				elevationAngle[i] = GPSEphemeride.getElevationAngle(
						receiverPosition, nmeaToSatdeltX, nmeaToSatdeltY,
						nmeaToSatdeltZ);
				azimuthAngle[i] = GPSEphemeride.getAzimuth(receiverPosition,
						nmeaToSatdeltX, nmeaToSatdeltY, nmeaToSatdeltZ);
				ionCorrection[i] = GNSSCorrection.ionCorrection(GPSEphemeride,
						elevationAngle[i], azimuthAngle[i],
						GPSEphemeride.nHead, nmeaPosition);
				tropCorrection[i] = GNSSCorrection.tropCorrection(
						elevationAngle[i], receiverPosition.h);
				satPositionOutput += satPosition.displayPosition();
				ionTropOutput += "#ION," + nList[i] + ",ELE,"
						+ format2.format(elevationAngle[i]) + ",AZI,"
						+ format2.format(azimuthAngle[i]) + ",Ion,"
						+ format2.format(ionCorrection[i]) + ",Trop,"
						+ format2.format(tropCorrection[i]) + "\n";
			} else {
				FlagNMEAandSatListFullMatched = false;
			}
		}

		if (FlagNMEAandSatListFullMatched) {
			Matrix A = new Matrix(aMatrix);
			Matrix AT = A.transpose();
			Matrix ATA = AT.times(A);
			Matrix H = ATA.inverse();
			double PDOP = Math.sqrt(Math.pow(H.get(0, 0), 2)
					+ Math.pow(H.get(1, 1), 2) + Math.pow(H.get(2, 2), 2));
			DGPSPDOP = format2.format(PDOP);
			Matrix ion = new Matrix(ionCorrection, ionCorrection.length);
			Matrix trop = new Matrix(tropCorrection, tropCorrection.length);

			try {
				Matrix result = null;
				if (index == 1) {
					result = A.solve(L);
				} else if (index == 2) {
					// L = L.plus(ion);
					result = A.solve(L);
				} else if (index == 3) {
					L = L.plus(ion);
					L = L.plus(trop);
					result = A.solve(L);
				}

				double XforResult = receiverPosition.xyz.x + result.get(0, 0);
				double YforResult = receiverPosition.xyz.y + result.get(1, 0);
				double ZforResult = receiverPosition.xyz.z + result.get(2, 0);
				receiverClockErr = result.get(3, 0)
						/ ConstantClass.SpeedOfLIGHT;

				XYZPosition resultDGPS = new XYZPosition(XforResult,
						YforResult, ZforResult);
				BLHPosition resultDGPSBLH = NMEAPosition.XYZtoBLH(resultDGPS);
				double LatDGPS = resultDGPSBLH.B * 180 / Math.PI;
				double LongDGPS = resultDGPSBLH.L * 180 / Math.PI;
				double HDGPS = resultDGPSBLH.H;
				TIME oTime = receiverPosition.time;
				SatList oSatList = receiverPosition.satlist;
				resultPosition = new NMEAPosition(LatDGPS, LongDGPS, HDGPS,
						oTime, oSatList);
				String typeResult = "";
				if (index == 1) {
					typeResult = "#DGPS,";
					recordresult1 = typeResult
							+ format2.format(resultPosition.xyz.x) + ","
							+ format2.format(resultPosition.xyz.y) + ","
							+ format2.format(resultPosition.xyz.z);
					recordresult11 = "#NEH,"
							+ format2.format(resultPosition.NEH.N) + ","
							+ format2.format(resultPosition.NEH.E) + ","
							+ format2.format(resultPosition.NEH.H);
				} else if (index == 2) {
					typeResult = "#DGPS+ion-old,";
					recordresult2 = typeResult
							+ format2.format(resultPosition.xyz.x) + ","
							+ format2.format(resultPosition.xyz.y) + ","
							+ format2.format(resultPosition.xyz.z);
					recordresult21 = typeResult
							+ format2.format(resultPosition.NEH.N) + ","
							+ format2.format(resultPosition.NEH.E) + ","
							+ format2.format(resultPosition.NEH.H);
				} else if (index == 3) {
					typeResult = "#DGPS+ion+trop,";
					recordresult3 = typeResult
							+ format2.format(resultPosition.xyz.x) + ","
							+ format2.format(resultPosition.xyz.y) + ","
							+ format2.format(resultPosition.xyz.z);
					recordresult31 = typeResult
							+ format2.format(resultPosition.NEH.N) + ","
							+ format2.format(resultPosition.NEH.E) + ","
							+ format2.format(resultPosition.NEH.H);
				}

				// edNMEA.append("------------------Solved-------------------\n");
			} catch (Exception e) {
				// edNMEA.append("------------------NOT Solved-------------------\n");

			}
		}
		return resultPosition;
		// } while (positionLoopFlag);
	}

	private static double toDecimal(String string) {
		// TODO Auto-generated method stub
		int index = string.indexOf(".");
		String a = string.substring(0, index - 2);
		String b = string.substring(index - 2);
		double result = Double.valueOf(a) + Double.valueOf(b) / 60;
		return result;
	}

}
