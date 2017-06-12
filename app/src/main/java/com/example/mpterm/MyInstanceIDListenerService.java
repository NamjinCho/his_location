package com.example.mpterm;

/**
 * Created by hong on 2016-05-20.
 */
import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by saltfactory on 6/8/15.
 */
public class MyInstanceIDListenerService extends InstanceIDListenerService {

	private static final String TAG = "MyInstanceIDLS";

	@Override
	public void onTokenRefresh() {
		Intent intent = new Intent(this, RegistrationIntentService.class);
		startService(intent);
	}
}