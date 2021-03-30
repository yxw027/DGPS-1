package com.polyu.GLONASSEphemeris;

import com.polyu.time.TIME;

public class GLONASSnFileData {
	int PRN;
	TIME TOC;
	double TauN;
	double GammaN;
	double tk;
	double SatPositionX;
	double SatPositionY;
	double SatPositionZ;

	double velocityX;
	double velocityY;
	double velocityZ;

	double accelerationX;
	double accelerationY;
	double accelerationZ;

	double health;
	double K;
	double E;

	public GLONASSnFileData(int PRN, TIME TOC, double TauN, double GammaN,
			double tk, double SatPositionX, double SatPositionY,
			double SatPositionZ, double velocityX, double velocityY,
			double velocityZ, double accelerationX, double accelerationY,
			double accelerationZ, double health, double K, double E) {
		this.PRN = PRN;
		this.TOC = TOC;
		this.TauN = TauN;
		this.GammaN = GammaN;
		this.tk = tk;
		this.SatPositionX = SatPositionX;
		this.SatPositionY = SatPositionY;
		this.SatPositionZ = SatPositionZ;
		this.velocityX = velocityX;
		this.velocityY = velocityY;
		this.velocityZ = velocityZ;

		this.accelerationX = accelerationX;
		this.accelerationY = accelerationY;
		this.accelerationZ = accelerationZ;

		this.health = health;
		this.K = K;
		this.E = E;
	}
}
