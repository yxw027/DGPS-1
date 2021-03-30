package com.polyu.GPSEphemeris;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.json.JSONException;

import android.util.Log;

import com.polyu.dgnss.ConstantClass;
import com.polyu.position.NMEAPosition;
import com.polyu.position.SatPosition;
import com.polyu.time.GPSTIME;
import com.polyu.time.TIME;

public class GPSEphemeris {
	String TAG = "CGPS";
	public GPSnFileHead nHead;
	public ArrayList<GPSnFileData> nData;
	final static double GM = 3.986005e+014;
	final static double ROTATEofEARTH = 7.2921151467e-005;

	public GPSEphemeris() {
		this.nData = new ArrayList<GPSnFileData>();
	}

	public boolean Read(InputStream inputStream) throws IOException,
			JSONException {
		// Read GPS Ephemeris file and stored in GPS Ephemeris

		String TAG = "READ N ";
		// FileInputStream fis;
		GPSnFileData nDataTemp = null;
		double storeIonA0 = 0;
		double storeIonA1 = 0;
		double storeIonA2 = 0;
		double storeIonA3 = 0;
		double storeB0 = 0;
		double storeB1 = 0;
		double storeB2 = 0;
		double storeB3 = 0;
		double storeA0 = 0;
		double storeA1 = 0;
		int storeT = 0;
		int storeWeek = 0;
		long storeLeaps = 0;
		int tempbyPRN = 0;
		TIME tempTOC = null;
		double tempdClkBias = 0;
		double tempdClkDrift = 0;
		double tempdClkDriftRate = 0;
		double tempdIODE = 0;
		double tempdCrs = 0;
		double tempdn = 0;
		double tempdM0 = 0;
		double tempdCuc = 0;
		double tempd_e = 0;// 轨道偏心率
		double tempdCus = 0;
		double tempdSqrtA = 0;
		double tempdTOE = 0;
		double tempdCic = 0;
		double tempdOMEGA = 0;
		double tempdCis = 0;
		double tempdi0 = 0;
		double tempdCrc = 0;
		double tempdShenJinDianJao = 0;
		double tempdOMEGAdot = 0;
		double tempdi0Dot = 0;
		double tempdCodesOnL2Channel = 0;
		double tempdGpsWeek = 0;
		double tempdL2PDataFlage = 0;
		double tempdSatPrecise = 0;
		double tempdSatHealth = 0;
		double tempdTGD = 0;
		double tempdIODC = 0;
		double tempdTransmissionTimeOfMesseage = 0;
		double tempdFitInterval = 0;

		try {
			// fis = new FileInputStream(location);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream));
			String lineText = reader.readLine();
			int count = 1;
			do {
				Log.i(TAG, "Loop:" + String.valueOf(count));
				Log.i(TAG, lineText);
				if (lineText.contains("ION ALPHA")) {
					Log.i(TAG, "-------ION ALPHA----------START");
					storeIonA0 = atof(lineText.substring(2, 14));
					storeIonA1 = atof(lineText.substring(14, 26));
					storeIonA2 = atof(lineText.substring(26, 38));
					storeIonA3 = atof(lineText.substring(38, 50));
					Log.i(TAG, "-------ION ALPHA----------END");

				}// 历书中的电离层参数（B0~B3）
				if (lineText.contains("ION BETA")) {
					Log.i(TAG, "ION BETA----------START");
					storeB0 = atof(lineText.substring(2, 14));
					storeB1 = atof(lineText.substring(14, 26));
					storeB2 = atof(lineText.substring(26, 38));
					storeB3 = atof(lineText.substring(38, 50));
					Log.i(TAG, "ION BETA----------END");

				}// 历书中的电离层参数（A0~A3）
				if (lineText.contains("DELTA-UTC: A0,A1,T,W")) {
					Log.i(TAG, "DELTA-UTC: A0,A1,T,W---------START");
					storeA0 = atof(lineText.substring(3, 22));
					storeA1 = atof(lineText.substring(22, 41));
					storeT = atoi(lineText.substring(41, 50));
					storeWeek = atoi(lineText.substring(50, 59));
					Log.i(TAG, "DELTA-UTC: A0,A1,T,W---------END");

				}// 用于计算UTC时间的历书参数
					// A0，A1：多项式系数
					// T：UTC数据的参考时刻
					// W：UTC参考周数
				if (lineText.contains("LEAP SECONDS")) {
					Log.i(TAG, "LEAP SECONDS-----START");
					storeLeaps = atoi(lineText.substring(0, 6));
					Log.i(TAG, "LEAP SECONDS-----END");

				}// 由于跳秒而造成的时间差
				lineText = reader.readLine();
				count++;
			} while (lineText.contains("END OF HEADER") == false);
			Log.i(TAG, "ADD nHEAD-----START");
			this.nHead = new GPSnFileHead(storeIonA0, storeIonA1, storeIonA2,
					storeIonA3, storeB0, storeB1, storeB2, storeB3, storeA0,
					storeA1, storeT, storeWeek, storeLeaps);
			Log.i(TAG, "ADD nHEAD-----END");

			int i = 0;
			lineText = reader.readLine();

			do {

				switch (i) {
				case 0: {
					Log.i(TAG, "CASE 0----------START");
					tempbyPRN = atoi(lineText.substring(0, 2));
					int tempTOCwYear = atoi(lineText.substring(3, 5)) + 2000;
					int tempTOCbyMonth = atoi(lineText.substring(5, 8));
					int tempTOCbyDay = atoi(lineText.substring(8, 11));
					int tempTOCbyHour = atoi(lineText.substring(11, 14));
					int tempTOCbyMinute = atoi(lineText.substring(14, 17));
					int tempTOCdSecond = atoi(lineText.substring(17, 22));
					tempTOC = new TIME(tempTOCwYear, tempTOCbyMonth,
							tempTOCbyDay, tempTOCbyHour, tempTOCbyMinute,
							tempTOCdSecond);
					tempdClkBias = atof(lineText.substring(22, 41));
					tempdClkDrift = atof(lineText.substring(41, 60));
					tempdClkDriftRate = atof(lineText.substring(60, 79));
					Log.i(TAG, "CASE 0----------END");

				}
					break;
				case 1: {
					Log.i(TAG, "CASE 1----------START");

					tempdIODE = atof(lineText.substring(3, 22));
					tempdCrs = atof(lineText.substring(22, 41));
					tempdn = atof(lineText.substring(41, 60));
					tempdM0 = atof(lineText.substring(60, 79));
					Log.i(TAG, "CASE 1----------END");

				}
					break;
				case 2: {
					Log.i(TAG, "CASE 2----------START");

					tempdCuc = atof(lineText.substring(3, 22));
					tempd_e = atof(lineText.substring(22, 41));
					tempdCus = atof(lineText.substring(41, 60));
					tempdSqrtA = atof(lineText.substring(60, 79));
					Log.i(TAG, "CASE 2----------END");

				}
					break;
				case 3: {
					Log.i(TAG, "CASE 3----------START");

					tempdTOE = atof(lineText.substring(3, 22));
					Log.i("", lineText);
					tempdCic = atof(lineText.substring(22, 41));
					tempdOMEGA = atof(lineText.substring(41, 60));
					tempdCis = atof(lineText.substring(60, 79));
					Log.i(TAG, "CASE 3----------END");

				}
					break;
				case 4: {
					Log.i(TAG, "CASE 4----------START");

					tempdi0 = atof(lineText.substring(3, 22));
					tempdCrc = atof(lineText.substring(22, 41));
					tempdShenJinDianJao = atof(lineText.substring(41, 60));
					tempdOMEGAdot = atof(lineText.substring(60, 79));
					Log.i(TAG, "CASE 4----------END");

				}
					break;
				case 5: {
					Log.i(TAG, "CASE 5----------START");

					tempdi0Dot = atof(lineText.substring(3, 22));
					tempdCodesOnL2Channel = atof(lineText.substring(22, 41));
					tempdGpsWeek = atof(lineText.substring(41, 60));
					tempdL2PDataFlage = atof(lineText.substring(60, 79));
					Log.i(TAG, "CASE 5----------END");

				}
					break;
				case 6: {
					Log.i(TAG, "CASE 6----------START");

					tempdSatPrecise = atof(lineText.substring(3, 22));
					tempdSatHealth = atof(lineText.substring(22, 41));
					tempdTGD = atof(lineText.substring(41, 60));
					tempdIODC = atof(lineText.substring(60, 79));
					Log.i(TAG, "CASE 6----------END");

				}
					break;
				case 7: {
					Log.i(TAG, "CASE 7----------START");

					tempdTransmissionTimeOfMesseage = atof(lineText.substring(
							3, 22));
					tempdFitInterval = atof(lineText.substring(22, 41));
					Log.i(TAG, "CASE 7----------END");

				}
					break;
				}

				++i;
				i = i % 8;
				if (i == 0) {

					Log.i(TAG, "-------ADD--------------------START");
					nDataTemp = new GPSnFileData(tempbyPRN, tempTOC,
							tempdClkBias, tempdClkDrift, tempdClkDriftRate,
							tempdIODE, tempdCrs, tempdn, tempdM0, tempdCuc,
							tempd_e, tempdCus, tempdSqrtA, tempdTOE, tempdCic,
							tempdOMEGA, tempdCis, tempdi0, tempdCrc,
							tempdShenJinDianJao, tempdOMEGAdot, tempdi0Dot,
							tempdCodesOnL2Channel, tempdGpsWeek,
							tempdL2PDataFlage, tempdSatPrecise, tempdSatHealth,
							tempdTGD, tempdIODC,
							tempdTransmissionTimeOfMesseage, tempdFitInterval);
					this.nData.add(nDataTemp);
					Log.i(TAG, "-------ADD--------------------END");

				}
				lineText = reader.readLine();
				// Log.i(TAG, lineText);

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

	public int FindTheBestFitTime(TIME oTime, int SatPRN) {
		int pn = 0;
		GPSTIME oGTime;
		GPSTIME nGTime;
		double max = 100000000;
		oGTime = TimeToGpsTime(oTime);
		for (int i = 0; i < this.nData.size(); i++) {
			if (SatPRN == this.nData.get(i).byPRN) {
				nGTime = TimeToGpsTime(this.nData.get(i).TOC);
				if (Math.abs(nGTime.lSecond - oGTime.lSecond) < max) {
					max = Math.abs(nGTime.lSecond - oGTime.lSecond);
					pn = i;
				}
			}
		}
		if (max == 100000000) {
			pn = -1;
		}

		return pn;

	}

	public SatPosition SatellitePosition(GPSnFileData Satellit,
			GPSTIME SendSignTime) {
		SatPosition resultPos = null;
		GPSTIME t = SendSignTime;// 发射信号是的时间
		double tk;
		double n0;
		double A;
		double n;
		double M;
		double E0;
		double Et;
		double f;
		double u0;
		double uu;
		double rr;
		double ii;
		double uk;
		double rk;
		double ik;
		double Xk;
		double Yk;
		double L;
		// 计算tk
		// t = TimeToGpsTime(SendSignTime);
		tk = t.lWeek * 604800 + t.lSecond - Satellit.dGpsWeek * 604800
				- Satellit.dTOE;
		if (tk > 302400) {
			tk = tk - 604800;
		}
		if (tk < -302400) {
			tk = tk + 604800;
		}
		A = Math.pow(Satellit.dSqrtA, 2);
		n0 = Math.sqrt(GM / Math.pow(A, 3));
		n = n0 + Satellit.dn;
		M = Satellit.dM0 + n * tk;
		Et = M;
		// 跌代Et
		do {
			E0 = Et;
			Et = M + Satellit.d_e * Math.sin(E0);
		} while (Math.abs(Et - E0) > 1e-012);// 跌代结束
		f = Math.atan2(
				(Math.sqrt(1 - Math.pow(Satellit.d_e, 2)) * Math.sin(Et)),
				(Math.cos(Et) - Satellit.d_e));
		u0 = Satellit.dShenJinDianJao + f;
		uu = Satellit.dCuc * Math.cos(2 * u0) + Satellit.dCus
				* Math.sin(2 * u0);
		rr = Satellit.dCrc * Math.cos(2 * u0) + Satellit.dCrs
				* Math.sin(2 * u0);
		ii = Satellit.dCic * Math.cos(2 * u0) + Satellit.dCis
				* Math.sin(2 * u0);
		uk = u0 + uu;
		rk = Math.abs(A * (1 - Satellit.d_e * Math.cos(Et)) + rr);
		ik = Satellit.di0 + ii + Satellit.di0Dot * tk;
		Xk = rk * Math.cos(uk);
		Yk = rk * Math.sin(uk);// no problem
		L = Satellit.dOMEGA + (Satellit.dOMEGAdot - ROTATEofEARTH) * tk
				- ROTATEofEARTH * Satellit.dTOE;
		double XX = Xk * Math.cos(L) - Yk * Math.cos(ik) * Math.sin(L);
		double YY = Xk * Math.sin(L) + Yk * Math.cos(ik) * Math.cos(L);
		double ZZ = Yk * Math.sin(ik);
		int PP = Satellit.byPRN;
		resultPos = new SatPosition(XX, YY, ZZ, PP);

		return resultPos;

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

	private int atoi(String s) {
		// TODO Auto-generated method stub

		String x;
		double y;
		int result;

		x = s.replace(" ", "");
		y = Double.parseDouble(x);
		result = (int) y;
		Log.i(TAG, "Result: " + String.valueOf(result));

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
		Log.i(TAG, "Result: " + String.valueOf(result));
		return result;
	}

	public SatPosition SatellitePositionWithRotation(GPSnFileData nFileData,
			GPSTIME testGPSTime, NMEAPosition nmeaPosition,
			double receiverClockErr) {
		// TODO Auto-generated method stub
		GPSTIME tR = testGPSTime;
		// GPSTIME tE1 = new GPSTIME(0, 0);
		GPSTIME tE0 = tR;
		double travelT0;
		double travelT = 0;
		SatPosition satResult;
		double deltT;
		// double caliDeltT;
		SatPosition satAfterRotation;
		satResult = this.SatellitePosition(nFileData, tE0);
		deltT = this.distance(satResult, nmeaPosition)
				/ ConstantClass.SpeedOfLIGHT;
		travelT0 = deltT;

		do {

			travelT = travelT0;
			tE0 = new GPSTIME(tR.lWeek, tR.lSecond - travelT0);
			satResult = this.SatellitePosition(nFileData, tE0);
			satAfterRotation = this.rotationCorrection(satResult, travelT0);
			travelT0 = this.distance(satAfterRotation, nmeaPosition)
					/ ConstantClass.SpeedOfLIGHT - receiverClockErr;

		} while (Math.abs(travelT - travelT0) > 1E-14);
		// Position resultx = satResult;
		// SatPosition resultxx = satAfterRotation;
		SatPosition resultxx = satResult;
		return resultxx;
	}

	public double distance(SatPosition satResult, NMEAPosition nmeaPosition) {
		// TODO Auto-generated method stub
		double dist;
		dist = Math.sqrt(Math.pow((satResult.XX - nmeaPosition.xyz.x), 2)
				+ Math.pow((satResult.YY - nmeaPosition.xyz.y), 2)
				+ Math.pow((satResult.ZZ - nmeaPosition.xyz.z), 2));
		return dist;
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

	/*
	 * private double GPSTimeDelt(GPSTIME t0, GPSTIME t1) { // TODO
	 * Auto-generated method stub double result; result = t0.lWeek * 604800 +
	 * t0.lSecond - t1.lWeek * 604800 - t1.lSecond; return result; }
	 */
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

		return 90 - ZENITH * 180 / Math.PI;
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
}
