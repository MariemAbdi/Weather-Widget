package com.example.myweatherwidget;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private LocationManager locationManager;
    private TextView cityStateTV, dateTimeTV, temperatureTV, temperatureMaxTV, temperatureMinTV, feelsLikeTV, descriptionTV, sunriseTV, sunsetTV, pressureTV, windTV, humidityTV, VisibilityTV, lastUpdatedTV;

    private ImageView weatherIconIV;

    private double longitude, latitude;
    private String stateName="";

    private static final String API_KEY = "8118ed6ee68db2debfaaa5a44c832918";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&units=metric&appid=%s";
    private static final int MSG_UPDATE_WEATHER = 1;
    public final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_UPDATE_WEATHER) {
                Bundle data = msg.getData();
                String temperature = data.getString("temperature");
                String weatherIcon = data.getString("weatherIcon");
                String weatherDescription = data.getString("weatherDescription");
                String tempMin = data.getString("tempMin");
                String tempMax = data.getString("tempMax");
                String feelsLike = data.getString("feelsLike");
                String pressure = data.getString("pressure");
                String windSpeed = data.getString("windSpeed");
                String sunrise = data.getString("sunrise");
                String humidity = data.getString("humidity");
                String visibility = data.getString("visibility");
                String sunset = data.getString("sunset");
                String city = data.getString("city");
                String lastUpdate = data.getString("lastUpdated");

                SharedPreferences preferences = getSharedPreferences("myPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("temperature", temperature);
                editor.putString("icon", weatherIcon);
                editor.putString("city", city);
                editor.putString("state", stateName);
                editor.apply();

                // Update UI with weather data
                //Current DateTime
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
                dateTimeTV.setText(sdf.format(new Date()));
                cityStateTV.setText(String.format(city + ", " + stateName));
                temperatureTV.setText(temperature);
                descriptionTV.setText(weatherDescription);
                temperatureMinTV.setText(tempMin);
                temperatureMaxTV.setText(tempMax);
                feelsLikeTV.setText(feelsLike);
                pressureTV.setText(pressure);
                windTV.setText(windSpeed);
                sunriseTV.setText(sunrise);
                humidityTV.setText(humidity);
                VisibilityTV.setText(visibility);
                sunsetTV.setText(sunset);
                lastUpdatedTV.setText(String.format("Last Update: " + lastUpdate));
                Picasso.get().load(weatherIcon).placeholder(R.drawable.cloudy).into(weatherIconIV);
            } else {
                super.handleMessage(msg);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //fullscreen -> hide status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        cityStateTV= findViewById(R.id.currentcity);
        dateTimeTV= findViewById(R.id.currentdate);
        temperatureTV= findViewById(R.id.degree);
        descriptionTV= findViewById(R.id.description);
        feelsLikeTV= findViewById(R.id.feelsLike);
        sunriseTV= findViewById(R.id.sunrise);
        sunsetTV= findViewById(R.id.sunset);
        pressureTV= findViewById(R.id.pressure);
        windTV= findViewById(R.id.wind);
        humidityTV= findViewById(R.id.humidity);
        temperatureMinTV= findViewById(R.id.tempMin);
        temperatureMaxTV= findViewById(R.id.tempMax);
        VisibilityTV= findViewById(R.id.visibility);
        lastUpdatedTV= findViewById(R.id.lastUpdated);
        weatherIconIV= findViewById(R.id.weatherIcon);

        // Initialize the LocationManager and LocationListener
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }


    @Override
    protected void onStart() {
        super.onStart();
        // Request location updates from the LocationManager
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // Update the UI with the current location
        latitude = location.getLatitude();
        longitude = location.getLongitude();

        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            stateName = addresses.get(0).getAdminArea();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        new Thread(() -> {
            try {
                String url = String.format(BASE_URL, latitude, longitude, API_KEY);
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("GET");
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                JSONObject jsonObject = new JSONObject(response.toString());
                JSONObject main = jsonObject.getJSONObject("main");
                JSONObject sys = jsonObject.getJSONObject("sys");

                String temperature = main.getString("temp");
                String tempMin = main.getString("temp_min") + "°C";
                String tempMax = main.getString("temp_max") + "°C";
                String feelsLike = main.getString("feels_like") + "°C";
                String pressure = main.getString("pressure");
                String humidity = main.getString("humidity");
                String sunrise = new SimpleDateFormat("HH:mm", Locale.ENGLISH).format(new Date(sys.getLong("sunrise") * 1000));
                String sunset = new SimpleDateFormat("HH:mm", Locale.ENGLISH).format(new Date(sys.getLong("sunset") * 1000));
                String windSpeed = jsonObject.getJSONObject("wind").getString("speed");
                String city = jsonObject.getString("name");
                String visibility = jsonObject.getString("visibility");
                String weatherDescription = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description");
                String weatherIcon = "https://openweathermap.org/img/w/" + jsonObject.getJSONArray("weather").getJSONObject(0).getString("icon") + ".png";
                String updatedAtText = new SimpleDateFormat("dd/MM/yy HH:mm", Locale.ENGLISH).format(new Date(jsonObject.getLong("dt") * 1000));

                Message message = mHandler.obtainMessage(MSG_UPDATE_WEATHER);
                Bundle data = new Bundle();
                data.putString("temperature", temperature.substring(0,temperature.indexOf('.'))+ "°C");
                data.putString("tempMin", tempMin);
                data.putString("tempMax", tempMax);
                data.putString("feelsLike", feelsLike);
                data.putString("pressure", pressure);
                data.putString("humidity", humidity);
                data.putString("sunrise", sunrise);
                data.putString("sunset", sunset);
                data.putString("windSpeed", windSpeed);
                data.putString("city",city);
                data.putString("state",stateName);
                data.putString("visibility",visibility);
                data.putString("weatherDescription",weatherDescription);
                data.putString("weatherIcon",weatherIcon);
                data.putString("lastUpdated",updatedAtText);
                message.setData(data);
                mHandler.sendMessage(message);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }).start();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Handle the result of the permission request
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onStart();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        // Stop location updates when the app is stopped
        locationManager.removeUpdates(this);
    }

    public void Refresh(View view) {
        // Initialize the LocationManager and LocationListener
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }
}
