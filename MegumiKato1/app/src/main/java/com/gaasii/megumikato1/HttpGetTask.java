package com.gaasii.megumikato1;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by negi on 2017/04/25.
 */

public class HttpGetTask extends AsyncTask<Void, Void, String>{
    private GoogleMap mMap;
    private Activity mParentActivity;
    private ProgressDialog mDialog = null;
    private JSONObject jsonData;
    private JSONArray jsonArrayData;
    private Double latitude, longitude;
    private PositionData pData;

    private List<PositionData> pDatas = new ArrayList<PositionData>();

    //private String mUrl = "https://www.yamagiwalab.jp/~yama/KPK/Hello.html";
    private String mUrl = "http://192.168.11.143:3000/positions/getpos.json";

    public HttpGetTask(Activity parentActivity, GoogleMap myMap, List<PositionData> pDatas){
        this.mParentActivity = parentActivity;
        this.mMap = myMap;
        this.pDatas = pDatas;
        pData = new PositionData(mMap);
    }

    @Override
    protected void onPreExecute(){
        //mDialog = new ProgressDialog(mParentActivity);
        //mDialog.setMessage("connecting...");
        //mDialog.show();
    }

    @Override
    protected String doInBackground(Void... arg0){
        return exec_get();
    }

    @Override
    protected void onPostExecute(String string){
        //mDialog.dismiss();

        LatLng pinPoint = new LatLng(0, 0);
        try {
//            jsonData = new JSONObject(string);
//            latitude = jsonData.getDouble("latitude");
//            longitude = jsonData.getDouble("longitude");
//            if(latitude != 0){
//                hyogo = new LatLng(latitude, longitude);
//                data.setRecentMarkerPosition(new MarkerOptions().position(hyogo));
//            }

            jsonArrayData = new JSONArray(string);
            latitude = jsonArrayData.getJSONObject(0).getDouble("latitude");
            longitude = jsonArrayData.getJSONObject(0).getDouble("longitude");

            pinPoint = new LatLng(latitude, longitude);
            pData.setRecentMarkerOptions(new MarkerOptions().position(pinPoint));

        }catch (Exception e){
            e.printStackTrace();
        }

        pData.setRecentMarker(mMap.addMarker(pData.getRecentMarkerOptions()));
        pDatas.add(pData);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(pinPoint));
        Log.d("Location Point", string);
        Log.d("Array_item_numerous", "numerous"+pDatas.size());
    }

    private String exec_get(){
        HttpURLConnection http = null;
        InputStream in = null;
        String src = "";

        try{
            URL url = new URL(mUrl);
            http = (HttpURLConnection)url.openConnection();
            http.setRequestMethod("GET");
            http.connect();

            in = http.getInputStream();
            src = readInputStream(in);

        }catch (Exception e){
            Log.d("myError", e.toString());
            e.printStackTrace();
        }finally {
            try{
                if(http != null){
                    http.disconnect();
                }
                if(in != null){
                    in.close();
                }
            }catch (Exception ignored){

            }

            return src;
        }
    }

    public String readInputStream(InputStream in) throws IOException, UnsupportedEncodingException {
        StringBuffer sb = new StringBuffer();
        String st = "";

        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        while((st = br.readLine()) != null)
        {
            sb.append(st);
        }
        try
        {
            in.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return sb.toString();
    }
}

