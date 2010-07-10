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