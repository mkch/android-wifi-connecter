/*
 * Wifi Connecter
 * 
 * Copyright (c) 2011 Kevin Yuan (farproc@gmail.com)
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

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Toast;
import android.widget.TwoLineListItem;
import android.widget.AdapterView.OnItemClickListener;

public class TestWifiScan extends ListActivity {
	
	private WifiManager mWifiManager;
	private List<ScanResult> mScanResults;
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

		setLocationPermission();
		if(permisionLocationOn())
		{
			checkLocationTurnOn();
		}


    	mWifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
    	
    	setListAdapter(mListAdapter);
    	
    	getListView().setOnItemClickListener(mItemOnClick);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		final IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		registerReceiver(mReceiver, filter);
		mWifiManager.startScan();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(mReceiver);
	}
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
				mScanResults = mWifiManager.getScanResults();
				mListAdapter.notifyDataSetChanged();
				
				mWifiManager.startScan();
			}
			
		}
	};
	
	private BaseAdapter mListAdapter = new BaseAdapter() {
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null || !(convertView instanceof TwoLineListItem)) {
				convertView = View.inflate(getApplicationContext(), 
						android.R.layout.simple_list_item_2, null);
			}
			
			final ScanResult result = mScanResults.get(position);
			((TwoLineListItem)convertView).getText1().setText(result.SSID);
			((TwoLineListItem)convertView).getText2().setText(
					String.format("%s  %d", result.BSSID, result.level)
					);
			return convertView;
		}
		
		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public Object getItem(int position) {
			return null;
		}
		
		@Override
		public int getCount() {
			return mScanResults == null ? 0 : mScanResults.size();
		}
	};
	
	private OnItemClickListener mItemOnClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			final ScanResult result = mScanResults.get(position);
			launchWifiConnecter(TestWifiScan.this, result);
		}
	};
	
	/**
	 * Try to launch Wifi Connecter with {@link #hostspot}. Prompt user to download if Wifi Connecter is not installed.
	 * @param activity
	 * @param hotspot
	 */
	private static void launchWifiConnecter(final Activity activity, final ScanResult hotspot) {
		final Intent intent = new Intent("com.farproc.wifi.connecter.action.CONNECT_OR_EDIT");
		intent.putExtra("com.farproc.wifi.connecter.extra.HOTSPOT", hotspot);
		try {
			activity.startActivity(intent);
		} catch(ActivityNotFoundException e) {
			// Wifi Connecter Library is not installed.
			Toast.makeText(activity, "Wifi Connecter is not installed.", Toast.LENGTH_LONG).show();
			downloadWifiConnecter(activity);
		}
	}

	private static void downloadWifiConnecter(final Activity activity) {
		Intent downloadIntent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("market://details?id=com.farproc.wifi.connecter"));
		try {
			activity.startActivity(downloadIntent);
			Toast.makeText(activity, "Please install this app.", Toast.LENGTH_LONG).show();
		} catch (ActivityNotFoundException e) {
			// Market app is not available in this device.
			// Show download page of this project.
			try {
				downloadIntent.setData(Uri.parse("http://code.google.com/p/android-wifi-connecter/downloads/list"));
				activity.startActivity(downloadIntent);
				Toast.makeText(activity, "Please download the apk and install it manully.", Toast.LENGTH_LONG).show();
			} catch  (ActivityNotFoundException e2) {
				// Even the Browser app is not available!!!!!
				// Show a error message!
				Toast.makeText(activity, "Fatel error! No web browser app in your device!!!", Toast.LENGTH_LONG).show();
			}
		}
	}
	public Boolean permisionLocationOn() {
		boolean permissionGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
		if (permissionGranted) {
			return true;
		} else {
			return false;
		}
	}
	public void setLocationPermission() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			//requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
			ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
		}

	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch (requestCode) {
			case 1001: {
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED
						&& (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
						|| ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
					//Start your service here
				}
			}
		}
	}

	public Boolean checkLocationTurnOn(){
		boolean onLocation=true;
		boolean permissionGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissionGranted) {
			LocationManager lm = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
			boolean gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
			if(!gps_enabled){
				onLocation =false;
				AlertDialog.Builder dialog = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.PlatformDialog));
				//android.support.v7.app.AlertDialog.Builder dialog = new android.support.v7.app.AlertDialog.Builder(this);
				dialog.setMessage("Please turn on your location");
				dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface paramDialogInterface, int paramInt) {
						Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						startActivity(myIntent);
					}
				});
				dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface paramDialogInterface, int paramInt) {

					}
				});
				dialog.show();
			}
		}
		return onLocation;
	}
}

