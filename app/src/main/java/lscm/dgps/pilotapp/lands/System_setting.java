package lscm.dgps.pilotapp.lands;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class System_setting extends Activity
{
	ToggleButton m_LogStatus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.system_setting);
		
    	findViewById(R.id.btn_apply_setting)
	    .setOnClickListener(new View.OnClickListener() {
	          @Override
	          public void onClick(View view) {
	        	  btn_ApplySettingNow();
	          }
	     });    	
    	
    	findViewById(R.id.btn_factory_restore)
	    .setOnClickListener(new View.OnClickListener() {
	          @Override
	          public void onClick(View view) {
	        	  btn_FactorRetore();
	          }
	     });
    	
    	m_LogStatus = (ToggleButton)findViewById(R.id.btn_enable_log);
    	
//    	m_LogStatus.setOnClickListener(new View.OnClickListener() {
//	          @Override
//	          public void onClick(View view) {
//	        	  btn_LogStatusClick();
//	          }
//	     });
		
    	;
    	
    	Spinner spinner_fixedasset = (Spinner) findViewById(R.id.spinner_fixed_asset);            	
    	ArrayAdapter<String> dataAdapter_fixedasset = new ArrayAdapter<String>
        	(this, android.R.layout.simple_spinner_item, AppConst.LoadDirectoryFiles(AppConst.GC_DEFAULT_DIRECTORY, AppConst.GC_FIXED_ASSET_FILE_NAME_PATTERN));
         
    	dataAdapter_fixedasset.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);       
    	spinner_fixedasset.setAdapter(dataAdapter_fixedasset);
    	
    	
    	Spinner spinner_polygon_zone = (Spinner) findViewById(R.id.spinner_polygon_zone);            	
    	ArrayAdapter<String> dataAdapter_polygon = new ArrayAdapter<String>
        	(this, android.R.layout.simple_spinner_item, AppConst.LoadDirectoryFiles(AppConst.GC_DEFAULT_DIRECTORY, AppConst.GC_POLYGON_FILE_NAME_PATTERN));
         
    	dataAdapter_polygon.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);       
    	spinner_polygon_zone.setAdapter(dataAdapter_polygon);    	
    		
    	Refresh_from_preference();
	}
	

	private void btn_ApplySettingNow()
	{
		try
		{
			if (m_LogStatus.isChecked() == true)
				LocalReferenceInfo.updateLocalReference(AppConst.SHARE_PREFERENCE_KEY.DGNSS_SERVER_LOG_MODE, "True", getApplicationContext());
			else
				LocalReferenceInfo.updateLocalReference(AppConst.SHARE_PREFERENCE_KEY.DGNSS_SERVER_LOG_MODE, "False", getApplicationContext());
			
			TextView v = (TextView)findViewById(R.id.txt_svr_ip);
			LocalReferenceInfo.updateLocalReference(AppConst.SHARE_PREFERENCE_KEY.DGNSS_SERVER_IP, v.getText().toString().trim().replaceAll(" ", ""), getApplicationContext());
			
			v = (TextView)findViewById(R.id.txt_svr_port);
			LocalReferenceInfo.updateLocalReference(AppConst.SHARE_PREFERENCE_KEY.DGNSS_SERVER_PORT, Integer.valueOf(v.getText().toString()), getApplicationContext());
			
			v = (TextView)findViewById(R.id.txt_filter_messages);
			LocalReferenceInfo.updateLocalReference(AppConst.SHARE_PREFERENCE_KEY.DGNSS_NMEA_FILTER, v.getText().toString().trim().replaceAll(" ", "").replaceAll("\r", "").replaceAll("\n", ""), getApplicationContext());

			Spinner curSpinner = (Spinner) findViewById(R.id.spinner_fixed_asset);			
			LocalReferenceInfo.updateLocalReference(AppConst.SHARE_PREFERENCE_KEY.FIXED_ASSET_FILE, curSpinner.getItemAtPosition(curSpinner.getSelectedItemPosition()).toString(), getApplicationContext());
			//Log.w("Curr spinner - asset", curSpinner.getItemAtPosition(curSpinner.getSelectedItemPosition()).toString());
			
			curSpinner = (Spinner) findViewById(R.id.spinner_polygon_zone);			
			LocalReferenceInfo.updateLocalReference(AppConst.SHARE_PREFERENCE_KEY.POLYGON_ZONE_FILE, curSpinner.getItemAtPosition(curSpinner.getSelectedItemPosition()).toString(), getApplicationContext());
			//Log.w("Curr spinner - polygon", curSpinner.getItemAtPosition(curSpinner.getSelectedItemPosition()).toString());
					
			
			v = (TextView)findViewById(R.id.txt_show_item_count);
			
			int m_nTempValue = Integer.valueOf(v.getText().toString());
			
			if (m_nTempValue <= 10)
				m_nTempValue = 10;
			else if (m_nTempValue > 1000)
				m_nTempValue = 1000;			
			
			LocalReferenceInfo.updateLocalReference(AppConst.SHARE_PREFERENCE_KEY.MAX_SHOW_ITEM_COUNT, m_nTempValue, getApplicationContext());			
			
			CheckBox cb;
			cb = (CheckBox)findViewById(R.id.checkBox_show_google_corrected_position);
			
			if (cb.isChecked() == true)
			{
				LocalReferenceInfo.updateLocalReference(AppConst.SHARE_PREFERENCE_KEY.SHOW_POSITION_POINTER_GOOGLE_DGPS, AppConst.GC_SHOW_FLAG_TRUE, getApplicationContext());
			}
			else
			{
				LocalReferenceInfo.updateLocalReference(AppConst.SHARE_PREFERENCE_KEY.SHOW_POSITION_POINTER_GOOGLE_DGPS, AppConst.GC_SHOW_FLAG_FALSE, getApplicationContext());
			}
			
			cb = (CheckBox)findViewById(R.id.checkBox_show_google_location_position);
			
			if (cb.isChecked() == true)
			{
				LocalReferenceInfo.updateLocalReference(AppConst.SHARE_PREFERENCE_KEY.SHOW_POSITION_POINTER_LOCATION_SERVICE, AppConst.GC_SHOW_FLAG_TRUE, getApplicationContext());
			}
			else
			{
				LocalReferenceInfo.updateLocalReference(AppConst.SHARE_PREFERENCE_KEY.SHOW_POSITION_POINTER_LOCATION_SERVICE, AppConst.GC_SHOW_FLAG_FALSE, getApplicationContext());
			}
			
			cb = (CheckBox)findViewById(R.id.checkBox_show_nmea_message_position);
			
			if (cb.isChecked() == true)
			{
				LocalReferenceInfo.updateLocalReference(AppConst.SHARE_PREFERENCE_KEY.SHOW_POSITION_POINTER_NMEA_MSG_GPS, AppConst.GC_SHOW_FLAG_TRUE, getApplicationContext());
			}
			else
			{
				LocalReferenceInfo.updateLocalReference(AppConst.SHARE_PREFERENCE_KEY.SHOW_POSITION_POINTER_NMEA_MSG_GPS, AppConst.GC_SHOW_FLAG_FALSE, getApplicationContext());
			}				

			v = (TextView)findViewById(R.id.txt_dgps_login);
			LocalReferenceInfo.updateLocalReference(AppConst.SHARE_PREFERENCE_KEY.DGPS_LOGIN_NAME, v.getText().toString(), getApplicationContext());
			
			v = (TextView)findViewById(R.id.txt_dgps_password);
			String pwd = v.getText().toString();
			LocalReferenceInfo.updateLocalReference(AppConst.SHARE_PREFERENCE_KEY.DGPS_LOGIN_PASSWORD, pwd, getApplicationContext());			
			
			LocalReferenceInfo.updateLocalReference(AppConst.SHARE_PREFERENCE_KEY.DGPS_LOGIN_PASSWORD_MD5,getMD5EncryptedString(pwd), getApplicationContext());
			
			finish();
		}
		catch (Exception ex)
		{
			Toast.makeText(this, "Invalid Data Input", Toast.LENGTH_LONG).show();
		}
		
		Refresh_from_preference();
	}
	
	private void btn_FactorRetore()
	{
		LocalReferenceInfo.updateLocalReference(AppConst.SHARE_PREFERENCE_KEY.DGNSS_SERVER_LOG_MODE, AppConst.GC_DEFAULT_LOG_ENABLE, getApplicationContext());
		LocalReferenceInfo.updateLocalReference(AppConst.SHARE_PREFERENCE_KEY.DGNSS_SERVER_IP, AppConst.GC_DEFAULT_DGPS_SERVER_IP, getApplicationContext());
		LocalReferenceInfo.updateLocalReference(AppConst.SHARE_PREFERENCE_KEY.DGNSS_SERVER_PORT, AppConst.GC_DEFAULT_DGPS_SERVER_PORT, getApplicationContext());
		LocalReferenceInfo.updateLocalReference(AppConst.SHARE_PREFERENCE_KEY.DGNSS_NMEA_FILTER, AppConst.GC_DEFAULT_DGPS_NMEA_FILTER, getApplicationContext());
		
		LocalReferenceInfo.updateLocalReference(AppConst.SHARE_PREFERENCE_KEY.FIXED_ASSET_FILE, AppConst.GC_DEFAULT_FIXED_ASSET_FILE, getApplicationContext());
		LocalReferenceInfo.updateLocalReference(AppConst.SHARE_PREFERENCE_KEY.POLYGON_ZONE_FILE, AppConst.GC_DEFAULT_POLYGON_FILE, getApplicationContext());		
		LocalReferenceInfo.updateLocalReference(AppConst.SHARE_PREFERENCE_KEY.MAX_SHOW_ITEM_COUNT, AppConst.GC_DEFAULT_MAX_SHOW_ITEM_COUNT, getApplicationContext());
		
		
		LocalReferenceInfo.updateLocalReference(AppConst.SHARE_PREFERENCE_KEY.SHOW_POSITION_POINTER_GOOGLE_DGPS, AppConst.GC_SHOW_FLAG_DEFAULT, getApplicationContext());
		LocalReferenceInfo.updateLocalReference(AppConst.SHARE_PREFERENCE_KEY.SHOW_POSITION_POINTER_LOCATION_SERVICE, AppConst.GC_SHOW_FLAG_DEFAULT, getApplicationContext());
		LocalReferenceInfo.updateLocalReference(AppConst.SHARE_PREFERENCE_KEY.SHOW_POSITION_POINTER_NMEA_MSG_GPS, AppConst.GC_SHOW_FLAG_DEFAULT, getApplicationContext());
		
		finish();
	}
	
	@SuppressWarnings("unchecked")
	private void Refresh_from_preference()
	{	
		if (LocalReferenceInfo.GetLocalReference(AppConst.SHARE_PREFERENCE_KEY.DGNSS_SERVER_LOG_MODE , getApplicationContext(), AppConst.GC_DEFAULT_LOG_ENABLE).compareToIgnoreCase("True") == 0)
			m_LogStatus.setChecked(true);
		else
			m_LogStatus.setChecked(false);
		
		TextView v = (TextView)findViewById(R.id.txt_svr_ip);
		
		v.setText(LocalReferenceInfo.GetLocalReference(AppConst.SHARE_PREFERENCE_KEY.DGNSS_SERVER_IP , getApplicationContext(), AppConst.GC_DEFAULT_DGPS_SERVER_IP));
		
		v = (TextView)findViewById(R.id.txt_svr_port);
		
		v.setText(String.valueOf(LocalReferenceInfo.GetLocalReference(AppConst.SHARE_PREFERENCE_KEY.DGNSS_SERVER_PORT, getApplicationContext(), AppConst.GC_DEFAULT_DGPS_SERVER_PORT)));

		v = (TextView)findViewById(R.id.txt_lands_map_ip);
		
		v.setText(LocalReferenceInfo.GetLocalReference(AppConst.SHARE_PREFERENCE_KEY.LANDS_MAP_SERVER_IP , getApplicationContext(), AppConst.GC_DEFAULT_LANDS_MAP_SERVER_IP));
		
		v = (TextView)findViewById(R.id.txt_lands_map_port);
		
		v.setText(String.valueOf(LocalReferenceInfo.GetLocalReference(AppConst.SHARE_PREFERENCE_KEY.LANDS_MAP_SERVER_PORT, getApplicationContext(), AppConst.GC_DEFAULT_LANDS_MAP_SERVER_PORT)));		
		
		
		v = (TextView)findViewById(R.id.txt_filter_messages);
		
		v.setText(String.valueOf(LocalReferenceInfo.GetLocalReference(AppConst.SHARE_PREFERENCE_KEY.DGNSS_NMEA_FILTER, getApplicationContext(), AppConst.GC_DEFAULT_DGPS_NMEA_FILTER)));
		
		//================================================================
		String strFileName = LocalReferenceInfo.GetLocalReference(AppConst.SHARE_PREFERENCE_KEY.FIXED_ASSET_FILE, getApplicationContext(), AppConst.GC_DEFAULT_FIXED_ASSET_FILE);		
		Spinner curSpinner = (Spinner) findViewById(R.id.spinner_fixed_asset);
		

		ArrayAdapter<String> myAdap = (ArrayAdapter<String>)curSpinner.getAdapter();
		int spinnerPosition = myAdap.getPosition(strFileName);
		
		if (spinnerPosition < 0)
			curSpinner.setSelection(0);
		else
			curSpinner.setSelection(spinnerPosition);

		//================================================================
		strFileName = LocalReferenceInfo.GetLocalReference(AppConst.SHARE_PREFERENCE_KEY.POLYGON_ZONE_FILE, getApplicationContext(), AppConst.GC_DEFAULT_POLYGON_FILE);		
		curSpinner = (Spinner) findViewById(R.id.spinner_polygon_zone);
		
		myAdap = (ArrayAdapter<String>)curSpinner.getAdapter();
		spinnerPosition = myAdap.getPosition(strFileName);
		
		if (spinnerPosition < 0)
			curSpinner.setSelection(0);
		else
			curSpinner.setSelection(spinnerPosition);		
		//================================================================
		
		v = (TextView)findViewById(R.id.txt_show_item_count);
		
		v.setText(String.valueOf(LocalReferenceInfo.GetLocalReference(AppConst.SHARE_PREFERENCE_KEY.MAX_SHOW_ITEM_COUNT, getApplicationContext(), AppConst.GC_DEFAULT_MAX_SHOW_ITEM_COUNT)));
		
		
		
		
		CheckBox cb;
		cb = (CheckBox)findViewById(R.id.checkBox_show_google_corrected_position);
		
		if (LocalReferenceInfo.GetLocalReference(AppConst.SHARE_PREFERENCE_KEY.SHOW_POSITION_POINTER_GOOGLE_DGPS, getApplicationContext(), AppConst.GC_SHOW_FLAG_DEFAULT).compareToIgnoreCase(AppConst.GC_SHOW_FLAG_TRUE) == 0)
		{
			cb.setChecked(true);
		}
		else
		{
			cb.setChecked(false);
		}
		
		cb = (CheckBox)findViewById(R.id.checkBox_show_google_location_position);
		
		if (LocalReferenceInfo.GetLocalReference(AppConst.SHARE_PREFERENCE_KEY.SHOW_POSITION_POINTER_LOCATION_SERVICE, getApplicationContext(), AppConst.GC_SHOW_FLAG_DEFAULT).compareToIgnoreCase(AppConst.GC_SHOW_FLAG_TRUE) == 0)
		{
			cb.setChecked(true);
		}
		else
		{
			cb.setChecked(false);
		}		

		
		cb = (CheckBox)findViewById(R.id.checkBox_show_nmea_message_position);
		
		if (LocalReferenceInfo.GetLocalReference(AppConst.SHARE_PREFERENCE_KEY.SHOW_POSITION_POINTER_NMEA_MSG_GPS, getApplicationContext(), AppConst.GC_SHOW_FLAG_DEFAULT).compareToIgnoreCase(AppConst.GC_SHOW_FLAG_TRUE) == 0)
		{
			cb.setChecked(true);
		}
		else
		{
			cb.setChecked(false);
		}		
		
		v = (TextView)findViewById(R.id.txt_dgps_login);
		v.setText(LocalReferenceInfo.GetLocalReference(AppConst.SHARE_PREFERENCE_KEY.DGPS_LOGIN_NAME, getApplicationContext(), ""));
		
		v = (TextView)findViewById(R.id.txt_dgps_password);
		v.setText(LocalReferenceInfo.GetLocalReference(AppConst.SHARE_PREFERENCE_KEY.DGPS_LOGIN_PASSWORD, getApplicationContext(), ""));		
		
	}

	public static String getMD5EncryptedString(String encTarget){
        MessageDigest mdEnc = null;
        try {
            mdEnc = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Exception while encrypting to md5");
            e.printStackTrace();
        } // Encryption algorithm
        mdEnc.update(encTarget.getBytes(), 0, encTarget.length());
        String md5 = new BigInteger(1, mdEnc.digest()).toString(16);
        while ( md5.length() < 32 ) {
            md5 = "0"+md5;
        }
        return md5;
    }	
	
}
