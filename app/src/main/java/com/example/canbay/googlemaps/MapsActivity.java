package com.example.canbay.googlemaps;

import android.*;
import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.identity.intents.Address;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private int distance;
    int [] A = new int[1];
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        int result=0;
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, result);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        else {
            if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                Toast.makeText(getApplicationContext(), "GPS Kapalı!",Toast.LENGTH_LONG).show();
            }
            else if(!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                Toast.makeText(getApplicationContext(), "İnternet Kapalı!",Toast.LENGTH_LONG).show();
            }
            // INTERNET ACIK
            else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new android.location.LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {

                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        LatLng latLng = new LatLng(latitude, longitude);
                        LatLng ikieylul = new LatLng(39.815025, 30.533280);
                        Geocoder geocoder = new Geocoder(getApplicationContext());
                        try {
                            List<android.location.Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
                            String str = addressList.get(0).getSubLocality() + " ,";
                            str += addressList.get(0).getCountryName();
                            mMap.addMarker(new MarkerOptions().position(latLng).title(str));
                            mMap.addMarker(new MarkerOptions().position(ikieylul).title("İki Eylül Kampüsü").icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10.2f));
                            String url = getRouteUrl(latLng,ikieylul);
                            GetDataTask getData = new GetDataTask();
                            getData.execute(url);

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
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
                });
            }
        }
    }
        @Override
        public void onMapReady (GoogleMap googleMap){
            mMap = googleMap;
        }


        private String getRouteUrl(LatLng origin,LatLng destination){

            String strorigin = "origin=" + origin.latitude + "," + origin.longitude;
            String strdest = "destination=" + destination.latitude + "," + destination.longitude;
            String sensor = "sensor=false";
            String mode = "mode=driving";
            String params = strorigin + "&" + strdest + "&" + sensor + "&" + mode;
            String output = "json";
            String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + params;

            return url;

        }

        private String getJSONResponse(String url){
            String response="";
            InputStream is = null;
            HttpURLConnection urlConnection= null;

            try{
                URL urlOpen = new URL(url);
                urlConnection = (HttpURLConnection) urlOpen.openConnection();
                is = urlConnection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuffer sb = new StringBuffer();
                String line = "";
                while((line = br.readLine()) != null){
                    sb.append(line);
                }
                response = sb.toString();
                br.close();
            }
            catch(Exception ex){
                ex.printStackTrace();
            }
            finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                urlConnection.disconnect();
            }
            return response;
        }

    private class GetDataTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = getJSONResponse(url[0]);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Background Task!",Toast.LENGTH_LONG).show();
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                JSONParser parser = new JSONParser();
                routes = parser.parse(jObject);
                distance = parser.getDistance();
                A[0] = distance;


            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList points;
            PolylineOptions lineOptions=new PolylineOptions();
            MarkerOptions markerOptions = new MarkerOptions();

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList();
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(11);
                lineOptions.color(Color.BLUE);
                lineOptions.geodesic(true);
            }
// Drawing polyline in the Google Map for the i-th route
            mMap.addPolyline(lineOptions);

            JSONParser parser = new JSONParser();
            if(A[0] < 500) {
                Toast.makeText(getApplicationContext(),"İki eylül kampüsüne 500 metreden daha yakınsınız, uzaklığınız: "+A[0]+" metre",Toast.LENGTH_LONG).show();
                setAlarm();
            }
        }
    }

    public void setAlarm(){
        // set alarm to the 10 seconds
        Long notifyTime = new GregorianCalendar().getTimeInMillis()+10*1000;
        Intent notificationIntent = new Intent(this,NotificationReceiver.class);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP,notifyTime, PendingIntent.getBroadcast(this,1,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT));
    }


}

