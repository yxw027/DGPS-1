package lscm.dgps.pilotapp.lands;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.graphics.Color;
import android.os.Environment;

public class AppConst 
{
	public static final String GC_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	public static final int GC_EVENT_ID_DGPS_DELTA_CORRECTION_RECEIVED = 3001;
	public static final int GC_EVENT_ID_DGPS_ABSOLUTE_CORRECTION_RECEIVED = 3002;
	public static final int GC_EVENT_ID_PERIODIC_REFRESH = 3003;
	public static final int GC_EVENT_ID_THREAD_START_CHANGE = 3004;
	
	public static final long GC_TIME_OUT_FOR_FIXED_ASSET_AND_POLYGON_REFRESH = 60000;
	//public static final int GC_SHOW_MAXIMUM_FIXED_ASSET_AND_POLYGON_ITEMS = 50;
	
	public static final String GC_DEFAULT_LOG_ENABLE = "True";
	public static final String GC_DEFAULT_DGPS_SERVER_IP = "202.155.229.170";
	public static final int GC_DEFAULT_DGPS_SERVER_PORT = 5000;
	public static final String GC_DEFAULT_DGPS_NMEA_FILTER = "$*GSV;$*GSA;$*GGA;$*GRS;$*GST;$*RMC";
	
	public static final String GC_DEFAULT_LANDS_MAP_SERVER_IP = "202.189.117.179";
	public static final int GC_DEFAULT_LANDS_MAP_SERVER_PORT = 8443;	
	
	public static final String GC_NOT_LOADING_FILE = "[ N/A ]";
	public static final String GC_DEFAULT_FIXED_ASSET_FILE = GC_NOT_LOADING_FILE;
	public static final String GC_DEFAULT_POLYGON_FILE = GC_NOT_LOADING_FILE;
	public static final int GC_DEFAULT_MAX_SHOW_ITEM_COUNT = 50;	

	public static final String GC_DEFAULT_DIRECTORY = "/DGNSS/ASSET_POLYGON";
	public static final String GC_POLYGON_FILE_NAME_PATTERN = "Polygon_*.txt";
	public static final String GC_FIXED_ASSET_FILE_NAME_PATTERN = "FixedAsset_*.txt";
	
	public static final String GC_DEFAULT_SHARP_LIB_DIRECTORY = "/DGNSS/SHARP_LIB";
	
	public static final String GC_SHOW_FLAG_TRUE = "TRUE";
	public static final String GC_SHOW_FLAG_FALSE = "FALSE";
	public static final String GC_SHOW_FLAG_DEFAULT = GC_SHOW_FLAG_TRUE;
	
	public static final int nStrokeWidth = 1;
	public static final int nStrokeColor = Color.RED;
	public static final int nFillColor = 0x40FF6A00;		//format 0x Transparent, Red,Green, Blue	
	
	public static enum USER_DEFINE_MARK_TYPE {
	    GOOGLE_MAP_MY_LOCATION,
	    LOCATION_SERVICE_LOCATION_UPDATE,
	    NMEA_MESSAGE_GGA_CONTENT,
	    DEFFERENTIAL_GPS_CONTENT,
	    FIXED_ASSET_TYPE_1,
	    FIXED_ASSET_TYPE_2,
	    FIXED_ASSET_TYPE_3,
	    FIXED_ASSET_TYPE_4,
	    FIXED_ASSET_TYPE_5,
	    FIXED_ASSET_TYPE_6, 
	    FIXED_ASSET_TYPE_7,
	    FIXED_ASSET_TYPE_8,
	    FIXED_ASSET_TYPE_9,	    
	    FIXED_ASSET_TYPE_DEFAULT,
	    POLYGON_MARKER
	}
	
	public final class MARKER_SNIPPET_TYPE {
	    public static final String POSITION_MARKER = "Position";
	    public static final String FIX_ASSET_MARKER = "FixedAsset";
	    public static final String POLYGON_MARKER = "Polygon";
	    
	    private MARKER_SNIPPET_TYPE(){
	    } 
	}	
	
	public final class SHARE_PREFERENCE_KEY {
	    public static final String DGNSS_SERVER_IP = "DGNSS_SERVER_IP";
	    public static final String DGNSS_SERVER_PORT = "DGNSS_SERVER_PORT";
	    public static final String LANDS_MAP_SERVER_IP = "LANDS_MAP_SERVER_IP";
	    public static final String LANDS_MAP_SERVER_PORT = "LANDS_MAP_SERVER_PORT";	    
	    public static final String DGNSS_SERVER_LOG_MODE = "LOG_NMEA_MODE";
	    public static final String DGNSS_NMEA_FILTER = "DGNSS_NMEA_FILTER";
	    public static final String FIXED_ASSET_FILE = "FIXED_ASSET";
	    public static final String POLYGON_ZONE_FILE = "POLYGON_ZONE";
	    public static final String MAX_SHOW_ITEM_COUNT = "MAX_SHOW_ITEM_COUNT";
	    public static final String SHOW_POSITION_POINTER_GOOGLE_DGPS = "SHOW_POSITION_POINTER_GOOGLE_DGPS";
	    public static final String SHOW_POSITION_POINTER_LOCATION_SERVICE = "SHOW_POSITION_POINTER_LOCATION_SERVICE";
	    public static final String SHOW_POSITION_POINTER_NMEA_MSG_GPS = "SHOW_POSITION_POINTER_NMEA_MSG_GPS";
	    public static final String DGPS_LOGIN_NAME = "DGPS_LOGIN_NAME";
	    public static final String DGPS_LOGIN_PASSWORD = "DGPS_LOGIN_PASSWORD";
	    public static final String DGPS_LOGIN_PASSWORD_MD5 = "DGPS_LOGIN_PASSWORD_MD5";	    
	    
	    private SHARE_PREFERENCE_KEY(){
	    } 
	}
	
	public static String GetZoomLevelScale_Display(int nZoomLevel)
	{
		switch (nZoomLevel)
		{
			case 20 : 
				return "10 m";
			case 19 : 
				return "20 m"; 
			case 18 : 
				return "50 m";
			case 17 : 
				return "100 m"; 
			case 16 : 
				return "200 m"; 
			case 15 : 
				return "500 m";
			case 14 : 
				return "1 km";
			case 13 : 
				return "2 km"; 
			case 12 : 
				return "3 km"; 
			case 11 : 
				return "5 km";
			case 10 : 
				return "10 km"; 
			case 9  : 
				return "30 km";
			case 8  : 
				return "50 km";
			case 7  : 
				return "100 km";
			case 6  : 
				return "200 km";
			case 5  : 
				return "500 km";
			case 4  : 
				return "1000 km";
			case 3  : 
				return "2000 km";
			case 2  : 
				return "3000 km"; 
			case 1  : 
				return "5000 km";
			default :
				return "--";
		}	
	}	
	public static double GetZoomLevelScale_inMeter(int nZoomLevel)
	{
		switch (nZoomLevel)
		{
			case 20 : 
				return 10;
			case 19 : 
				return 20; 
			case 18 : 
				return 50;
			case 17 : 
				return 100; 
			case 16 : 
				return 200; 
			case 15 : 
				return 500;
			case 14 : 
				return 1000;
			case 13 : 
				return 2000; 
			case 12 : 
				return 3000; 
			case 11 : 
				return 5000;
			case 10 : 
				return 10000; 
			case 9  : 
				return 30000;
			case 8  : 
				return 50000;
			case 7  : 
				return 100000;
			case 6  : 
				return 200000;
			case 5  : 
				return 500000;
			case 4  : 
				return 1000000;
			case 3  : 
				return 2000000;
			case 2  : 
				return 3000000; 
			case 1  : 
				return 5000000;
			default :
				return 0;
		}	
	}	
	
//	public static double GetZoomLevelRatio(int nZoomLevel)
//	{
//		//Google Map Scale
//		switch (nZoomLevel)
//		{
//			case 20 : 
//				return 1128.497220;
//			case 19 : 
//				return 2256.994440; 
//			case 18 : 
//				return 4513.988880;
//			case 17 : 
//				return 9027.977761; 
//			case 16 : 
//				return 18055.955520; 
//			case 15 : 
//				return 36111.911040;
//			case 14 : 
//				return 72223.822090;
//			case 13 : 
//				return 144447.644200; 
//			case 12 : 
//				return 288895.288400; 
//			case 11 : 
//				return 577790.576700;
//			case 10 : 
//				return 1155581.153000; 
//			case 9  : 
//				return 2311162.307000;
//			case 8  : 
//				return 4622324.614000;
//			case 7  : 
//				return 9244649.227000;
//			case 6  : 
//				return 18489298.450000;
//			case 5  : 
//				return 36978596.910000;
//			case 4  : 
//				return 73957193.820000;
//			case 3  : 
//				return 147914387.600000;
//			case 2  : 
//				return 295828775.300000; 
//			case 1  : 
//				return 591657550.500000;
//			default :
//				return 0;
//		}	
//	}
	
	public static DecimalFormat getGeoPointFormater()
	{
		return new DecimalFormat("0.0000####");
	}
	
	public static List<String> LoadDirectoryFiles(String strPath, String strPatternFileName)
	{
		List<String> curlist =  new ArrayList<String>();
		
		curlist.add(AppConst.GC_NOT_LOADING_FILE);
		
		String path = Environment.getExternalStorageDirectory().getPath() + strPath;
		//Log.w("Files", "Path: " + path);
		File f = new File(path);
		
		if (f.exists() == false)
		{
			f.mkdirs();
		}		
		
		File file[] = f.listFiles();
		//Log.w("Files", "Size: "+ file.length);
		for (int i=0; i < file.length; i++)
		{
			String [] fileNamePatterns = strPatternFileName.split("\\*");
			
			String curFileName = file[i].getName().toLowerCase(Locale.US);
			
			if (AppConst.wildCardMatch(curFileName, strPatternFileName.toLowerCase(Locale.US)) == true)
			{
				//Log.w("Files", "FileName:" + file[i].getName());
				if (fileNamePatterns.length == 2)
				{
					if ((curFileName.startsWith(fileNamePatterns[0].toLowerCase(Locale.US)) == true) &&
							(curFileName.endsWith(fileNamePatterns[1].toLowerCase(Locale.US)) == true))
					{
						curlist.add(file[i].getName());
					}
				}
			}
		}

		return curlist;
	}
	
	public static String formatTime_TO_String_InItsTimeZone(long pDateTimeInMillis)
	{
		Calendar pDateTime = Calendar.getInstance();
		
		pDateTime.setTimeInMillis(pDateTimeInMillis);
		
	    SimpleDateFormat outputFormat = new SimpleDateFormat(GC_DATETIME_FORMAT, Locale.US);		
		
	    return outputFormat.format(pDateTime.getTime());
	}
	
	public static String formatTime_TO_String_InItsTimeZone(Calendar pDateTime)
	{
	    SimpleDateFormat outputFormat = new SimpleDateFormat(GC_DATETIME_FORMAT, Locale.US);		
		
	    return outputFormat.format(pDateTime.getTime());
	}
	
	public static boolean wildCardMatch(String strSearchInThisText, String strTargetPattern)
    {
        // Create the cards by splitting using a RegEx. If more speed 
        // is desired, a simpler character based splitting can be done.
        String [] cards = strTargetPattern.split("\\*");

        try
        {
	        // Iterate over the cards.
	        for (String card : cards)
	        {
	            int idx = strSearchInThisText.indexOf(card);
	            
	            // Card not detected in the text.
	            if(idx == -1)
	            {
	                return false;
	            }
	            
	            // Move ahead, towards the right of the text.
	            strSearchInThisText = strSearchInThisText.substring(idx + card.length());
	        }
	        
	        return true;
        }
        catch (Exception ex)
        {
        	return false;
        }
    }	
}
