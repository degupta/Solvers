package com.example.exercise101;

import java.util.ArrayList;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class WWFOpenHelper extends SQLiteOpenHelper 
{
    private static final String DATABASE_NAME = "WWFOpenHelper.sqlite";
    private static final int DATABASE_VERSION = 2;

    private String tableName = "";
    private String tableCreate = "";
    private String[][] columnsDef;
    
    public static String CHAT_TABLE_NAME = "CHAT_TABLE";
    public static String[][] CHAT_TABLE_COLUMN_DEF = new String[][]
	{
		{"name", "TEXT"},
		{"message", "BLOB"},
		{"timestamp", "INTEGER"},
		{"byMe", "INTEGER"}
	};
    public static String[] CHAT_TABLE_COLUMNS = new String[] {"message", "byMe"};
    
    public WWFOpenHelper(Context context, String _tableName, String[][] _columnsDef) 
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.tableName = _tableName;
        this.columnsDef = _columnsDef;
        this.createTableCreateString();
    }

    private void createTableCreateString() 
    {
		tableCreate = "CREATE TABLE " + this.tableName + " (";
		for(int i = 0; i < this.columnsDef.length; i++)
		{
			tableCreate += this.columnsDef[i][0] + " " + this.columnsDef[i][1];
			if(i < this.columnsDef.length - 1)
				this.tableCreate += ", ";
		}
		tableCreate += ");";
	}

	public void onCreate(SQLiteDatabase db) 
    {
        db.execSQL(this.tableCreate);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
    {
        // TODO Some logic to i.e. add/remove columns...
    }
    
    public static void insert(Context context, Object[][] keyValuePairs, String _tableName, String[][] _columnsDef)
    {
    	final SQLiteDatabase db = new WWFOpenHelper(context, _tableName, _columnsDef).getWritableDatabase();
    	ContentValues contentValues = new ContentValues();
    	for(int i = 0; i < keyValuePairs.length; i++)
    	{
    		for(int j = 0; j < _columnsDef.length; j++)
    		{
    			if(keyValuePairs[i][0] == _columnsDef[j][0])
    			{
    				putByType(contentValues, (String)keyValuePairs[i][0], keyValuePairs[i][1], _columnsDef[j][1]);
    				break;
    			}
    		}
    	}
    	final long rowID = db.insert(_tableName, null, contentValues);
    	db.close();
    }
    
    private static void putByType(ContentValues contentValues, String key, Object object, String type) 
    {
    	if(type.equals("INTEGER"))
			contentValues.put(key, (Integer)object);
    	else if(type.equals("TEXT") || type.equals("BLOB"))
			contentValues.put(key, (String)object);
    	else if(type.equals("FLOAT"))
			contentValues.put(key, (Float)object);
	}

	public static void query(Context context, String _tableName, String[][] _columnsDef, String[] columnsToQuery, 
    		String selection, String[] selectionArgs, String groupBy, String having, String orderBy, ICallback callback)
    {
    	final SQLiteDatabase db = new WWFOpenHelper(context, _tableName, _columnsDef).getWritableDatabase();
    	final Cursor c = db.query(_tableName, columnsToQuery, selection, selectionArgs, groupBy, having, orderBy);
    	if (c != null) 
    	{
    	    if (!c.moveToFirst())
    	    {
    	    	callback.callback(new ArrayList<Object[]>());
    	    }
    	    else
    	    {
    	    	ArrayList<Object[]> values = new ArrayList<Object[]>();
    	    	int rows = c.getCount();
    	    	int cols = c.getColumnCount();
    	    	for(int i = 0; i < rows; i++)
    	    	{
    	    		Object[] vals = new Object[cols];
    	    		for(int j = 0; j < cols; j++)
    	    		{
    	    			final int definitionColumnIndex = c.getColumnIndex(columnsToQuery[j]);
    	    			vals[j] = getByType(c, definitionColumnIndex);
    	    		}
    	    		values.add(vals);
    	    		c.moveToNext();
    	    	}
    	    	
    	    	callback.callback(values);
    	    }
    	    c.close();
    	}
    	else
    	{
    		callback.callback(null);
	    	return;
    	}
    	db.close();
    }

	private static Object getByType(Cursor c, int definitionColumnIndex) 
	{
		switch(c.getType(definitionColumnIndex))
		{
			case Cursor.FIELD_TYPE_STRING :
				return c.getString(definitionColumnIndex);
			case Cursor.FIELD_TYPE_BLOB :
				return c.getBlob(definitionColumnIndex);
			case Cursor.FIELD_TYPE_INTEGER :
				return (Integer)c.getInt(definitionColumnIndex);
			case Cursor.FIELD_TYPE_FLOAT :
				return (Float)c.getFloat(definitionColumnIndex);
			case Cursor.FIELD_TYPE_NULL :
				return null;
		}
		
		return null;
	}
}

