package net.kollnig.alarm;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Check Availability of Play Services
		GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this);

		FirebaseApp.initializeApp(this);
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

	public void onError (int resId) {
		View main = this.getWindow().getDecorView().findViewById(android.R.id.content);
		Snackbar s = Snackbar.make(main, getText(resId), Snackbar.LENGTH_INDEFINITE);
		s.setAction(android.R.string.ok, v1 -> s.dismiss());
		s.setActionTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
		s.show();
	}

	/**
	 * Open URL to set alarm
	 *
	 * @param v View
	 */
	public void clickBtnUrl (View v) {
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
					&& !Settings.canDrawOverlays(this)) {
				Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
						Uri.parse("package:" + this.getPackageName()));
				PackageManager packageManager = this.getPackageManager();
				if (intent.resolveActivity(packageManager) != null) {
					startActivity(intent);
				} else {
					onError(R.string.draw_over_apps_permission);
				}
				return;
			}
		} catch (Exception e) {
			onError(R.string.draw_over_apps_permission);
		}

		FirebaseInstanceId.getInstance().getInstanceId()
				.addOnFailureListener(e -> onError(R.string.regidUnavailable))
				.addOnSuccessListener(instanceIdResult -> {
					String regid = instanceIdResult.getToken();

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
				});
	}
}
