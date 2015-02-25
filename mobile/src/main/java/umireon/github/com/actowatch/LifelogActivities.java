package umireon.github.com.actowatch;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

/**
 * Created by umireon on 15/02/22.
 */
public class LifelogActivities extends AsyncTask<String, Void, String> {
    private Activity mActivity;

    private static final String ACTIVITIES_KEY = "com.github.umireon.activities";
    private GoogleApiClient mGoogleApiClient;
    private double activities = 0.0;

    public LifelogActivities(Activity activity, GoogleApiClient googleApiClient) {
        mActivity = activity;
        mGoogleApiClient = googleApiClient;
    }

    @Override
    protected String doInBackground(String... params) {
        HttpGet request = new HttpGet(params[0]);

        DefaultHttpClient httpClient = new DefaultHttpClient();
        Credentials credentials = new UsernamePasswordCredentials(params[1], params[2]);

        String result = "";
        try {
            request.addHeader(new BasicScheme().authenticate(credentials, request));
            result = httpClient.execute(request, new ResponseHandler<String>() {
                @Override
                public String handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {
                    switch (httpResponse.getStatusLine().getStatusCode()) {
                        case HttpStatus.SC_OK:
                            return EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
                        default:
                            throw new RuntimeException("Unexpected Error" + httpResponse.getStatusLine().toString());
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
        return result;
    }

    @Override
    protected void onPostExecute(String s) {
        double seconds = 0.0;
        try {
            JSONObject root = new JSONObject(s);
            seconds = root.getDouble("seconds");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("LifelogActivities", s);
            seconds = 0.0;
        }
        TextView textView = (TextView) mActivity.findViewById(R.id.hello);
        textView.setText(Integer.toString((int)seconds));

        float hue = (float)(seconds / 86400.0 * 4.0 * 360.0);
        RelativeLayout relativeLayout = (RelativeLayout)mActivity.findViewById(R.id.relative_layout);
        relativeLayout.setBackgroundColor(Color.HSVToColor(new float[]{hue, 0.7f, 0.7f}));

        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/activities");
        putDataMapReq.getDataMap().putDouble(ACTIVITIES_KEY, seconds);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
    }
}
