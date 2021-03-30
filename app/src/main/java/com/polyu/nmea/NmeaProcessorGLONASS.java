package com.polyu.nmea;

import java.text.DecimalFormat;

import Jama.Matrix;
import android.os.Bundle;
import android.util.Log;

import com.polyu.GLONASSEphemeris.GLONASSEphemeris;
import com.polyu.GPSEphemeris.GPSEphemeris;
import com.polyu.dgnss.ConstantClass;
import com.polyu.dgnss.CustomLog;
import com.polyu.eleazi.SatelliteELEAZIList;
import com.polyu.position.BLHPosition;
import com.polyu.position.NMEAPosition;
import com.polyu.position.SatPosition;
import com.polyu.position.XYZPosition;
import com.polyu.rtcm.SatelliteRTCM;
import com.polyu.rtcm.SatelliteRTCMList;
import com.polyu.time.GPSTIME;
import com.polyu.time.TIME;

public class NmeaProcessorGLONASS {

	String resultOutput = "";
	String satPositionOutput = "";
	String ionTropOutput = "";
	String[] satlist;
	SatList GPSSatlist;
	SatList GLONASSSatlist;
	String AmatrixForRecord;
	String LmatrixForRecord;

	String satNumForRecord;
	String satListForRecord;
	String deltXforRecord;
	String deltYforRecord;
	String deltZforRecord;
	String deltNforRecord;
	String deltEforRecord;
	String deltHforRecord;

	boolean FlagGPRMC = false;
	boolean FlagGPGSA = false;
	boolean FlagGPGGA = false;
	boolean FlagGNGSA = false;
	boolean FlagGNGNS = false;
	boolean FlagHasGPRMC = false;
	boolean FlagHasGPGSA = false;
	boolean FlagHasGPGGA = false;
	boolean FlagHasGNGSA = false;
	boolean FlagHasGNGNS = false;
	boolean FlagGNGSAGPS = false;
	boolean FlagGNGSAGLONASS = false;
	boolean FlagNMEAandRTCMSatListFullMatched = true;
	SatList GNSSSatList = null;
	SatList GPSSatList = null;

	NMEAPosition nmeaPosition;
	NMEAPosition _finalResultPositionDGPSPlusIon = null;
	double NMEALatitude;
	double NMEALongitude;
	double NMEAh;
	double GNSSNMEALatitude;
	double GNSSNMEALongitude;
	double GNSSNMEAh;
	double GPSNMEALatitude;
	double GPSNMEALongitude;
	double GPSNMEAh;
	String NMEAPDOP;
	String DGPSPDOP;
	String GNSSmode = "N";
	String GPSmode = "0";
	String GNSSSatNum = null;
	String GPSSatNum = null;

	TIME NMEAtime;
	DecimalFormat format2 = new DecimalFormat("#.00");
	DecimalFormat format6 = new DecimalFormat("#.000000");

	String NMEASatSNR = "";
	String recordresult1 = "";
	String recordresult2 = "";
	String recordresult3 = "";
	String recordresult11 = "";
	String recordresult21 = "";
	String recordresult31 = "";
	static String FILENAMEFORRECORD = "";
	Bundle resultsBundle = new Bundle();
	NMEAPosition GNSSDGPSResult;

	static public void setFileNmeaForRecord(String filename) {
		FILENAMEFORRECORD = filename;
	}

	public NMEAPosition getFinalResultPositionDGPSPlusIon() {
		return _finalResultPositionDGPSPlusIon;
	}

	public Bundle process(String nmea, boolean nmeaStatement,
			SatelliteRTCMList SatRTCM, GPSEphemeris GPSEphemeride,
			GLONASSEphemeris GLONASSEphemeris, SatelliteELEAZIList satEleAziData) {
		long p1 = System.nanoTime();

		resultsBundle.putString("FinalLatitude", "");
		resultsBundle.putString("FinalLongitude", "");
		resultsBundle.putString("FinalAltitude", "");
		resultsBundle.putString("Latitude", "");
		resultsBundle.putString("Longitude", "");
		resultsBundle.putString("Altitude", "");
		resultsBundle.putString("Time", "");
		resultsBundle.putString("Fix", "");
		resultsBundle.putString("SatList", "");
		resultsBundle.putString("GNSS_Mode", "");
		resultsBundle.putString("SatNum", "");
		resultsBundle.putString("Delt", "");

		try {
			if (nmeaStatement) {
				Log.i("", nmea);
				String[] seperaNMEA = nmea.split("\\,");
				if (nmea.startsWith("$GNGNS")) {
					// deal different NMEA sentence
					// provide Latitude, Longitude, H and the GNSS mode
					FlagHasGNGNS = true;
					GNSSmode = seperaNMEA[6];
					GNSSSatNum = seperaNMEA[7];

					if (!GNSSmode.equals("NN")) {
						GNSSNMEALatitude = toDecimal(seperaNMEA[2]);
						GNSSNMEALongitude = toDecimal(seperaNMEA[4]);
						if (seperaNMEA[3].equals("S")) {
							GNSSNMEALatitude = GNSSNMEALatitude * -1;
						}
						if (seperaNMEA[5].equals("W")) {
							GNSSNMEALongitude = GNSSNMEALongitude * -1;
						}
						GNSSNMEAh = Double.valueOf(seperaNMEA[9]);
						FlagGNGNS = true;
					}

				} else if (nmea.startsWith("$GPRMC")) {
					// provide Time of positioning

					FlagHasGPRMC = true;

					if (seperaNMEA[2].equals("V")) {
						resultsBundle.putString("Time", "Void");
						FlagGPRMC = false;
					} else if (seperaNMEA[2].equals("A")) {
						String nmeatime = seperaNMEA[1];
						String nmeadate = seperaNMEA[9];
						int nmeaHH = Integer.valueOf(nmeatime.substring(0, 2));
						int nmeaMM = Integer.valueOf(nmeatime.substring(2, 4));
						int nmeaSS = Integer.valueOf(nmeatime.substring(4, 6));
						int nmeaDAY = Integer.valueOf(nmeadate.substring(0, 2));
						int nmeaMON = Integer.valueOf(nmeadate.substring(2, 4));
						int nmeaYY = Integer.valueOf(nmeadate.substring(4)) + 2000;

						nmeaSS += 16;
						if (nmeaSS > 60) {
							nmeaSS -= 60;
							nmeaMM += 1;
							if (nmeaMM > 60) {
								nmeaMM -= 60;
								nmeaHH += 1;
								if (nmeaHH > 24) {
									nmeaHH -= 24;
									nmeaDAY += 1;
								}
							}
						}
						// convert the UTC time in NMEA to GPS time
						TIME nmeaTime = new TIME(nmeaYY, nmeaMON, nmeaDAY,
								nmeaHH, nmeaMM, nmeaSS);
						NMEAtime = nmeaTime;
						resultsBundle.putString("Time", nmeaTime.display());
						FlagGPRMC = true;

					}

				} else if (nmea.startsWith("$GNGSA")) {
					// provide satellites used for positioning

					FlagHasGNGSA = true;

					// GPS + GLONASS
					if (seperaNMEA[2].equals("1")) {
						resultsBundle.putString("Fix", "NO FIX");
					} else if (seperaNMEA[2].equals("2")) {
						resultsBundle.putString("Fix", "2D FIX");
						satlist = findSatList(seperaNMEA);
						if (satlist != null) {
							double firstSatID = Double.valueOf(satlist[0]);
							if (firstSatID > 0 && firstSatID < 33) {
								GPSSatlist = new SatList(satlist);
								FlagGNGSAGPS = true;
							} else if (firstSatID > 64 && firstSatID < 97) {
								GLONASSSatlist = new SatList(satlist);
								FlagGNGSAGLONASS = true;
							}
						}

						if (FlagGNGSAGPS && FlagGNGSAGLONASS) {
							GNSSSatList = GPSSatlist.combine(GLONASSSatlist);
							FlagGNGSAGPS = false;
							FlagGNGSAGLONASS = false;
							if (GNSSSatList.satCount > 0) {
								FlagGNGSA = true;
							}

						}
						NMEAPDOP = seperaNMEA[15];
					} else if (seperaNMEA[2].equals("3")) {
						resultsBundle.putString("Fix", "3D FIX");
						satlist = findSatList(seperaNMEA);
						if (satlist != null) {
							double firstSatID = Double.valueOf(satlist[0]);
							if (firstSatID > 0 && firstSatID < 33) {
								GPSSatlist = new SatList(satlist);
								FlagGNGSAGPS = true;
							} else if (firstSatID > 64 && firstSatID < 97) {
								GLONASSSatlist = new SatList(satlist);
								FlagGNGSAGLONASS = true;
							}
						}

						if (FlagGNGSAGPS && FlagGNGSAGLONASS) {
							GNSSSatList = GPSSatlist.combine(GLONASSSatlist);
							FlagGNGSAGPS = false;
							FlagGNGSAGLONASS = false;
							if (GNSSSatList.satCount > 0) {
								FlagGNGSA = true;
							}
						}
						NMEAPDOP = seperaNMEA[15];
					}

				} else if (nmea.startsWith("$GPGGA")) {
					// provide Latitude, Longitude, H and the GNSS mode

					FlagHasGPGGA = true;

					if (seperaNMEA[9].length() > 0
							&& seperaNMEA[11].length() > 0) {
						double altitude;
						altitude = Double.valueOf(seperaNMEA[9])
								+ Double.valueOf(seperaNMEA[11]);
						GPSmode = seperaNMEA[6];
						resultsBundle.putString("GNSS_Mode", GNSSmode);
						GPSSatNum = seperaNMEA[7];
						GPSNMEAh = altitude;
						GPSNMEALatitude = toDecimal(seperaNMEA[2]);
						GPSNMEALongitude = toDecimal(seperaNMEA[4]);
						if (seperaNMEA[3].equals("S")) {
							GPSNMEALatitude = GPSNMEALatitude * -1;
						}
						if (seperaNMEA[5].equals("W")) {
							GPSNMEALongitude = GPSNMEALongitude * -1;
						}

						FlagGPGGA = true;

					} else {
						FlagGPGGA = false;
					}

				} else if (nmea.startsWith("$GPGSA")) {
					// provide satellites used for positioning

					FlagHasGPGSA = true;

					if (seperaNMEA[2].equals("1")) {
						// no fix
						resultsBundle.putString("Fix", "NO FIX");
						FlagGPGSA = false;

					} else if (seperaNMEA[2].equals("2")) {
						// 2D fix
						resultsBundle.putString("Fix", "2D FIX");
						satlist = findSatList(seperaNMEA);
						if (satlist != null) {
							GPSSatList = new SatList(satlist);
							FlagGPGSA = true;

							NMEAPDOP = seperaNMEA[15];
						}

					} else if (seperaNMEA[2].equals("3")) {
						// 3D fix
						resultsBundle.putString("Fix", "3D FIX");
						satlist = findSatList(seperaNMEA);
						if (satlist != null) {
							GPSSatList = new SatList(satlist);

							FlagGPGSA = true;
							NMEAPDOP = seperaNMEA[15];

						}

					}

				}

				if (!FlagHasGNGSA && !FlagHasGNGNS) {
					if (GPSSatNum != null) {
						resultsBundle.putString("SatNum", GPSSatNum);
						satNumForRecord = String.valueOf(GPSSatNum);
					}
					if (GPSSatList != null) {
						resultsBundle.putString("SatList",
								GPSSatList.dispSatList());
						satListForRecord = GPSSatList.dispSatList();
					}
					if (GPSmode != null) {
						resultsBundle.putString("GNSS_Mode", GPSmode);

					}

					// Only GPS
					if (FlagGPRMC && FlagGPGSA && FlagGPGGA) {
						NMEALatitude = GPSNMEALatitude;
						NMEALongitude = GPSNMEALongitude;
						NMEAh = GPSNMEAh;
						nmeaPosition = new NMEAPosition(NMEALatitude,
								NMEALongitude, NMEAh, NMEAtime, GPSSatList);

						if (SatRTCM.Count() > 0 && GPSEphemeride != null
								&& satEleAziData != null) {
							GNSSDGPSResult = calculateFinalDGPSresult(
									nmeaPosition, SatRTCM, GPSEphemeride,
									GLONASSEphemeris, satEleAziData, 0);

						} else {
							GNSSDGPSResult = nmeaPosition;
						}

						FlagGPRMC = false;
						FlagGPGSA = false;
						FlagGPGGA = false;

					}
				} else if (FlagHasGNGSA && FlagHasGNGNS) {
					if (GNSSSatNum != null) {
						resultsBundle.putString("SatNum", GNSSSatNum);
						satNumForRecord = String.valueOf(GNSSSatNum);

					}
					if (GNSSSatList != null) {
						resultsBundle.putString("SatList",
								GNSSSatList.dispSatList());
						satListForRecord = GNSSSatList.dispSatList();

					}
					if (GNSSmode != null) {
						resultsBundle.putString("GNSS_Mode", GNSSmode);
					}

					if (FlagGNGSA && FlagGNGNS && FlagGPRMC) {
						if (GNSSmode.equals("AA") || GNSSmode.equals("DD")
								|| GNSSmode.equals("AD")
								|| GNSSmode.equals("DA")) {
							// fix situation: GLONASS +GPS
							NMEALatitude = GNSSNMEALatitude;
							NMEALongitude = GNSSNMEALongitude;
							NMEAh = GNSSNMEAh;
							nmeaPosition = new NMEAPosition(NMEALatitude,
									NMEALongitude, NMEAh, NMEAtime, GNSSSatList);
							if (SatRTCM.Count() > 0 && GPSEphemeride != null
									&& satEleAziData != null
									&& GLONASSEphemeris != null) {
								// Calculate Rectified Location
								GNSSDGPSResult = calculateFinalDGPSresult(
										nmeaPosition, SatRTCM, GPSEphemeride,
										GLONASSEphemeris, satEleAziData, 1);
							}

						} else if (GNSSmode.equals("AN")
								|| GNSSmode.equals("DN")) {
							// fix situation: GPS only
							NMEALatitude = GNSSNMEALatitude;
							NMEALongitude = GNSSNMEALongitude;
							NMEAh = GNSSNMEAh;
							nmeaPosition = new NMEAPosition(NMEALatitude,
									NMEALongitude, NMEAh, NMEAtime, GNSSSatList);
							if (SatRTCM.Count() > 0 && GPSEphemeride != null
									&& satEleAziData != null
									&& GLONASSEphemeris != null) {
								GNSSDGPSResult = calculateFinalDGPSresult(
										nmeaPosition, SatRTCM, GPSEphemeride,
										GLONASSEphemeris, satEleAziData, 0);
							}
						}

						else {
							GNSSDGPSResult = nmeaPosition;
						}

						FlagGNGSA = false;
						FlagGNGNS = false;
						FlagGPRMC = false;
					}

				} else if (FlagHasGNGSA && !FlagHasGNGNS) {
					if (GPSSatNum != null) {
						resultsBundle.putString("SatNum", GPSSatNum);
						satNumForRecord = String.valueOf(GPSSatNum);

					}
					if (GNSSSatList != null) {
						resultsBundle.putString("SatList",
								GNSSSatList.dispSatList());
						satListForRecord = GNSSSatList.dispSatList();

					}
					if (GPSmode != null) {
						resultsBundle.putString("GNSS_Mode", GPSmode);
					}

					if (FlagGNGSA && FlagGPGGA && FlagGPRMC) {
						if (GPSmode.equals("1")) {
							// cellphone: GLONASS + GPS ; fix situation: GLONASS
							// +
							// GPS
							NMEALatitude = GPSNMEALatitude;
							NMEALongitude = GPSNMEALongitude;
							NMEAh = GPSNMEAh;
							nmeaPosition = new NMEAPosition(NMEALatitude,
									NMEALongitude, NMEAh, NMEAtime, GNSSSatList);
							if (SatRTCM.Count() > 0 && GPSEphemeride != null
									&& satEleAziData != null
									&& GLONASSEphemeris != null) {
								boolean satHasGNSS = satHasGNSS(GNSSSatList);
								if (satHasGNSS) {
									GNSSDGPSResult = calculateFinalDGPSresult(
											nmeaPosition, SatRTCM,
											GPSEphemeride, GLONASSEphemeris,
											satEleAziData, 1);
								} else {
									GNSSDGPSResult = calculateFinalDGPSresult(
											nmeaPosition, SatRTCM,
											GPSEphemeride, GLONASSEphemeris,
											satEleAziData, 0);
								}

							}

						} else {
							GNSSDGPSResult = nmeaPosition;
						}

						FlagGNGSA = false;
						FlagGNGNS = false;
						FlagGPRMC = false;
					}

				} else {
					GNSSDGPSResult = nmeaPosition;

				}
				if (GNSSDGPSResult != null && nmeaPosition != null) {
					resultsBundle.putString("FinalLatitude",
							format2.format(GNSSDGPSResult.NEH.N));
					resultsBundle.putString("FinalLongitude",
							format2.format(GNSSDGPSResult.NEH.E));
					resultsBundle.putString("FinalAltitude",
							format2.format(GNSSDGPSResult.NEH.H));
					_finalResultPositionDGPSPlusIon = GNSSDGPSResult;

				} else if (GNSSDGPSResult == null && nmeaPosition != null) {

					resultsBundle.putString("FinalLatitude",
							format2.format(nmeaPosition.NEH.N));
					resultsBundle.putString("FinalLongitude",
							format2.format(nmeaPosition.NEH.E));
					resultsBundle.putString("FinalAltitude",
							format2.format(nmeaPosition.NEH.H));
					_finalResultPositionDGPSPlusIon = nmeaPosition;
				}
				if (nmeaPosition != null) {
					resultsBundle.putString("Latitude",
							format2.format(nmeaPosition.NEH.N));
					resultsBundle.putString("Longitude",
							format2.format(nmeaPosition.NEH.E));
					resultsBundle.putString("Altitude",
							format2.format(nmeaPosition.NEH.H));
				}
				if (GNSSDGPSResult != null && nmeaPosition != null) {
					String deltOutput = "";
					double deltN = nmeaPosition.NEH.N - GNSSDGPSResult.NEH.N;
					double deltE = nmeaPosition.NEH.E - GNSSDGPSResult.NEH.E;
					double deltH = nmeaPosition.NEH.H - GNSSDGPSResult.NEH.H;
					deltOutput = "deltN:" + format2.format(deltN) + "	deltE:"
							+ format2.format(deltE) + "	deltH:"
							+ format2.format(deltH) + "\n";

					resultsBundle.putString("Delt", deltOutput);

				}

			}
		} catch (Exception e) {

		}
		long p2 = System.nanoTime() - p1;
		Log.i("TIME", "Process:" + String.valueOf(p2));
		return resultsBundle;

	}

	private boolean satHasGNSS(SatList satList) {
		// TODO Auto-generated method stub
		// boolean whether the satellites used contains GLOASS
		boolean result = false;
		for (int i = 0; i < satList.satCount; i++) {
			double satId = Double.valueOf(satList.satPRNList[i]);
			if (satId > 64) {
				result = true;
				i = satList.satCount;
			}
		}

		return result;
	}

	private NMEAPosition calculateFinalDGPSresult(NMEAPosition nmeaPosition,
			SatelliteRTCMList SatRTCM, GPSEphemeris GPSEphemeride,
			GLONASSEphemeris GLONASSEphemeris,
			SatelliteELEAZIList satEleAziData, int GNSSmode) {
		// Calculate the coorection: AX= L
		NMEAPosition resultPositon = nmeaPosition;
		String[] nList = nmeaPosition.satlist.satPRNList;
		int numberOfSat = nmeaPosition.satlist.satCount;

		// ***********FORM L MATRIX***********************>>>>>>
		double[] lMatrix = new double[numberOfSat];
		String satDGPSforRecord = "";
		FlagNMEAandRTCMSatListFullMatched = true;
		for (int i = 0; i < numberOfSat; i++) {
			int rtcmIndex = SatRTCM.Match(Integer.valueOf(nList[i]));
			int eleIndex = satEleAziData.Match(Integer.valueOf(nList[i]));
			if (rtcmIndex != -1 && eleIndex != -1) {
				SatelliteRTCM rtcmUnit = SatRTCM.SatelliteRTCM(rtcmIndex);
				double RTCMPRC = rtcmUnit.pseudorangeCorrection;
				double RTCMRRC = GNSSCorrection.rrcCorrection(nmeaPosition,
						rtcmUnit);
				double RTCMcorrection = RTCMPRC + RTCMRRC;
				String RTCMZcount = String.valueOf(rtcmUnit.modernZ);
				lMatrix[i] = RTCMcorrection;
				satDGPSforRecord += "#RTCM," + nList[i] + ","
						+ format2.format(lMatrix[i]) + "," + RTCMZcount + "\n";
			} else {
				FlagNMEAandRTCMSatListFullMatched = false;
				Log.i("GLONASS", "L NOT MATCHED");

			}
		}

		Matrix L = new Matrix(lMatrix, lMatrix.length);

		if (FlagNMEAandRTCMSatListFullMatched && numberOfSat > 3) {

			NMEAPosition receiverPosition = nmeaPosition;
			receiverPosition = nmeaPosition;
			NMEAPosition finalResultPositionDGPSPlusIon = ReceiverPositionBasedOnDGPS(
					numberOfSat, receiverPosition, GPSEphemeride,
					GLONASSEphemeris, nList, L, 3, GNSSmode);

			resultOutput = "#DATE," + nmeaPosition.time.display() + "\n"
					+ "#WGS84," + recordresult1 + ",," + recordresult2 + ",,"
					+ recordresult3 + ",,NMEA,"
					+ format2.format(nmeaPosition.xyz.x) + ","
					+ format2.format(nmeaPosition.xyz.y) + ","
					+ format2.format(nmeaPosition.xyz.z) + "\n" + "#HK80,"
					+ recordresult11 + ",," + recordresult21 + ",,"
					+ recordresult31 + ",,NMEA,"
					+ format2.format(nmeaPosition.NEH.N) + ","
					+ format2.format(nmeaPosition.NEH.E) + ","
					+ format2.format(nmeaPosition.NEH.H) + "\n" + "#DELTXYZ,"
					+ deltXforRecord + "," + deltYforRecord + ","
					+ deltZforRecord + "\n" + "#DELTNEH," + deltNforRecord
					+ "," + deltEforRecord + "," + deltHforRecord + "\n"
					+ "#PDOP," + NMEAPDOP + "," + DGPSPDOP + "\n" + "#SATNUM,"
					+ satNumForRecord + "\n" + satDGPSforRecord + ionTropOutput
					+ satPositionOutput + AmatrixForRecord + LmatrixForRecord;
			CustomLog.recordResult(FILENAMEFORRECORD, resultOutput);
			resultPositon = finalResultPositionDGPSPlusIon;

		} else {
			resultOutput = "#DATE," + nmeaPosition.time.display() + "\n"
					+ "#NOTMATCH," + format2.format(nmeaPosition.xyz.x) + ","
					+ format2.format(nmeaPosition.xyz.y) + ","
					+ format2.format(nmeaPosition.xyz.z) + "\n";
			CustomLog.recordResult(FILENAMEFORRECORD, resultOutput);

		}
		return resultPositon;

	}

	private String[] findSatList(String[] seperaNMEA) {
		// TODO Auto-generated method stub
		String[] tempresult = null;
		String[] result = null;

		if (seperaNMEA[2].equals("0")) {
			return null;
		} else {
			int satCount = 0;
			for (int i = 3; i < 15; i++) {
				if (!seperaNMEA[i].isEmpty()) {
					satCount += 1;
				}

			}
			tempresult = new String[satCount];
			int satIndex = 0;
			for (int i = 3; i < 15; i++) {
				if (!seperaNMEA[i].isEmpty()) {
					tempresult[satIndex] = seperaNMEA[i];
					satIndex += 1;
				}

			}

			int length = satIndex;
			int QNZZNum = 0;

			for (int i = 0; i < length; i++) {
				double satID = Double.valueOf(tempresult[i]);
				if (satID > 100) {
					QNZZNum += 1;
				}
			}

			result = new String[length - QNZZNum];
			int resultIndex = 0;
			for (int i = 0; i < length; i++) {
				double firstSat = Double.valueOf(tempresult[i]);
				if (firstSat > 191) {
					Log.i("QNZZ", String.valueOf(i));
				} else {
					result[resultIndex] = tempresult[i];
					resultIndex += 1;

				}
			}

		}
		return result;

	}

	private NMEAPosition ReceiverPositionBasedOnDGPS(int numberOfSat,
			NMEAPosition receiverPosition, GPSEphemeris GPSEphemeride,
			GLONASSEphemeris GLONASSEphemeris, String[] nList, Matrix L,
			int index, int GNSSmode) {
		// TODO Auto-generated method stub
		// calculate the Position
		double receiverClockErr = 0;
		boolean FlagNMEAandSatListFullMatched = true;
		double[][] aMatrix = null;
		double[] elevationAngle = new double[numberOfSat];
		double[] azimuthAngle = new double[numberOfSat];

		double[] ionCorrection = new double[numberOfSat];
		double[] tropCorrection = new double[numberOfSat];
		TIME requestTime = receiverPosition.time;
		satPositionOutput = "";
		ionTropOutput = "";
		NMEAPosition resultPosition = null;

		if (GNSSmode == 0) {
			// GPS
			aMatrix = new double[numberOfSat][4];

		} else if (GNSSmode == 1) {
			// GPS+GNSS
			aMatrix = new double[numberOfSat][5];
		}
		// ***********FORM A MATRIX***********************<<<<<<
		for (int i = 0; i < numberOfSat; i++) {
			int SatID = Integer.valueOf(nList[i]);
			if (SatID < 33) {

				// GPS
				Log.i("SatID", "G" + nList[i]);
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
					double nmeaToSatdeltX = receiverPosition.xyz.x
							- satPosition.XX;
					double nmeaToSatdeltY = receiverPosition.xyz.y
							- satPosition.YY;
					double nmeaToSatdeltZ = receiverPosition.xyz.z
							- satPosition.ZZ;
					double nmeaToSatDistance = Math.sqrt(Math.pow(
							nmeaToSatdeltX, 2)
							+ Math.pow(nmeaToSatdeltY, 2)
							+ Math.pow(nmeaToSatdeltZ, 2));
					aMatrix[i][0] = (nmeaToSatdeltX / nmeaToSatDistance);
					aMatrix[i][1] = (nmeaToSatdeltY / nmeaToSatDistance);
					aMatrix[i][2] = (nmeaToSatdeltZ / nmeaToSatDistance);
					aMatrix[i][3] = 1;
					if (GNSSmode == 1) {
						aMatrix[i][4] = 0;
					}
					// Calculate elevation angle based on satellite position
					elevationAngle[i] = GPSEphemeride.getElevationAngle(
							receiverPosition, nmeaToSatdeltX, nmeaToSatdeltY,
							nmeaToSatdeltZ);
					// Calculate azimuth angle based on satellite position
					azimuthAngle[i] = GPSEphemeride.getAzimuth(
							receiverPosition, nmeaToSatdeltX, nmeaToSatdeltY,
							nmeaToSatdeltZ);
					// Calculate ionosphere correction
					ionCorrection[i] = GNSSCorrection.ionCorrection(
							GPSEphemeride, elevationAngle[i], azimuthAngle[i],
							GPSEphemeride.nHead, nmeaPosition);
					// Calculate troposphere correction
					tropCorrection[i] = GNSSCorrection.tropCorrection(
							elevationAngle[i], receiverPosition.h);
					satPositionOutput += satPosition.displayPosition();
					ionTropOutput += "#ATMO," + nList[i] + ","
							+ format2.format(elevationAngle[i]) + ","
							+ format2.format(azimuthAngle[i]) + ","
							+ format2.format(ionCorrection[i]) + ","
							+ format2.format(tropCorrection[i]) + "\n";
				} else {
					FlagNMEAandSatListFullMatched = false;
				}

			} else if (SatID > 64) {
				// GLONASS
				Log.i("SatID", "R" + nList[i]);

				int satEphIndex = GLONASSEphemeris.FindTheBestFitTime(
						requestTime, Integer.valueOf(nList[i]));
				Log.i("SatID", "R" + nList[i]);

				if (satEphIndex != -1) {

					GPSTIME requestGPSTime = GLONASSEphemeris
							.TimeToGpsTime(requestTime);

					SatPosition satPosition = GLONASSEphemeris
							.SatellitePositionWithRotation(
									GLONASSEphemeris.nData.get(satEphIndex),
									requestGPSTime, receiverPosition,
									receiverClockErr);
					double nmeaToSatdeltX = receiverPosition.xyz.x
							- satPosition.XX;
					double nmeaToSatdeltY = receiverPosition.xyz.y
							- satPosition.YY;
					double nmeaToSatdeltZ = receiverPosition.xyz.z
							- satPosition.ZZ;
					double nmeaToSatDistance = Math.sqrt(Math.pow(
							nmeaToSatdeltX, 2)
							+ Math.pow(nmeaToSatdeltY, 2)
							+ Math.pow(nmeaToSatdeltZ, 2));
					aMatrix[i][0] = (nmeaToSatdeltX / nmeaToSatDistance);
					aMatrix[i][1] = (nmeaToSatdeltY / nmeaToSatDistance);
					aMatrix[i][2] = (nmeaToSatdeltZ / nmeaToSatDistance);
					aMatrix[i][3] = 1;
					if (GNSSmode == 1) {
						aMatrix[i][4] = 1;
					}
					// Calculate elevation angle
					elevationAngle[i] = GLONASSEphemeris.getElevationAngle(
							receiverPosition, nmeaToSatdeltX, nmeaToSatdeltY,
							nmeaToSatdeltZ);
					// Calculate azimuth angle
					azimuthAngle[i] = GLONASSEphemeris.getAzimuth(
							receiverPosition, nmeaToSatdeltX, nmeaToSatdeltY,
							nmeaToSatdeltZ);
					// Calculate ionosphere correction
					ionCorrection[i] = GNSSCorrection.GLONASionCorrection(
							GPSEphemeride, elevationAngle[i], azimuthAngle[i],
							GPSEphemeride.nHead, nmeaPosition, SatID);
					// Calculate troposphere correction
					tropCorrection[i] = GNSSCorrection.tropCorrection(
							elevationAngle[i], receiverPosition.h);

					satPositionOutput += satPosition.displayPosition();
					ionTropOutput += "#ATMO," + nList[i] + ","
							+ format2.format(elevationAngle[i]) + ","
							+ format2.format(azimuthAngle[i]) + ","
							+ format2.format(ionCorrection[i]) + ","
							+ format2.format(tropCorrection[i]) + "\n";
				} else {
					FlagNMEAandSatListFullMatched = false;
					Log.i("SatID", "NOT FULL MATCHED");

				}

			}

		}

		if (FlagNMEAandSatListFullMatched) {
			Matrix A = new Matrix(aMatrix);
			Matrix ion = new Matrix(ionCorrection, ionCorrection.length);
			Matrix trop = new Matrix(tropCorrection, tropCorrection.length);
			if (GNSSmode == 1) {
				// GLONASS RTCM correction Matrix
				double[] gloCorrection = new double[5];
				gloCorrection[0] = -8.873527;
				gloCorrection[1] = -3.9332;
				gloCorrection[2] = 0.02121272;
				gloCorrection[3] = 0;
				gloCorrection[4] = 0;
				Matrix GloCorrection = new Matrix(gloCorrection,
						gloCorrection.length);
				Matrix LCorrection = A.times(GloCorrection);
				for (int i = 0; i < numberOfSat; i++) {
					int Sate = Integer.valueOf(nList[i]);
					if (Sate < 33) {
						LCorrection.set(i, 0, 0);
					}
				}
				L = L.plus(LCorrection);

			}
			try {
				Matrix result = null;
				if (index == 1) {
					result = A.solve(L);
				} else if (index == 2) {
					result = A.solve(L);
				} else if (index == 3) {
					// index == 3 calculate results
					L = L.plus(ion);
					L = L.plus(trop);
					result = A.solve(L);
					AmatrixForRecord = "";
					int AColumn = A.getColumnDimension();
					int ARow = A.getRowDimension();
					String RowString = "#A";
					for (int i = 0; i < ARow; i++) {
						for (int j = 0; j < AColumn; j++) {
							RowString += "," + String.valueOf(A.get(i, j));
						}
						AmatrixForRecord += RowString + "\n";
						RowString = "#A";
					}
					int LRow = L.getRowDimension();
					LmatrixForRecord = "";
					for (int i = 0; i < LRow; i++) {
						LmatrixForRecord += "#L," + String.valueOf(L.get(i, 0))
								+ "\n";
					}

				}
				double XforResult = receiverPosition.xyz.x + result.get(0, 0);
				double YforResult = receiverPosition.xyz.y + result.get(1, 0);
				double ZforResult = receiverPosition.xyz.z + result.get(2, 0);
				deltXforRecord = format2.format(result.get(0, 0));
				deltYforRecord = format2.format(result.get(1, 0));
				deltZforRecord = format2.format(result.get(2, 0));

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
				deltNforRecord = format2.format(resultPosition.NEH.N
						- receiverPosition.NEH.N);
				deltEforRecord = format2.format(resultPosition.NEH.E
						- receiverPosition.NEH.E);
				deltHforRecord = format2.format(resultPosition.NEH.H
						- receiverPosition.NEH.H);
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
					typeResult = "#DGPS+i+t-old,";
					recordresult2 = typeResult
							+ format2.format(resultPosition.xyz.x) + ","
							+ format2.format(resultPosition.xyz.y) + ","
							+ format2.format(resultPosition.xyz.z);
					recordresult21 = typeResult
							+ format2.format(resultPosition.NEH.N) + ","
							+ format2.format(resultPosition.NEH.E) + ","
							+ format2.format(resultPosition.NEH.H);
				} else if (index == 3) {
					typeResult = "#DGPS,";
					recordresult3 = typeResult
							+ format2.format(resultPosition.xyz.x) + ","
							+ format2.format(resultPosition.xyz.y) + ","
							+ format2.format(resultPosition.xyz.z);
					recordresult31 = typeResult
							+ format2.format(resultPosition.NEH.N) + ","
							+ format2.format(resultPosition.NEH.E) + ","
							+ format2.format(resultPosition.NEH.H);
				}

			} catch (Exception e) {

			}
		}
		return resultPosition;
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
