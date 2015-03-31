package com.saransh.twitterfeed;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;


public class MainActivity extends ActionBarActivity {

    TextView t;
    GoogleMap map;
    ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        map = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setMyLocationEnabled(true);
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        dialog = new ProgressDialog(MainActivity.this);
        dialog.setMessage("Loading...");
        dialog.show();
        String[] positions=getPosition();
        if (positions==null)
        {
            Toast.makeText(getApplicationContext(),
                    "Location Cannot be Acessed",
                    Toast.LENGTH_LONG).show();
            dialog.dismiss();
        }
        else
        {
            try {
                new get_tweets().execute(positions);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    class get_tweets extends AsyncTask<String,String,String[][]>
    {
        @Override
        protected String[][] doInBackground(String[] position)
        {
            JSONParser jsonParser=new JSONParser();
            String Lat = position[0];
            String Lng = position[1];
            String q="q=e&";
            String geocode = "geocode="+Lat+","+Lng+","+"1mi&";
            String count="count=100";
            String twitter_url="https://api.twitter.com/1.1/search/tweets.json?";
            twitter_url = twitter_url+q+geocode+count;

            {
                JSONObject jObj = jsonParser.getJSONFromUrl(twitter_url);
                String[][] data= jsonParser.getdata(jObj);
                if(data[0][0].equals("N"))
                {
                    return null;
                }
                else
                {
                    return data;
                }
            }
        }

        @Override
        protected void onPostExecute(String data[][])
        {
            dialog.dismiss();
            if(data==null)
            {
                Toast.makeText(getApplicationContext(),
                        "No Tweets",
                        Toast.LENGTH_LONG).show();
            }
            else
            {
                int count=0;
                for(int i=0;data[0][i]!=null;i++) {
                    count++;
                    String tweet = data[0][i];
                    double Lat = Double.parseDouble(data[1][i]);
                    double Lng = Double.parseDouble(data[2][i]);
                    final LatLng Tweet = new LatLng(Lat, Lng);
                    map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
                    Marker Tweets = map.addMarker(new MarkerOptions().position(Tweet).title(tweet));
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(Tweet, 15));
                }
                Log.d("counter_check", String.valueOf(count));
            }

        }
    }
    private String[] getPosition()
    {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo ni = cm.getActiveNetworkInfo();

        if(ni == null)
        {
            Toast.makeText(getApplicationContext(), "Can't Connect to the Internet", Toast.LENGTH_LONG).show();
            return null;
        }
        else {
                Location mylocation;
                final String position[] = new String[2];
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                LocationListener locationListener = new LocationListener()
                {
                    @Override
                    public void onLocationChanged(Location location) {
                    position[0] = String.valueOf(location.getLatitude());
                    position[1] = String.valueOf(location.getLongitude());
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }
                    @Override
                    public void onProviderEnabled(String provider) {
                    }
                    @Override
                    public void onProviderDisabled(String provider) {
                    }
                };
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    buildAlertNoGps();
                }
                else {
                mylocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                position[0] = String.valueOf(mylocation.getLatitude());
                position[1] = String.valueOf(mylocation.getLongitude());
                final LatLng MyLocation = new LatLng(mylocation.getLatitude(),mylocation.getLongitude());                    
                map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
                Marker myLocation = map.addMarker(new MarkerOptions().position(MyLocation).title(" MY Location"));
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(MyLocation, 15));
                }
            return position;
        }
    }

    private void buildAlertNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You GPS seems to be disabled, do u want to enable it?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog,@SuppressWarnings("unused") final int id)
            {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener(){
            public void onClick(final DialogInterface dialog,@SuppressWarnings("unused") final int id)
            {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
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
        if (id == R.id.action_settings)
        {
            ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo ni = cm.getActiveNetworkInfo();

            if(ni == null) {

                Toast.makeText(getApplicationContext(), "Can't Connect to the Internet", Toast.LENGTH_LONG).show();
                return false;

            }
            else
            {
                String position[] = new String[2];
                position[0] = "37.781157";
                position[1] = "-122.398720";
                dialog.show();
                try {
                    new get_tweets().execute(position);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
