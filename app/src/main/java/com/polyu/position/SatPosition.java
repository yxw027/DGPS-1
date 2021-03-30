package com.polyu.position;

import java.text.DecimalFormat;

public class SatPosition {
	public double XX;
	public double YY;
	public double ZZ;
	public int PRN;

	public SatPosition(double x, double y, double z, int i) {
		this.XX = x;
		this.YY = y;
		this.ZZ = z;
		this.PRN = i;
	}

	public SatPosition(double x, double y, double z) {
		this.XX = x;
		this.YY = y;
		this.ZZ = z;
	}

	public String displayPosition() {
		String result = "";
		DecimalFormat format5 = new DecimalFormat("#.00");

		result = "#SAT," + String.valueOf(this.PRN) + ","
				+ format5.format(this.XX) + "," + format5.format(this.YY) + ","
				+ format5.format(this.ZZ) + '\n';
		return result;
	}
}
