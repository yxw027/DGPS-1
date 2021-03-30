package lscm.dgps.pilotapp.lands;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.polyu.position.BLHPosition;
import com.polyu.position.NEHPosition;
import com.polyu.position.NMEAPosition;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.OnNmeaMessageListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity implements OnMapClickListener, OnCameraChangeListener, OnMarkerClickListener, LocationListener, OnMapReadyCallback {
	private GoogleMap map;
	
	private TileOverlay m_pOverlay = null;
	
	private static boolean gc_bIs_enable_log = true;
	
	private static String gc_str_remoteServerIP = AppConst.GC_DEFAULT_DGPS_SERVER_IP;
	private static long gc_str_remoteServerPort = AppConst.GC_DEFAULT_DGPS_SERVER_PORT;
	
	private static String gc_str_landMapServerIP = AppConst.GC_DEFAULT_LANDS_MAP_SERVER_IP;
	private static long gc_str_landMapServerPort = AppConst.GC_DEFAULT_LANDS_MAP_SERVER_PORT;	
	
	private static double gc_previous_zoom_level = -1;
	
	private static int gc_nShowItemsCount = AppConst.GC_DEFAULT_MAX_SHOW_ITEM_COUNT;
	
	private Context m_Activity = null; 
	
	LocationManager locationManager = null;
	GpsStatus.NmeaListener mNmeaListenerold = null;
	OnNmeaMessageListener mNmeaListener = null;
	private static GPSRecordLog logFile = new GPSRecordLog();
	NMEA_Memory_Queue m_NMEA_MSG_Queue = null;
	
	String[] m_NMEA_FilterArrayList = null;
	
	private boolean m_bIsNeedToRefresh = true;
	
	NMEA_position_history m_NMEA_Position_History = null;
	
	StringBuilder m_strSocketRecButter = new StringBuilder(200);
	private boolean m_bIsQuickThread =  false;
	private Object threadWaiter = new Object();
	
	private Location m_MyLocationGoogle = null;
	@SuppressWarnings("unused")
	private Marker m_MyLocationGoogle_Marker = null;
	private Location m_LocationServices = null;
	@SuppressWarnings("unused")
	private Marker m_LocationServices_Marker = null;
	private Location m_NMEA_MSG_Position = null;
	@SuppressWarnings("unused")
	private Marker m_NMEA_MSG_Position_Marker = null;
	private Location m_DGPS_CorrectedLocatoin = null;
	@SuppressWarnings("unused")
	private Marker m_DGPS_CorrectedLocatoin_Marker = null;
	
	private FixAssetPaser m_FixedAsset = null;
	private PolygonPaser m_Polygon = null;
	
	ArrayList<Marker> m_FixedAssetMarkerList = new ArrayList<Marker>();
	ArrayList<Marker> m_PolygonMarkerList = new ArrayList<Marker>();
	
	private static boolean m_currentConnection_isConnected = false;
	
	private static boolean m_bShowGoogleDGPS_Marker = true;
	private static boolean m_bShowLocationSrv_Marker = true;
	private static boolean m_bShowNMEA_GPS_Marker = true;
	private static boolean m_bCentralizePosition = true;	
	
	PreviousCenter previousLoc = null;	
	
	private static String imei ="";
	private long latestTimeStamp = 0;

	

	@Override
	public void onMapReady(GoogleMap googleMap) {

	}


	class PreviousCenter {
		
		//private Location _previousLoc = null;
		private NEHPosition _previousNEH = null;
		final double[] zoomLevel2distance = new double[]{ 3000*1000,3000*1000,3000*1000,3000*1000,3000*1000,3000*1000,3000*1000,3000*1000,3000*1000,3000*1000,10*1000,5*1000,3*1000,2*1000,1000,500,200,100,50,20,10,10,10,10,10,10,10,10 };
		public PreviousCenter(Location previousCenter) {
			//_previousLoc = previousCenter;
			_previousNEH = NMEAPosition.BLHtoNEH(new BLHPosition(previousCenter.getLatitude()*Math.PI/180,previousCenter.getLongitude()*Math.PI/180,0));
		}
//		public void setPreviousCenter(Location previousCenter) {
//			_previousLoc = previousCenter;
//			_previousNEH = NMEAPosition.BLHtoNEH(new BLHPosition(_previousLoc.getLatitude()*Math.PI/180,_previousLoc.getLongitude()*Math.PI/180,0));
//		}
		
		public boolean isLongDistance(Location currentLoc,float zoomLevel) {
			
			NEHPosition _currentNEH = NMEAPosition.BLHtoNEH(new BLHPosition(currentLoc.getLatitude()*Math.PI/180,currentLoc.getLongitude()*Math.PI/180,0));
			if (_previousNEH != null) {
				double threshold = longdistance(zoomLevel);
				//String longLongDis = "zoomLevel:" + Float.toString(zoomLevel) + " Threshold:" + Double.toString(threshold) + " cN " + Double.toString(_currentNEH.N) + " cE " + Double.toString(_currentNEH.E)+ " pN " + Double.toString(_previousNEH.N) + " pE " + Double.toString(_previousNEH.E);
				//String longLongDis = "zoomLevel:" + Float.toString(zoomLevel) + " Threshold:" + Double.toString(threshold) + " dN " + Double.toString(Math.abs(_currentNEH.N - _previousNEH.N)) + " dE " + Double.toString(Math.abs(_currentNEH.E - _previousNEH.E));
				//CustomLog.recordResult(FILENAMEFORRECORD+"longdistance",longLongDis);
				if (Math.abs(_currentNEH.N - _previousNEH.N) > threshold || Math.abs(_currentNEH.E - _previousNEH.E) > threshold) {
					_previousNEH = _currentNEH;
					return true;
				} else {
					return false;
				}
			} else {
				_previousNEH = _currentNEH;
				return true;
			}
		}
		
		double longdistance(float zoomLevel) {
			double distance = zoomLevel2distance[(int)zoomLevel];
			return distance/16;
		}
	}	
	
	
	@SuppressLint("HandlerLeak")
	private final Handler handler = new Handler() 
	{    	
		@Override
		public void handleMessage(final Message msgs) 
		{

			if (m_DGPS_CorrectedLocatoin == null)
			{
				m_DGPS_CorrectedLocatoin = new Location("D-GPS");
				//m_DGPS_CorrectedLocatoin.setAccuracy(0);  don't set accuracy, and examin hasAccuracy later
				m_DGPS_CorrectedLocatoin.setTime(0);	//Means not valid
			}
			
        	if (msgs.what == AppConst.GC_EVENT_ID_DGPS_DELTA_CORRECTION_RECEIVED)
         	{      		
        		String strLatLng = (String)msgs.obj;
        		
        		if (strLatLng != null)
        		{
        			//<D> and </D> are removed before passing into this part
        			//<D>TIMESTAMP, Latitude, Longitude</D>
	        		String[] strFields = strLatLng.split(",", 4);
	        		
	        		if (strFields.length == 4)
	        		{
	        			if (m_NMEA_MSG_Position != null)
	        			{
	        				try
	        				{
	        					
	        					long timeOfRectifiedLoc = Long.valueOf(strFields[1]);
	        					if (latestTimeStamp < timeOfRectifiedLoc + 8000) {	        					
	        					
		        					m_DGPS_CorrectedLocatoin.setTime(timeOfRectifiedLoc);
		        					
		        					Location l_LocPreviousHistory = m_NMEA_Position_History.LookupLocationByTimeStamp(timeOfRectifiedLoc);
		        					
		        					if (l_LocPreviousHistory == null)
		        					{
		        						//Log.w("Previuos NMEA records", "Not Find");
		        						
		        						logFile.WriteFormatLogToFile(strLatLng + " <= NMEA Delta Correction, Timestamp not find in Memory!");
		        						
		        						//Previous Location with this timestamp is not find
				        				m_DGPS_CorrectedLocatoin.setLatitude(m_NMEA_MSG_Position.getLatitude() + Double.valueOf(strFields[2]));
				        				m_DGPS_CorrectedLocatoin.setLongitude(m_NMEA_MSG_Position.getLongitude() + Double.valueOf(strFields[3]));
		        					}
		        					else
		        					{
		        						//Log.w("Previuos NMEA records", "Find");
		        						
		        						logFile.WriteFormatLogToFile(strLatLng + " <= NMEA Delta Correction, Timestamp find in Memory!");
		        						
		        						//Previous Location with this timestamp is find
				        				m_DGPS_CorrectedLocatoin.setLatitude(l_LocPreviousHistory.getLatitude() + Double.valueOf(strFields[2]));
				        				m_DGPS_CorrectedLocatoin.setLongitude(l_LocPreviousHistory.getLongitude() + Double.valueOf(strFields[3]));	        						
		        					}
		        					
		        					float distance = m_DGPS_CorrectedLocatoin.distanceTo(m_NMEA_MSG_Position);
	        						if (Math.abs(distance) > 2000.0) {
	        							m_DGPS_CorrectedLocatoin = m_NMEA_MSG_Position;
	        						}		        					
		        					
		        					m_DGPS_CorrectedLocatoin.setProvider("NMEA - DELTA");
	        					}
		        				if (((ToggleButton)findViewById(R.id.enable_auto_refresh_button)).isChecked() == true)
		        				{
		        					RefreshAllMarkers(true, true);
		        				}
		        				else
		        				{
		        					RefreshAllMarkers(true, false);
		        				}
	        				}
	        				catch (Exception ex)
	        				{
	        					//Log.w("Invalid Delta Correction Message", "Content = " + strLatLng);
	        				}
	        			}
	        		}
        		}        		        		
         	}
        	else if (msgs.what == AppConst.GC_EVENT_ID_DGPS_ABSOLUTE_CORRECTION_RECEIVED)
        	{
        		String strLatLng = (String)msgs.obj;
        		
        		if (strLatLng != null)
        		{
        			//<A> and </A> are removed before passing into this part
        			//<A>TIMESTAMP, Latitude, Longitude</A>
	        		String[] strFields = strLatLng.split(",", 4);
	        		
	        		if (strFields.length == 4)
	        		{

        				try
        				{
        					/*
    						MainActivity.this.runOnUiThread(new Runnable() {
    						    public void run() {
    						    	Toast.makeText(MainActivity.this, "DGPS set ok", Toast.LENGTH_SHORT).show();
    						    }
    						});
    						*/
        					final long timeOfRectifiedLoc = Long.valueOf(strFields[1]);
        					
        					/*
    						MainActivity.this.runOnUiThread(new Runnable() {
    						    public void run() {
    						    	Toast.makeText(MainActivity.this, "time diff =" + (latestTimeStamp - timeOfRectifiedLoc), Toast.LENGTH_SHORT).show();
    						    }
    						});        					
        					*/
        					if (latestTimeStamp < timeOfRectifiedLoc + 8000) {
        						m_DGPS_CorrectedLocatoin.setTime(timeOfRectifiedLoc);        					
        						m_DGPS_CorrectedLocatoin.setLatitude(Double.valueOf(strFields[2]));
        						m_DGPS_CorrectedLocatoin.setLongitude(Double.valueOf(strFields[3]));
	        				
        						m_DGPS_CorrectedLocatoin.setProvider("NMEA - ABSOLUTE");
        						final float distance = m_DGPS_CorrectedLocatoin.distanceTo(m_NMEA_MSG_Position);

        						/*
        						MainActivity.this.runOnUiThread(new Runnable() {
        						    public void run() {
        						    	Toast.makeText(MainActivity.this, "distance diff =" + distance, Toast.LENGTH_SHORT).show();
        						    }
        						});        						
        						*/
        						if (Math.abs(distance) > 2000.0) {
        							m_DGPS_CorrectedLocatoin = m_NMEA_MSG_Position;
        						}
        					}

	        				if (((ToggleButton)findViewById(R.id.enable_auto_refresh_button)).isChecked() == true)
	        				{
	        					RefreshAllMarkers(true, true);
	        				}
	        				else
	        				{
	        					RefreshAllMarkers(true, false);
	        				}
        				}
        				catch (Exception ex)
        				{
        					//Log.w("Invalid Absolute Correction Message", "Content = " + strLatLng);
        				}
	        		}
        		}
        	}
        	else if (msgs.what == AppConst.GC_EVENT_ID_THREAD_START_CHANGE)
        	{
        		RefreshAllMarkers(true, false);
        	}
        	else if (msgs.what == AppConst.GC_EVENT_ID_PERIODIC_REFRESH)
        	{
        		synchronized(this)
        		{
        			if (m_bIsNeedToRefresh == true)
        			{
        				if (((ToggleButton)findViewById(R.id.enable_auto_refresh_button)).isChecked() == true)
        				{
        					RefreshAllMarkers(true, true);
        				}
        				else
        				{
        					RefreshAllMarkers(true, false);
        				}
        			}
        			
        			m_bIsNeedToRefresh = false;
        		}
        		
        		handler.sendEmptyMessageDelayed(AppConst.GC_EVENT_ID_PERIODIC_REFRESH, 1000);
        	}
		}		
	};
	
	private void Simulate_DGNSS_Response()
	{	
		if (m_NMEA_MSG_Position == null)
		{
			Toast.makeText(this, "NMEA position is not available!", Toast.LENGTH_SHORT).show();
			return;
		}
		
		double fDelta_latitude = 0;
		double fDelta_longitude = 0;
		
		Random rand = new Random();
		
		rand.setSeed(Calendar.getInstance().getTimeInMillis());
		
		fDelta_latitude = (rand.nextDouble() * 40 - 20) * 0.00001; 
		fDelta_longitude = (rand.nextDouble() * 40 - 20) * 0.00001;
		
		if (m_NMEA_MSG_Position != null)
		{
			m_DGPS_CorrectedLocatoin.setTime(Calendar.getInstance().getTimeInMillis());
			m_DGPS_CorrectedLocatoin.setLatitude(m_NMEA_MSG_Position.getLatitude() +  fDelta_latitude);
			m_DGPS_CorrectedLocatoin.setLongitude(m_NMEA_MSG_Position.getLongitude() +  fDelta_longitude);       				
			
			m_DGPS_CorrectedLocatoin.setProvider("NMEA - DELTA (Simulate)");	
		}
		
		RefreshAllMarkers(true, true);
	}
	
	private void LandsDept_SetTileProvider()
	{
		if (map == null)
			return;
		
    	if (map.getMapType() == GoogleMap.MAP_TYPE_NONE)
    	{
    		if (m_pOverlay == null)
    		{
		    	TileProvider geoServerTileProvider = TileProviderFactory.getGeoServerTileProvider(gc_str_landMapServerIP,gc_str_landMapServerPort);
		    	
		    	m_pOverlay = map.addTileOverlay(new TileOverlayOptions().tileProvider(geoServerTileProvider).zIndex(10000).visible(true));
    		}
    	}
    	else
    	{
    		if (m_pOverlay != null)
    		{
	    		m_pOverlay.remove();
	    		
	    		m_pOverlay = null;
    		}
    	}
	}
	
	private boolean BuildSnapshotResultRecord(CustomMarkerSnippet pCurFixedAssetSnippet)
	{			
		ResultRecords mResultRec = new ResultRecords();
		
		if (mResultRec.OpenLogFile() == false)
		{
			Toast.makeText(this, "Fail to save result into file!", Toast.LENGTH_LONG).show();
			return false;
		}
		
		mResultRec.WriteRecordBreak(true);
		
		if (pCurFixedAssetSnippet != null)
		{
			mResultRec.WriteLogToFile(String.format(Locale.US, "Fixed Asset Name: %s\nID:%s\nGPS TimeStamp:%s\nGeoPosition: %s, %s\n----------------------------------------\n", pCurFixedAssetSnippet.m_strMarkerName, pCurFixedAssetSnippet.m_strFixedAssetID, AppConst.formatTime_TO_String_InItsTimeZone(pCurFixedAssetSnippet.m_dDateTime), AppConst.getGeoPointFormater().format(pCurFixedAssetSnippet.m_fLatitude),AppConst.getGeoPointFormater().format(pCurFixedAssetSnippet.m_fLongitude)));
		}
		else
		{
			mResultRec.WriteLogToFile("Fixed Asset Name: --\n----------------------------------------\n");
		}
		
        if (m_MyLocationGoogle != null)
        {
        	mResultRec.WriteLogToFile(String.format(Locale.US, "Google : TS(%s) GPS(%s,%s) %.2f %s to Fixed Asset\nZone List : %s\n", AppConst.formatTime_TO_String_InItsTimeZone(m_MyLocationGoogle.getTime()), AppConst.getGeoPointFormater().format(m_MyLocationGoogle.getLatitude()),AppConst.getGeoPointFormater().format(m_MyLocationGoogle.getLongitude()), 
        			((pCurFixedAssetSnippet != null)? m_MyLocationGoogle.distanceTo(pCurFixedAssetSnippet.getLocationObject()) : 0.0), 
        			getString(R.string.fixed_asset_marker_info_windows_dist_unit),
        			getInZoneList(new LatLng(m_MyLocationGoogle.getLatitude(), m_MyLocationGoogle.getLongitude()))));
        }
        
        if (m_LocationServices != null)
        {
        	mResultRec.WriteLogToFile(String.format(Locale.US, "GPS Svc : TS(%s) GPS(%s,%s) %.2f %s to Fixed Asset\nZone List : %s\n", AppConst.formatTime_TO_String_InItsTimeZone(m_LocationServices.getTime()), AppConst.getGeoPointFormater().format(m_LocationServices.getLatitude()),AppConst.getGeoPointFormater().format(m_LocationServices.getLongitude()), 
        			((pCurFixedAssetSnippet != null)? m_LocationServices.distanceTo(pCurFixedAssetSnippet.getLocationObject()) : 0.0), 
        			getString(R.string.fixed_asset_marker_info_windows_dist_unit),
        			getInZoneList(new LatLng(m_LocationServices.getLatitude(), m_LocationServices.getLongitude()))));
        }
        
        if (m_NMEA_MSG_Position != null)
        {
        	mResultRec.WriteLogToFile(String.format(Locale.US, "NMEA : TS(%s) GPS(%s,%s) %.2f %s to Fixed Asset\nZone List : %s\n", AppConst.formatTime_TO_String_InItsTimeZone(m_NMEA_MSG_Position.getTime()), AppConst.getGeoPointFormater().format(m_NMEA_MSG_Position.getLatitude()),AppConst.getGeoPointFormater().format(m_NMEA_MSG_Position.getLongitude()), 
        			((pCurFixedAssetSnippet != null)? m_NMEA_MSG_Position.distanceTo(pCurFixedAssetSnippet.getLocationObject()) : 0.0), 
        			getString(R.string.fixed_asset_marker_info_windows_dist_unit),
        			getInZoneList(new LatLng(m_NMEA_MSG_Position.getLatitude(), m_NMEA_MSG_Position.getLongitude()))));
        }  
        
        if (m_DGPS_CorrectedLocatoin != null)
        {
        	if (m_DGPS_CorrectedLocatoin.getTime() > 0)
        	{        	
	        	mResultRec.WriteLogToFile(String.format(Locale.US, "NMEA : TS(%s) GPS(%s,%s) %.2f %s to Fixed Asset\nZone List : %s\n", AppConst.formatTime_TO_String_InItsTimeZone(m_DGPS_CorrectedLocatoin.getTime()), AppConst.getGeoPointFormater().format(m_DGPS_CorrectedLocatoin.getLatitude()),AppConst.getGeoPointFormater().format(m_DGPS_CorrectedLocatoin.getLongitude()), 
	        			((pCurFixedAssetSnippet != null)? m_DGPS_CorrectedLocatoin.distanceTo(pCurFixedAssetSnippet.getLocationObject()) : 0.0), 
	        			getString(R.string.fixed_asset_marker_info_windows_dist_unit),
	        			getInZoneList(new LatLng(m_DGPS_CorrectedLocatoin.getLatitude(), m_DGPS_CorrectedLocatoin.getLongitude()))));
        	}
        }         
        
		mResultRec.WriteRecordBreak(false);
		mResultRec.CloseLogFile();
		
		Toast.makeText(this, "Record taked successfully!", Toast.LENGTH_SHORT).show();
		
		return true;
	}
	
	private String BuildGeoSummaryForFixAssetInfoWindows(Location pCurFixedAssetLocationObj)
	{
        StringBuilder strSummaryBuilder = new StringBuilder(200);
        //findViewById(R.id.geo_point_summary_row).setVisibility(View.GONE);
        
        if (m_MyLocationGoogle != null)
        {
        	strSummaryBuilder.append(String.format(Locale.US, "(Google) %.2f %s\n", m_MyLocationGoogle.distanceTo(pCurFixedAssetLocationObj), getString(R.string.fixed_asset_marker_info_windows_dist_unit)));
        }

        if (m_LocationServices != null)
        {
        	strSummaryBuilder.append(String.format(Locale.US, "(GPS Svc) %.2f %s\n", m_LocationServices.distanceTo(pCurFixedAssetLocationObj), getString(R.string.fixed_asset_marker_info_windows_dist_unit)));
        }
        
        if (m_NMEA_MSG_Position != null)
        {
        	strSummaryBuilder.append(String.format(Locale.US, "(NMEA) %.2f %s\n", m_NMEA_MSG_Position.distanceTo(pCurFixedAssetLocationObj), getString(R.string.fixed_asset_marker_info_windows_dist_unit)));
        }
        
        if (m_DGPS_CorrectedLocatoin != null)
        {
        	if (m_DGPS_CorrectedLocatoin.getTime() > 0)
        	{
        		strSummaryBuilder.append(String.format(Locale.US, "(DGPS) %.2f %s\n", m_DGPS_CorrectedLocatoin.distanceTo(pCurFixedAssetLocationObj), getString(R.string.fixed_asset_marker_info_windows_dist_unit)));
        	}
        }
        
        if(strSummaryBuilder.length() > 0)
        {
        	strSummaryBuilder.deleteCharAt(strSummaryBuilder.length() - 1);		//remove last \n characters
        }
        
        return strSummaryBuilder.toString();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_main);
		
		m_Activity = this;
		
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
//		mNmeaListener = this;
		
//		Context tm = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);

		imei = getDeviceId(this);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
			if (mNmeaListener == null){
				mNmeaListener = new OnNmeaMessageListener() {
					@Override
					public void onNmeaMessage(String message, long timestamp) {
						reveiverdNema(timestamp, message);
					}
				};
			}
			locationManager.addNmeaListener(mNmeaListener);
		}else{
			if (mNmeaListenerold == null){
				mNmeaListenerold = new GpsStatus.NmeaListener() {
					@Override
					public void onNmeaReceived(long timestamp, String nmea) {
						reveiverdNema(timestamp, nmea);
					}
				};
			}
			locationManager.addNmeaListener(mNmeaListenerold);
		}

		
		m_NMEA_MSG_Queue = new NMEA_Memory_Queue(10000,imei);	//Keep Last 10 Seconds in the memory queue only
		
		m_NMEA_Position_History = new NMEA_position_history(60000);		//Keep Last 60 Seconds in the memory queue only
		
		findViewById(R.id.main_map_record_now_button).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				BuildSnapshotResultRecord(null);
			}
			
		});
		
		findViewById(R.id.autoCenter).setBackgroundColor(0x200000ff);
		findViewById(R.id.autoCenter).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//m_bCentralizePosition = (m_bCentralizePosition == true)?false:true;
				if (m_bCentralizePosition == true) {
					m_bCentralizePosition = false;
					//v.setBackgroundColor(0x80404040);
					ImageButton bn = (ImageButton)v;
					bn.setImageResource(R.drawable.autocenteroff);
				} else {
					ImageButton bn = (ImageButton)v;
					bn.setImageResource(R.drawable.autocenteron);
					m_bCentralizePosition = true;
					//v.setBackgroundColor(0x400000ff);
		        	Location curLastKnow = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);		        	
		        	if (curLastKnow != null)
		        		map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(curLastKnow.getLatitude(), curLastKnow.getLongitude()), 15));
				}
			}
			
		});

//		SupportMapFragment mapFragment = ( SupportMapFragment) getFragmentManager()
//				.findFragmentById(R.id.map_zone);
//
//		mapFragment.getMapAsync(this);
	    map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map_zone)).getMap();

		   	    
		    
		    if (map!=null)
		    {    	
		    	
			    map.setOnMapClickListener(this);
			    map.setOnMarkerClickListener(this);
			    
			    map.setOnCameraChangeListener(this);
		    	
		    	map.setMyLocationEnabled(true);
		        
		    	map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		    	map.getUiSettings().setZoomControlsEnabled(true);
		    	map.getUiSettings().setCompassEnabled(true);
		        map.getUiSettings().setMyLocationButtonEnabled(true);
		        map.getUiSettings().setAllGesturesEnabled(true);	        

		        if (map.getMyLocation() != null)
		        {		        	
		        	map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(map.getMyLocation().getLatitude(), map.getMyLocation().getLongitude()), 15));
		        }
		        else
		        {
		        	Location curLastKnow = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		        	
		        	if (curLastKnow != null)
		        	{
		        		map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(curLastKnow.getLatitude(), curLastKnow.getLongitude()), 15));
		        	}
		        	else
		        	{
		        		//Hardcode, center of hong kong
		        		map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(22.297339, 114.172351), 12));
		        	}
		        }
		    	
		    	map.setOnMapClickListener(this);
		    	
		    	map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
		            @Override
		            public void onInfoWindowClick(Marker marker) 
		            {
		            	CustomMarkerSnippet curSnippet = new CustomMarkerSnippet();
			            
		            	if (curSnippet.setSnipperContent(marker.getSnippet()) == true)
			            {
			            	if (curSnippet.m_strMarketSnippetType.compareTo(AppConst.MARKER_SNIPPET_TYPE.FIX_ASSET_MARKER) == 0)
			            	{
			            		BuildSnapshotResultRecord(curSnippet);
			            	}
			            }
		            }
		        }); 		    	
		    	
		    	map.setInfoWindowAdapter(new InfoWindowAdapter() {

					@Override
					public View getInfoContents(Marker arg0) {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public View getInfoWindow(Marker pMarker) 
					{

		            	CustomMarkerSnippet curSnippet = new CustomMarkerSnippet();
		            	
		            	if (curSnippet.setSnipperContent(pMarker.getSnippet()) == true)
			            {		            	
			            	View v = null;
			            	
			            	if (curSnippet.m_strMarketSnippetType.compareTo(AppConst.MARKER_SNIPPET_TYPE.POLYGON_MARKER) == 0)
			            	{
			            		v = getLayoutInflater().inflate(R.layout.polygon_marker_info_window, null);
			            		
			            		TextView txtView = (TextView) v.findViewById(R.id.txt_title);
			            		
			            		txtView.setText(pMarker.getTitle());
			            		
			                	txtView = (TextView) v.findViewById(R.id.txt_polygon_id);
			                    
			                    txtView.setText(curSnippet.m_strFixedAssetID);			            		
			            		
			                	txtView = (TextView) v.findViewById(R.id.txt_polygon_desc);
			                    
			                    txtView.setText(curSnippet.m_strFixedAssetDesc);
			                    
			                	txtView = (TextView) v.findViewById(R.id.txt_pointlist);
			                    
			                    txtView.setText(curSnippet.m_strPolygonPointList);			                    
			                    
			            	}
			            	if (curSnippet.m_strMarketSnippetType.compareTo(AppConst.MARKER_SNIPPET_TYPE.FIX_ASSET_MARKER) == 0)
			            	{
			            		v = getLayoutInflater().inflate(R.layout.fixed_asset_marker_info_window, null);

			            		TextView txtView = (TextView) v.findViewById(R.id.txt_title);
			            		
			            		txtView.setText(pMarker.getTitle());
			            		
			                	txtView = (TextView) v.findViewById(R.id.txt_asset_id);
			                    
			                    txtView.setText(curSnippet.m_strFixedAssetID);
			                    
			                    txtView = (TextView) v.findViewById(R.id.txt_latlng_gps);
			                    
			                    txtView.setText(String.format(Locale.US, "(%s,%s)", AppConst.getGeoPointFormater().format(curSnippet.m_fLatitude), AppConst.getGeoPointFormater().format(curSnippet.m_fLongitude)));
	
			                    txtView = (TextView) v.findViewById(R.id.txt_desc);
			                    
			                    txtView.setText(curSnippet.m_strFixedAssetDesc);
			                    
			                    
			                    String strSummary = BuildGeoSummaryForFixAssetInfoWindows(curSnippet.getLocationObject());
			                    
			                    
			                    if (strSummary.length() > 0)
			                    {
				                    txtView = (TextView) v.findViewById(R.id.txt_geo_summary);
				                    
				                    txtView.setText(strSummary.toString());			                    	
			                    }
			                    else
			                    {
			                    	v.findViewById(R.id.geo_point_summary_row).setVisibility(View.GONE);
			                    }
			            	}
			            	else if (curSnippet.m_strMarketSnippetType.compareTo(AppConst.MARKER_SNIPPET_TYPE.POSITION_MARKER) == 0)
			            	{
			                	v = getLayoutInflater().inflate(R.layout.marker_info_window, null);
			                	
			                	TextView txtView = (TextView) v.findViewById(R.id.txt_title);
			                	
			                	txtView.setText(pMarker.getTitle());
			                	
			                	txtView = (TextView) v.findViewById(R.id.txt_datetime);
			                    
			                    txtView.setText(AppConst.formatTime_TO_String_InItsTimeZone(curSnippet.m_dDateTime));
			                    
			                    txtView = (TextView) v.findViewById(R.id.txt_latlng_gps);
			                    
			                    txtView.setText(String.format(Locale.US, "(%s,%s)", AppConst.getGeoPointFormater().format(curSnippet.m_fLatitude), AppConst.getGeoPointFormater().format(curSnippet.m_fLongitude)));
	
			                    txtView = (TextView) v.findViewById(R.id.txt_accuracy);
			                    
			                    if (curSnippet.m_fAccuracyInMeter >= 0)
			                    {
			                    	txtView.setText(String.format(Locale.US, "%.2f %s", curSnippet.m_fAccuracyInMeter, getString(R.string.accuracy_unit)));
			                    }
			                    else
			                    {
			                    	txtView.setText(String.format(Locale.US, "-.-- %s", getString(R.string.accuracy_unit)));
			                    }
	
			                    txtView = (TextView) v.findViewById(R.id.txt_gps_provider);
			                    
			                    txtView.setText(String.format(Locale.US, "%s", curSnippet.m_strProvider));
			                    
			                    if (curSnippet.m_strInZoneList.length() > 0)
			                    {
			                    	txtView = (TextView) v.findViewById(R.id.txt_zonelist);
			                    	txtView.setText(curSnippet.m_strInZoneList);
			                    	
			                    	v.findViewById(R.id.tablerow_zonelist).setVisibility(View.VISIBLE);
			                    }			                    
			                    
			            	} 		                    

			            	((ToggleButton)findViewById(R.id.enable_auto_refresh_button)).setChecked(false);
			            	
			            	return v;
			            }
		            	else
		            		return null;
					}
	
		    	});

		 }
		    
		 handler.sendEmptyMessageDelayed(AppConst.GC_EVENT_ID_PERIODIC_REFRESH, 5000);
	}

	public static String getDeviceId(Context context) {

		String deviceId;

		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			deviceId = Settings.Secure.getString(
					context.getContentResolver(),
					Settings.Secure.ANDROID_ID);
		} else {
			final TelephonyManager mTelephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			if (mTelephony.getDeviceId() != null) {
				deviceId = mTelephony.getDeviceId();
			} else {
				deviceId = Settings.Secure.getString(
						context.getContentResolver(),
						Settings.Secure.ANDROID_ID);
			}
		}

		return deviceId;
	}
	
	public void CleanAllMarker()
	{
		if (map == null)
			return;
		
		map.clear();
		
		if (m_pOverlay != null)
		{
    		m_pOverlay.remove();
    		
    		m_pOverlay = null;
		}		
		
		LandsDept_SetTileProvider();
	}
	
	
    public Bitmap getScaleBitmapfromResource(int pResID, double pRatio)
    {
    	BitmapDrawable bd=(BitmapDrawable) getResources().getDrawable(pResID);
    	Bitmap b=bd.getBitmap();
    	Bitmap bNewSize = Bitmap.createScaledBitmap(b, (int)Math.floor((double)b.getWidth() * pRatio), (int)Math.floor((double)b.getHeight() * pRatio), false);
    	
    	return bNewSize;
    }	
	
	private Marker AddMarkerToMap(LatLng pCurGeoPoint, AppConst.USER_DEFINE_MARK_TYPE pMARK_TYPE, CustomMarkerSnippet pSnipperObj)
	{
		if (pMARK_TYPE == AppConst.USER_DEFINE_MARK_TYPE.GOOGLE_MAP_MY_LOCATION)
		{
//			return map.addMarker(
//						new MarkerOptions().position(pCurGeoPoint)
//						.icon(BitmapDescriptorFactory.fromBitmap(getScaleBitmapfromResource(R.drawable.google_my_position_marker, 0.5)))
//						.title(pSnipperObj.m_strMarkerName)
//						.snippet(pSnipperObj.getSnippetContent())
//					);
			
			return map.addMarker(
					new MarkerOptions().position(pCurGeoPoint)
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.google_my_position_marker))
					.title(pSnipperObj.m_strMarkerName)
					.snippet(pSnipperObj.getSnippetContent())
				);			
		}
		else if (pMARK_TYPE == AppConst.USER_DEFINE_MARK_TYPE.LOCATION_SERVICE_LOCATION_UPDATE)
		{
//			return map.addMarker(
//					new MarkerOptions().position(pCurGeoPoint)
//					.icon(BitmapDescriptorFactory.fromBitmap(getScaleBitmapfromResource(R.drawable.location_service_marker, 0.5)))
//					.title(pSnipperObj.m_strMarkerName)
//					.snippet(pSnipperObj.getSnippetContent())
//				);	
			
			return map.addMarker(
					new MarkerOptions().position(pCurGeoPoint)
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.location_service_marker))
					.title(pSnipperObj.m_strMarkerName)
					.snippet(pSnipperObj.getSnippetContent())
				);			
		}
		else if (pMARK_TYPE == AppConst.USER_DEFINE_MARK_TYPE.NMEA_MESSAGE_GGA_CONTENT)
		{
//			return map.addMarker(
//					new MarkerOptions().position(pCurGeoPoint)
//					.icon(BitmapDescriptorFactory.fromBitmap(getScaleBitmapfromResource(R.drawable.nmea_marker, 0.5)))
//					.title(pSnipperObj.m_strMarkerName)
//					.snippet(pSnipperObj.getSnippetContent())
//				);	
			
			return map.addMarker(
			new MarkerOptions().position(pCurGeoPoint)
			.icon(BitmapDescriptorFactory.fromResource(R.drawable.nmea_marker))
			.title(pSnipperObj.m_strMarkerName)
			.snippet(pSnipperObj.getSnippetContent())
		);				
		}		
		else if (pMARK_TYPE == AppConst.USER_DEFINE_MARK_TYPE.DEFFERENTIAL_GPS_CONTENT)
		{
//			return map.addMarker(
//					new MarkerOptions().position(pCurGeoPoint)
//					.icon(BitmapDescriptorFactory.fromBitmap(getScaleBitmapfromResource(R.drawable.dgps_marker, 0.5)))
//					.title(pSnipperObj.m_strMarkerName)
//					.snippet(pSnipperObj.getSnippetContent())
//				);	
			
			return map.addMarker(
					new MarkerOptions().position(pCurGeoPoint)
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.dgps_marker))
					.title(pSnipperObj.m_strMarkerName)
					.snippet(pSnipperObj.getSnippetContent())
				);			
		}
		else if (pMARK_TYPE == AppConst.USER_DEFINE_MARK_TYPE.FIXED_ASSET_TYPE_1)
		{
//			return map.addMarker(
//					new MarkerOptions().position(pCurGeoPoint)
//					.icon(BitmapDescriptorFactory.fromBitmap(getScaleBitmapfromResource(R.drawable.fixed_asset_type_1, 0.5)))
//					.title(pSnipperObj.m_strMarkerName)
//					.snippet(pSnipperObj.getSnippetContent())
//				);
			
			return map.addMarker(
					new MarkerOptions().position(pCurGeoPoint)
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.fixed_asset_type_1))
					.title(pSnipperObj.m_strMarkerName)
					.snippet(pSnipperObj.getSnippetContent())
				);			
		}
		else if (pMARK_TYPE == AppConst.USER_DEFINE_MARK_TYPE.FIXED_ASSET_TYPE_2)
		{
//			return map.addMarker(
//					new MarkerOptions().position(pCurGeoPoint)
//					.icon(BitmapDescriptorFactory.fromBitmap(getScaleBitmapfromResource(R.drawable.fixed_asset_type_2, 0.5)))
//					.title(pSnipperObj.m_strMarkerName)
//					.snippet(pSnipperObj.getSnippetContent())
//				);	
			
			return map.addMarker(
					new MarkerOptions().position(pCurGeoPoint)
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.fixed_asset_type_2))
					.title(pSnipperObj.m_strMarkerName)
					.snippet(pSnipperObj.getSnippetContent())
				);			
		}
		else if (pMARK_TYPE == AppConst.USER_DEFINE_MARK_TYPE.FIXED_ASSET_TYPE_3)
		{
//			return map.addMarker(
//					new MarkerOptions().position(pCurGeoPoint)
//					.icon(BitmapDescriptorFactory.fromBitmap(getScaleBitmapfromResource(R.drawable.fixed_asset_type_3, 0.5)))
//					.title(pSnipperObj.m_strMarkerName)
//					.snippet(pSnipperObj.getSnippetContent())
//				);		
			
			return map.addMarker(
					new MarkerOptions().position(pCurGeoPoint)
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.fixed_asset_type_3))
					.title(pSnipperObj.m_strMarkerName)
					.snippet(pSnipperObj.getSnippetContent())
				);				
		}
		else if (pMARK_TYPE == AppConst.USER_DEFINE_MARK_TYPE.FIXED_ASSET_TYPE_4)
		{
//			return map.addMarker(
//					new MarkerOptions().position(pCurGeoPoint)
//					.icon(BitmapDescriptorFactory.fromBitmap(getScaleBitmapfromResource(R.drawable.fixed_asset_type_4, 0.5)))
//					.title(pSnipperObj.m_strMarkerName)
//					.snippet(pSnipperObj.getSnippetContent())
//				);
			
			return map.addMarker(
					new MarkerOptions().position(pCurGeoPoint)
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.fixed_asset_type_4))
					.title(pSnipperObj.m_strMarkerName)
					.snippet(pSnipperObj.getSnippetContent())
				);				
		}
		else if (pMARK_TYPE == AppConst.USER_DEFINE_MARK_TYPE.FIXED_ASSET_TYPE_5)
		{
//			return map.addMarker(
//					new MarkerOptions().position(pCurGeoPoint)
//					.icon(BitmapDescriptorFactory.fromBitmap(getScaleBitmapfromResource(R.drawable.fixed_asset_type_5, 0.5)))
//					.title(pSnipperObj.m_strMarkerName)
//					.snippet(pSnipperObj.getSnippetContent())
//				);			
			
			return map.addMarker(
					new MarkerOptions().position(pCurGeoPoint)
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.fixed_asset_type_5))
					.title(pSnipperObj.m_strMarkerName)
					.snippet(pSnipperObj.getSnippetContent())
				);				
		}
		else if (pMARK_TYPE == AppConst.USER_DEFINE_MARK_TYPE.FIXED_ASSET_TYPE_6)
		{
//			return map.addMarker(
//					new MarkerOptions().position(pCurGeoPoint)
//					.icon(BitmapDescriptorFactory.fromBitmap(getScaleBitmapfromResource(R.drawable.fixed_asset_type_6, 0.5)))
//					.title(pSnipperObj.m_strMarkerName)
//					.snippet(pSnipperObj.getSnippetContent())
//				);
			
			return map.addMarker(
					new MarkerOptions().position(pCurGeoPoint)
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.fixed_asset_type_6))
					.title(pSnipperObj.m_strMarkerName)
					.snippet(pSnipperObj.getSnippetContent())
				);			
		}		
		else if (pMARK_TYPE == AppConst.USER_DEFINE_MARK_TYPE.FIXED_ASSET_TYPE_7)
		{
//			return map.addMarker(
//					new MarkerOptions().position(pCurGeoPoint)
//					.icon(BitmapDescriptorFactory.fromBitmap(getScaleBitmapfromResource(R.drawable.fixed_asset_type_7, 0.5)))
//					.title(pSnipperObj.m_strMarkerName)
//					.snippet(pSnipperObj.getSnippetContent())
//				);	
			
			return map.addMarker(
					new MarkerOptions().position(pCurGeoPoint)
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.fixed_asset_type_7))
					.title(pSnipperObj.m_strMarkerName)
					.snippet(pSnipperObj.getSnippetContent())
				);			
		}
		else if (pMARK_TYPE == AppConst.USER_DEFINE_MARK_TYPE.FIXED_ASSET_TYPE_8)
		{
//			return map.addMarker(
//					new MarkerOptions().position(pCurGeoPoint)
//					.icon(BitmapDescriptorFactory.fromBitmap(getScaleBitmapfromResource(R.drawable.fixed_asset_type_8, 0.5)))
//					.title(pSnipperObj.m_strMarkerName)
//					.snippet(pSnipperObj.getSnippetContent())
//				);
			
			return map.addMarker(
					new MarkerOptions().position(pCurGeoPoint)
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.fixed_asset_type_8))
					.title(pSnipperObj.m_strMarkerName)
					.snippet(pSnipperObj.getSnippetContent())
				);			
		}
		else if (pMARK_TYPE == AppConst.USER_DEFINE_MARK_TYPE.FIXED_ASSET_TYPE_9)
		{
//			return map.addMarker(
//					new MarkerOptions().position(pCurGeoPoint)
//					.icon(BitmapDescriptorFactory.fromBitmap(getScaleBitmapfromResource(R.drawable.fixed_asset_type_9, 0.5)))
//					.title(pSnipperObj.m_strMarkerName)
//					.snippet(pSnipperObj.getSnippetContent())
//				);	
			return map.addMarker(
					new MarkerOptions().position(pCurGeoPoint)
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.fixed_asset_type_9))
					.title(pSnipperObj.m_strMarkerName)
					.snippet(pSnipperObj.getSnippetContent())
				);			
		}	
		else if (pMARK_TYPE == AppConst.USER_DEFINE_MARK_TYPE.FIXED_ASSET_TYPE_DEFAULT)
		{
//			return map.addMarker(
//					new MarkerOptions().position(pCurGeoPoint)
//					.icon(BitmapDescriptorFactory.fromBitmap(getScaleBitmapfromResource(R.drawable.fixed_asset_type_0, 0.5)))
//					.title(pSnipperObj.m_strMarkerName)
//					.snippet(pSnipperObj.getSnippetContent())
//				);
			
			return map.addMarker(
					new MarkerOptions().position(pCurGeoPoint)
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.fixed_asset_type_0))
					.title(pSnipperObj.m_strMarkerName)
					.snippet(pSnipperObj.getSnippetContent())
				);			
		}
		else if (pMARK_TYPE == AppConst.USER_DEFINE_MARK_TYPE.POLYGON_MARKER)
		{
//			return map.addMarker(
//					new MarkerOptions().position(pCurGeoPoint)
//					.icon(BitmapDescriptorFactory.fromBitmap(getScaleBitmapfromResource(R.drawable.polygon_mark, 0.5)))
//					.title(pSnipperObj.m_strMarkerName)
//					.snippet(pSnipperObj.getSnippetContent())
//				);	
			
			return map.addMarker(
					new MarkerOptions().position(pCurGeoPoint)
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.polygon_mark))
					.title(pSnipperObj.m_strMarkerName)
					.snippet(pSnipperObj.getSnippetContent())
				);			
		}			
		else
			return null;
	}	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onMarkerClick(Marker arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onMapClick(LatLng arg0) {
		// TODO Auto-generated method stub
		((ToggleButton)findViewById(R.id.enable_auto_refresh_button)).setChecked(true);
		
		
		if (map != null)
		{
			Point clickPoint = map.getProjection().toScreenLocation(arg0);
			
			TextView txtView = (TextView) findViewById(R.id.txt_summary_info);
			
			if (txtView.getVisibility() != View.VISIBLE)
			{
				if (clickPoint.y <= 50)
				{
					txtView.setVisibility(View.VISIBLE);
				}
			}
			else
			{
				if (clickPoint.y < txtView.getHeight())
				{
					txtView.setVisibility(View.INVISIBLE);
				}
			}
			
			//txtView.getHeight();
			
			//Toast.makeText(this, String.format(Locale.US, "X = %d, Y = %d, Height = %d", clickPoint.x, clickPoint.y, txtView.getHeight()), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) 
        {
            case R.id.action_show_satellite_view:
            	if (map != null)
            	{
            		map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            		LandsDept_SetTileProvider();
            	}
            	return true;
            case R.id.action_show_map_view:
            	
            	if (map != null)
            	{
            		map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            		LandsDept_SetTileProvider();
            	}

            	return true;
            case R.id.action_show_lands_dept_map_view:
            	
            	if (map != null)
            	{
            		map.setMapType(GoogleMap.MAP_TYPE_NONE);
            		
            		LandsDept_SetTileProvider();
            	}

            	return true;            	
            case R.id.action_show_refresh_all:

            	RefreshAllMarkers(true, true);

            	return true;  
            case R.id.action_show_setting:
            	
            	//Force the Fixed Asset and Polygon to refresh on resume
            	//Set it to null value before starting the setting activity, return from setting activity will force to refresh those files

            	m_FixedAsset = null;
            	m_Polygon = null;
            	
            	Intent myIntent = new Intent(this, System_setting.class);
            	startActivity(myIntent);

            	return true;
            case R.id.action_reload_nearest_fixed_asset_and_polygon_items:
            	
            	Reload_NearestItems();
            		
            	return true;            	
            case R.id.action_test_simulate_DGNSS_Response:
            	Simulate_DGNSS_Response();
            	return true;
            default:
                return super.onOptionsItemSelected(item);
        }
	}

	@Override
	protected void onPause() {
		
		//Log.w("OnPause", "Executed");
		
		locationManager.removeUpdates(this);
		
		logFile.WriteSessionBreak(false);
		logFile.CloseLogFile();
			
		m_bIsQuickThread = true;
		
		try 
		{
			synchronized(threadWaiter)
			{
				threadWaiter.wait(3000);
			}
		} 
		catch (Exception ex) 
		{
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}		
		
		super.onPause();
	}

	@Override
	protected void onResume() {

		//Log.w("onResume", "Executed");
		
		m_NMEA_MSG_Queue = new NMEA_Memory_Queue(10000,imei);
		
		if (LocalReferenceInfo.GetLocalReference(AppConst.SHARE_PREFERENCE_KEY.DGNSS_SERVER_LOG_MODE , getApplicationContext(), "True").compareToIgnoreCase("True") == 0)
			gc_bIs_enable_log = true;
		else
			gc_bIs_enable_log = false;
		
		String l_strFilterNmeaList = LocalReferenceInfo.GetLocalReference(AppConst.SHARE_PREFERENCE_KEY.DGNSS_NMEA_FILTER , getApplicationContext(), AppConst.GC_DEFAULT_DGPS_NMEA_FILTER);
		
		if (l_strFilterNmeaList.length() <= 0)
		{
			m_NMEA_FilterArrayList = null;
		}
		else
		{
			m_NMEA_FilterArrayList = l_strFilterNmeaList.split(";");
			
			boolean bIsAllEmpty = true;
			//Test is all empty
			for (String strfilters : m_NMEA_FilterArrayList)
			{
				if (strfilters.length() > 0)
				{
					bIsAllEmpty = false;
					break;
				}
			}
			
			if (bIsAllEmpty == true)
			{
				m_NMEA_FilterArrayList = null;
			}
		}
		
		gc_str_remoteServerIP = LocalReferenceInfo.GetLocalReference(AppConst.SHARE_PREFERENCE_KEY.DGNSS_SERVER_IP , getApplicationContext(), AppConst.GC_DEFAULT_DGPS_SERVER_IP);
		gc_str_remoteServerPort = LocalReferenceInfo.GetLocalReference(AppConst.SHARE_PREFERENCE_KEY.DGNSS_SERVER_PORT, getApplicationContext(), AppConst.GC_DEFAULT_DGPS_SERVER_PORT);	

		gc_str_landMapServerIP = LocalReferenceInfo.GetLocalReference(AppConst.SHARE_PREFERENCE_KEY.LANDS_MAP_SERVER_IP , getApplicationContext(), AppConst.GC_DEFAULT_LANDS_MAP_SERVER_IP);
		gc_str_landMapServerPort = LocalReferenceInfo.GetLocalReference(AppConst.SHARE_PREFERENCE_KEY.LANDS_MAP_SERVER_PORT, getApplicationContext(), AppConst.GC_DEFAULT_LANDS_MAP_SERVER_PORT);		
		
		gc_nShowItemsCount = (int)LocalReferenceInfo.GetLocalReference(AppConst.SHARE_PREFERENCE_KEY.MAX_SHOW_ITEM_COUNT, getApplicationContext(), AppConst.GC_DEFAULT_MAX_SHOW_ITEM_COUNT);	

		if (gc_nShowItemsCount <= 10)
			gc_nShowItemsCount = 10;
		else if (gc_nShowItemsCount > 1000)
			gc_nShowItemsCount = 1000;
		
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
		
		logFile.OpenLogFile();
		logFile.WriteSessionBreak(true);
		
		if (LocalReferenceInfo.GetLocalReference(AppConst.SHARE_PREFERENCE_KEY.SHOW_POSITION_POINTER_GOOGLE_DGPS, getApplicationContext(), AppConst.GC_SHOW_FLAG_DEFAULT).compareToIgnoreCase(AppConst.GC_SHOW_FLAG_TRUE) == 0)
		{
			m_bShowGoogleDGPS_Marker = true;
		}
		else
		{
			m_bShowGoogleDGPS_Marker = false;
		}		
		
		if (LocalReferenceInfo.GetLocalReference(AppConst.SHARE_PREFERENCE_KEY.SHOW_POSITION_POINTER_LOCATION_SERVICE, getApplicationContext(), AppConst.GC_SHOW_FLAG_DEFAULT).compareToIgnoreCase(AppConst.GC_SHOW_FLAG_TRUE) == 0)
		{
			m_bShowLocationSrv_Marker = true;
		}
		else
		{
			m_bShowLocationSrv_Marker = false;
		}		
		
		if (LocalReferenceInfo.GetLocalReference(AppConst.SHARE_PREFERENCE_KEY.SHOW_POSITION_POINTER_NMEA_MSG_GPS, getApplicationContext(), AppConst.GC_SHOW_FLAG_DEFAULT).compareToIgnoreCase(AppConst.GC_SHOW_FLAG_TRUE) == 0)
		{
			m_bShowNMEA_GPS_Marker = true;
		}
		else
		{
			m_bShowNMEA_GPS_Marker = false;
		}			
		
		Reload_FixedAsset();
		Reload_Polygon();
		
		RefreshAllMarkers(true, true);
		
		StartTCPSocketThread();
		
		TurnGPSOn_IfNotEnabled();
		
		ShowScaleRulerOnMap();
		
		super.onResume();
	}
	
	private void Reload_Polygon()
	{
		//========================================================================================
		StringBuilder strLoadingExternalFileErrorReason = new StringBuilder(50);		
		//========================================================================================
		
		if (m_Polygon != null)
		{
			if (m_Polygon.IsTimeoutForRefresh() == false)	//No need to refresh 
			{
				return;
			}
		}
		
		m_Polygon = new PolygonPaser();
		
		if (m_Polygon.ReadPolygonFile(LocalReferenceInfo.GetLocalReference(AppConst.SHARE_PREFERENCE_KEY.POLYGON_ZONE_FILE, getApplicationContext(), AppConst.GC_DEFAULT_POLYGON_FILE), strLoadingExternalFileErrorReason) == false)
		{
			if (strLoadingExternalFileErrorReason.length() > 0)
			{
				Toast.makeText(this, strLoadingExternalFileErrorReason, Toast.LENGTH_LONG).show();
			}
			
			m_Polygon = null;
		}
		else
		{
			m_Polygon.RefreshNearestPolygon(map.getCameraPosition().target, gc_nShowItemsCount);
		}
		//========================================================================================
		
	}
	
	private void Reload_NearestItems()
	{
		if (m_FixedAsset != null)
		{
			m_FixedAsset.RefreshNearestFixedAsset(map.getCameraPosition().target, gc_nShowItemsCount);
		}
		else
			Reload_FixedAsset();
		
		if (m_Polygon != null)
		{
			m_Polygon.RefreshNearestPolygon(map.getCameraPosition().target, gc_nShowItemsCount);
		}
		else
			Reload_Polygon();
		
		RefreshAllMarkers(true, true);

		try
		{
			int nFixedAssetTotalCounter = 0;
			int nFixedAssetDisplayCounter = 0;
			
			int nPolygonTotalCounter = 0;
			int nPolygonDisplayCounter = 0;			
		
			if (m_FixedAsset != null)
			{
				nFixedAssetTotalCounter = m_FixedAsset.getTotalItems();
				nFixedAssetDisplayCounter = m_FixedAsset.getShortListedItemsCountForDisplay();
			}
			
			if (m_Polygon != null)
			{
				nPolygonTotalCounter = m_Polygon.getTotalItems();
				nPolygonDisplayCounter = m_Polygon.getShortListedItemsCountForDisplay();			
			}
			
			Toast.makeText(this, String.format(Locale.US, "Asset( %d over %d )\nPolygon( %d over %d )\nReloaded Successfully!", nFixedAssetDisplayCounter, nFixedAssetTotalCounter, nPolygonDisplayCounter, nPolygonTotalCounter) , Toast.LENGTH_SHORT).show();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
	}
	
	private void Reload_FixedAsset()
	{
		//========================================================================================
		StringBuilder strLoadingExternalFileErrorReason = new StringBuilder(50);
		//========================================================================================
		
		if (m_FixedAsset != null)
		{
			if (m_FixedAsset.IsTimeoutForRefresh() == false)	//No need to refresh 
			{
				return;
			}
		}
				
		m_FixedAsset = new FixAssetPaser();
		
		if (m_FixedAsset.ReadFixedAssetFile(LocalReferenceInfo.GetLocalReference(AppConst.SHARE_PREFERENCE_KEY.FIXED_ASSET_FILE, getApplicationContext(), AppConst.GC_DEFAULT_FIXED_ASSET_FILE), strLoadingExternalFileErrorReason) == false)
		{
			if (strLoadingExternalFileErrorReason.length() > 0)
			{
				Toast.makeText(this, strLoadingExternalFileErrorReason, Toast.LENGTH_LONG).show();
			}
						
			m_FixedAsset = null;
		}
		else
		{		
			m_FixedAsset.RefreshNearestFixedAsset(map.getCameraPosition().target, gc_nShowItemsCount);
		}
		
	}
	
	private void DrawScreenCenterMarket()
	{
//		CustomMarkerSnippet pTest = new CustomMarkerSnippet();
//		
//		pTest.m_fLatitude = map.getCameraPosition().target.latitude;
//		pTest.m_fLongitude = map.getCameraPosition().target.longitude;
//		pTest.m_strFixedAssetID = "1";
//		pTest.m_strMarkerName = "Test - Screen Center";
//		pTest.m_strFixedAssetID = "Dummy - 1";
//		
//		AddMarkerToMap(map.getCameraPosition().target, AppConst.USER_DEFINE_MARK_TYPE.FIXED_ASSET_TYPE_1, pTest);		
	}
	
	
	private void TurnGPSOn_IfNotEnabled()
	{
		String strProvider = "";
		
		try
		{
//			Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
//			intent.putExtra("enabled", true);
//			sendBroadcast(intent);
		    			
			strProvider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
			
			//
			
			if (strProvider == null)
			{
				//Toast.makeText(this, "GPS module is not enabled!", Toast.LENGTH_SHORT).show();
				
				TurnGPSOn_IfNotEnabled_DialogBox();
				
				return;
			}
			else
			{
				if(!strProvider.contains("gps"))
				{
					//Toast.makeText(this, "GPS module is not enabled!", Toast.LENGTH_SHORT).show();
					TurnGPSOn_IfNotEnabled_DialogBox();
					return;
				}
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Toast.makeText(this, "Accessing GPS module failture!", Toast.LENGTH_SHORT).show();
		}
		
//		Log.w("LocationMode", strProvider);
//		
//		if(!strProvider.contains("gps"))
//		{ //if gps is off
//			try
//			{
//				final Intent enablegps = new Intent();
//				enablegps.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider"); 
//				enablegps.addCategory(Intent.CATEGORY_ALTERNATIVE);
//				enablegps.setData(Uri.parse("3")); 
//				sendBroadcast(enablegps);
//			}
//			catch(Exception ex)
//			{
//				ex.printStackTrace();
//				Toast.makeText(this, "Try to Turn-On GPS failure!", Toast.LENGTH_SHORT).show();
//			}
//		}
	}
	
    private void TurnGPSOn_IfNotEnabled_DialogBox()
    {
        new AlertDialog.Builder(this)
        .setIcon(R.drawable.warning_sign)
        .setTitle(R.string.alert_box_gps_module_is_not_enabled)
        .setMessage(R.string.alert_box_turn_on_gps_module_now)
        .setPositiveButton(R.string.alert_box_yes, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

            	startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
            }

        })
        .setNegativeButton(R.string.alert_box_no, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
            }

        })
        .show(); 	
    }	
	
	public void ShowPolygonOnMap()
	{
		if (m_PolygonMarkerList.size() > 0) {
			for(Marker maker:m_PolygonMarkerList) {
				maker.remove();
			}
			m_PolygonMarkerList.clear();		
		}
		if (m_Polygon == null)
			return;
		
		if (m_Polygon.m_arrPolygon == null)
			return;
			
		for (PolygonPaser.Polygon_Data curPolygon : m_Polygon.m_arrPolygon)
		{
			if (curPolygon.m_PolygonGeoPoints.size() >= 3)
			{
					
				PolygonOptions polyOpt = new PolygonOptions().strokeWidth(AppConst.nStrokeWidth).strokeColor(AppConst.nStrokeColor).fillColor(AppConst.nFillColor);
 
				polyOpt.addAll(curPolygon.m_PolygonGeoPoints);
				  
				map.addPolygon(polyOpt);
					
				LatLng centerPoint = curPolygon.findPolygonCenterPoint();
		  		  					
		    	CustomMarkerSnippet pSnippet = new CustomMarkerSnippet();		    		
		    	pSnippet.SetMarkerSnippetType(AppConst.MARKER_SNIPPET_TYPE.POLYGON_MARKER);
		    	pSnippet.m_strMarkerName = curPolygon.m_strPolygonName;
		    	pSnippet.m_fLatitude = centerPoint.latitude;
		    	pSnippet.m_fLongitude = centerPoint.longitude;
		    	pSnippet.m_strFixedAssetID = curPolygon.m_strPolygonID;
		    	pSnippet.m_strFixedAssetDesc = curPolygon.m_strPolygonDesc;
		    	pSnippet.m_strPolygonPointList = curPolygon.getPointList();
		    		
		    	m_PolygonMarkerList.add(AddMarkerToMap(pSnippet.getGeoPosition(), AppConst.USER_DEFINE_MARK_TYPE.POLYGON_MARKER, pSnippet));
			}
		}		
	}
	
	
	public void ShowFixedAssetOnMap()
	{
		if (m_FixedAssetMarkerList.size() >0) {	
			for(Marker maker:m_FixedAssetMarkerList) {
				maker.remove();
			}
			m_FixedAssetMarkerList.clear();		
		}
		
		if (m_FixedAsset == null)
			return;
		
		if (m_FixedAsset.m_arrFixedAssets == null)
			return;
						
		for (FixAssetPaser.FixedAsset_Data curAsset : m_FixedAsset.m_arrFixedAssets)
		{

			if (curAsset.m_GeoPoint != null)
			{
	    		CustomMarkerSnippet pSnippet = new CustomMarkerSnippet();
	    		
	    		pSnippet.SetMarkerSnippetType(AppConst.MARKER_SNIPPET_TYPE.FIX_ASSET_MARKER);
	    		pSnippet.m_strMarkerName = curAsset.m_strAssetName;
	    		pSnippet.m_fLatitude = curAsset.m_GeoPoint.latitude;
	    		pSnippet.m_fLongitude = curAsset.m_GeoPoint.longitude;
	    		pSnippet.m_strFixedAssetID = curAsset.m_strAssetID;
	    		pSnippet.m_strFixedAssetDesc = curAsset.m_strAssetDesc;
	    		
	    		int nAssetTypeID = 0;
	    		try
	    		{
	    			nAssetTypeID = Integer.valueOf(curAsset.m_strAssetTypeID);
	    		}
	    		catch (Exception ex)
	    		{
	    			nAssetTypeID = 0;
	    		}
	    		
	    		switch (nAssetTypeID)
	    		{
	    			case 0:	    			
	    				m_FixedAssetMarkerList.add(AddMarkerToMap(pSnippet.getGeoPosition(), AppConst.USER_DEFINE_MARK_TYPE.FIXED_ASSET_TYPE_DEFAULT, pSnippet));
		    			break;	    				    		
		    		case 1:
		    			m_FixedAssetMarkerList.add(AddMarkerToMap(pSnippet.getGeoPosition(), AppConst.USER_DEFINE_MARK_TYPE.FIXED_ASSET_TYPE_1, pSnippet));
		    			break;
		    		case 2:
		    			m_FixedAssetMarkerList.add(AddMarkerToMap(pSnippet.getGeoPosition(), AppConst.USER_DEFINE_MARK_TYPE.FIXED_ASSET_TYPE_2, pSnippet));
		    			break;
		    		case 3:
		    			m_FixedAssetMarkerList.add(AddMarkerToMap(pSnippet.getGeoPosition(), AppConst.USER_DEFINE_MARK_TYPE.FIXED_ASSET_TYPE_3, pSnippet));
		    			break;
		    		case 4:
		    			m_FixedAssetMarkerList.add(AddMarkerToMap(pSnippet.getGeoPosition(), AppConst.USER_DEFINE_MARK_TYPE.FIXED_ASSET_TYPE_4, pSnippet));
		    			break;
		    		case 5:
		    			m_FixedAssetMarkerList.add(AddMarkerToMap(pSnippet.getGeoPosition(), AppConst.USER_DEFINE_MARK_TYPE.FIXED_ASSET_TYPE_5, pSnippet));
		    			break;
		    		case 6:
		    			m_FixedAssetMarkerList.add(AddMarkerToMap(pSnippet.getGeoPosition(), AppConst.USER_DEFINE_MARK_TYPE.FIXED_ASSET_TYPE_6, pSnippet));
		    			break;		    			
		    		case 7:
		    			m_FixedAssetMarkerList.add(AddMarkerToMap(pSnippet.getGeoPosition(), AppConst.USER_DEFINE_MARK_TYPE.FIXED_ASSET_TYPE_7, pSnippet));
		    			break;		    			
		    		case 8:
		    			m_FixedAssetMarkerList.add(AddMarkerToMap(pSnippet.getGeoPosition(), AppConst.USER_DEFINE_MARK_TYPE.FIXED_ASSET_TYPE_8, pSnippet));
		    			break;	    			
		    		case 9:
		    			m_FixedAssetMarkerList.add(AddMarkerToMap(pSnippet.getGeoPosition(), AppConst.USER_DEFINE_MARK_TYPE.FIXED_ASSET_TYPE_9, pSnippet));
		    			break;		    			
		    		default:
		    			m_FixedAssetMarkerList.add(AddMarketToMap_customized_icon(pSnippet.getGeoPosition(), nAssetTypeID, pSnippet));
		    			break;	    			
	    		}
			}
	    		
		}

	}
	
	private Marker AddMarketToMap_customized_icon(LatLng pCurGeoPoint, int pnAssetType_ID, CustomMarkerSnippet pSnipperObj)
	{
		String l_strPath = Environment.getExternalStorageDirectory().getPath() + AppConst.GC_DEFAULT_SHARP_LIB_DIRECTORY;
		String l_strFileName = String.format(Locale.US, "fixed_asset_type_%d.png", pnAssetType_ID);
		
		File targetPath = new File(l_strPath);
		
		File file = new File(l_strPath, l_strFileName);
		
		targetPath.mkdirs();
		
		if (file.exists() == false)
		{			
			return AddMarkerToMap(pCurGeoPoint, AppConst.USER_DEFINE_MARK_TYPE.FIXED_ASSET_TYPE_DEFAULT, pSnipperObj);
		}
		else
		{
			try
			{
				return map.addMarker(
						new MarkerOptions().position(pCurGeoPoint)
						.icon(BitmapDescriptorFactory.fromPath(l_strPath + "/" + l_strFileName))
						.title(pSnipperObj.m_strMarkerName)
						.snippet(pSnipperObj.getSnippetContent())
					);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				
				return AddMarkerToMap(pCurGeoPoint, AppConst.USER_DEFINE_MARK_TYPE.FIXED_ASSET_TYPE_DEFAULT, pSnipperObj);
			}
		}
	}
	
	public void CloseSocketSafely(Socket curSocket)
	{
		try
		{
			if (curSocket != null)
			{
				curSocket.close();
			}
		}
		catch (Exception ex)
		{
			//Log.w("CloseSocketSafely", "Close Socket Exception");
		}
	}
	
	private void ThreadBody()
	{
		Socket m_Socket = null;
		
		//Log.w("ThreadBody", "Entry - Thread Started");
		
		try
		{	

			while (m_bIsQuickThread == false)
			{
				if (m_Socket == null)
				{
					m_Socket = new Socket();
				}
				
				//Log.w("ThreadBody", "1");
				
				if (ConnectRemoteServer(m_Socket, gc_str_remoteServerIP, gc_str_remoteServerPort) == true)
				{
					m_currentConnection_isConnected = true;
					boolean l_IsSendRecOccurs = false;
					
					//Log.w("ThreadBody", "2");
					
					if (m_NMEA_MSG_Queue.getSize() > 0)
					{	
						//Log.w("ThreadBody", "3");
						
						String curNMEA = m_NMEA_MSG_Queue.GetNmeaFromQueueHead(false);
						
						byte[] bytes = curNMEA.getBytes();
					
						try
						{
							//Log.w("ThreadBody", "4");
							
							m_Socket.getOutputStream().write(bytes);
							String asc = new String(bytes, "US_ASCII");
							//Log.i("To GIS gateway",asc);							
							m_NMEA_MSG_Queue.GetNmeaFromQueueHead(true);	//Remove just send out message from queue
						}
						catch(Exception ex)
						{
							//Log.w("ThreadBody", "Send NMEA to Server Failure, Close socket now!");
							
							CloseSocketSafely(m_Socket);
							
							m_Socket = null;
						}
						
						l_IsSendRecOccurs = true;
					}
					
					//Log.w("ThreadBody", "5");
					
					if (m_Socket != null)
					{
						//Log.w("ThreadBody", "6");
						if (m_Socket.getInputStream().available() > 0)
						{
							//Log.w("ThreadBody", "7");
							
							byte[] RecBytes = new byte[200];
							
							Arrays.fill(RecBytes, (byte)0);
							
							int RecCount = 0;
							
							try
							{
								//Log.w("ThreadBody", "8");
								
								RecCount = m_Socket.getInputStream().read(RecBytes);
								
								if (RecCount > 0)
								{
									m_strSocketRecButter.append(new String(RecBytes, 0, RecCount));

									//String asc = new String(RecBytes, "US_ASCII");
									//Log.i("from GIS gateway",asc);
									//String asc = m_strSocketRecButter.toString();
									if ( m_strSocketRecButter.toString().contains("<R>FAIL")) {
									
										MainActivity.this.runOnUiThread(new Runnable() {
										    public void run() {
										    	Toast.makeText(m_Activity, "Login fail", Toast.LENGTH_SHORT).show();
										    }
										});
									}
									
								}
							}
							catch(Exception ex)
							{
								//ex.printStackTrace();
								//Log.w("ThreadBody", "Receive Correction From Server Failure, Close socket now!");
							}
							
							l_IsSendRecOccurs = true;
						}
					}
					
					//Log.w("ThreadBody", "9");
					
					if (m_strSocketRecButter.length() > 0)
					{
						//There is buffer information need to process
						
						//Log.w("Socket Buffer recevied", m_strSocketRecButter.toString());
						/*
						MainActivity.this.runOnUiThread(new Runnable() {
						    public void run() {
						    	Toast.makeText(MainActivity.this, "m_strSocketRecButter.length() > 0", Toast.LENGTH_SHORT).show();
						    }
						});
						*/
						
						int EndOfMsgPos = m_strSocketRecButter.indexOf("\n");
						
						//Log.w("ThreadBody", "10");
						if (EndOfMsgPos < 0)
						{
							//Means End of Message is not find '\n'
							//General, each message should be less than 50 characters in length, e.g. <D>1396933112244, 123.12345678, 123.12345678</D>
							
							if (m_strSocketRecButter.length() > 100)
							{
								logFile.WriteFormatLogToFile(m_strSocketRecButter.toString() + " <= Msg Len more than 100 before reaching the end. Too Long!");
								//Clear the stringbuilder with new instance.
								m_strSocketRecButter = new StringBuilder(200);
							
							}
						}
						else // if ( EndOfMsgPos >= 0)
						{
							String m_ReplyMsg = m_strSocketRecButter.substring(0, EndOfMsgPos + 1);
							
							m_strSocketRecButter.delete(0, EndOfMsgPos + 1);
							
							m_ReplyMsg = m_ReplyMsg.replaceAll("\r", "").replaceAll("\n", "").toUpperCase(Locale.US);
							
							//Log.w("ThreadBody", "11");
							
							if ((m_ReplyMsg.startsWith("<D>") == true) && (m_ReplyMsg.endsWith("</D>") == true))
							{
								logFile.WriteFormatLogToFile(m_ReplyMsg + " <= Delta Correction Detected!");
								//Delta Correction message "<D> LAT, LONG </D>\n"
								m_ReplyMsg = m_ReplyMsg.replaceAll("<D>", "").replaceAll("</D>", "");
								
								if (handler != null)
								{
									try
									{
										
										Message msg = new Message();
										msg.what = AppConst.GC_EVENT_ID_DGPS_DELTA_CORRECTION_RECEIVED;
										msg.obj = m_ReplyMsg;
										
										handler.sendMessage(msg);
									}
									catch (Exception ex)
									{
										//Log.w("ThreadBody", "Notify Delta Correction Failure!");
									}									
								}								
							}
							else if ((m_ReplyMsg.startsWith("<A>") == true) && (m_ReplyMsg.endsWith("</A>") == true))
							{
								//Absolute Correction message "<A> LAT, LONG </A>\n"
								logFile.WriteFormatLogToFile(m_ReplyMsg + " <= Absolute Correction Detected!");
								
								m_ReplyMsg = m_ReplyMsg.replaceAll("<A>", "").replaceAll("</A>", "");
								
								if (handler != null)
								{
									try
									{
										Message msg = new Message();
										msg.what = AppConst.GC_EVENT_ID_DGPS_ABSOLUTE_CORRECTION_RECEIVED;
										msg.obj = m_ReplyMsg;
			        				
										final String result = m_ReplyMsg;
										/*
			    						MainActivity.this.runOnUiThread(new Runnable() {
			    						    public void run() {
			    						    	Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
			    						    }
			    						});
			    						*/			        					
										handler.sendMessage(msg);
									}
									catch (Exception ex)
									{
										//Log.w("ThreadBody", "Notify Absolute Correction Failure!");
									}									
								}								
							}
							else if ((m_ReplyMsg.startsWith("<I>") == true) && (m_ReplyMsg.endsWith("</I>") == true))
							{
								logFile.WriteFormatLogToFile(m_ReplyMsg + " <= Insufficient Data Response Detected!");															
							}							
							else
							{
								logFile.WriteFormatLogToFile(m_ReplyMsg + " <= Invalid Response, Not identified!");
							}
						}
					}
					
					//Log.w("ThreadBody", "12");
					
					if (l_IsSendRecOccurs == false)
					{
						//No any send & Receive Activity, put the thread into sleep for while to release resource. 
						
						try
						{
							Thread.sleep(200);
						}
						catch (Exception ex)
						{
							ex.printStackTrace();
						}				
					}
					
					//Log.w("ThreadBody", "13");
				}
				else
				{
					//In case of connection request failure, sleep for a while before retry!
					m_currentConnection_isConnected = false;
					
					m_Socket = null;
					
					try
					{
						Thread.sleep(200);
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
			
			//Log.w("Thread Quite", "m_bIsQuickThread == true!");
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
		//Log.w("ThreadBody", "Leave - Thread Stopped");
	}
	
	private boolean ConnectRemoteServer(Socket curSocket, String pstrRemoteHost, long pnRemotePort)
	{
				
		try
		{
			if (curSocket.isConnected() == true)
				return true;

			String pName = LocalReferenceInfo.GetLocalReference(AppConst.SHARE_PREFERENCE_KEY.DGPS_LOGIN_NAME, getApplicationContext(), "");
			String pwd = LocalReferenceInfo.GetLocalReference(AppConst.SHARE_PREFERENCE_KEY.DGPS_LOGIN_PASSWORD_MD5, getApplicationContext(), "");
			
			if (pName.isEmpty() || pwd.isEmpty()) {
				//Log.w("DGPS server login", "name or pwd is empty");
				return false;
			}
					
			
			//Read Timeout 0.5 seconds
			curSocket.setSoTimeout(5000);
			
			curSocket.connect(new InetSocketAddress(pstrRemoteHost, (int)pnRemotePort), 5000);
			//String loginStr = "<L>PETER, b471f2b105a8ba549e5f428220acd0b6, DeveloperTest-01</L>\r\n";
			String loginStr = "<L>" + pName + "," + pwd + "," + "DeveloperTest-01" + "</L>\r\n"; 
			curSocket.getOutputStream().write(loginStr.getBytes());
			//Log.w("ConnectRemoteServer", "Connection Remote Server Success!");
			
			return true;
		}
		catch (Exception ex)
		{
			//ex.printStackTrace();
			
			//Log.w("ConnectRemoteServer", "Connection Request Failure!");
			
			return false;
		}		
	}
	
	private void StartTCPSocketThread()
	{
		m_bIsQuickThread = false;

		//Thread Definition
		new Thread(new Runnable() 
        { 
            public void run()
            {
            	try
            	{
            		if (handler != null)
            		{
            			try
            			{
            				//Force to refresh the status
            				handler.sendEmptyMessage(AppConst.GC_EVENT_ID_THREAD_START_CHANGE);
            			}
            			catch (Exception ex)
            			{
            				//Just ignore if any exception occurs
            			}									
            		}
            		
            		ThreadBody();
            		
            		m_bIsQuickThread = true;
            		
            		if (handler != null)
            		{
            			try
            			{
            				//Force to refresh the status
            				handler.sendEmptyMessage(AppConst.GC_EVENT_ID_THREAD_START_CHANGE);
            			}
            			catch (Exception ex)
            			{
            				//Just ignore if any exception occurs
            			}									
            		}            		
            	}
            	catch (Exception ex)
            	{
            		ex.printStackTrace();
            	}
            	
            	try
            	{
        			synchronized(threadWaiter)
        			{            		
        				threadWaiter.notifyAll();
        			}
            	}
            	catch (Exception ex)
            	{
            		ex.printStackTrace();
            	}            	
            }
    	}).start();
	}	

	private String getInZoneList(LatLng pCurTestPoint)
	{
		if (m_Polygon == null)
			return "";
		
		if (m_Polygon.m_arrPolygon == null)
			return "";
		
		StringBuilder mZoneList = new StringBuilder(200);
	
		for (PolygonPaser.Polygon_Data pCurPolygon : m_Polygon.m_arrPolygon)
		{
			if (pCurPolygon.m_PolygonGeoPoints != null)
			{
				if (pCurPolygon.m_PolygonGeoPoints.size() >= 3)
				{
					if (pCurPolygon.IsInsidePolygonZone(pCurTestPoint) == true)
					{
						if (mZoneList.length() > 0)
						{
							mZoneList.append(",");
						}
						
						mZoneList.append(pCurPolygon.m_strPolygonName);
					}
				}
			}
		}
		
		return mZoneList.toString();
	}
	
	public void ShowMyLocationMapMark()
	{
		if (map == null)
			return;
		
    	m_MyLocationGoogle = map.getMyLocation();
    	
    	if (m_MyLocationGoogle != null)
    	{
    		CustomMarkerSnippet pSnippet = new CustomMarkerSnippet();
    		
    		pSnippet.SetMarkerSnippetType(AppConst.MARKER_SNIPPET_TYPE.POSITION_MARKER);
    		pSnippet.m_strMarkerName = "My Location";
    		pSnippet.m_fLatitude = m_MyLocationGoogle.getLatitude();
    		pSnippet.m_fLongitude = m_MyLocationGoogle.getLongitude();
    		pSnippet.m_dDateTime.setTimeInMillis(m_MyLocationGoogle.getTime());
    		pSnippet.m_fAccuracyInMeter = m_MyLocationGoogle.getAccuracy();
    		pSnippet.m_strProvider = m_MyLocationGoogle.getProvider();
    		pSnippet.m_strInZoneList = getInZoneList(pSnippet.getGeoPosition());	
    		
    		if (m_MyLocationGoogle_Marker != null)
    			m_MyLocationGoogle_Marker.remove();    		
    		
    		m_MyLocationGoogle_Marker = AddMarkerToMap(pSnippet.getGeoPosition(), AppConst.USER_DEFINE_MARK_TYPE.GOOGLE_MAP_MY_LOCATION, pSnippet);
    	}
    	else
    	{
    		//Toast.makeText(this, "getMyLocation() Return null vlaue", Toast.LENGTH_SHORT).show();
    	}
	}
	

	
	public void ShowCurrentLocationMark(Location curLoc)
	{
		if (map == null)
			return;
		    	
    	if (curLoc != null)
    	{
    		CustomMarkerSnippet pSnippet = new CustomMarkerSnippet();
    		
    		pSnippet.SetMarkerSnippetType(AppConst.MARKER_SNIPPET_TYPE.POSITION_MARKER);
    		pSnippet.m_strMarkerName = "Location Service";
    		pSnippet.m_fLatitude = curLoc.getLatitude();
    		pSnippet.m_fLongitude = curLoc.getLongitude();
    		pSnippet.m_dDateTime.setTimeInMillis(curLoc.getTime());    		
    		pSnippet.m_fAccuracyInMeter = curLoc.getAccuracy();
    		pSnippet.m_strProvider = curLoc.getProvider();
    		pSnippet.m_strInZoneList = getInZoneList(pSnippet.getGeoPosition());
    		
    		if (m_LocationServices_Marker != null)
    			m_LocationServices_Marker.remove();
    		
    		m_LocationServices_Marker = AddMarkerToMap(pSnippet.getGeoPosition(), AppConst.USER_DEFINE_MARK_TYPE.LOCATION_SERVICE_LOCATION_UPDATE, pSnippet);
    	}
    	else
    	{
    		//Toast.makeText(this, "getMyLocation() Return null vlaue", Toast.LENGTH_SHORT).show();
    	}
	}
	
	public void ShowNMEAMark(Location curLoc)
	{
		if (map == null)
			return;
		    	
    	if (curLoc != null)
    	{
    		CustomMarkerSnippet pSnippet = new CustomMarkerSnippet();
    		
    		pSnippet.SetMarkerSnippetType(AppConst.MARKER_SNIPPET_TYPE.POSITION_MARKER);
    		pSnippet.m_strMarkerName = "NMEA Message";
    		pSnippet.m_fLatitude = curLoc.getLatitude();
    		pSnippet.m_fLongitude = curLoc.getLongitude();
    		pSnippet.m_dDateTime.setTimeInMillis(curLoc.getTime());
    		
    		if (curLoc.hasAccuracy() == true)
    			pSnippet.m_fAccuracyInMeter = curLoc.getAccuracy();
    		else
    			pSnippet.m_fAccuracyInMeter = -1;
    		
    		pSnippet.m_strProvider = curLoc.getProvider();
    		pSnippet.m_strInZoneList = getInZoneList(pSnippet.getGeoPosition());
    		
    		if (m_NMEA_MSG_Position_Marker != null)
    			m_NMEA_MSG_Position_Marker.remove();
    		    		
    		m_NMEA_MSG_Position_Marker = AddMarkerToMap(pSnippet.getGeoPosition(), AppConst.USER_DEFINE_MARK_TYPE.NMEA_MESSAGE_GGA_CONTENT, pSnippet);
    	}
    	else
    	{
    		//Toast.makeText(this, "getMyLocation() Return null vlaue", Toast.LENGTH_SHORT).show();
    	}
	}
	
	public void ShowDGPSCorrectedMark(Location curLoc)
	{
		if (map == null)
			return;
		    	
    	if (curLoc != null)
    	{
    		if (curLoc.getTime() > 0)
    		{
	    		CustomMarkerSnippet pSnippet = new CustomMarkerSnippet();
	    		
	    		pSnippet.SetMarkerSnippetType(AppConst.MARKER_SNIPPET_TYPE.POSITION_MARKER);
	    		pSnippet.m_strMarkerName = "Differential";
	    		pSnippet.m_fLatitude = curLoc.getLatitude();
	    		pSnippet.m_fLongitude = curLoc.getLongitude();
	    		pSnippet.m_dDateTime.setTimeInMillis(curLoc.getTime());
	    		
	    		if (curLoc.hasAccuracy() == true)
	    			pSnippet.m_fAccuracyInMeter = curLoc.getAccuracy();
	    		else
	    			pSnippet.m_fAccuracyInMeter = -1;
	    		
	    		pSnippet.m_strProvider = curLoc.getProvider();
	    		pSnippet.m_strInZoneList = getInZoneList(pSnippet.getGeoPosition());
	    		
	    		if (m_DGPS_CorrectedLocatoin_Marker != null)
	    			m_DGPS_CorrectedLocatoin_Marker.remove();	    		
	    		
	    		m_DGPS_CorrectedLocatoin_Marker = AddMarkerToMap(pSnippet.getGeoPosition(), AppConst.USER_DEFINE_MARK_TYPE.DEFFERENTIAL_GPS_CONTENT, pSnippet);
    		}
    	}
    	else
    	{
    		//Toast.makeText(this, "getMyLocation() Return null vlaue", Toast.LENGTH_SHORT).show();
    	}		
	}
	


	public void reveiverdNema(long timestamp, String nmea)
	{
		nmea = nmea.replaceAll("\r", "").replaceAll("\n", "");
		latestTimeStamp = timestamp;
		long groupedTimeStamp = Group_time.getGroupedTime(timestamp);
		
		if (m_NMEA_FilterArrayList == null)
		{	
			//No filter is applied
			m_NMEA_MSG_Queue.AddNmeaToQueueTail(nmea, groupedTimeStamp);
		}
		else
		{
			for (String strFilterNMEA : m_NMEA_FilterArrayList)
			{
				if (AppConst.wildCardMatch(nmea, strFilterNMEA) == true)
				{
					m_NMEA_MSG_Queue.AddNmeaToQueueTail(nmea, groupedTimeStamp);
					break;
				}
			}
		}
		
		if (gc_bIs_enable_log == true)
		{
			String newString  = String.format(Locale.US, "[%s] %s:%d", AppConst.formatTime_TO_String_InItsTimeZone(Calendar.getInstance()), nmea, groupedTimeStamp);	
			logFile.WriteLogToFile(newString);
		}
		
		if (nmea.startsWith("$GPGGA,") == true)
		{
			if (Process_NMEA_GGA(nmea, timestamp) == true)
			{
				synchronized(this)
				{
					m_bIsNeedToRefresh = true;
				}
				//RefreshAllMarkers();
			}
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		
		if (location != null) {
			m_LocationServices = location;
			if (m_bCentralizePosition) {
				if (previousLoc != null) {
					if (previousLoc.isLongDistance(location, map.getCameraPosition().zoom) )			
						map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), map.getCameraPosition().zoom));
				} else {
					previousLoc = new PreviousCenter(location);
				}
			}
			synchronized(this)
			{
				m_bIsNeedToRefresh = true;
			}		
		//RefreshAllMarkers();
		}					
	}
	
	public boolean Process_NMEA_GGA(String strNMEAMsg, long pnNMEATimeStamp)
	{
                Location mCur = new Location("NMEA");
		
		String[] MsgFields = strNMEAMsg.split(",", 20);
		
		if (MsgFields == null)
			return false;
		
		if (MsgFields.length >= 7)
		{
			//Field Index 6 indicate availability of GPS, "0" = not available, "1" = Available
			if (MsgFields[6].contains("1") == true)
			{			
				//Field Index 2 = Latitude, DDMM.MMMMM
				
				int positionOfDot = MsgFields[2].indexOf(".");
				
				double fDegree = Double.valueOf(MsgFields[2].substring(0, positionOfDot - 2));
				double fMinutes = Double.valueOf(MsgFields[2].substring(positionOfDot - 2));
				
				double fLatitue = fDegree + fMinutes / 60;
				
				//Field Index 3 = direction of latitude, South, is negative
				if (MsgFields[3].contains("S") == true)
				{
					fLatitue = fLatitue * -1;
				}
				
				//Field Index 4 = Long, DDMM.MMMMM
				
				positionOfDot = MsgFields[4].indexOf(".");
				
				fDegree = Double.valueOf(MsgFields[4].substring(0, positionOfDot - 2));
				fMinutes = Double.valueOf(MsgFields[4].substring(positionOfDot - 2));
				
				double fLongitude = fDegree + fMinutes / 60;
				
				//Field Index 5 = direction of Longitude, West, is negative
				if (MsgFields[5].contains("W") == true)
				{
					fLongitude = fLongitude * -1;
				}
				
				mCur.setLatitude(fLatitue);
				mCur.setLongitude(fLongitude);
				
				mCur.setTime(pnNMEATimeStamp);
				
				m_NMEA_MSG_Position = mCur;
				
				m_NMEA_Position_History.PutToTail(mCur);
				
				return true;
			}
		}
	
		return false;
	}
	
	public void ShowScaleRulerOnMap()
	{
		if (map == null)
		{
			return;
		}
		
		//Zoom Level
		int nCurrentZoomLevel = 1;
		
		if (map.getCameraPosition().zoom <= 1)
			nCurrentZoomLevel = 1;
		else if (map.getCameraPosition().zoom > 20)
			nCurrentZoomLevel = 20;
		else
			nCurrentZoomLevel = (int)Math.ceil(map.getCameraPosition().zoom);		
		
		//Calculates X and Y distances in meters.		
		//resultY = deltaLatitude * 40008000 / 360
		
		//We get  deltaLatitude = resultY * 360 / 40008000;
		
		double fSuggestedLength_inMeter = AppConst.GetZoomLevelScale_inMeter(nCurrentZoomLevel);
		
		Point pt_ScreenCenterPoint = map.getProjection().toScreenLocation(map.getCameraPosition().target);
		
		Point pt_OtherPoint_withSuggestedMeterAway = map.getProjection().toScreenLocation(new LatLng(map.getCameraPosition().target.latitude + ( fSuggestedLength_inMeter * 360 / 40008000) ,map.getCameraPosition().target.longitude));

		double LengthInPoint =  Math.sqrt(Math.pow(pt_ScreenCenterPoint.x - pt_OtherPoint_withSuggestedMeterAway.x, 2) + Math.pow(pt_ScreenCenterPoint.y - pt_OtherPoint_withSuggestedMeterAway.y, 2));
		
		TextView txtView = (TextView) findViewById(R.id.scale_bar_text);
		
		
		
		//ImageView iv = (ImageView) findViewById(R.id.scale_bar_imageview);
		
		
		//Log.w("calculated Length", "Zoom = " + String.valueOf(map.getCameraPosition().zoom));
		
		if ((LengthInPoint > 50) && (LengthInPoint <= 800))
		{
			ViewGroup.LayoutParams params = txtView.getLayoutParams();
			
			params.width = (int)LengthInPoint;
			
			txtView.setLayoutParams(params);
			
			
			txtView.setText(AppConst.GetZoomLevelScale_Display(nCurrentZoomLevel));
		}
		else
		{
			ViewGroup.LayoutParams params = txtView.getLayoutParams();
			
			params.width = 50;
			
			txtView.setLayoutParams(params);
			
			txtView.setText("--");
		}
	}
	
	public void RefreshAllMarkers(boolean bRefreshSummary, boolean bRefreshMapView)
	{
		if (bRefreshSummary)
		{
			TextView txtView = (TextView) findViewById(R.id.txt_summary_info);
			
			String strSummary = "";
					
			if (m_currentConnection_isConnected == true)
			{
				strSummary = "DGPS Server Connected (" + ((m_bIsQuickThread)? "Stopped":"Running") + ")";
			}
			else
				strSummary = "Connecting DGPS Server (" + ((m_bIsQuickThread)? "Stopped":"Running") + ") ...";
			
			if (m_MyLocationGoogle != null)
			{
				strSummary += String.format(Locale.US, "\nGoogle Pos : (%s, %s)", AppConst.getGeoPointFormater().format(m_MyLocationGoogle.getLatitude()), AppConst.getGeoPointFormater().format(m_MyLocationGoogle.getLongitude())); 
			}		
			
			if (m_LocationServices != null)
			{
				strSummary += String.format(Locale.US, "\nLocation Svc : (%s, %s)", AppConst.getGeoPointFormater().format(m_LocationServices.getLatitude()), AppConst.getGeoPointFormater().format(m_LocationServices.getLongitude())); 
			}
			 
			if (m_NMEA_MSG_Position != null)
			{
				strSummary += String.format(Locale.US, "\nNMEA Msg : (%s, %s)", AppConst.getGeoPointFormater().format(m_NMEA_MSG_Position.getLatitude()), AppConst.getGeoPointFormater().format(m_NMEA_MSG_Position.getLongitude())); 
			}
			
			if (m_DGPS_CorrectedLocatoin != null)
			{
				if (m_DGPS_CorrectedLocatoin.getTime() > 0)
				{
					strSummary += String.format(Locale.US, "\nD-GPS : (%s, %s)", AppConst.getGeoPointFormater().format(m_DGPS_CorrectedLocatoin.getLatitude()), AppConst.getGeoPointFormater().format(m_DGPS_CorrectedLocatoin.getLongitude()));
				}
			}
			
			txtView.setText(strSummary);
		}

		if (bRefreshMapView)
		{
			//CleanAllMarker();
			
			if (m_bShowLocationSrv_Marker == true)				
				ShowCurrentLocationMark(m_LocationServices);
			
			if (m_bShowGoogleDGPS_Marker == true)
				ShowMyLocationMapMark();
			
			if (m_bShowNMEA_GPS_Marker == true)
				ShowNMEAMark(m_NMEA_MSG_Position);
			
			ShowDGPSCorrectedMark(m_DGPS_CorrectedLocatoin);
			
			ShowFixedAssetOnMap();			
			ShowPolygonOnMap();			
		}
		
		//Debug purpose only
		DrawScreenCenterMarket();
	}

	@Override
	public void onCameraChange(CameraPosition arg0) 
	{
		// TODO Auto-generated method stub

		if ((gc_previous_zoom_level != arg0.zoom) || (gc_previous_zoom_level <= 0))
		{
			gc_previous_zoom_level = arg0.zoom;
			ShowScaleRulerOnMap();				
		}
	}

	public static class Group_time
	{
		static private long mTimeStamp = 0;
		public static long getGroupedTime(long timeStamp) {
			/*
			if (mTimeStamp == 0) {
				mTimeStamp = timeStamp;
				return timeStamp;
			}
			*/
			if ( Math.abs(mTimeStamp - timeStamp) < 750 ) {
				return mTimeStamp;
			} else {
				mTimeStamp = timeStamp;
				return timeStamp;
			}
				
		}
	}	
}
