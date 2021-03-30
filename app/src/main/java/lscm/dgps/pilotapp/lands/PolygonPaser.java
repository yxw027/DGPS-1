package lscm.dgps.pilotapp.lands;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.google.android.gms.maps.model.LatLng;

import android.location.Location;
import android.os.Environment;

public class PolygonPaser 
{	
	public class Polygon_Data implements Comparable<Polygon_Data>
	{
		String m_strPolygonName = "";
		ArrayList<LatLng> m_PolygonGeoPoints = null;
		String m_strPolygonID;
		String m_strPolygonDesc;
				
		private double m_Min_Latitude, m_Min_Longitude;
		private double m_Max_Latitude, m_Max_Longitude;		
		
		protected float m_distanceToScreenCenterReferencePoint_InMeters;
		
		public Polygon_Data()
		{
			m_distanceToScreenCenterReferencePoint_InMeters = 0;
			
			m_strPolygonName = "";
			m_PolygonGeoPoints = new ArrayList<LatLng>();
			m_strPolygonID = "";
			m_strPolygonDesc = "";
			
			m_Min_Latitude = 200;
			m_Min_Longitude = 200;
			
			m_Max_Latitude = -200;
			m_Max_Longitude = -200;			
		}
		
		public void SetDistanceToScreenCenter(float pDist_in_Meter)
		{
			m_distanceToScreenCenterReferencePoint_InMeters = pDist_in_Meter;
		}
		
		public String getPointList()
		{
			if (m_PolygonGeoPoints == null)
				return "";
			
			StringBuilder m_pointList = new StringBuilder(400);
			
			int pointCounter = 0;
			
			for (LatLng curPoint: m_PolygonGeoPoints)
			{
				if (pointCounter >= 10)
				{
					break;
				}
				else
				{
					if (m_pointList.length() > 0)
					{
						m_pointList.append(", ");
					}
					
					pointCounter++;
					
					m_pointList.append(String.format(Locale.US, "(%s, %s)", AppConst.getGeoPointFormater().format(curPoint.latitude), AppConst.getGeoPointFormater().format(curPoint.longitude)));
				}
			}
			
			return m_pointList.toString();
		}
		
	    public LatLng findPolygonCenterPoint() 
	    {
	        double[] centroid = { 0.0, 0.0 };

	        for (int i = 0; i < m_PolygonGeoPoints.size(); i++) {
	            centroid[0] += m_PolygonGeoPoints.get(i).latitude;
	            centroid[1] += m_PolygonGeoPoints.get(i).longitude;
	        }

	        int totalPoints = m_PolygonGeoPoints.size();
	        centroid[0] = centroid[0] / totalPoints;
	        centroid[1] = centroid[1] / totalPoints;

	        return new LatLng(centroid[0], centroid[1]);
	    }		
	
		private boolean IsInsideMaxZoneBound(LatLng pTestPoint)
		{
			if ((pTestPoint.latitude >= m_Min_Latitude) && (pTestPoint.latitude <= m_Max_Latitude))
			{
				if ((pTestPoint.longitude >= m_Min_Longitude) && (pTestPoint.longitude <= m_Max_Longitude))
				{
					return true;
				}
			}
			
			return false;
		}	    
	    
		public boolean IsInsidePolygonZone(LatLng pTestPoint)
		{
			
			if (m_PolygonGeoPoints == null)
				return false;
			
			if (m_PolygonGeoPoints.size() <= 2)
				return false;	// Two points cannot create any polygon
			
			if (IsInsideMaxZoneBound(pTestPoint) == false)
				return false;	//Not inside the max. zone boundary, the point must outside this polygon
			
			//Even inside the max. zone boundary, the point are not guarantee in the polygon, using Point-In-Polygon Algorithm  (counting number of intersect sides)
			
			int polySides = m_PolygonGeoPoints.size();	//Number of points = number of sides.. three point triangle with 3 sides.
			
			int   i, j = polySides - 1;
			  
			boolean oddNodes = false;
			
			for (i=0; i < polySides ; i++) 
			{
			    if ((m_PolygonGeoPoints.get(i).latitude < pTestPoint.latitude && m_PolygonGeoPoints.get(j).latitude >= pTestPoint.latitude
			    ||   m_PolygonGeoPoints.get(j).latitude < pTestPoint.latitude && m_PolygonGeoPoints.get(i).latitude >= pTestPoint.latitude)
			    &&  (m_PolygonGeoPoints.get(i).longitude <= pTestPoint.longitude || m_PolygonGeoPoints.get(j).longitude <= pTestPoint.longitude)) 
			    {
			      oddNodes ^= (m_PolygonGeoPoints.get(i).longitude + (pTestPoint.latitude - m_PolygonGeoPoints.get(i).latitude)/(m_PolygonGeoPoints.get(j).latitude-m_PolygonGeoPoints.get(i).latitude)*(m_PolygonGeoPoints.get(j).longitude- m_PolygonGeoPoints.get(i).longitude) < pTestPoint.longitude); 
			    }
			    
			    j=i; 
			}

			return oddNodes;		
		}	    
	    
		public void AddPolygonGeoPoint(String strLatLng)
		{
			String[] fields = strLatLng.split(",");
			
			if (fields == null)
			{
				return;
			}
			
			if (fields.length != 2)
			{
				return;			
			}
			
			try
			{
				LatLng pCurPoint = new LatLng(Double.valueOf(fields[0]),Double.valueOf(fields[1]));
				
				m_PolygonGeoPoints.add(pCurPoint);
				
				//******************* Determine Max Point Boundary ***********************
				if (pCurPoint.latitude > m_Max_Latitude)
					m_Max_Latitude = pCurPoint.latitude;
				
				if (pCurPoint.longitude > m_Max_Longitude)
					m_Max_Longitude = pCurPoint.longitude;
				//******************* Determine Max Point Boundary ***********************				
				
				//******************* Determine Min Point Boundary ***********************
				if (pCurPoint.latitude < m_Min_Latitude)
					m_Min_Latitude = pCurPoint.latitude;
				
				if (pCurPoint.longitude < m_Min_Longitude)
					m_Min_Longitude = pCurPoint.longitude;
				//******************* Determine Max Point Boundary ***********************				
			}
			catch (Exception ex)
			{
				return;				
			}
		}

		@Override
		public int compareTo(Polygon_Data another) 
		{
			if (m_distanceToScreenCenterReferencePoint_InMeters > another.m_distanceToScreenCenterReferencePoint_InMeters)
				return 1;
			else if (m_distanceToScreenCenterReferencePoint_InMeters < another.m_distanceToScreenCenterReferencePoint_InMeters)
				return -1;
			else
				return 0;
		}
	};
	
	File file = null;

	private boolean bIsSDCard = false;
	public ArrayList<Polygon_Data> m_arrPolygon = null;
	private ArrayList<Polygon_Data> m_arrPolygon_InternalAllEntry = null;
	
	private long m_LastReloadTime = 0;	
	
	public PolygonPaser()
	{
		m_LastReloadTime = 0;
				
		bIsSDCard = CheckExternalStorageState();		
	}	
		
	public int getTotalItems()
	{
		if (m_arrPolygon_InternalAllEntry == null)
			return 0;
		else
			return m_arrPolygon_InternalAllEntry.size();
	}
	
	public int getShortListedItemsCountForDisplay()
	{
		if (m_arrPolygon == null)
			return 0;
		else
			return m_arrPolygon.size();
	}		
	
	private File getFilePath()
	{
		File Path = new File(Environment.getExternalStorageDirectory().getPath() + AppConst.GC_DEFAULT_DIRECTORY);
		
		return Path;
	}
	
	private boolean ProcessPolygonXML(File pFileOfXml)
	{
		
		XmlPullParserFactory pullParserFactory;
		
		try 
		{
			pullParserFactory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = pullParserFactory.newPullParser();
			
			InputStream in_s = new BufferedInputStream(new FileInputStream(pFileOfXml));
	        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in_s, null);
			
			parseXML(parser);

			return true;
		} 
		catch (XmlPullParserException e) {

			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}
	
	private void parseXML(XmlPullParser parser) throws XmlPullParserException,IOException
	{
		m_arrPolygon_InternalAllEntry = null;
        int eventType = parser.getEventType();
        Polygon_Data currentPolygon = null;

        while (eventType != XmlPullParser.END_DOCUMENT){
            String name = null;
            switch (eventType){
                case XmlPullParser.START_DOCUMENT:
                	m_arrPolygon_InternalAllEntry = new ArrayList<Polygon_Data>();
                	//Log.w("PARSER", "Start Document");
                    break;
                case XmlPullParser.START_TAG:
                    name = parser.getName();
                    //Log.w("PARSER - Tags", name);
                    if (name.compareToIgnoreCase("Polygon") == 0)
                    {
                    	currentPolygon = new Polygon_Data();
                    } 
                    else if (currentPolygon != null)
                    {
                        if (name.compareToIgnoreCase("PolygonName") == 0)
                        {
                        	currentPolygon.m_strPolygonName = parser.nextText();
                        }
                        else if (name.compareToIgnoreCase("PolygonGeoPoint") == 0)
                        {
                        	currentPolygon.AddPolygonGeoPoint(parser.nextText());
                        }                         
                        else if (name.compareToIgnoreCase("PolygonID") == 0)
                        {
                        	currentPolygon.m_strPolygonID = parser.nextText();
                        } 
                        else if (name.compareToIgnoreCase("PolygonDesc") == 0)
                        {
                        	currentPolygon.m_strPolygonDesc= parser.nextText();
                        } 
                    }
                    break;
                case XmlPullParser.END_TAG:
                    name = parser.getName();
                    if (name.equalsIgnoreCase("Polygon") && currentPolygon != null)
                    {
                    	if (currentPolygon.m_PolygonGeoPoints.size() >= 3) 
                    	{
                    		//Log.w("Polygon", "Added");
                    		m_arrPolygon_InternalAllEntry.add(currentPolygon);
                    	}
                    	else
                    	{
                    		//Log.w("Polygon", "NOT Added < 3");
                    	}
                    }
                	else
                	{
                		//Log.w("Polygon", "NOT Added, null");
                	}                    
            }
            eventType = parser.next();
        }
	}	
	
	public boolean IsTimeoutForRefresh()
	{
		if (m_LastReloadTime <= 0)
			return true;
		
		if ((Calendar.getInstance().getTimeInMillis() - m_LastReloadTime) > AppConst.GC_TIME_OUT_FOR_FIXED_ASSET_AND_POLYGON_REFRESH) //60 seconds
		{
			return true;
		}
		else
			return false;
	}
	
	public boolean ReadPolygonFile(String strFileName, StringBuilder strFailureReasonString)
	{
		m_LastReloadTime = 0;
		
		if (bIsSDCard == false)
		{
			strFailureReasonString.append("Storage access deny!");
			return false;
		}
		
		if (strFileName.compareToIgnoreCase(AppConst.GC_NOT_LOADING_FILE) == 0)
		{
			// No reason is applied because of user's choice
			strFailureReasonString.setLength(0);
			return false;
		}
		
		try
		{
			File targetPath = getFilePath();
			file = new File(targetPath, strFileName);
			
			targetPath.mkdirs();
			
			if (file.exists() == false)
			{
				strFailureReasonString.append(String.format(Locale.US,  "Polygon File [%s] is not exist!", strFileName));
				return false;
			}
			
			boolean ProcessPolygonFileResult = ProcessPolygonXML(file);
			
			if (ProcessPolygonFileResult == true)
			{
				strFailureReasonString.setLength(0);
				
				m_LastReloadTime = Calendar.getInstance().getTimeInMillis();
			}
			else
			{
				strFailureReasonString.append(String.format(Locale.US,  "Invalid Polygon File [%s] format!", strFileName));
			}
			
			return ProcessPolygonFileResult;
		}
		catch (Exception ex)
		{
			strFailureReasonString.append(String.format(Locale.US,  "Loading Polygon File [%s] Exception!", strFileName));
			file = null;			
			return false;
		}
	}
	
	public void RefreshNearestPolygon(LatLng pcurScreenCenterPoint, int pnMaxItems)
	{
		//=============================================================================================
		m_arrPolygon = null;
		//=============================================================================================
		
		if (m_arrPolygon_InternalAllEntry == null)
		{			
			return;
		}

		m_arrPolygon = new ArrayList<Polygon_Data>();
		
		if (m_arrPolygon_InternalAllEntry.size() <= pnMaxItems)
		{
			m_arrPolygon.addAll(m_arrPolygon_InternalAllEntry);
			
			return;
		}		
		
		float[] curDist_inMeter = new float[1];
		
		for(Polygon_Data curPolygon : m_arrPolygon_InternalAllEntry)
		{
			if (pcurScreenCenterPoint != null)
			{
				LatLng pPolygonCenterPoint = curPolygon.findPolygonCenterPoint();
				if (pPolygonCenterPoint != null)
				{
					try
					{
						curDist_inMeter[0] = -1;
						Location.distanceBetween(pcurScreenCenterPoint.latitude, pcurScreenCenterPoint.longitude, pPolygonCenterPoint.latitude, pPolygonCenterPoint.longitude, curDist_inMeter);
						
						curPolygon.SetDistanceToScreenCenter(curDist_inMeter[0]);
						
						m_arrPolygon.add(curPolygon);
					}
					catch(Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
			else
			{
				m_arrPolygon.add(curPolygon);
			}
		}
		//=============================================================================================
		
		Collections.sort(m_arrPolygon);
		
//		for (int i = 0 ; i < m_arrFixedAssets.size() ; i++)
//		{
//			Log.w("Result", "Distance = " + String.valueOf(m_arrFixedAssets.get(i).m_distanceToScreenCenterReferencePoint_InMeters));
//		}
//			
		
		//Remove un-necessary items
		if (m_arrPolygon.size() > pnMaxItems)
		{
			while(pnMaxItems < m_arrPolygon.size())
			{
				m_arrPolygon.remove(pnMaxItems);
			}
		}			
	}
	
	private boolean CheckExternalStorageState()
    {
    	boolean mExternalStorageAvailable = false;
    	boolean mExternalStorageWriteable = false;
    	String state = Environment.getExternalStorageState();

    	if (Environment.MEDIA_MOUNTED.equals(state)) {
    	    // We can read and write the media
    	    mExternalStorageAvailable = mExternalStorageWriteable = true;
    	    
    	    //Toast.makeText(this, "Read/Write", Toast.LENGTH_SHORT).show();
    	    
    	} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
    	    // We can only read the media
    	    mExternalStorageAvailable = true;
    	    mExternalStorageWriteable = false;
    	    //Toast.makeText(this, "Read only", Toast.LENGTH_SHORT).show();
    	} else {
    	    // Something else is wrong. It may be one of many other states, but all we need
    	    //  to know is we can neither read nor write
    	    mExternalStorageAvailable = mExternalStorageWriteable = false;
    	    
    	    //Toast.makeText(this, "Not available", Toast.LENGTH_SHORT).show();
    	}
    	
    	
    	return mExternalStorageAvailable & mExternalStorageWriteable;
    }	
}
