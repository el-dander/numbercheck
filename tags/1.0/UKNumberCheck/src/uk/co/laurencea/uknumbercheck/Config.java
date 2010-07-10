/*
 * Copyright (C) 2010 Laurence Anderson
 * 
 * This file is part of NumberCheck (http://numbercheck.googlecode.com)
 * 
 * NumberCheck is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package uk.co.laurencea.uknumbercheck;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class Config extends PreferenceActivity {
	
    // Need handler for callbacks to the UI thread
    /*final Handler mHandler = new Handler();

    // Create runnable for posting
    final Runnable mUpdateResults = new Runnable() {
        public void run() {
    		dialog.dismiss();
        }
    };

    // "Working" dialog when processing address book
    ProgressDialog dialog;*/
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        /*findPreference("check_addr_book").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
        	public boolean onPreferenceClick(Preference preference) {        		
        		dialog = ProgressDialog.show(Config.this, "", getString(R.string.working_string), false);
                Thread t = new Thread() {
                    public void run() {
                		try {
                        	Thread.sleep(1000);
                		} catch( InterruptedException e ) {}
                        mHandler.post(mUpdateResults);
                    }
                };
                t.start();
        		return true;
        	}
        });*/
        
        findPreference("about").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				AlertDialog.Builder d = new AlertDialog.Builder(Config.this);
				d.setTitle(R.string.about_title);
				d.setIcon(android.R.drawable.ic_menu_info_details);
				d.setPositiveButton(R.string.ok_string, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
		            	   dialog.cancel();
					}
				});
				View v = LayoutInflater.from(Config.this).inflate(R.layout.about_dialog, null);
				TextView text = (TextView) v.findViewById(R.id.dialogText);
				text.setText(getString(R.string.about_message));
				d.setView(v);

		        d.show();
				return true;
			}
		});
    }    
}