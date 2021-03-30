package com.polyu.eleazi;

public class SatelliteELEAZI {

	public int satelliteID;
	public double azi;
	public double ele;
	public double snr;

	public SatelliteELEAZI(int satelliteID, double azi, double ele, double snr) {

		this.satelliteID = satelliteID;
		this.azi = azi;
		this.ele = ele;
		this.snr = snr;
	}
}