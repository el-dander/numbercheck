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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class Prompt extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String desc = getIntent().getStringExtra("uk.co.laurencea.UKNumberCheck.Description");
        if( desc == null )
        	desc = getString(R.string.unknown_number);
        final String number = getIntent().getStringExtra("uk.co.laurencea.UKNumberCheck.Number");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.prompt_text, number, desc))
               .setCancelable(false)
               .setPositiveButton(R.string.yes_string, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   dialog.cancel();
                       startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:***" + number )));
                       finish();
                   }
               })
               .setNegativeButton(R.string.no_string, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finish();
                   }
               });

        AlertDialog alert = builder.create();
        alert.show();
    }
}