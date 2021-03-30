package com.polyu.time;

public class TIME {
	public int wYear;
	public int byMonth;
	public int byDay;
	public int byHour;
	public int byMinute;
	public int dSecond;
	public int byDayOfWeek;

	public TIME(int y, int m, int d, int h, int min, int sec, int DofW) {
		this.wYear = y;
		this.byMonth = m;
		this.byDay = d;
		this.byHour = h;
		this.byMinute = min;
		this.dSecond = sec;
		this.byDayOfWeek = DofW;

	}

	public TIME(int y, int m, int d, int h, int min, int sec) {
		this.wYear = y;
		this.byMonth = m;
		this.byDay = d;
		this.byHour = h;
		this.byMinute = min;
		this.dSecond = sec;
	}

	public String display() {
		String dis = String.valueOf(this.wYear) + "-"
				+ String.valueOf(this.byMonth) + "-"
				+ String.valueOf(this.byDay) + ","
				+ String.valueOf(this.byHour) + ":"
				+ String.valueOf(this.byMinute) + ":"
				+ String.valueOf(this.dSecond);
		return dis;

	}
}
