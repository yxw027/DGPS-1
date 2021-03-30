package com.polyu.GPSEphemeris;

public class GPSnFileHead {
	public double IonA0;
	public double IonA1;
	public double IonA2;
	public double IonA3;
	public double B0;
	public double B1;
	public double B2;
	public double B3;
	public double A0;
	public double A1;
	int T;
	int Week;
	long leaps;

	public GPSnFileHead(double ia0, double ia1, double ia2, double ia3,
			double b0, double b1, double b2, double b3, double a0, double a1,
			int t, int week, long leapes) {
		this.IonA0 = ia0;
		this.IonA1 = ia1;
		this.IonA2 = ia2;
		this.IonA3 = ia3;
		this.B0 = b0;
		this.B1 = b1;
		this.B2 = b2;
		this.B3 = b3;
		this.A0 = a0;
		this.A1 = a1;
		this.T = t;
		this.Week = week;
		this.leaps = leapes;

	}
}