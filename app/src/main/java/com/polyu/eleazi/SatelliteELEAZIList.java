package com.polyu.eleazi;

import java.util.ArrayList;
import java.util.List;

public class SatelliteELEAZIList {
	public List<SatelliteELEAZI> dataList = new ArrayList<SatelliteELEAZI>();

	public int Match(int satelliteID) {
		for (int index = 0; index < dataList.size(); index++) {
			if (dataList.get(index).satelliteID == satelliteID)
				return index;
		}
		return -1;
	}

	public void Add(SatelliteELEAZI data) {
		int index = this.Match(data.satelliteID);
		if (index == -1) {
			this.dataList.add(data);
		} else {
			this.dataList.get(index).azi = data.azi;
			this.dataList.get(index).ele = data.ele;
			this.dataList.get(index).snr = data.snr;

		}
	}

	public int Count() {
		return dataList.size();
	}

	public SatelliteELEAZI SatelliteELEAZI(int index) {
		if (index > this.Count()) {
			return (SatelliteELEAZI) null;
		} else if (index < 0) {
			return (SatelliteELEAZI) null;

		}
		return (SatelliteELEAZI) dataList.get(index);
		// dataList.dd(value);

	}
}
