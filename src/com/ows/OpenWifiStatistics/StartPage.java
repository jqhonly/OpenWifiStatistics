package com.ows.OpenWifiStatistics;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import com.ows.OpenWifiStatistics.R;
import com.ows.OpenWifiStatistics.Services.EScanResult;
import com.ows.OpenWifiStatistics.Services.MonitoringService;
import com.ows.OpenWifiStatistics.Services.ResultUploader;

import Utils.StorageUtils;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class StartPage extends Activity {
	
	private StorageUtils storage;
	private String prefName = "servicestarted";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startpage);
        //this.monitoringStarted = false;
        storage = new StorageUtils(getApplicationContext());
        String pref = storage.getPreference(prefName, prefName);
        if(pref.equalsIgnoreCase("")) 
        	storage.savePreference(prefName, prefName, "false");
        else if(pref.equalsIgnoreCase("true") ){
        	Button button = (Button) findViewById(R.id.toggleMonitoring);
        	button.setText(R.string.stop_monitoring);
        }
    }
    
    @Override
	public void onDestroy() {
    	super.onDestroy();
		this.finish();
	}
    
    public void goToMeasureConnection(View v) {
        startActivity(new Intent(this, MeasureConnectionPage.class));
    }
    
    public void uploadResults(View v) {
        if(Globals.service!=null) {
        	Globals.service.uploadResults();
        } else {
        	Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show();
        	(new Timer()).schedule(new TimerTask() {
        		@SuppressWarnings("unchecked")
				@Override public void run() {
        			if(!MonitoringService.uploading) {
        				MonitoringService.uploading = true;
        				StorageUtils storageUtils = new StorageUtils(getApplicationContext());
        				ConcurrentHashMap<String, EScanResult> scanResults = 
        						(ConcurrentHashMap<String, EScanResult>) storageUtils.loadObjectFromInnerStorage("scanresults");
        				ResultUploader formUploader = new ResultUploader("http://uberspot.ath.cx/wifistats.php");
        				formUploader.sendAll(scanResults);
        				storageUtils.saveObjectToInnerStorage(scanResults, "scanresults");
        				MonitoringService.uploading = false;
        			}
        		}
        	}, 500);
        }
    }
    
    public void goToSettings(View v) {
        startActivity(new Intent(this, SettingsPage.class)); 
    }
    
    public void viewResults(View v) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://uberspot.ath.cx/wifi/results.php")));
    }
    
    public void toggleMonitoring(View v) {
    	Button button = (Button) findViewById(R.id.toggleMonitoring);
    	if(storage.getPreference(prefName, prefName).equalsIgnoreCase("true") ){
    		stopService(new Intent(StartPage.this, MonitoringService.class));
    		button.setText(R.string.start_monitoring);
    		storage.savePreference(prefName, prefName, "false");
    	}else {
            startService(new Intent(StartPage.this, MonitoringService.class));
            button.setText(R.string.stop_monitoring);
            storage.savePreference(prefName, prefName, "true");
            startActivity(new Intent(this, ScanResultsPage.class));
    	}
        
    }

}