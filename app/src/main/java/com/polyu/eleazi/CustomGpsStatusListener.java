package com.polyu.eleazi;

import java.lang.ref.WeakReference;
import java.util.Iterator;


import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.util.Log;

public class CustomGpsStatusListener implements
		android.location.GpsStatus.Listener {

	private WeakReference<SatelliteELEAZIList> _satEleAziData;
	private LocationManager _lm;

	public CustomGpsStatusListener(LocationManager lm,
			SatelliteELEAZIList satEleAziData) {
		_satEleAziData = new WeakReference<SatelliteELEAZIList>(satEleAziData);
		_lm = lm;
	}

	@Override
	public void onGpsStatusChanged(int event) {
		// TODO Auto-generated method stub

		int Satellites = 0;
		int SatellitesInFix = 0;
		int timetofix = _lm.getGpsStatus(null).getTimeToFirstFix();
		for (GpsSatellite sat : _lm.getGpsStatus(null).getSatellites()) {
			if (sat.usedInFix()) {
				SatellitesInFix++;
			}
			Satellites++;
		}
		String x = _lm.getGpsStatus(null).getSatellites().toString();
		;
		Log.i("TAG", x);
		GpsStatus gpsStatus = _lm.getGpsStatus(null);
		if (gpsStatus != null) {

			Iterable<GpsSatellite> satellites = gpsStatus.getSatellites();

			Iterator<GpsSatellite> satI = satellites.iterator();

			int satNum = 0;
			double satSnr = 0;
			double satAzi = 0;
			double satEle = 0;

			try {
				String showout = "";
				while (satI.hasNext()) {

					GpsSatellite satellite = (GpsSatellite) satI.next();

					satNum = satellite.getPrn();
					satSnr = satellite.getSnr();
					satAzi = satellite.getAzimuth();
					satEle = satellite.getElevation();
					// int satEleMatch = ((SatelliteELEAZIList)
					// _satEleAziData).Match(satNum);
					int satEleMatch = _satEleAziData.get().Match(satNum);
					if (satEleMatch == -1) {
						SatelliteELEAZI satELE = new SatelliteELEAZI(satNum,
								satAzi, satEle, satSnr);
						_satEleAziData.get().Add(satELE);
						// Log.e("ELE", "ADD NEW SNR&ELE&AZI");
					} else {
						_satEleAziData.get().dataList.get(satEleMatch).azi = satAzi;
						_satEleAziData.get().dataList.get(satEleMatch).ele = satEle;
						_satEleAziData.get().dataList.get(satEleMatch).snr = satSnr;
						// Log.e("ELE", "UPDATE SNR&ELE&AZI");

					}

					if (satellite.usedInFix() == true) {
						String output = "#SATNR," + String.valueOf(satNum)
								+ "," + String.valueOf(satSnr) + ","
								+ String.valueOf(satAzi) + " ,"
								+ String.valueOf(satEle) + "\n";
						// Log.i("TAG", output);
						showout += output;
					}

				}
				// disp.append("------------------------\n");
				// Log.e("ELE", showout);
				String NMEASatSNR = showout;
				// showout = "";

			} catch (Exception e) {

			}
			;

		}
	}
}