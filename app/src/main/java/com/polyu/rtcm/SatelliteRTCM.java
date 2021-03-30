package com.polyu.rtcm;

public class SatelliteRTCM {
	public int satelliteID;
	public int scaleFactor;
	public int udre;
	public double pseudorangeCorrection;
	public double range_rateCorrection;
	public int issueOfData;
	public double modernZ;

	public SatelliteRTCM(int scaleFactorIn, int udreIn, int satelliteIDIn,
			double pseudorangeCorrectionIn, double range_rateCorrectionIn,
			int issueOfDataIn, double modernZIn) {
		scaleFactor = scaleFactorIn;
		udre = udreIn;
		satelliteID = satelliteIDIn;
		pseudorangeCorrection = pseudorangeCorrectionIn;
		range_rateCorrection = range_rateCorrectionIn;
		issueOfData = issueOfDataIn;
		modernZ = modernZIn;
	}

}
