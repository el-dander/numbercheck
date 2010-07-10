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

// For next version:
// Add shortcodes
// Decode international numbers
// Own whitelist (can be populated on prompt - don't ask again etc)
// Give suggestion (eg. 0800 - use 0800buster etc)

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.widget.Toast;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

// Sources of information:
// http://www.ofcom.org.uk/telecoms/ioi/numbers/numbers_administered/download
// http://www.three.co.uk/_standalone/Link_Document?content_aid=1214305748126
// http://www.tesco.com/mobilenetwork/content.aspx?page=36

public class NumberReceiver extends BroadcastReceiver {
	private static final String[] SAFE_MOBILE_OPERATORS = { "Hutchison 3G UK Ltd", "O2 (UK) Limited", "T-Mobile (UK) Limited", "Vodafone Ltd", "Orange", "BT" };
	private static final String MOBILE_SERVICE = "Mobile services";
	
	class NumberDetails {
		public NumberDetails(String description, boolean safe) {
			this.description = description;
			this.safe = safe;
			this.unknown = false;
		}
		public NumberDetails() {
			this.safe = false;
			this.unknown = true;
		}
		public String description;
		public boolean safe;
		public boolean unknown;
	}
	
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
			String number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
			
			// Pass through numbers the user has agreed to
			if( number.startsWith("***") ) {
				setResultData( number.substring(3) );
				return;
			}

			// Ignore USSD
			if (number.length() > 1 && (number.startsWith("#") || number.startsWith("*")))
				return;

			// Ignore "operator services" (3 & 4 digit numbers)
			if (number.length() == 3 || number.length() == 4)
				return;

			NumberDetails result = lookupNumber(context, number);
								
			if( result.safe ) {
				if( pref.getBoolean("every_number", false) )
				{
					Toast t = Toast.makeText(context, result.description, Toast.LENGTH_LONG);
					t.setGravity(Gravity.TOP, 0, 0);
					t.show();
				}
			} else {
				if( !(result.unknown && pref.getBoolean("ignore_unknown", false)) )
				{
					setResultData(null);
					Intent i = new Intent();
					i.setClass(context, Prompt.class);
					i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					i.putExtra("uk.co.laurencea.UKNumberCheck.Description", result.description);
					i.putExtra("uk.co.laurencea.UKNumberCheck.Number", number);
					context.startActivity(i);
				}
			}
		}
	}
	
	private NumberDetails lookupNumber(Context context, String number)
	{		
		if (number.length() > 4 && number.startsWith("0044"))
			return lookupUKNumber(context, number.substring(4));
		else if (number.length() > 3 && number.startsWith("+44"))
			return lookupUKNumber(context, number.substring(3));
		else if ((number.length() > 2 && number.startsWith("00")) || (number.length() > 1 && number.startsWith("+")) )
			return new NumberDetails(context.getString(R.string.international_number), false);
		else if (number.length() > 1 && number.startsWith("0"))
			return lookupUKNumber(context, number.substring(1));
		else if (number.length() == 6 && number.startsWith("118"))
			return new NumberDetails(context.getString(R.string.directory_enq), false);
		else
			return new NumberDetails();
	}
	
	// nationalNumber has leading +44, 0044 or 0 stripped off
	private NumberDetails lookupUKNumber(Context context, String nationalNumber)
	{
		switch(nationalNumber.charAt(0)) {
		case '1':
		case '2':
			BufferedReader sabc = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.sabc)));
			try {
				String line;
				if( nationalNumber.length() < 5 )
					return new NumberDetails();
				String search1 = "\"" + nationalNumber.substring(0,4) + "\"" + ",,";
				String search2 = "\"" + nationalNumber.substring(0,4) + "\"" + ",\"" + nationalNumber.substring(4,5) + "\","; 
				while((line = sabc.readLine()) != null) {
					if( line.startsWith(search1) || line.startsWith(search2) )
					{
						String[] parts = line.split(",");
						boolean is_safe = true;
						// Check for Isle of Man, Guernsey, Jersey national numbers
						if( nationalNumber.substring(0,4).equals("1624") || nationalNumber.substring(0,4).equals("1481") || nationalNumber.substring(0,4).equals("1534"))
							is_safe = false;
						if( parts.length > 3 )
							return new NumberDetails(context.getString(R.string.geographic_number, parts[3].replaceAll("\"", "")), is_safe);
						else
							return new NumberDetails();
					}
				}
			} catch(IOException e) {}
			break;
		case '3':
			return new NumberDetails(context.getString(R.string.nationwide_nongeographic), true);
		case '5':
			if( nationalNumber.length() > 3 && nationalNumber.startsWith("500") )
				return new NumberDetails(context.getString(R.string.freephone), false);
			else if( nationalNumber.length() > 2 && nationalNumber.startsWith("55") )
				return new NumberDetails(context.getString(R.string.corporate_numbering), false);
			else if( nationalNumber.length() > 2 && nationalNumber.startsWith("56") )
				return new NumberDetails(context.getString(R.string.location_independent), false);
			break;
		case '7':
			BufferedReader s7_code = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.s7_code)));
			try {
				String line;
				if( nationalNumber.length() < 6 )
					return new NumberDetails();
				String search1 = "\"" + nationalNumber.substring(0,4) + "\"" + ",\"" + nationalNumber.substring(4,5) + "\"";
				String search2 = "\"" + nationalNumber.substring(0,4) + "\"" + ",\"" + nationalNumber.substring(4,6) + "\""; 
				while((line = s7_code.readLine()) != null) {
					if( line.startsWith(search1) || line.startsWith(search2) )
					{
						String[] parts = line.split(",");
						if( parts.length < 6 )
							return new NumberDetails();
						if( !parts[2].equals("\"Allocated\"") )
							return new NumberDetails();
						String operator = parts[4].replaceAll("\"", "");
						String type = parts[5].replaceAll("\"", "");
						boolean safe = false;
						for (int i = 0; i < SAFE_MOBILE_OPERATORS.length; i++)
							if( SAFE_MOBILE_OPERATORS[i].equals( operator ) )
								safe = true;
						if( !type.equals(MOBILE_SERVICE) )
							safe = false;
						if( safe )
							return new NumberDetails("UK Mobile", true);
						else
							return new NumberDetails(type + ": " + operator, false);
					}
				}
			} catch(IOException e) {}
			break;
		case '8':
			if( nationalNumber.length() > 2 && nationalNumber.startsWith("80") )
				return new NumberDetails(context.getString(R.string.freephone), false);
			else if( nationalNumber.length() > 2 && (nationalNumber.startsWith("84") || nationalNumber.startsWith("87")) )
				return new NumberDetails(context.getString(R.string.special_services), false);
			break;
		case '9':
			return new NumberDetails(context.getString(R.string.premium_rate), false);
		}
		
		return new NumberDetails();
	}
}
