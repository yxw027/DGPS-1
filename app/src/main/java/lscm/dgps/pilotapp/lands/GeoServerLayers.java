package lscm.dgps.pilotapp.lands;

import android.util.Log;

public class GeoServerLayers {
	
	public GeoServerLayers()
	{
		
	}

	
	static public String getLayersByZoomLevel(int nZoomLevel)
	{
		//layer name is case sensitive
		
		String strVal = "mapHK:Sea,mapHK:Land,mapHK:Plac_text1,mapHK:Plac_text2,mapHK:Plac_text3,mapHK:Park,mapHK:park_text2,mapHK:Rail,mapHK:Road1,mapHK:Road2,mapHK:Road3,mapHK:Road4,mapHK:road4_text,mapHK:BLDG,mapHK:BLDG_text";
		
//		if (nZoomLevel >= 12)
//		{
//			strVal += "";
//		}
//		
//		if (nZoomLevel >= 16)
//		{
//			strVal += "";
//		}
//		
//		if (nZoomLevel >= 17)
//		{
//			strVal += "";
//		}		
		
//		Log.w("Layers", strVal);
		
		return strVal;
	}
}
