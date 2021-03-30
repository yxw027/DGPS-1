package lscm.dgps.pilotapp.lands;

import java.util.Calendar;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class CustomMarkerSnippet 
{
	private static final String L_Separator = ";";
	
	public String m_strMarkerName = "";
	public Calendar m_dDateTime = Calendar.getInstance(); 
	public double m_fLatitude = 0;
	public double m_fLongitude = 0;
	public String m_strInZoneList = "";	
	public double m_fAccuracyInMeter = 0;
	public String m_strProvider = "";
	
	public String m_strMarketSnippetType;	//AppConst.MARKER_SNIPPET_TYPE
	public String m_strFixedAssetID;
	public String m_strFixedAssetDesc;
	
	public String m_strPolygonPointList = "";
	
	public CustomMarkerSnippet()
	{
		Reset();
	}
	
	public Location getLocationObject()
	{
		Location mloc = new Location("dummy");
		
		mloc.setLatitude(m_fLatitude);
		mloc.setLongitude(m_fLongitude);
		
		return mloc;
	}
	
	public void Reset()
	{
		m_dDateTime = Calendar.getInstance();
		m_strMarkerName = "";
		m_fLatitude = 0;
		m_fLongitude = 0;
		m_strInZoneList = "";
		m_fAccuracyInMeter = 0;
		m_strProvider = "";
		
		m_strMarketSnippetType = "";	//AppConst.MARKER_SNIPPET_TYPE
		m_strFixedAssetID = "";
		m_strFixedAssetDesc = "";	
		
		m_strPolygonPointList = "";
	}
	
	public void SetMarkerSnippetType(String pstrMARKER_SNIPPET_TYPE)
	{
		m_strMarketSnippetType = pstrMARKER_SNIPPET_TYPE;
	}
	
	public LatLng getGeoPosition()
	{
		return new LatLng(m_fLatitude, m_fLongitude);
	}
	
	public boolean setSnipperContent(String strSnippetContent)
	{
		try
		{
			String[] fields = strSnippetContent.split(L_Separator, 20);
			
			if (fields != null)
			{
				for (int i = 0; i < fields.length ; i++)
				{
					switch(i)
					{
						case 0:
							m_strMarkerName = fields[i].trim();
							break;
						case 1:
							m_dDateTime.setTimeInMillis(Long.valueOf(fields[i]));
							break;							
						case 2:
							m_fLatitude = Double.valueOf(fields[i]);
							break;
						case 3:
							m_fLongitude = Double.valueOf(fields[i]);
							break;
						case 4:
							m_strInZoneList = fields[i].trim();
							break;
						case 5:
							m_fAccuracyInMeter = Float.valueOf(fields[i]);
							break;
						case 6:
							m_strProvider = fields[i].trim();
							break;
							
						case 7:
							m_strMarketSnippetType = fields[i].trim();
							break;
						case 8:
							m_strFixedAssetID = fields[i].trim();
							break;
						case 9:
							m_strFixedAssetDesc = fields[i].trim();
							break;
						case 10:
							m_strPolygonPointList = fields[i].trim();
							break;
						default:
							break;
					}			
				}
			}
			
			return true;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			return false;
		}
	}
	
	public String getSnippetContent()
	{
		StringBuilder m_Builder = new StringBuilder(200);
		
		m_Builder.append(m_strMarkerName);
		m_Builder.append(L_Separator);
		m_Builder.append(String.valueOf(m_dDateTime.getTimeInMillis()));
		m_Builder.append(L_Separator);
		m_Builder.append(AppConst.getGeoPointFormater().format(m_fLatitude));
		m_Builder.append(L_Separator);
		m_Builder.append(AppConst.getGeoPointFormater().format(m_fLongitude));
		m_Builder.append(L_Separator);
		m_Builder.append(m_strInZoneList);
		m_Builder.append(L_Separator);
		m_Builder.append(String.valueOf(m_fAccuracyInMeter));
		m_Builder.append(L_Separator);
		m_Builder.append(m_strProvider);
		m_Builder.append(L_Separator);
		m_Builder.append(m_strMarketSnippetType);		
		m_Builder.append(L_Separator);
		m_Builder.append(m_strFixedAssetID);			
		m_Builder.append(L_Separator);
		m_Builder.append(m_strFixedAssetDesc);
		m_Builder.append(L_Separator);
		m_Builder.append(m_strPolygonPointList);	
		
		return m_Builder.toString();
	}

}
