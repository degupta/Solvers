package com.example.exercise101;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class FileHelper 
{
	public static final String XPROMO_IMAGE_FILE_PREFIX = "xpromoImage";
	public static final String CACHE = "cache";
	public static final String GAMES = "games";
	
	private static boolean mExternalStorageAvailable = false;
	private static boolean mExternalStorageWriteable = false;
	private static boolean initialized = false;
	
	private static void init()
	{
		if(initialized)
			return;
		
		final String state = Environment.getExternalStorageState();
		
		if (Environment.MEDIA_MOUNTED.equals(state)) 
		{
		    mExternalStorageAvailable = true;
		    mExternalStorageWriteable = true;
		} 
		else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) 
		{
		    mExternalStorageAvailable = true;
		    mExternalStorageWriteable = false;
		}
		
		initialized = true;
	}

	
	public static String getAbsolutePath(String directoryName, String fileName, Context context)
	{
		String absPath = "";
		
		if(android.os.Build.VERSION.SDK_INT >= 8)
		{
			if(directoryName.equals(CACHE))
				absPath = context.getExternalCacheDir().getAbsolutePath();
			else
				absPath = context.getExternalFilesDir(directoryName).getAbsolutePath();
		}
		else
			absPath = Environment.getExternalStorageDirectory() + context.getApplicationInfo().packageName + "/" + directoryName;
		
		return absPath + "/" + fileName;
	}
	
	public static boolean fileExistsInCache(String fileName, Context context)
	{
		return fileExists(CACHE, fileName, context);
	}
	
	public static boolean writeToCache(String fileName, byte[] data, Context context)
	{
		return write(CACHE, fileName, data, context, false);
	}
	
	public static byte[] readFromCache(String fileName, Context context)
	{
		return read(CACHE, fileName, context);
	}
	
	public static boolean fileExists(String directoryName, String fileName, Context context)
	{
		init();
		if(!mExternalStorageAvailable)
			return false;
		return new File(getAbsolutePath(directoryName, fileName, context)).exists();
	}
	
	public static boolean write(String directoryName, String fileName, byte[] data, Context context, boolean append)
	{
		init();
		
		if(!mExternalStorageAvailable || !mExternalStorageWriteable)
			return false;
		
		boolean success = true;
		FileOutputStream out = null;
		try 
		{
		    out = new FileOutputStream(new File(getAbsolutePath(directoryName, fileName, context)), append);
		    out.write(data);
		}  
		catch (final Exception e) 
		{
		    Log.e("FILE_ERROR", "FILE_ERROR", e);
		    success = false;
		} 
		finally 
		{
		    if (out != null) 
		    {
		        try 
		        {
		            out.close();
		        } 
		        catch (Exception e) 
		        {
		            Log.e("FILE_ERROR", "FILE_ERROR", e);
		            success = false;
		        }
		    }
		}
		
		return success;
	}
	
	public static byte[] read(String directoryName, String fileName, Context context)
	{
		init();
		
		if(!mExternalStorageAvailable)
			return null;
		
		ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
		boolean success = true;
		FileInputStream in = null;
		try 
		{
			in = new FileInputStream(new File(getAbsolutePath(directoryName, fileName, context)));
		    int read = -1;
		    while ((read = in.read()) != -1) 
		    	byteArrayOut.write(read);
		} 
		catch (Exception e) 
		{
		    Log.e("FILE_ERROR", "FILE_ERROR", e);
		    success = false;
		} 
		finally 
		{
		    if (in != null) 
		    {
		        try 
		        {
		            in.close();
		        } 
		        catch (Exception e) 
		        {
		            Log.e("FILE_ERROR", "FILE_ERROR", e);
		            success = false;
		        }
		    }
		}
		
		return success ? byteArrayOut.toByteArray() : null;
	}
}
