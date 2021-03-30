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

public class FixAssetPaser 
{	
	public class FixedAsset_Data implements Comparable<FixedAsset_Data>
	{
		String m_strAssetName = "";
		LatLng m_GeoPoint = null;
		String m_strAssetID = "";
		String m_strAssetTypeID = "";
		String m_strAssetDesc = "";
		
		protected float m_distanceToScreenCenterReferencePoint_InMeters;
		
		public FixedAsset_Data()
		{
			m_strAssetName = "";
			m_GeoPoint = null;
			m_strAssetID = "";
			m_strAssetTypeID = "";
			m_strAssetDesc = "";
			
			m_distanceToScreenCenterReferencePoint_InMeters = 0;
		}
		
		public void SetDistanceToScreenCenter(float pDist_in_Meter)
		{
			m_distanceToScreenCenterReferencePoint_InMeters = pDist_in_Meter;
		}
	
		public void SetGeoPoint(String strLatLng)
		{
			String[] fields = strLatLng.split(",");
			
			if (fields == null)
			{
				m_GeoPoint = null;
				return;
			}
			
			if (fields.length != 2)
			{
				m_GeoPoint = null;
				return;			
			}
			
			try
			{
				m_GeoPoint = new LatLng(Double.valueOf(fields[0]),Double.valueOf(fields[1]));				
			}
			catch (Exception ex)
			{
				m_GeoPoint = null;
				return;				
			}
		}

		@Override
		public int compareTo(FixedAsset_Data another) 
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
	
	public ArrayList<FixedAsset_Data> m_arrFixedAssets = null;
	
	private ArrayList<FixedAsset_Data> m_arrFixedAssets_InternalAllEntry = null;
	
	private long m_LastReloadTime = 0;	

	public FixAssetPaser()
	{
		m_LastReloadTime = 0;
		bIsSDCard = CheckExternalStorageState();		
	}	
	
	private File getFilePath()
	{
		File Path = new File(Environment.getExternalStorageDirectory().getPath() + AppConst.GC_DEFAULT_DIRECTORY);
		
		return Path;
	}
	
	private boolean ProcessFixedAssetXML(File pFileOfXml)
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
	
	public int getTotalItems()
	{
		if (m_arrFixedAssets_InternalAllEntry == null)
			return 0;
		else
			return m_arrFixedAssets_InternalAllEntry.size();
	}
	
	public int getShortListedItemsCountForDisplay()
	{
		if (m_arrFixedAssets == null)
			return 0;
		else
			return m_arrFixedAssets.size();
	}	
	
	private void parseXML(XmlPullParser parser) throws XmlPullParserException,IOException
	{
		m_arrFixedAssets_InternalAllEntry = null;
        int eventType = parser.getEventType();
        FixedAsset_Data currentAsset = null;

        while (eventType != XmlPullParser.END_DOCUMENT){
            String name = null;
            switch (eventType){
                case XmlPullParser.START_DOCUMENT:
                	m_arrFixedAssets_InternalAllEntry = new ArrayList<FixedAsset_Data>();
                	//Log.w("PARSER", "Start Document");
                    break;
                case XmlPullParser.START_TAG:
                    name = parser.getName();
                    //Log.w("PARSER - Tags", name);
                    if (name.compareToIgnoreCase("Asset") == 0)
                    {
                    	currentAsset = new FixedAsset_Data();
                    } 
                    else if (currentAsset != null)
                    {
                        if (name.compareToIgnoreCase("AssetName") == 0)
                        {
                        	currentAsset.m_strAssetName = parser.nextText();
                        }
                        else if (name.compareToIgnoreCase("GeoPoint") == 0)
                        {
                        	currentAsset.SetGeoPoint(parser.nextText());
                        }                         
                        else if (name.compareToIgnoreCase("AssetID") == 0)
                        {
                        	currentAsset.m_strAssetID = parser.nextText();
                        } 
                        else if (name.compareToIgnoreCase("AssetTypeID") == 0)
                        {
                        	currentAsset.m_strAssetTypeID= parser.nextText();
                        } 
                        else if (name.compareToIgnoreCase("AssetDesc") == 0)
                        {
                        	currentAsset.m_strAssetDesc= parser.nextText();
                        } 
                    }
                    break;
                case XmlPullParser.END_TAG:
                    name = parser.getName();
                    if (name.equalsIgnoreCase("Asset") && currentAsset != null)
                    {
                    	if (currentAsset.m_GeoPoint != null)
                    	{
                    		m_arrFixedAssets_InternalAllEntry.add(currentAsset);
                    	}
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
	
	public void RefreshNearestFixedAsset(LatLng pcurScreenCenterPoint, int pnMaxItems)
	{
		//=============================================================================================
		m_arrFixedAssets = null;
		//=============================================================================================
		
		if (m_arrFixedAssets_InternalAllEntry == null)
		{			
			return;
		}

		m_arrFixedAssets = new ArrayList<FixedAsset_Data>();
		
		if (m_arrFixedAssets_InternalAllEntry.size() <= pnMaxItems)
		{
			m_arrFixedAssets.addAll(m_arrFixedAssets_InternalAllEntry);
			
			return;
		}
		

		float[] curDist_inMeter = new float[1];
		
		for(FixedAsset_Data curFix : m_arrFixedAssets_InternalAllEntry)
		{
			if (pcurScreenCenterPoint != null)
			{
				if (curFix.m_GeoPoint != null)
				{
					try
					{
						curDist_inMeter[0] = -1;
						Location.distanceBetween(pcurScreenCenterPoint.latitude, pcurScreenCenterPoint.longitude, curFix.m_GeoPoint.latitude, curFix.m_GeoPoint.longitude, curDist_inMeter);
						
						curFix.SetDistanceToScreenCenter(curDist_inMeter[0]);
						
						m_arrFixedAssets.add(curFix);
					}
					catch(Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
			else
			{
				m_arrFixedAssets.add(curFix);
			}
		}
		//=============================================================================================
		
		Collections.sort(m_arrFixedAssets);
		
//		for (int i = 0 ; i < m_arrFixedAssets.size() ; i++)
//		{
//			Log.w("Result", "Distance = " + String.valueOf(m_arrFixedAssets.get(i).m_distanceToScreenCenterReferencePoint_InMeters));
//		}
//			
		
		//Remove un-necessary items
		if (m_arrFixedAssets.size() > pnMaxItems)
		{
			while(pnMaxItems < m_arrFixedAssets.size())
			{
				m_arrFixedAssets.remove(pnMaxItems);
			}
		}	
	}
	
	public boolean ReadFixedAssetFile(String strFileName, StringBuilder strFailureReasonString)
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
				strFailureReasonString.append(String.format(Locale.US,  "Fixed Asset File [%s] is not exist!", strFileName));
				return false;
			}
			
			boolean ProcessFixedAssetFileResult = ProcessFixedAssetXML(file);					
					
			if (ProcessFixedAssetFileResult == true)
			{
				strFailureReasonString.setLength(0);
				
				m_LastReloadTime = Calendar.getInstance().getTimeInMillis();
			}
			else
			{
				strFailureReasonString.append(String.format(Locale.US,  "Invalid Fixed Asset File [%s] format!", strFileName));
			}			
			
			return ProcessFixedAssetFileResult;
		}
		catch (Exception ex)
		{
			strFailureReasonString.append(String.format(Locale.US,  "Loading Fixed Asset File [%s] Exception!", strFileName));
			file = null;			
			return false;
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
