package com.polyu.position;

import java.text.DecimalFormat;

import com.polyu.dgnss.ConstantClass;
import com.polyu.nmea.SatList;
import com.polyu.time.TIME;

public class NMEAPosition {
	public XYZPosition xyz;
	public NEHPosition NEH;
	public BLHPosition BLH;
	public double latitude;
	public double longitude;
	public double h;
	public TIME time;
	public SatList satlist;

	public NMEAPosition(double latitude, double longitude, double h, TIME time,
			SatList satlist) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.h = h;
		this.time = time;
		this.satlist = satlist;
		double b = latitude * Math.PI / 180;
		double l = longitude * Math.PI / 180;
		BLHPosition blh = new BLHPosition(b, l, h);
		this.BLH = blh;
		this.xyz = BLHtoXYZ(blh);
		this.NEH = BLHtoNEH(blh);
	}

	public NMEAPosition(double latitude, double longitude, double h, TIME time) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.h = h;
		this.time = time;
		double b = latitude * Math.PI / 180;
		double l = longitude * Math.PI / 180;
		BLHPosition blh = new BLHPosition(b, l, h);
		this.xyz = BLHtoXYZ(blh);
		this.NEH = BLHtoNEH(blh);
		this.BLH = blh;
	}

	public NMEAPosition(double latitude, double longitude, double h) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.h = h;
		double b = latitude * Math.PI / 180;
		double l = longitude * Math.PI / 180;
		BLHPosition blh = new BLHPosition(b, l, h);
		this.xyz = BLHtoXYZ(blh);
		this.NEH = BLHtoNEH(blh);
		this.BLH = blh;

		// double x = xyz.x;
		// double y = xyz.y;
		// this.xyz = new XYZPosition(x, y, h);
	}

	public static XYZPosition BLHtoXYZ(BLHPosition BLH) {
		double e = (Math.pow(ConstantClass.HalfAxisLong, 2) - Math.pow(
				ConstantClass.HalfAxisShort, 2))
				/ Math.pow(ConstantClass.HalfAxisLong, 2);
		double sinB = Math.sin(BLH.B);
		double cosB = Math.cos(BLH.B);
		double sinL = Math.sin(BLH.L);
		double cosL = Math.cos(BLH.L);
		double n = ConstantClass.HalfAxisLong / Math.sqrt(1 - e * sinB * sinB);
		double XX = (n + BLH.H) * cosB * cosL;
		double YY = (n + BLH.H) * cosB * sinL;
		double ZZ = (n * (1.0 - e) + BLH.H) * sinB;
		XYZPosition XYZ = new XYZPosition(XX, YY, ZZ);
		return XYZ;
	}

	public String displayXYZ() {
		DecimalFormat format6 = new DecimalFormat("#.000000");

		String result = format6.format(this.latitude) + ","
				+ format6.format(this.longitude) + "," + String.valueOf(this.h);

		return result;

	}

	public static BLHPosition XYZtoBLH(XYZPosition XYZ) {

		BLHPosition BLH1 = new BLHPosition(0, 0, 0);

		if (XYZ.x > -1e12 && XYZ.x < 1e12) {

			double squre = XYZ.x * XYZ.x + XYZ.y * XYZ.y + XYZ.z * XYZ.z;
			squre = Math.sqrt(squre);
			if (squre == 0) {
				return BLH1;
			}

			double esqd = 2 * ConstantClass.WGS_Flat
					- Math.pow(ConstantClass.WGS_Flat, 2);
			double e = Math.sqrt(esqd);
			double b = (1 - ConstantClass.WGS_Flat)
					* ConstantClass.HalfAxisLong;
			double e1 = Math.sqrt(esqd / (1.0 - esqd));
			double p = Math.sqrt(Math.pow(XYZ.x, 2) + Math.pow(XYZ.y, 2));

			double theta = Math
					.atan(XYZ.z / p * ConstantClass.HalfAxisLong / b);
			double a1 = XYZ.z + e1 * e1 * b * Math.pow(Math.sin(theta), 3);
			double a2 = p - e * e * ConstantClass.HalfAxisLong
					* Math.pow(Math.cos(theta), 3);

			double BB = Math.atan(a1 / a2);
			double LL = Math.atan2(XYZ.y, XYZ.x);

			double u = Math.atan(b / ConstantClass.HalfAxisLong * Math.tan(BB));
			a1 = p - ConstantClass.HalfAxisLong * Math.cos(u);
			a2 = XYZ.z - b * Math.sin(u);
			double HH = Math.signum(a1) * Math.sqrt(a1 * a1 + a2 * a2);
			BLH1 = new BLHPosition(BB, LL, HH);
			return BLH1;
		}
		return BLH1;

	}

	public static NEHPosition BLHtoNEH(BLHPosition BLH) {

		NEHPosition resultPosition = null;
		// XYZPosition wgs84XYZ = NMEAPosition.BLHtoXYZ(BLH);
		double WGS84_A = (6378137.000);
		double WGS84_Flattening = 1 / 298.257223560;
		XYZPosition wgs84XYZ = GeodeticToCartesian(BLH, WGS84_A,
				WGS84_Flattening);
		double dX = 162.619; // shift along x-axis
		double dY = 276.961; // shift along y-axis
		double dZ = 161.763;
		double ThetaX = De2Ra(0.067741e-4); // Rotation about x-axis
		double ThetaY = De2Ra(-2.243649e-4);
		double ThetaZ = De2Ra(-1.158827e-4);
		double S = 1.094239e-6; // Scale factor

		double hk80X = dX + (1 + S) * wgs84XYZ.x + ThetaZ * wgs84XYZ.y - ThetaY
				* wgs84XYZ.z;
		double hk80Y = dY - ThetaZ * wgs84XYZ.x + (1 + S) * wgs84XYZ.y + ThetaX
				* wgs84XYZ.z;
		double hk80Z = dZ + ThetaY * wgs84XYZ.x - ThetaX * wgs84XYZ.y + (1 + S)
				* wgs84XYZ.z;
		XYZPosition hk80XYZ = new XYZPosition(hk80X, hk80Y, hk80Z);
		// BLHPosition hk80BLH= NMEAPosition.XYZtoBLH(hk80XYZ);
		double HK80_A = (6378388.000);
		double HK80_Flattening = 1 / 297.0;
		BLHPosition hk80BLH = CartesianToGeodetic(hk80XYZ, HK80_A,
				HK80_Flattening);

		double a = 6378388; // semi-major axis of the reference ellipsoid
		double f = 1 / 297.0; // flattening of the reference ellipsoid
		double e2 = 2 * f - f * f; // first eccentricity of the reference
									// ellipsoid

		double n0 = 819069.8; // Northing of Projection origin
		double e0 = 836694.05; // Easting of Projection origin

		double lat0 = De2Ra(22.184368); // latitude of projection origin in
										// radiance
		double lon0 = De2Ra(114.104280); // Longitude of projection origin in
											// radiance

		double vs = a / Math.sqrt(1 - e2 * Math.sin(lat0) * Math.sin(lat0)); // radius
																				// of
																				// curvature
																				// in
																				// the
																				// prime
																				// vertical
		double ps = vs * (1 - e2) / (1 - e2 * Math.sin(lat0) * Math.sin(lat0)); // radius
																				// of
																				// curvature
																				// in
																				// the
																				// meridian
		double phis = vs / ps; // isometric latitue

		double a0 = 1 - e2 / 4 - e2 * e2 * 3 / 64; // parametris for calculating
													// Meridian distance
		double a2 = (e2 + e2 * e2 / 4) * 3 / 8;
		double a4 = e2 * e2 * 15 / 256;

		double lat = hk80BLH.B;// + 0.00055); // latitude of the measured point
								// P
		double lon = hk80BLH.L;// - 0.00088); // longitude of the measured point
								// P
		double m0 = 1.0; // scale factor on the central meridian
		double M = a
				* (a0 * lat - a2 * Math.sin(2 * lat) + a4 * Math.sin(4 * lat)); // Meridian
																				// distance
																				// measured
																				// fro
																				// the
																				// Equator
																				// to
																				// P
		double M0 = a
				* (a0 * lat0 - a2 * Math.sin(2 * lat0) + a4
						* Math.sin(4 * lat0)); // Meridian distance measured
												// from the Equator to origin of
												// projection
		double delta_lamda = lon - lon0;

		double Easting, Northing;
		double ResultE = e0
				+ m0
				* (vs * delta_lamda * Math.cos(lat) + vs
						* Math.pow(delta_lamda * Math.cos(lat), 3)
						* (phis - Math.tan(lat) * Math.tan(lat)));
		double ResultN = n0
				+ m0
				* (M - M0 + vs
						* (Math.sin(lat) * (delta_lamda * delta_lamda / 2) * (Math
								.cos(lat))));

		resultPosition = new NEHPosition(ResultN, ResultE, BLH.H);
		return resultPosition;

	}

	private static XYZPosition GeodeticToCartesian(BLHPosition geodetic,
			double Semi_MajorAxis, double Flattening) {
		// TODO Auto-generated method stub
		XYZPosition cartesian = new XYZPosition(0, 0, 0);
		double a, e_square, N;
		a = Semi_MajorAxis;
		double f = Flattening; // flattening of the reference ellipsoid
		e_square = 2 * f - f * f;
		N = a / Math.sqrt(1 - e_square * Math.pow(Math.sin(geodetic.B), 2));
		double ResultX = (N + geodetic.H) * Math.cos(geodetic.B)
				* Math.cos(geodetic.L);
		double ResultY = (N + geodetic.H) * Math.cos(geodetic.B)
				* Math.sin(geodetic.L);
		double ResultZ = (N * (1 - e_square) + geodetic.H)
				* Math.sin(geodetic.B);
		cartesian = new XYZPosition(ResultX, ResultY, ResultZ);
		return cartesian;
	}

	private static BLHPosition CartesianToGeodetic(XYZPosition cartesian,
			double Semi_MajorAxis, double Flattening) {
		// TODO Auto-generated method stub
		BLHPosition geodetic = new BLHPosition(0, 0, 0);
		double a, e_square, N, W, B = 0.0, B1 = 0.0;
		a = Semi_MajorAxis; // semi-major axis of the reference ellipsoid
		e_square = 2 * Flattening - Flattening * Flattening; // first
																// eccentricity
																// of the
																// reference
																// ellipsoid
		double resultL = Math.atan2(cartesian.y, cartesian.x);
		B1 = Math.atan2(cartesian.z, Math.sqrt(cartesian.x * cartesian.x
				+ cartesian.y * cartesian.y));
		do {
			W = Math.sqrt(1 - e_square * Math.pow(Math.sin(B1), 2));
			N = a / W;
			B = Math.atan2(
					(cartesian.z + N * e_square * Math.sin(B1)),
					Math.sqrt(cartesian.x * cartesian.x + cartesian.y
							* cartesian.y));
			if (Math.abs(B - B1) < 0.00000000001)
				break;
			else
				B1 = B;
		} while (1 > 0);
		double resultB = B;
		N = a / Math.sqrt(1 - e_square * Math.pow(Math.sin(B), 2));
		double resultH = Math.sqrt(cartesian.x * cartesian.x + cartesian.y
				* cartesian.y)
				/ Math.cos(B) - N;
		geodetic = new BLHPosition(resultB, resultL, resultH);
		return geodetic;
	}

	// / <summary>
	// / input = radiance
	// / </summary>
	// / <param name="geodetic"></param>
	// / <returns></returns>

	private static double De2Ra(double Deg) {
		// TODO Auto-generated method stub
		int dd = (int) Deg;
		int mm = (int) ((Deg - dd) * 100.0);
		double ss = (Deg - dd - mm / 100.0) * 10000.0;
		double dddd = dd + mm / 60.0 + ss / 3600.0;
		return dddd * Math.PI / 180.0;
	}
}
