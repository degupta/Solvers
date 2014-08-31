package com.example.exercise101;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
public class YesNoDialog 
{
	public ICallback callback;
	public int actionID;
	
	public YesNoDialog(Activity activity, int _actionID, ICallback _callback)
	{
		this.callback = _callback;
		this.actionID = _actionID;
		
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() 
		{
		    @Override
		    public void onClick(DialogInterface dialog, int which) 
		    {
		        switch (which)
		        {
		        	case DialogInterface.BUTTON_POSITIVE:
		        		dialog.dismiss();
		        		callback.callback(actionID);
		        		break;

			        case DialogInterface.BUTTON_NEGATIVE:
			            dialog.dismiss();
			            break;
		        }
		    }
		};
		
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
		    .setNegativeButton("No", dialogClickListener).show();
	}
}
