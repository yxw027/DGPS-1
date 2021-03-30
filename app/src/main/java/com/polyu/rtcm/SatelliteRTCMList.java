package com.polyu.rtcm;

import java.util.ArrayList;
import java.util.List;

public class SatelliteRTCMList {

	public List<SatelliteRTCM> dataList = new ArrayList<SatelliteRTCM>();

	public int Match(int satelliteID) {
		for (int index = 0; index < dataList.size(); index++) {
			if (dataList.get(index).satelliteID == satelliteID)
				return index;
		}
		return -1;
	}

	public void Add(SatelliteRTCM data) {
		int index = this.Match(data.satelliteID);
		if (index == -1) {
			this.dataList.add(data);
		} else {
			this.dataList.get(index).issueOfData = data.issueOfData;
			this.dataList.get(index).modernZ = data.modernZ;
			this.dataList.get(index).pseudorangeCorrection = data.pseudorangeCorrection;
			this.dataList.get(index).range_rateCorrection = data.range_rateCorrection;
			this.dataList.get(index).scaleFactor = data.scaleFactor;
			this.dataList.get(index).udre = data.udre;
		}
	}

	public int Count() {
		return dataList.size();
	}

	public SatelliteRTCM SatelliteRTCM(int index) {
		if (index > this.Count()) {
			return (SatelliteRTCM) null;
		} else if (index < 0) {
			return (SatelliteRTCM) null;

		}
		return (SatelliteRTCM) dataList.get(index);
		// dataList.dd(value);

	}
}
