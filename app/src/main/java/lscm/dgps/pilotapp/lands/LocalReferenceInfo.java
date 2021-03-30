package lscm.dgps.pilotapp.lands;

import android.content.Context;
import android.content.SharedPreferences;

public class LocalReferenceInfo 
{
	public static final String GC_PREFERENCE_DGPS = "lscm.dgps.pilotapp.preference_dgps";

	public static void updateLocalReference(String str_SHARE_PREFERENCE_KEY, long pnValue, Context pContext_Activity)
	{
		if (str_SHARE_PREFERENCE_KEY.trim().length() <= 0)
			return ;
		
		try
		{
			SharedPreferences prefs = pContext_Activity.getSharedPreferences(GC_PREFERENCE_DGPS, Context.MODE_PRIVATE);
			
			SharedPreferences.Editor cur_Editor = prefs.edit();
			
			cur_Editor.putLong(str_SHARE_PREFERENCE_KEY, pnValue);
			
			cur_Editor.commit();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public static void updateLocalReference(String str_SHARE_PREFERENCE_KEY, float pfValue, Context pContext_Activity)
	{
		if (str_SHARE_PREFERENCE_KEY.trim().length() <= 0)
			return ;
		
		try
		{
			SharedPreferences prefs = pContext_Activity.getSharedPreferences(GC_PREFERENCE_DGPS, Context.MODE_PRIVATE);
			
			SharedPreferences.Editor cur_Editor = prefs.edit();
			
			cur_Editor.putFloat(str_SHARE_PREFERENCE_KEY, pfValue);
			
			cur_Editor.commit();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}	
	
	public static void updateLocalReference(String str_SHARE_PREFERENCE_KEY, String pstrValue, Context pContext_Activity)
	{
		if (str_SHARE_PREFERENCE_KEY.trim().length() <= 0)
			return ;
		
		try
		{
			SharedPreferences prefs = pContext_Activity.getSharedPreferences(GC_PREFERENCE_DGPS, Context.MODE_PRIVATE);
			
			SharedPreferences.Editor cur_Editor = prefs.edit();
			
			cur_Editor.putString(str_SHARE_PREFERENCE_KEY, pstrValue);
			
			cur_Editor.commit();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}	

	public static String GetLocalReference(String str_SHARE_PREFERENCE_KEY, Context pContext_Activity, String pstrDefaultValue)
	{
		try
		{
			SharedPreferences prefs_debug = pContext_Activity.getSharedPreferences(GC_PREFERENCE_DGPS, Context.MODE_PRIVATE);
			
			String strValue = prefs_debug.getString(str_SHARE_PREFERENCE_KEY, pstrDefaultValue);
			
			return strValue.trim();			
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return pstrDefaultValue;
		}		
	}
	
	public static long GetLocalReference(String str_SHARE_PREFERENCE_KEY, Context pContext_Activity, long pnDefaultValue)
	{
		try
		{
			SharedPreferences prefs_debug = pContext_Activity.getSharedPreferences(GC_PREFERENCE_DGPS, Context.MODE_PRIVATE);
			
			long nValue = prefs_debug.getLong(str_SHARE_PREFERENCE_KEY, pnDefaultValue);
			
			return nValue;			
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return pnDefaultValue;
		}		
	}
	
	public static float GetLocalReference(String str_SHARE_PREFERENCE_KEY, Context pContext_Activity, float pfDefaultValue)
	{
		try
		{
			SharedPreferences prefs_debug = pContext_Activity.getSharedPreferences(GC_PREFERENCE_DGPS, Context.MODE_PRIVATE);
			
			float fValue = prefs_debug.getFloat(str_SHARE_PREFERENCE_KEY, pfDefaultValue);
			
			return fValue;			
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return pfDefaultValue;
		}		
	}	
}
