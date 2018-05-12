package net.kollnig.alarm;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class MainActivity extends Activity {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Check Availability of Play Services
		GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this);
	}

	/**
	 * The check in onResume() ensures that if the user returns
	 * to the running app through some other means, such as through
	 * the back button, the check of Play Services is performed.
	 */
	@Override
	protected void onResume () {
		super.onResume();
		GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this);
	}

	@Override
	public boolean onCreateOptionsMenu (Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected (MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.menuContact:
				openLink(Config.contact);
				return true;
			case R.id.menuPrivacy:
				openLink(Config.privacy);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Opens links in default browser
	 *
	 * @param url The link to open
	 */
	private void openLink (String url) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(browserIntent);
	}

	/**
	 * Open URL to set alarm
	 *
	 * @param v View
	 */
	public void clickBtnUrl (View v) {
		// Get token
		String regid = FirebaseInstanceId.getInstance().getToken();
		if (regid == null) { // no token has been generated yet
			Toast.makeText(MainActivity.this, R.string.regidUnavailable, Toast.LENGTH_LONG).show();
			return;
		}

		// Open URL, derived from token
		try {
			String url = Config.host + "?id=" + URLEncoder.encode(regid, "UTF-8");

			// Offer to share link
			Intent sendIntent = new Intent(Intent.ACTION_SEND);
			sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Cloud Alarm - Config Page");
			sendIntent.putExtra(Intent.EXTRA_TEXT, url);
			sendIntent.setType("text/plain");
			startActivity(Intent.createChooser(sendIntent, "Transfer link to your computer"));
		} catch (UnsupportedEncodingException ex) { // due to URLEncoder.encode, never happens
			throw new AssertionError("UTF-8 is unknown.");
		}
	}
}
