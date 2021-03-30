package lscm.dgps.pilotapp.lands;

import java.util.ArrayList;
import java.util.Calendar;

import android.location.Location;

public class NMEA_position_history 
{
	private long m_nKeepLastMillseoncdsRecords;
	ArrayList<Location> m_NmeaHistory = new ArrayList<Location>();
	
	public NMEA_position_history(long pnKeepLastMillseoncdsRecords)
	{
		if (pnKeepLastMillseoncdsRecords < 1000)
			m_nKeepLastMillseoncdsRecords = 1000;
		else
			m_nKeepLastMillseoncdsRecords = pnKeepLastMillseoncdsRecords;
	}
	
	public void PutToTail(Location curLoc)
	{
		RemoveOutDateRecords();
		
		m_NmeaHistory.add(curLoc);
	}
	
	public Location LookupLocationByTimeStamp(long curTimeStamp)
	{
		RemoveOutDateRecords();
		
		for (int i = 0; i < m_NmeaHistory.size(); i++)
		{
			try
			{
				if (m_NmeaHistory.get(i).getTime() == curTimeStamp)
				{
					return m_NmeaHistory.get(i);
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		
		return null;
	}
	
	private void RemoveOutDateRecords()
	{
		for (int i = 0; i < m_NmeaHistory.size(); i++)
		{
			long curTimeStamp = Calendar.getInstance().getTimeInMillis();
		
			try
			{
				if ((curTimeStamp - m_NmeaHistory.get(0).getTime()) > m_nKeepLastMillseoncdsRecords)
				{
					m_NmeaHistory.remove(0);
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}
}
