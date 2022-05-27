package com.example.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.database.sqlite.SQLiteDatabase;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.weatherapp.db.MyDbHelper;
import com.example.weatherapp.db.MyDbManager;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private RelativeLayout homeRL;
    private ProgressBar loadingPB;
    private TextView cityNameTV, temperatureTV, conditionTV;
    private RecyclerView weatherRV;
    private TextInputEditText cityEdt;
    private ImageView backIV, iconIV, searchIV;
    private ArrayList<WeatherRvModal> weatherRvModalArrayList;
    private WeatherRvAdapter weatherRvAdapter;
    private LocationManager locationManager;
    private int PERMISSION_CODE = 1;
    private String cityName;
    private double lat;
    private double lon;
    boolean gps_enabled = false;

    private MyDbManager myDbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setContentView(R.layout.activity_main);
        homeRL = findViewById(R.id.idRLHome);
        loadingPB = findViewById(R.id.idPBLoading);
        cityNameTV = findViewById(R.id.idCityName);
        temperatureTV = findViewById(R.id.idTemperature);
        conditionTV = findViewById(R.id.idCondition);
        weatherRV = findViewById(R.id.idRvWeather);
        cityEdt = findViewById(R.id.idEdtCity);
        backIV = findViewById(R.id.idBackground);
        iconIV = findViewById(R.id.idMainIcon);
        searchIV = findViewById(R.id.idSearchButton);
        weatherRvModalArrayList = new ArrayList<>();
        weatherRvAdapter = new WeatherRvAdapter(this, weatherRvModalArrayList);
        weatherRV.setAdapter(weatherRvAdapter);
        myDbManager = new MyDbManager(this);

        // наличие GPS
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_CODE);
        }

        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null) {
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 1000, this);

            } catch (NullPointerException e) {
                lat = -1.0;
                lon = -1.0;
            }

        } else if (gps_enabled) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 1000, this);
        } else {
            cityName = "Krasnodar";
        }

        getWeatherInfo(cityName);

        searchIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = cityEdt.getText().toString();
                if (city.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Пожалуйста, введите название города", Toast.LENGTH_SHORT).show();
                } else {
                    cityNameTV.setText(cityName);
                    getWeatherInfo(city);
                    myDbManager.closeDb();
                }
            }
        });
    }

// Разрешение на доступ к местоположению

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Разрешения предоставлены..", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Пожалуйста, предоставьте разрешения", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

// Загрузка данных из API для города, введенного с локации или после ввода города. дополнительные функции, отображающие погоду на весь день

    private void getWeatherInfo(String cityName) {
        String url = "http://api.weatherapi.com/v1/forecast.json?key=e1574305ae6641109ad183532210512&q=" + cityName + "&days=1&api=yes&alerts=no&lang=ru";
        cityNameTV.setText(cityName);
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loadingPB.setVisibility(View.GONE);
                homeRL.setVisibility(View.VISIBLE);
                weatherRvModalArrayList.clear();

                try {
                    String temperature = response.getJSONObject("current").getString("temp_c");
                    temperatureTV.setText(temperature + "°c");
                    int isDay = response.getJSONObject("current").getInt("is_day");
                    String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    Picasso.get().load("https:".concat(conditionIcon)).into(iconIV);
                    conditionTV.setText(condition);

                    //утро
                    if (isDay == 1 && condition.equals("Переменная облачность") || condition.equals("Облачно") || condition.equals("Пасмурно")) {
                        Picasso.get().load("https://images.unsplash.com/photo-1445297983845-454043d4eef4?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1170&q=80").into(backIV);
                    } else if (isDay == 1 && condition.equals("Небольшой ливневый дождь") || condition.equals("Небольшой дождь") || condition.equals("Местами небольшой дождь") || condition.equals("Местами дождь")) {
                        Picasso.get().load("https://images.unsplash.com/photo-1589799790421-178330f91cfa?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=387&q=80").into(backIV);
                    } else if (isDay == 1 && condition.equals("Солнечно") || condition.equals("Ясно")) {
                        Picasso.get().load("https://images.unsplash.com/photo-1500382017468-9049fed747ef?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1332&q=80").into(backIV);
                    }
                    //ночь
                    if (isDay == 0 && condition.equals("Переменная облачность") || condition.equals("Облачно") || condition.equals("Пасмурно")) {
                        Picasso.get().load("https://images.unsplash.com/photo-1500740516770-92bd004b996e?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1172&q=80").into(backIV);
                    } else if (isDay == 0 && condition.equals("Небольшой ливневый дождь") || condition.equals("Небольшой дождь") || condition.equals("Местами небольшой дождь") || condition.equals("Местами дождь")) {
                        Picasso.get().load("https://images.unsplash.com/photo-1532203512255-3c9c9d666c50?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=421&q=80").into(backIV);
                    } else if (isDay == 0 && condition.equals("Солнечно") || condition.equals("Ясно")) {
                        Picasso.get().load("https://images.unsplash.com/photo-1573166331058-c9e7ef82687d?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=465&q=80").into(backIV);
                    }


                    JSONObject forecastObj = response.getJSONObject("forecast");
                    JSONObject forcastO = forecastObj.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourArray = forcastO.getJSONArray("hour");

                    //JSONObject location = response.getJSONObject("location");
                    //String name = location.getString("name");

                    myDbManager.openDb();

                    for (int i = 0; i < hourArray.length(); i++) {
                        JSONObject hourObj = hourArray.getJSONObject(i);
                        String time = hourObj.getString("time");
                        String temper = hourObj.getString("temp_c");
                        String img = hourObj.getJSONObject("condition").getString("icon");
                        String condit = hourObj.getJSONObject("condition").getString("text");
                        String wind = hourObj.getString("wind_kph");

                        myDbManager.insertToDb(time,"Rac",temper,condit);

                        weatherRvModalArrayList.add(new WeatherRvModal(time, temper, img, wind));
                    }

                    weatherRvAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            // Если указано неверное название города
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Пожалуйста, введите правильное название города...", Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue.add(jsonObjectRequest);

    }

// Для загрузки сайтов на регулярной основе

    @Override
    public void onLocationChanged(@NonNull Location location) {
        try {
            //скачать местоположение с gps
            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            String address = addresses.get(0).getAddressLine(0);
            // вызов метода для запроса из API
            getWeatherInfo(address);

        } catch (Exception e) {

        }
    }
}