/*
 * Wifi Connecter
 * 
 * Copyright (c) 20101 Kevin Yuan (farproc@gmail.com)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 **/ 

package com.farproc.wifi.connecter;

import java.util.List;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.IBinder;

public class ReenableAllApsWhenNetworkStateChanged {
	public static void schedule(final Context ctx) {
		ctx.startService(new Intent(ctx, BackgroundService.class));
	}
	
	private static void reenableAllAps(final Context ctx) {
		final WifiManager wifiMgr = (WifiManager)ctx.getSystemService(Context.WIFI_SERVICE);
		final List<WifiConfiguration> configurations = wifiMgr.getConfiguredNetworks();
		for(final WifiConfiguration config:configurations) {
			wifiMgr.enableNetwork(config.networkId, false);
		}
	}
	
	public static class BackgroundService extends Service {

		private boolean mReenabled;
		
		private BroadcastReceiver mReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();
				if(WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
					final NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
					final NetworkInfo.DetailedState detailed = networkInfo.getDetailedState();
					switch(detailed) {
					case DISCONNECTED:
					case DISCONNECTING:
					case SCANNING:
						return;
					default:
						if(!mReenabled) {
							mReenabled = true;
							reenableAllAps(context);
							stopSelf();
						}
					}
				}
			}
		};
		
		private IntentFilter mIntentFilter;
		
		@Override
		public IBinder onBind(Intent intent) {
			return null; // We need not bind to it at all.
		}
		
		@Override
		public void onCreate() {
			super.onCreate();
			mReenabled = false;
			mIntentFilter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
			registerReceiver(mReceiver, mIntentFilter);
		}
		
		@Override
		public void onDestroy() {
			super.onDestroy();
			unregisterReceiver(mReceiver);
		}

	}
}
