package com.saransh.twitterfeed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.gson.Gson;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.util.Base64;
import android.util.Log;

public class JSONParser {

    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";

    //Keys

    final static String CONSUMER_KEY = "nCMZCUVAGODegCjtegE8tpsu5";
    final static String CONSUMER_SECRET = "58VuKJMtJmT62GDGf1GlagwtZe9FmXYkI0NeHXgAhUVFLNcj5a";

    //Twitter URLs

    final static String TwitterTokenURL = "https://api.twitter.com/oauth2/token";
    public JSONParser()
    {

    }
    public JSONObject getJSONFromUrl(String twitter_url)
    {
        try {

            DefaultHttpClient httpClient = new DefaultHttpClient();

            String urlApiKey = URLEncoder.encode(CONSUMER_KEY, "UTF-8");
            String urlApiSecret = URLEncoder.encode(CONSUMER_SECRET, "UTF-8");

            String combined = urlApiKey + ":" + urlApiSecret;

            String base64Encoded = Base64.encodeToString(combined.getBytes(), Base64.NO_WRAP);

            HttpPost httpPost = new HttpPost(TwitterTokenURL);

            httpPost.setHeader("Authorization", "Basic " + base64Encoded);
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

            httpPost.setEntity(new StringEntity("grant_type=client_credentials"));

            String rawAuthorisation = getResponseBody(httpPost);

            Authenticated auth = jsonToAuthenticated(rawAuthorisation);

            if(auth != null && auth.token_type.equals("bearer"))
            {
                HttpGet httpGet = new HttpGet(twitter_url);
                httpGet.setHeader("Authorization", "Bearer " + auth.access_token);
                httpGet.setHeader("Content-Type", "application/json");

                String results = getResponseBody(httpGet);
                JSONObject jObj = new JSONObject(results);
                return jObj;

            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return null;
    }

    private Authenticated jsonToAuthenticated(String rawAuthorization) {
        Authenticated auth = null;
        if (rawAuthorization != null && rawAuthorization.length() > 0) {
            try {
                Gson gson = new Gson();
                auth = gson.fromJson(rawAuthorization, Authenticated.class);
            } catch (IllegalStateException ex) {
                // just eat the exception
            }
        }
        return auth;
    }

    private String getResponseBody(HttpRequestBase request) {
        StringBuilder sb = new StringBuilder();
        try {

            DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            String reason = response.getStatusLine().getReasonPhrase();

            if (statusCode == 200) {

                HttpEntity entity = response.getEntity();
                InputStream inputStream = entity.getContent();

                BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                String line = null;
                while ((line = bReader.readLine()) != null) {
                    sb.append(line);
                }
            } else {
                sb.append(reason);
            }
        } catch (UnsupportedEncodingException ex) {
        } catch (ClientProtocolException ex1) {
        } catch (IOException ex2) {
        }
        return sb.toString();
    }

    String[][] getdata(JSONObject jObj)
    {
        String[][] Data=new String[4][100];
        JSONArray statuses = new JSONArray();
        try {
            statuses = jObj.getJSONArray("statuses");
            if(statuses!=null)
            {
                for (int i=0;i<statuses.length();i++)
                {
                    JSONObject tweets = statuses.getJSONObject(i);
                    //Data[0] - tweets
                    Data[0][i] = tweets.getString("text");

                    JSONObject geo = tweets.getJSONObject("geo");
                    JSONArray coordinate=geo.getJSONArray("coordinates");
                    //Data[1] - geo-latitude
                    Data[1][i]= coordinate.getString(0);
                    //Data[1] - geo-longitude
                    Data[2][i]= coordinate.getString(1);
                }
                if (statuses.length()==0)
                    Data[0][0]="N";
            }
            else
                return null;
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return Data;
    }

}
