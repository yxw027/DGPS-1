package lscm.dgps.pilotapp.lands;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.os.Environment;

public class GPSRecordLog {

	File file = null;
	FileWriter fw = null;	
	
	private final String BACKUP_SUB_PATH = "/DGNSS/DGPS_NMEA_LOG";

	private boolean bIsSDCard = false;
		
	public GPSRecordLog()
	{
		bIsSDCard = CheckExternalStorageState();
	}
	
	public void CloseLogFile()
	{
		if (fw != null)
		{
			try {
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
			fw = null;
		}
		
		file = null;
	}
	
	public boolean OpenLogFile()
	{
		boolean bIsNewCreateFile = false;
		
		if (bIsSDCard == false)
			return false;
		
		try
		{
			File targetPath = getFilePath();
			file = new File(targetPath, getDefaultFileName());
			
			if (file.exists() == false)
			{
				bIsNewCreateFile = true;
			}
			
			//Create Directories if necessary
			targetPath.mkdirs();

			//Open file with Append Feature
			fw = new FileWriter(file, true);
			
			if (bIsNewCreateFile == true)
			{
				//Write first line Header now
				
				fw.write("NMEA DATA LOG\r\n");				
			}
			
			return true;
		}
		catch (Exception ex)
		{
			file = null;			
			fw  = null;
			return false;
		}
	}
	
	public void WriteFormatLogToFile(String strLogContent)
	{
		WriteLogToFile(String.format(Locale.US, "[%s] %s", AppConst.formatTime_TO_String_InItsTimeZone(Calendar.getInstance()), strLogContent));
	}
		
	public void WriteLogToFile(String strLogContent)
	{
		if ((file == null) || (fw == null))
			return;
		
    	try
    	{
    		String strContent = strLogContent.trim() + "\r\n";
    		
    		if (strContent.length() > 0)
    		{ 
    			synchronized(fw)
    			{
    				fw.write(strContent);
    			}   			
    			//Log.w("Data Log", strContent);
    		}
    	}
    	catch(Exception ex)
    	{
    		//Log.w("Write File Exception", "Storage Media testing");    		
    		
    	}
	}
	
	public void WriteSessionBreak(boolean bIsStartSession)
	{
		String strContent = String.format("============================================================\r\n============       %s    [%s]     ============\r\n============================================================\r\n", (bIsStartSession)? "SESSION START":"SESSION END", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Calendar.getInstance().getTime()));
		
		WriteLogToFile(strContent);
	}
	
	private File getFilePath()
	{
		File Path = new File(Environment.getExternalStorageDirectory().getPath() + BACKUP_SUB_PATH);
		
		return Path;
	}	
		
	@SuppressLint("SimpleDateFormat")
	private String getDefaultFileName()
	{
		Calendar curDate = Calendar.getInstance();		
		
		return String.format("NMEA_%s.LOG",  new SimpleDateFormat("yyyy_MM_dd").format(curDate.getTime()));
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

	  static public boolean isNumeric(String str)
	  {
	    return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
	  }			  
	
	
}
