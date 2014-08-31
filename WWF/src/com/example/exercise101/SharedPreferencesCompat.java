package com.example.exercise101;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
public class SharedPreferencesCompat 
{
	public static final String APP_PREFIX = "com.example.DEVANSH_WWF"; 
	public static final String USER_ID_PREFERENCE = "USER_ID";
	
    private static final Method EDITOR_APPLY_METHOD = SharedPreferencesCompat.findApplyMethod();

    private static Method findApplyMethod() 
    {
        try 
        {
            final Class<SharedPreferences.Editor> cls = SharedPreferences.Editor.class;
            return cls.getMethod("apply");
        } 
        catch (final NoSuchMethodException e) 
        {
            // fall through
        }
        return null;
    }

     public static void apply(final SharedPreferences.Editor pEditor) 
     {
    	 if (EDITOR_APPLY_METHOD != null) 
    	 {
             try 
             {
                 EDITOR_APPLY_METHOD.invoke(pEditor);
                 return;
             } 
             catch (final InvocationTargetException e) 
             {
                 // fall through
             } catch (final IllegalAccessException e) 
             {
                 // fall through
             }
         }
         
    	 pEditor.commit();

	 }
     
     public static String getPreferenceString(String key, Context pContext)
     {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(pContext);
		return preferences.getString(APP_PREFIX + "." + key, "");
     }
     
     public static void putString(String key, String value, Context pContext)
     {
    	 final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(pContext);
    	 final Editor editor = preferences.edit();
         editor.putString(APP_PREFIX + "." + key, value);
         apply(editor);
     }
}

