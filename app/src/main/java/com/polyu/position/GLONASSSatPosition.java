package com.polyu.position;

public class GLONASSSatPosition {
	SatPosition SatPosition;
	double[] velocity;

	public GLONASSSatPosition(SatPosition SatPosition, double[] velocity) {
		this.SatPosition = SatPosition;
		this.velocity = velocity;
	}

}
