package com.polyu.nmea;

public class SatList {
	public String[] satPRNList;
	public int satCount;

	public SatList(String[] satlist) {
		if (satlist.length > 0) {
			this.satPRNList = satlist;
			this.satCount = satlist.length;
		}

	}

	public String dispSatList() {
		String result = "";

		for (int i = 0; i < this.satCount; i++) {
			result += this.satPRNList[i] + "	";
		}
		return result;
	}

	public SatList combine(SatList satlist) {
		int totalLength = this.satCount + satlist.satCount;
		String[] totalSatList = new String[totalLength];
		for (int i = 0; i < this.satCount; i++) {
			totalSatList[i] = this.satPRNList[i];
		}
		for (int i = 0; i < satlist.satCount; i++) {
			totalSatList[i + this.satCount] = satlist.satPRNList[i];
		}
		SatList result = new SatList(totalSatList);
		return result;

	}

}
