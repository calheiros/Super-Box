package com.jefferson.application.br;
import android.content.*;
import android.widget.*;

public class ReceiverPasswordChanged extends BroadcastReceiver
{

	@Override
	public void onReceive(Context context, Intent intent)
	{
		String newPass = (String) intent.getExtras().get("pass");
	}
}
