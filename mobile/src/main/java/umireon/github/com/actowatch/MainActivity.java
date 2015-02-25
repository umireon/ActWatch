package umireon.github.com.actowatch;

import android.content.Entity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.util.Calendar;
import java.util.Date;


public class MainActivity extends ActionBarActivity {
    private RelativeLayout mRelativeLayout;
    private TextView mTextView;
    private GoogleApiClient mGoogleApiClient;
    private DefaultHttpClient mDefaultHttpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRelativeLayout = (RelativeLayout)findViewById(R.id.relative_layout);
        mTextView = (TextView)findViewById(R.id.hello);

        mTextView.setText("0");
        mRelativeLayout.setBackgroundColor(Color.HSVToColor(new float[]{0.0f, 0.7f, 0.7f}));

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {

                    }
                })
                .addApi(Wearable.API)
                .build();
        mDefaultHttpClient = new DefaultHttpClient();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(i);
            return true;
        } else if (id == R.id.action_refresh) {
            retrieveActivity();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void retrieveActivity() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String endpoint = sharedPref.getString("endpoint", "");
        String email = sharedPref.getString("email", "");
        String password = sharedPref.getString("password", "");
        new LifelogActivities(this, mGoogleApiClient).execute(endpoint, email, password);
    }
}
