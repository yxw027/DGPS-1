package com.polyu.GLONASSEphemeris;

import com.polyu.time.TIME;

public class GLONASSnFileHead {
	double REINEX_Version;
	String TYPE;
	String PGM;
	String RUN_By;
	TIME UTC_Date;
	int LEAP_SECOND;

	public GLONASSnFileHead(double REINEX_Version, String TYPE, String PGM,
			String RUN_By, TIME UTC_Date, int LEAP_SECOND) {
		this.REINEX_Version = REINEX_Version;
		this.TYPE = TYPE;
		this.PGM = PGM;
		this.RUN_By = RUN_By;
		this.UTC_Date = UTC_Date;
		this.LEAP_SECOND = LEAP_SECOND;

	}
}
