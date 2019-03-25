package com.example.zj.cool;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zj.cool.gson.AQI;
import com.example.zj.cool.gson.Forecast;
import com.example.zj.cool.gson.Lifestyle;
import com.example.zj.cool.gson.Weather;
import com.example.zj.cool.util.HttpUtil;
import com.example.zj.cool.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    private ScrollView weatherLayout;
    private TextView titleCity, titleUpdateTime, degreeText, weatherInfoText, aqiText, pm25Text, comfortText, carWashText, sportText;
    private LinearLayout forecasetLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        initView();

    }

    public void initView() {
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        forecasetLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = preferences.getString("weather", null);
        String aqiString = preferences.getString("AQI", null);
        if ((weatherString != null) && (aqiString != null)) {
            Weather weather = Utility.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
            AQI aqi = Utility.handleAQIResponse(aqiString);
            showAQIInfo(aqi);
        } else {
            String weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }


    }

    public void requestWeather(final String weatherId) {
        String pm25Url = "http://web.juhe.cn:8080/environment/air/pm?city=" + getIntent().getStringExtra("cityName") + "&key=d10a64c982c97e3cbbeb0b1dee9ba04e";
        String weatherUrl = "https://free-api.heweather.net/s6/weather?location=" + weatherId + "&key=f917a5a6303f4fe08b6273d72d269e4e";
        Log.e("发出天气请求", "requestWeather: " + weatherUrl);
        Log.e("发出AQI请求", "requestAQI: " + pm25Url);
        HttpUtil.sendOkHttpRequest(pm25Url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取当前PM2.5失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final AQI aqi = Utility.handleAQIResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (aqi != null) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("AQI", responseText);
                            editor.apply();
                            showAQIInfo(aqi);
                            Log.e("!", "run: " + aqi);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取AQI信息失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取当前天气失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                            Log.e("!", "run: " + weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void showAQIInfo(AQI aqi) {
        if (aqi.AQI != null) {
            aqiText.setText(aqi.AQI);
            pm25Text.setText(aqi.PM25);
        }
    }

    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecasetLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecasetLayout, false);
            TextView dataText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dataText.setText(forecast.date);
            infoText.setText(forecast.info);
            maxText.setText(forecast.max);
            minText.setText(forecast.min);
            forecasetLayout.addView(view);
        }

        for (Lifestyle lifestyle : weather.lifestyle) {
            if (lifestyle.type.equals("comf")) {
                String comfort = "舒适度" + lifestyle.txt;
                comfortText.setText(comfort);
            }
            if (lifestyle.type.equals("cw")) {
                String carWash = "洗车指数" + lifestyle.txt;
                carWashText.setText(carWash);
            }
            if (lifestyle.type.equals("sport")) {
                String sport = "运动建议" + lifestyle.txt;
                sportText.setText(sport);
            }

        }
        weatherLayout.setVisibility(View.VISIBLE);
    }
}

