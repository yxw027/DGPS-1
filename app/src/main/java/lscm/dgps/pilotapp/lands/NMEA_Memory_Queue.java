package lscm.dgps.pilotapp.lands;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class NMEA_Memory_Queue 
{
	public class NMEA_Memory_Queue_Data
	{
		long m_nTimeInMillisSecond = 0;
		String m_strNMEA = null;
		long m_nNMEA_Msg_Timestamp;
		
		public NMEA_Memory_Queue_Data(String pstrInitializedNMEA, long pnNMEA_Msg_Timestamp)
		{
			m_nTimeInMillisSecond = Calendar.getInstance().getTimeInMillis();
			m_strNMEA = pstrInitializedNMEA;
			m_nNMEA_Msg_Timestamp = pnNMEA_Msg_Timestamp;
		}
	};
	
	
	ArrayList<NMEA_Memory_Queue_Data> myList = null;
	private long m_nKeepLatestRecords_inMillis = 0;
	private String mDevID = "";
	
	public NMEA_Memory_Queue(long pnKeepLatestRecords_inMillis,String devID)
	{
		mDevID = devID;
		myList = new ArrayList<NMEA_Memory_Queue_Data>();
		
		if (pnKeepLatestRecords_inMillis >= 0)
			m_nKeepLatestRecords_inMillis = pnKeepLatestRecords_inMillis;
		else
			m_nKeepLatestRecords_inMillis = 0;
	}
	
	public void AddNmeaToQueueTail(String strNMEA, long pnNMEA_Msg_Timestamp)
	{
		RemoveOutDateRecords();
		
		if (myList == null)
			return;
		
		synchronized(this)
		{
			//myList.add(new NMEA_Memory_Queue_Data(strNMEA, Group_time.getGroupedTime(pnNMEA_Msg_Timestamp)));
			myList.add(new NMEA_Memory_Queue_Data(strNMEA, pnNMEA_Msg_Timestamp));
		}
	}
	
	public void RemoveOutDateRecords()
	{	
		synchronized(this)
		{
			long m_nCurrentTimeInMillisSeconds = Calendar.getInstance().getTimeInMillis();
			
			try
			{
				while (myList.size() > 0)
				{
					if ((m_nCurrentTimeInMillisSeconds - myList.get(0).m_nTimeInMillisSecond) > m_nKeepLatestRecords_inMillis)
					{
						//Remove head records
						myList.remove(0);
					}
					else
						break;
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}
	
	public int getSize()
	{
		synchronized(this)
		{
			return myList.size();
		}
	}

	public String GetNmeaFromQueueHead(boolean bRemovedAfterRead)
	{
		RemoveOutDateRecords();
		
		if (myList == null)
			return null;

		synchronized(this)
		{
			if (myList.size() <= 0)
				return null;
			else
			{
				NMEA_Memory_Queue_Data strNMEAData = myList.get(0);
				
				if (bRemovedAfterRead == true)
					myList.remove(0);
				
				return String.format(Locale.US, "%s:%d:%s\n", strNMEAData.m_strNMEA, strNMEAData.m_nNMEA_Msg_Timestamp,mDevID);
			}
		}
	}
	
}
