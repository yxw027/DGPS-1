package com.polyu.GLONASSEphemeris;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.json.JSONException;

import android.util.Log;

import com.polyu.GPSEphemeris.GPSnFileData;
import com.polyu.dgnss.ConstantClass;
import com.polyu.position.GLONASSSatPosition;
import com.polyu.position.NMEAPosition;
import com.polyu.position.SatPosition;
import com.polyu.time.GPSTIME;
import com.polyu.time.TIME;

public class GLONASSEphemeris {
	public GLONASSnFileHead nHead;
	public ArrayList<GLONASSnFileData> nData;

	public GLONASSEphemeris() {
		this.nData = new ArrayList<GLONASSnFileData>();
	}

	public boolean Read(InputStream inputStream) throws IOException,
			JSONException {
		// Read GLONASS Ephemeris file and stored in GLONASS Ephemeris
		GLONASSnFileData nDataTemp = null;
		double storeREINEX_Version = 0;
		String storeTYPE = null;
		String storePGM = null;
		String storeRUN_By = null;
		TIME storeUTC_Date = null;
		int storeLEAP_SECOND = 0;

		int tempbyPRN = 0;
		TIME tempTOC = null;
		double tempTauN = 0;
		double tempGammaN = 0;
		double temptk = 0;
		double tempSatPositionX = 0;
		double tempSatPositionY = 0;
		double tempSatPositionZ = 0;
		double tempVelocityX = 0;
		double tempVelocityY = 0;
		double tempVelocityZ = 0;

		double tempAccelertionX = 0;
		double tempAccelertionY = 0;
		double tempAccelertionZ = 0;

		double tempHealth = 0;
		double tempK = 0;
		double tempE = 0;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream));
			String lineText = reader.readLine();
			int count = 1;
			String TAG = "GLO N FILE";
			do {
				Log.i(TAG, "Loop:" + String.valueOf(count));
				Log.i(TAG, lineText);
				if (lineText.contains("RINEX VERSION / TYPE")) {
					Log.i(TAG, "-------RINEX VERSION / TYPE----------START");
					storeREINEX_Version = Double.valueOf(lineText.substring(1,
							20));
					storeTYPE = lineText.substring(20, 40);
					Log.i(TAG, "-------RINEX VERSION / TYPE----------END");

				}
				if (lineText.contains("PGM / RUN BY / DATE")) {
					Log.i(TAG, "PGM / RUN BY / DATE----------START");
					storePGM = lineText.substring(0, 20);
					storeRUN_By = lineText.substring(21, 40);
					// storeUTC_Date = GLONASSHeadTime(lineText.substring(40,
					// 60));
					Log.i(TAG, "PGM / RUN BY / DATEA----------END");

				}

				if (lineText.contains("LEAP SECONDS")) {
					Log.i(TAG, "LEAP SECONDS-----START");
					storeLEAP_SECOND = atoi(lineText.substring(0, 6));
					Log.i(TAG, "LEAP SECONDS-----END");

				}// 由于跳秒而造成的时间差
				lineText = reader.readLine();
				count++;
			} while (lineText.contains("END OF HEADER") == false);
			Log.i(TAG, "ADD nHEAD-----START");
			this.nHead = new GLONASSnFileHead(storeREINEX_Version, storeTYPE,
					storePGM, storeRUN_By, storeUTC_Date, storeLEAP_SECOND);
			Log.i(TAG, "ADD nHEAD-----END");

			int i = 0;
			lineText = reader.readLine();

			do {

				switch (i) {
				case 0: {
					Log.i(TAG, "CASE 0----------START");
					tempbyPRN = atoi(lineText.substring(0, 2)) + 64;
					int tempTOCwYear = atoi(lineText.substring(3, 5)) + 2000;
					int tempTOCbyMonth = atoi(lineText.substring(5, 8));
					int tempTOCbyDay = atoi(lineText.substring(8, 11));
					int tempTOCbyHour = atoi(lineText.substring(11, 14));
					int tempTOCbyMinute = atoi(lineText.substring(14, 17));
					int tempTOCdSecond = atoi(lineText.substring(17, 22));
					tempTOC = new TIME(tempTOCwYear, tempTOCbyMonth,
							tempTOCbyDay, tempTOCbyHour, tempTOCbyMinute,
							tempTOCdSecond);
					tempTauN = atof(lineText.substring(22, 41));
					tempGammaN = atof(lineText.substring(41, 60));
					temptk = atof(lineText.substring(60, 79));
					Log.i(TAG, "CASE 0----------END");

				}
					break;
				case 1: {
					Log.i(TAG, "CASE 1----------START");

					tempSatPositionX = atof(lineText.substring(3, 22)) * 1000;
					tempVelocityX = atof(lineText.substring(22, 41)) * 1000;
					tempAccelertionX = atof(lineText.substring(41, 60)) * 1000;
					tempHealth = atof(lineText.substring(60, 79));
					Log.i(TAG, "CASE 1----------END");

				}
					break;
				case 2: {
					Log.i(TAG, "CASE 2----------START");
					tempSatPositionY = atof(lineText.substring(3, 22)) * 1000;
					tempVelocityY = atof(lineText.substring(22, 41)) * 1000;
					tempAccelertionY = atof(lineText.substring(41, 60)) * 1000;
					tempK = atof(lineText.substring(60, 79));
					Log.i(TAG, "CASE 2----------END");

				}
					break;
				case 3: {
					Log.i(TAG, "CASE 3----------START");
					tempSatPositionZ = atof(lineText.substring(3, 22)) * 1000;
					tempVelocityZ = atof(lineText.substring(22, 41)) * 1000;
					tempAccelertionZ = atof(lineText.substring(41, 60)) * 1000;
					tempE = atof(lineText.substring(60, 79));
					Log.i(TAG, "CASE 3----------END");

				}
					break;

				}

				++i;
				i = i % 4;
				if (i == 0) {

					Log.i(TAG, "-------ADD--------------------START");

					nDataTemp = new GLONASSnFileData(tempbyPRN, tempTOC,
							tempTauN, tempGammaN, temptk, tempSatPositionX,
							tempSatPositionY, tempSatPositionZ, tempVelocityX,
							tempVelocityY, tempVelocityZ, tempAccelertionX,
							tempAccelertionY, tempAccelertionZ, tempHealth,
							tempK, tempE);
					this.nData.add(nDataTemp);
					Log.i(TAG, "-------ADD--------------------END");

				}
				lineText = reader.readLine(); // Log.i(TAG, lineText);

			} while (lineText != null && lineText != "EOF");

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	private TIME GLONASSHeadTime(String s) {
		// TODO Auto-generated method stub
		TIME result = null;
		int year = 0;
		int mon = 0;
		int day = 0;
		int hour = 0;
		int min = 0;
		int sec = 0;
		year = Integer.valueOf(s.substring(0, 4));
		mon = Integer.valueOf(s.substring(4, 6));
		day = Integer.valueOf(s.substring(6, 8));
		hour = Integer.valueOf(s.substring(9, 11));
		min = Integer.valueOf(s.substring(12, 14));
		sec = Integer.valueOf(s.substring(15, 17));
		result = new TIME(year, mon, day, hour, min, sec);
		return result;
	}

	private int atoi(String s) {
		// TODO Auto-generated method stub

		String x;
		double y;
		int result;

		x = s.replace(" ", "");
		y = Double.parseDouble(x);
		result = (int) y;

		return result;
	}

	private double atof(String s) {
		// TODO Auto-generated method stub
		int divide = s.indexOf("D");
		String pre = s.substring(0, divide);
		String post = s.substring(divide + 1);
		double partOne = Double.parseDouble(pre);
		double partTwo = Double.parseDouble(post);
		double result = partOne * (Math.pow(10, partTwo));
		return result;
	}

	public SatPosition SatellitePositionWithRotation(
			GLONASSnFileData nFileData, GPSTIME testGPSTime,
			NMEAPosition nmeaPosition, double receiverClockErr) {
		// TODO Auto-generated method stub
		GPSTIME tR = testGPSTime;
		// GPSTIME tE1 = new GPSTIME(0, 0);
		GPSTIME tE0 = tR;
		double travelT0;
		double travelT = 0;
		SatPosition satResult;
		SatPosition satResultPZ;
		double deltT;
		// double caliDeltT;
		SatPosition satAfterRotation;
		satResultPZ = this.SatellitePosition(nFileData, tE0);
		// satResult = PZ90toWGS84(satResultPZ);

		deltT = this.distance(satResultPZ, nmeaPosition)
				/ ConstantClass.SpeedOfLIGHT;
		travelT0 = deltT;

		do {

			travelT = travelT0;
			tE0 = new GPSTIME(tR.lWeek, tR.lSecond - travelT0);
			satResultPZ = this.SatellitePosition(nFileData, tE0);
			// satResult = PZ90toWGS84(satResultPZ);
			satAfterRotation = this.rotationCorrection(satResultPZ, travelT0);
			travelT0 = this.distance(satAfterRotation, nmeaPosition)
					/ ConstantClass.SpeedOfLIGHT - receiverClockErr;

		} while (Math.abs(travelT - travelT0) > 1E-14);
		// Position resultx = satResult;
		// SatPosition resultxx = satAfterRotation;
		SatPosition resultxx = satResultPZ;
		return resultxx;
	}

	private SatPosition PZ90toWGS84(SatPosition sat) {
		// TODO Auto-generated method stub
		SatPosition result = null;
		double PZ90_WGS84_X_OFFSET = 0.404;
		double PZ90_WGS84_Y_OFFSET = 0.357;
		double PZ90_WGS84_Z_OFFSET = -0.476;
		double PZ90_WGS84_SCALEFACTOR = -2.614e-9;
		double PZ90_WGS84_DOMEGA = -1.664e-6;
		double PZ90_WGS84_DPHI = -0.058e-6;
		;
		double PZ90_WGS84_DSIGMA = 0.118e-6;

		double PZ90_X = sat.XX;
		double PZ90_Y = sat.YY;
		double PZ90_Z = sat.ZZ;
		double WGS84_X = PZ90_WGS84_X_OFFSET
				+ (1 + PZ90_WGS84_SCALEFACTOR)
				* ((PZ90_X) + PZ90_WGS84_DOMEGA * (PZ90_Y) - PZ90_WGS84_DPHI
						* (PZ90_Z));
		double WGS84_Y = PZ90_WGS84_Y_OFFSET
				+ (1 + PZ90_WGS84_SCALEFACTOR)
				* (-PZ90_WGS84_DOMEGA * (PZ90_X) + (PZ90_Y) + PZ90_WGS84_DSIGMA
						* (PZ90_Z));
		double WGS84_Z = PZ90_WGS84_Z_OFFSET
				+ (1 + PZ90_WGS84_SCALEFACTOR)
				* (PZ90_WGS84_DPHI * (PZ90_X) - PZ90_WGS84_DSIGMA * (PZ90_Y) + (PZ90_Z));

		result = new SatPosition(WGS84_X, WGS84_Y, WGS84_Z, sat.PRN);
		return result;
	}

	public SatPosition rotationCorrection(SatPosition satResult, double t) {
		// TODO Auto-generated method stub
		SatPosition resultPos;
		double earthRotateAngle = ConstantClass.Oearth * t;
		double xx = Math.cos(earthRotateAngle) * satResult.XX
				+ Math.sin(earthRotateAngle) * satResult.YY;
		double yy = -Math.sin(earthRotateAngle) * satResult.XX
				+ Math.cos(earthRotateAngle) * satResult.YY;
		double zz = satResult.ZZ;
		resultPos = new SatPosition(xx, yy, zz, satResult.PRN);
		return resultPos;
	}

	public double distance(SatPosition satResult, NMEAPosition nmeaPosition) {
		// TODO Auto-generated method stub
		double dist;
		dist = Math.sqrt(Math.pow((satResult.XX - nmeaPosition.xyz.x), 2)
				+ Math.pow((satResult.YY - nmeaPosition.xyz.y), 2)
				+ Math.pow((satResult.ZZ - nmeaPosition.xyz.z), 2));
		return dist;
	}

	public SatPosition SatellitePosition(GLONASSnFileData nFile, GPSTIME t) {
		// TODO Auto-generated method stub
		// GLONASSSatPosition result = null;
		SatPosition resultSat;
		double h = 0;
		double dt = 0;
		double n = 0;
		double[] pos = new double[3];
		double[] vel = new double[3];
		double[] accelerate = new double[3];
		double[][] k = new double[6][4];

		GPSTIME GPStimenFile = this.TimeToGpsTime(nFile.TOC);
		// GPSTIME GPStimeT = this.TimeToGpsTime(t);
		double gpst = GPStimenFile.lSecond;
		double tt = t.lSecond - 16;
		if (tt >= gpst) {
			h = 60;
		} else {
			h = -60;
		}
		dt = tt - gpst;
		n = Math.floor(Math.abs(dt / h));
		for (int i = 0; i <= n; i++) {
			if (i == 0) {
				pos[0] = nFile.SatPositionX;
				pos[1] = nFile.SatPositionY;
				pos[2] = nFile.SatPositionZ;

				vel[0] = nFile.velocityX;
				vel[1] = nFile.velocityY;
				vel[2] = nFile.velocityZ;

				accelerate[0] = nFile.accelerationX;
				accelerate[1] = nFile.accelerationY;
				accelerate[2] = nFile.accelerationZ;

			} else if (i == n) {
				h = dt - n * h;
			}
			double[] acc1 = acceleration(pos, vel, accelerate);
			for (int j = 0; j < 3; j++) {
				k[j][0] = h * acc1[j];
				k[j + 3][0] = h * vel[j];
			}

			double[] tempPos = new double[3];
			double[] tempVel = new double[3];

			for (int j = 0; j < 3; j++) {
				tempPos[j] = pos[j] + k[j + 3][0] / 2;
				tempVel[j] = vel[j] + k[j][0] / 2;
			}
			double[] acc2 = acceleration(tempPos, tempVel, accelerate);
			for (int j = 0; j < 3; j++) {
				k[j][1] = h * acc2[j];
				k[j + 3][1] = h * (vel[j] + k[j][0] / 2);
			}

			for (int j = 0; j < 3; j++) {
				tempPos[j] = pos[j] + k[j + 3][1] / 2;
				tempVel[j] = vel[j] + k[j][1] / 2;
			}
			double[] acc3 = acceleration(tempPos, tempVel, accelerate);
			for (int j = 0; j < 3; j++) {
				k[j][2] = h * acc3[j];
				k[j + 3][2] = h * (vel[j] + k[j][1] / 2);
			}

			for (int j = 0; j < 3; j++) {
				tempPos[j] = pos[j] + k[j + 3][2];
				tempVel[j] = vel[j] + k[j][2];
			}
			double[] acc4 = acceleration(tempPos, tempVel, accelerate);
			for (int j = 0; j < 3; j++) {
				k[j][3] = h * acc4[j];
				k[j + 3][3] = h * (vel[j] + k[j][2]);
			}
			for (int j = 0; j < 3; j++) {
				vel[j] = vel[j]
						+ (k[j][0] + 2 * k[j][1] + 2 * k[j][2] + k[j][3]) / 6;
				pos[j] = pos[j]
						+ (k[j + 3][0] + 2 * k[j + 3][1] + 2 * k[j + 3][2] + k[j + 3][3])
						/ 6;
			}

		}
		resultSat = new SatPosition(pos[0], pos[1], pos[2], nFile.PRN);
		// result = new GLONASSSatPosition(resultSat, vel);

		return resultSat;
	}

	private double[] acceleration(double[] pos, double[] vel,
			double[] accelerate) {
		// TODO Auto-generated method stub
		double[] result = new double[3];
		double GM = ConstantClass.GM;
		double ae = ConstantClass.HalfAxisLong;
		double w = ConstantClass.Oearth;
		double C2 = -0.001082657;

		double R = Math.sqrt(Math.pow(pos[0], 2) + Math.pow(pos[1], 2)
				+ Math.pow(pos[2], 2));
		double R3 = Math.pow(R, 3);
		double R5 = Math.pow(R, 5);
		double R2 = Math.pow(R, 2);
		double ae2 = Math.pow(ae, 2);
		double pos22 = Math.pow(pos[2], 2);
		double w2 = Math.pow(w, 2);
		result[0] = -(GM / R3) * pos[0] + 1.5 * (C2 * GM * ae2 / R5) * pos[0]
				* (1 - 5 * pos22 / R2) + w2 * pos[0] + 2 * w * vel[1]
				+ accelerate[0];
		result[1] = -(GM / R3) * pos[1] + 1.5 * (C2 * GM * ae2 / R5) * pos[1]
				* (1 - 5 * pos22 / R2) + w2 * pos[1] - 2 * w * vel[0]
				+ accelerate[1];
		result[2] = -(GM / R3) * pos[2] + 1.5 * (C2 * GM * ae2 / R5) * pos[2]
				* (3 - 5 * pos22 / R2) + accelerate[2];
		return result;
	}

	public int FindTheBestFitTime(TIME oTime, int SatPRN) {
		int pn = 0;
		GPSTIME oGTime;
		GPSTIME nGTime;
		double max = 100000000;
		oGTime = TimeToGpsTime(oTime);
		for (int i = 0; i < this.nData.size(); i++) {
			if (SatPRN == this.nData.get(i).PRN) {
				nGTime = TimeToGpsTime(this.nData.get(i).TOC);
				if (Math.abs(nGTime.lSecond - oGTime.lSecond + 16) < max) {
					max = Math.abs(nGTime.lSecond - oGTime.lSecond + 16);
					pn = i;
				}
			}
		}
		if (max == 100000000) {
			pn = -1;
		}

		return pn;

	}

	public double getElevationAngle(NMEAPosition receiverPosition,
			double nmeaToSatdeltX, double nmeaToSatdeltY, double nmeaToSatdeltZ) {
		// TODO Auto-generated method stub
		double SPHI = Math.sin(receiverPosition.BLH.B);
		double CPHI = Math.cos(receiverPosition.BLH.B);
		double SLMB = Math.sin(receiverPosition.BLH.L);
		double CLMB = Math.cos(receiverPosition.BLH.L);
		double[] stationToSatellite = new double[3];
		stationToSatellite[0] = -nmeaToSatdeltX;
		stationToSatellite[1] = -nmeaToSatdeltY;
		stationToSatellite[2] = -nmeaToSatdeltZ;

		// COMPUTE ROTATION MATRIX
		double[][] DRMAT = new double[3][3];
		DRMAT[0][0] = -SPHI * CLMB;
		DRMAT[0][1] = -SPHI * SLMB;
		DRMAT[0][2] = CPHI;
		DRMAT[1][0] = -SLMB;
		DRMAT[1][1] = CLMB;
		DRMAT[1][2] = 0.0;
		DRMAT[2][0] = CPHI * CLMB;
		DRMAT[2][1] = CPHI * SLMB;
		DRMAT[2][2] = SPHI;

		// ROTATE ECCENTRICITIES
		double[] XLOC = new double[3];
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				XLOC[i] += DRMAT[i][j] * stationToSatellite[j];
			}
		}
		double XL12 = Math.sqrt(Math.pow(XLOC[0], 2) + Math.pow(XLOC[1], 2));
		double ZENITH = 0;
		if (XL12 != 0.0 || XLOC[2] != 0.0) {
			ZENITH = Math.atan2(XL12, XLOC[2]);
		}

		return Math.abs(90 - ZENITH * 180 / Math.PI);
	}

	public double getAzimuth(NMEAPosition receiverPosition,
			double nmeaToSatdeltX, double nmeaToSatdeltY, double nmeaToSatdeltZ) {
		// TODO Auto-generated method stub
		// station: LATITUDE,LONGITUDE,HEIGHT IN RADIAN,RADIAN,METERS
		// stationToSatellite: in meters
		// SIN AND COS FUNCTIONS

		double SPHI = Math.sin(receiverPosition.BLH.B);
		double CPHI = Math.cos(receiverPosition.BLH.B);
		double SLMB = Math.sin(receiverPosition.BLH.L);
		double CLMB = Math.cos(receiverPosition.BLH.L);
		double[] stationToSatellite = new double[3];
		stationToSatellite[0] = -nmeaToSatdeltX;
		stationToSatellite[1] = -nmeaToSatdeltY;
		stationToSatellite[2] = -nmeaToSatdeltZ;
		// COMPUTE ROTATION MATRIX
		double[][] DRMAT = new double[3][3];
		DRMAT[0][0] = -SPHI * CLMB;
		DRMAT[0][1] = -SPHI * SLMB;
		DRMAT[0][2] = CPHI;
		DRMAT[1][0] = -SLMB;
		DRMAT[1][1] = CLMB;
		DRMAT[1][2] = 0.0;
		DRMAT[2][0] = CPHI * CLMB;
		DRMAT[2][1] = CPHI * SLMB;
		DRMAT[2][2] = SPHI;
		double[] XLOC = new double[3];
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				XLOC[i] += DRMAT[i][j] * stationToSatellite[j];
			}
		}
		// ROTATE ECCENTRICITIES
		double AZIMUT = 0;
		if (XLOC[0] != 0.0 || XLOC[1] != 0.0) {
			AZIMUT = Math.atan2(XLOC[1], XLOC[0]);
		}
		if (AZIMUT < 0.0) {
			AZIMUT = AZIMUT + 2 * Math.PI;
		}
		return AZIMUT * 180 / Math.PI;
	}

	public GPSTIME TimeToGpsTime(TIME Time) {
		double JD, UT;
		long m, y;
		int WN;
		double Wsecond;
		UT = (double) Time.byHour + ((double) Time.byMinute) / 60
				+ ((double) Time.dSecond) / 3600;
		if (Time.byMonth <= 2) {
			y = Time.wYear - 1;
			m = Time.byMonth + 12;
		} else {
			y = Time.wYear;
			m = Time.byMonth;
		}
		JD = (int) (365.25 * y) + (int) (30.6001 * (m + 1)) + Time.byDay + UT
				/ 24 + 1720981.5;
		WN = (int) ((JD - 2444244.5) / 7);
		Wsecond = (JD - 2444244.5 - 7 * WN) * 86400;
		GPSTIME result = new GPSTIME(WN, Wsecond);
		return result;
	}

}
