package com.polyu.nmea;

import android.util.Log;

import com.polyu.GPSEphemeris.GPSEphemeris;
import com.polyu.GPSEphemeris.GPSnFileHead;
import com.polyu.dgnss.ConstantClass;
import com.polyu.eleazi.SatelliteELEAZI;
import com.polyu.position.NMEAPosition;
import com.polyu.rtcm.SatelliteRTCM;
import com.polyu.time.GPSTIME;
import com.polyu.time.TIME;

public class GNSSCorrection {

	public static double ionCorrection(GPSEphemeris SatEphemeride,
			SatelliteELEAZI satelliteELEAZI, GPSnFileHead nHead,
			NMEAPosition nmeaPosition) {
		// TODO Auto-generated method stub
		TIME time = nmeaPosition.time;
		GPSTIME gpstime = SatEphemeride.TimeToGpsTime(time);
		double tgps = gpstime.lSecond;
		double lat = (nmeaPosition.latitude * Math.PI) / 180;
		double lon = (nmeaPosition.longitude * Math.PI) / 180;
		double el = (satelliteELEAZI.ele * Math.PI) / 180;
		double az = (satelliteELEAZI.azi * Math.PI) / 180;
		double alpha0 = nHead.IonA0;
		double alpha1 = nHead.IonA1;
		double alpha2 = nHead.IonA2;
		double alpha3 = nHead.IonA3;
		double beta0 = nHead.B0;
		double beta1 = nHead.B1;
		double beta2 = nHead.B2;
		double beta3 = nHead.B3;
		double elsm = el / Math.PI;
		// double azsm = az / Math.PI;
		double latsm = lat / Math.PI;
		double lonsm = lon / Math.PI;
		double psi = 0.0137 / (elsm + 0.11) - 0.022;
		double iono_lat = latsm + psi * Math.cos(az);
		if (iono_lat > 0.416) {
			iono_lat = 0.416;
		} else if (iono_lat < -0.416) {
			iono_lat = -0.416;
		}
		double iono_lon = lonsm + (psi * Math.sin(az))
				/ (Math.cos(iono_lat * Math.PI));
		double localt = 43200 * iono_lon + tgps;
		int kk = 0;
		while ((localt >= 86400.) & (kk < 10)) {
			localt = localt - 86400;
			kk = kk + 1;
		}
		while ((localt < 0.) & (kk < 10)) {
			localt = localt + 86400;
			kk = kk + 1;
		}
		if (kk == 10) {
			return -99999;
		}
		double latm = iono_lat + 0.064 * Math.cos((iono_lon - 1.617) * Math.PI);
		double per = ((beta3 * latm + beta2) * latm + beta1) * latm + beta0;
		if (per < 72000) {
			per = 72000;
		}
		double x = (Math.PI + Math.PI) * (localt - 50400) / per;
		double slantf = 0.53 - elsm;
		slantf = 1. + 16. * slantf * slantf * slantf;
		double amp = ((alpha3 * latm + alpha2) * latm + alpha1) * latm + alpha0;
		if (amp < 0) {
			amp = 0.;
		}
		double ionocorr = 0;
		if (Math.abs(x) < 1.57) {
			double xx = x * x;
			ionocorr = slantf
					* (5.e-9 + amp * (1. - 0.5 * xx + (xx * xx / 24)))
					* ConstantClass.SpeedOfLIGHT;
		} else {
			ionocorr = slantf * 5.e-9 * ConstantClass.SpeedOfLIGHT;
		}

		return ionocorr;
	}

	public static double ionCorrection(GPSEphemeris SatEphemeride, double ele,
			double azi, GPSnFileHead nHead, NMEAPosition nmeaPosition) {
		// TODO Auto-generated method stub
		TIME time = nmeaPosition.time;
		GPSTIME gpstime = SatEphemeride.TimeToGpsTime(time);
		double tgps = gpstime.lSecond;
		double lat = (nmeaPosition.latitude * Math.PI) / 180;
		double lon = (nmeaPosition.longitude * Math.PI) / 180;
		double el = (ele * Math.PI) / 180;
		double az = (azi * Math.PI) / 180;
		double alpha0 = nHead.IonA0;
		double alpha1 = nHead.IonA1;
		double alpha2 = nHead.IonA2;
		double alpha3 = nHead.IonA3;
		double beta0 = nHead.B0;
		double beta1 = nHead.B1;
		double beta2 = nHead.B2;
		double beta3 = nHead.B3;
		double elsm = el / Math.PI;
		// double azsm = az / Math.PI;
		double latsm = lat / Math.PI;
		double lonsm = lon / Math.PI;
		double psi = 0.0137 / (elsm + 0.11) - 0.022;
		double iono_lat = latsm + psi * Math.cos(az);
		if (iono_lat > 0.416) {
			iono_lat = 0.416;
		} else if (iono_lat < -0.416) {
			iono_lat = -0.416;
		}
		double iono_lon = lonsm + (psi * Math.sin(az))
				/ (Math.cos(iono_lat * Math.PI));
		double localt = 43200 * iono_lon + tgps;
		int kk = 0;
		while ((localt >= 86400.) & (kk < 10)) {
			localt = localt - 86400;
			kk = kk + 1;
		}
		while ((localt < 0.) & (kk < 10)) {
			localt = localt + 86400;
			kk = kk + 1;
		}
		if (kk == 10) {
			return -99999;
		}
		double latm = iono_lat + 0.064 * Math.cos((iono_lon - 1.617) * Math.PI);
		double per = ((beta3 * latm + beta2) * latm + beta1) * latm + beta0;
		if (per < 72000) {
			per = 72000;
		}
		double x = (Math.PI + Math.PI) * (localt - 50400) / per;
		double slantf = 0.53 - elsm;
		slantf = 1. + 16. * slantf * slantf * slantf;
		double amp = ((alpha3 * latm + alpha2) * latm + alpha1) * latm + alpha0;
		if (amp < 0) {
			amp = 0.;
		}
		double ionocorr = 0;
		if (Math.abs(x) < 1.57) {
			double xx = x * x;
			ionocorr = slantf
					* (5.e-9 + amp * (1. - 0.5 * xx + (xx * xx / 24)))
					* ConstantClass.SpeedOfLIGHT;
		} else {
			ionocorr = slantf * 5.e-9 * ConstantClass.SpeedOfLIGHT;
		}

		return ionocorr;
	}

	public static double tropCorrection(double zenithAngle, double height) {
		// TODO Auto-generated method stub
		zenithAngle = Math.PI / 2 - (zenithAngle * Math.PI) / 180;

		double Rearth = 6378137;
		double T = GetTemperature(height) + 273.16;// From Celsius to
													// Kelvin
		double P = GetAtmosphericPressure(height);
		double RH = GetRelativeHumidity(height);
		double e = GetPartialPressure(RH, T);
		double[] N = new double[2];
		N[0] = -12.96 * e / T + 371800 * e / T / T;// wet part;
		N[1] = 77.64 * P / T;// dry part;
		double[] H = new double[2];
		H[0] = 11000;// wet part;
		H[1] = 40136 + 148.72 * (T - 273.16);// dry part;
		double[] a = new double[2];
		double[] b = new double[2];
		double[][] f = new double[9][2];
		double[] r = new double[2];
		for (int index = 0; index < 2; index++) {
			a[index] = -Math.cos(zenithAngle) / H[index];
			b[index] = -Math.pow(Math.sin(zenithAngle), 2)
					/ (2 * H[index] * Rearth);
			r[index] = Math.sqrt(Math.pow(Rearth + H[index], 2)
					- Math.pow(Rearth, 2) * Math.pow(Math.sin(zenithAngle), 2))
					- Rearth * Math.cos(zenithAngle);
			f[0][index] = 1;
			f[1][index] = 4 * a[index];
			f[2][index] = 6 * Math.pow(a[index], 2) + 4 * b[index];
			f[3][index] = 4 * a[index] * (Math.pow(a[index], 2) + 3 * b[index]);
			f[4][index] = Math.pow(a[index], 4) + 12 * Math.pow(a[index], 2)
					* b[index] + 6 * Math.pow(b[index], 2);
			f[5][index] = 4 * a[index] * b[index]
					* (Math.pow(a[index], 2) + 3 * b[index]);
			f[6][index] = Math.pow(b[index], 2)
					* (6 * Math.pow(a[index], 2) + 4 * b[index]);
			f[7][index] = 4 * a[index] * Math.pow(b[index], 3);
			f[8][index] = Math.pow(b[index], 4);
		}
		double[] troposphereDelay = new double[2];
		// troposphereDelay.Initialize();
		for (int index2 = 0; index2 < 2; index2++) {
			for (int index9 = 0; index9 < 9; index9++) {
				troposphereDelay[index2] += N[index2] * f[index9][index2]
						* Math.pow(r[index2], index9 + 1) / (index9 + 1)
						/ 1.0E+6;
			}
		}
		double result = troposphereDelay[1] + troposphereDelay[0];
		return result;
	}

	private static double GetTemperature(double height) {
		// TODO Auto-generated method stub
		return ConstantClass.TropStandardTemperature - 0.0065
				* (height - ConstantClass.TropReferenceHeight);
	}

	private static double GetAtmosphericPressure(double height) {
		return ConstantClass.TropStandardAtmosphericPressure
				* Math.pow(
						(1 - 0.0000226 * (height - ConstantClass.TropReferenceHeight)),
						5.225);
	}

	private static double GetRelativeHumidity(double height) {
		return ConstantClass.TropStandardRelativeHumidity
				* Math.exp(-0.0006396
						* (height - ConstantClass.TropReferenceHeight));
	}

	private static double GetPartialPressure(double RH, double T)// Input:
	// relative
	// humidity,
	// and
	// temperature
	{
		return RH * Math.exp(-37.2465 + 0.213166 * T - 0.000256908 * T * T);
	}

	public static double rrcCorrection(NMEAPosition nmeaPosition,
			SatelliteRTCM satelliteRTCM) {
		// TODO Auto-generated method stub
		double result = 0;
		double deltSecond = 0;
		int nmeaTime = 0;
		double satTime = 0;
		nmeaTime = nmeaPosition.time.byMinute * 60 + nmeaPosition.time.dSecond;
		satTime = satelliteRTCM.modernZ;
		deltSecond = nmeaTime - satTime;
		if (deltSecond < 0 && Math.abs(deltSecond) > 3500) {
			deltSecond += 3600;
		}
		if (satTime < 30 && nmeaTime > 3569) {
			deltSecond -= 3600;
		}
		result = deltSecond * satelliteRTCM.range_rateCorrection;
		if (Math.abs(result) > 5) {
			Log.i("", "");
		}
		/*
		 * String deltsecond = String.valueOf(satelliteRTCM.satelliteID) +
		 * ",NMEA," + String.valueOf(nmeaTime) + ",Sat," +
		 * String.valueOf(satTime) + ",delt," + String.valueOf(deltSecond) +
		 * ",result," + String.valueOf(result);
		 */
		// recordDeltSecond(deltsecond);
		return result;
	}

	public static double GLONASionCorrection(GPSEphemeris GPSEphemeride,
			double ele, double azi, GPSnFileHead nHead,
			NMEAPosition nmeaPosition, int satID) {
		// TODO Auto-generated method stub
		double result = 0;
		double GPSionCorrection = GNSSCorrection.ionCorrection(GPSEphemeride,
				ele, azi, nHead, nmeaPosition);
		satID -= 64;
		double K = 0;
		switch (satID) {
		case 1:
			K = 1;
			break;
		case 2:
			K = -4;
			break;
		case 3:
			K = 5;
			break;
		case 4:
			K = 6;
			break;
		case 5:
			K = 1;
			break;
		case 6:
			K = -4;
			break;
		case 7:
			K = 5;
			break;
		case 8:
			K = 6;
			break;
		case 9:
			K = -2;
			break;
		case 10:
			K = -7;
			break;
		case 11:
			K = 0;
			break;
		case 12:
			K = -1;
			break;
		case 13:
			K = -2;
			break;
		case 14:
			K = -7;
			break;
		case 15:
			K = 0;
			break;
		case 16:
			K = -1;
			break;
		case 17:
			K = 4;
			break;
		case 18:
			K = -3;
			break;
		case 19:
			K = 3;
			break;
		case 20:
			K = 2;
			break;
		case 21:
			K = 4;
			break;
		case 22:
			K = -3;
			break;
		case 23:
			K = 3;
			break;
		case 24:
			K = 2;
			break;
		}
		double GLONASSFrequency = ConstantClass.GLONASSL1FrequencyBase
				+ ConstantClass.GLONASSL1FrequencyFactor * K;
		double GLONASSScale = ConstantClass.GPSL1Frequency / GLONASSFrequency;
		double GLONASScale2 = Math.pow(GLONASSScale, 2);
		result = GLONASScale2 * GPSionCorrection;
		return result;
	}
}
