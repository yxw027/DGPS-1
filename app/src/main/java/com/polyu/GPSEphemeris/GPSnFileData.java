package com.polyu.GPSEphemeris;

import com.polyu.time.TIME;

public class GPSnFileData {
	int byPRN;
	TIME TOC;
	double dClkBias;
	double dClkDrift;
	double dClkDriftRate;
	double dIODE;
	double dCrs;
	double dn;
	double dM0;
	double dCuc;
	double d_e;// 轨道偏心率
	double dCus;
	double dSqrtA;
	double dTOE;
	double dCic;
	double dOMEGA;
	double dCis;
	double di0;
	double dCrc;
	double dShenJinDianJao; // 升交点
	double dOMEGAdot;
	double di0Dot;
	double dCodesOnL2Channel;
	double dGpsWeek;
	double dL2PDataFlage;
	double dSatPrecise;
	double dSatHealth;
	double dTGD;
	double dIODC;
	double dTransmissionTimeOfMesseage;
	double dFitInterval;
	double dSparel1;
	double dSparel2;

	public GPSnFileData(int byPRN, TIME TOC, double dClkBias, double dClkDrift,
			double dClkDriftRate, double dIODE, double dCrs, double dn,
			double dM0, double dCuc, double d_e, double dCus, double dSqrtA,
			double dTOE, double dCic, double dOMEGA, double dCis, double di0,
			double dCrc, double dShenJinDianJao, double dOMEGAdot,
			double di0Dot, double dCodesOnL2Channel, double dGpsWeek,
			double dL2PDataFlage, double dSatPrecise, double dSatHealth,
			double dTGD, double dIODC, double dTransmissionTimeOfMesseage,
			double dFitInterval) {
		this.byPRN = byPRN;
		this.TOC = TOC;
		this.dClkBias = dClkBias;
		this.dClkDrift = dClkDrift;
		this.dClkDriftRate = dClkDriftRate;
		this.dIODE = dIODE;
		this.dCrs = dCrs;
		this.dn = dn;
		this.dM0 = dM0;
		this.dCuc = dCuc;
		this.d_e = d_e;// 轨道偏心率
		this.dCus = dCus;
		this.dSqrtA = dSqrtA;
		this.dTOE = dTOE;
		this.dCic = dCic;
		this.dOMEGA = dOMEGA;
		this.dCis = dCis;
		this.di0 = di0;
		this.dCrc = dCrc;
		this.dShenJinDianJao = dShenJinDianJao;
		this.dOMEGAdot = dOMEGAdot;
		this.di0Dot = di0Dot;
		this.dCodesOnL2Channel = dCodesOnL2Channel;
		this.dGpsWeek = dGpsWeek;
		this.dL2PDataFlage = dL2PDataFlage;
		this.dSatPrecise = dSatPrecise;
		this.dSatHealth = dSatHealth;
		this.dTGD = dTGD;
		this.dIODC = dIODC;
		this.dTransmissionTimeOfMesseage = dTransmissionTimeOfMesseage;
		this.dFitInterval = dFitInterval;
	}

}
