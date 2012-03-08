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

import com.farproc.wifi.connecter.R;

import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class CurrentNetworkContent extends BaseContent {

	public CurrentNetworkContent(Floating floating, WifiManager wifiManager,
			ScanResult scanResult) {
		super(floating, wifiManager, scanResult);
		
		mView.findViewById(R.id.Status).setVisibility(View.GONE);
		mView.findViewById(R.id.Speed).setVisibility(View.GONE);
		mView.findViewById(R.id.IPAddress).setVisibility(View.GONE);
		mView.findViewById(R.id.Password).setVisibility(View.GONE);
		
		final WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
		if(wifiInfo == null) {
			Toast.makeText(mFloating, R.string.toastFailed, Toast.LENGTH_LONG).show();
		} else {
			final SupplicantState state = wifiInfo.getSupplicantState();
			final NetworkInfo.DetailedState detailedState = WifiInfo.getDetailedStateOf(state);
			if(detailedState == NetworkInfo.DetailedState.CONNECTED
					|| (detailedState == NetworkInfo.DetailedState.OBTAINING_IPADDR && wifiInfo.getIpAddress() != 0)) {
				mView.findViewById(R.id.Status).setVisibility(View.VISIBLE);
				mView.findViewById(R.id.Speed).setVisibility(View.VISIBLE);
				mView.findViewById(R.id.IPAddress).setVisibility(View.VISIBLE);
				
				((TextView)mView.findViewById(R.id.Status_TextView)).setText(R.string.status_connected);
				((TextView)mView.findViewById(R.id.LinkSpeed_TextView)).setText(wifiInfo.getLinkSpeed() + " " + WifiInfo.LINK_SPEED_UNITS);
				((TextView)mView.findViewById(R.id.IPAddress_TextView)).setText(getIPAddress(wifiInfo.getIpAddress()));
			} else if(detailedState == NetworkInfo.DetailedState.AUTHENTICATING
				|| detailedState == NetworkInfo.DetailedState.CONNECTING
				|| detailedState == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
			mView.findViewById(R.id.Status).setVisibility(View.VISIBLE);
			((TextView)mView.findViewById(R.id.Status_TextView)).setText(R.string.status_connecting);
			}
		}
	}

	@Override
	public int getButtonCount() {
		// No Modify button for open network.
		return mIsOpenNetwork ? 2 : 3;
	}

	@Override
	public OnClickListener getButtonOnClickListener(int index) {
		if(mIsOpenNetwork && index == 1) {
			// No Modify button for open network.
			// index 1 is Cancel(index 2).
			return mOnClickListeners[2];
		}
		return mOnClickListeners[index];
	}

	@Override
	public CharSequence getButtonText(int index) {
		switch(index) {
		case 0:
			return mFloating.getString(R.string.forget_network);
		case 1:
			if(mIsOpenNetwork) {
				// No Modify button for open network.
				// index 1 is Cancel.
				return getCancelString();
			}
			return mFloating.getString(R.string.button_change_password);
		case 2:
			return getCancelString();
		default:
			return null;
		}
	}

	@Override
	public CharSequence getTitle() {
		return mScanResult.SSID;
	}
	
	private OnClickListener mForgetOnClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			final WifiConfiguration config = Wifi.getWifiConfiguration(mWifiManager, mScanResult, mScanResultSecurity);
			boolean result = false;
			if(config != null) {
				result = mWifiManager.removeNetwork(config.networkId)
					&& mWifiManager.saveConfiguration();
			}
			if(!result) {
				Toast.makeText(mFloating, R.string.toastFailed, Toast.LENGTH_LONG).show();
			}
			
			mFloating.finish();
		}
	};
	
	
	private OnClickListener mOnClickListeners[] = {mForgetOnClick, mChangePasswordOnClick, mCancelOnClick};
	
	private String getIPAddress(int address) {
		StringBuilder sb = new StringBuilder();
		sb.append(address & 0x000000FF).append(".")
		.append((address & 0x0000FF00) >> 8).append(".")
		.append((address & 0x00FF0000) >> 16).append(".")
		.append((address & 0xFF000000L) >> 24);
		return sb.toString();
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		return false;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
	}

}
