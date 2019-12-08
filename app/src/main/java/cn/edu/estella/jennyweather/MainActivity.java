package cn.edu.estella.jennyweather;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {
    private ImageButton imageButton;

    private String url = "http://api.jisuapi.com/weather/query?appkey=fd2507b34c72c646&city=";
    //需要更新内容的控件
    private TextView lowText, nowText, highText;
    private TextView infoLeft, infoRight;
    private ImageView nowView;
    private ImageView firstImg, secondImg, thirdImg, fourthImg;
    private TextView firstWeather, firstDate, secondWeather, secondDate,
            thirdWeather, thirdDate, fourthWeather, fourthDate;
    private String defaultName = "天津";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //当天
        lowText = (TextView)findViewById(R.id.lowest_text);
        nowText = (TextView)findViewById(R.id.now_text);
        highText = (TextView)findViewById(R.id.highest_text);
        infoLeft = (TextView)findViewById(R.id.info_left_text);
        infoRight = (TextView)findViewById(R.id.info_right);
        nowView = (ImageView)findViewById(R.id.now_img);
//后四天天气
        firstImg = (ImageView)findViewById(R.id.first_img);
        firstWeather = (TextView)findViewById(R.id.first_weather);
        firstDate = (TextView)findViewById(R.id.first_date);
        secondImg = (ImageView)findViewById(R.id.second_img);
        secondWeather = (TextView)findViewById(R.id.second_weather);
        secondDate = (TextView)findViewById(R.id.second_date);
        thirdImg = (ImageView)findViewById(R.id.third_img);
        thirdWeather = (TextView)findViewById(R.id.third_weather);
        thirdDate = (TextView)findViewById(R.id.third_date);
        fourthImg = (ImageView)findViewById(R.id.fourth_img);
        fourthWeather = (TextView)findViewById(R.id.fourth_weather);
        fourthDate = (TextView)findViewById(R.id.fourth_date);



        imageButton = (ImageButton)findViewById(R.id.city_button);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CityActivity.class);
                Bundle bundle = new Bundle();
                intent.putExtras(bundle);
                startActivityForResult(intent, 1);
            }
        });

        Getweather getweather = new Getweather();
        getweather.execute(url, defaultName);


        IntentFilter inf = new IntentFilter();
        inf.addAction("com.weather.refresh");
        registerReceiver(broadcastReceiver, inf);

        startService(new Intent(this, RefreshService.class));


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            Bundle bundle = data.getExtras();
            Toast.makeText(this, bundle.getString("city"), Toast.LENGTH_LONG).show();
        }
    }


    //访问网络的内部类
    private class Getweather extends AsyncTask<String, String, String> {

        private String openConnection(String address, String cityId){
            String result = "";
            try{
                URL url = new URL(address + URLEncoder.encode(cityId, " UTF-8"));
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();
                InputStream in = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    result = result + line;
                }
            }catch(Exception e) {
                e.printStackTrace();
            }
            // Log.i("info", result);
            return result;
        }

        @Override
        protected String doInBackground(String... params) {
            return openConnection(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(String result){
            try{
                JSONObject object = new JSONObject(result);
                JSONObject today = (JSONObject) object.get("result");
//获取当天天气
                highText.setText(today.getString("temphigh"));
                lowText.setText(today.getString("templow"));
                nowText.setText(today.getString("temp")+"\u2103");
                infoLeft.setText(today.getString("city") + "\n\n" + today.getString("week") + "\n\n" + today.getString("weather"));
                infoRight.setText(today.getString("date") + "\n\n" + today.getString("humidity") + "\n\n" + today.getString("pressure"));
                Log.i("infotest",today.getString("date") + "\n\n" + today.getString("winddirect") + "\n\n" + today.getString("windpower"));
                Log.i("infotest",today.getString("city") + "\n\n" + today.getString("week") + "\n\n" + today.getString("weather"));
                //获取后四天的预告
                JSONArray forecast = (JSONArray) today.get("daily");
                firstWeather.setText((((JSONObject) forecast.get(0)).getJSONObject("day")).getString("weather") + "\n\n" +
                        ((((JSONObject) forecast.get(0)).getJSONObject("day")).getString("temphigh") + "/" + ((((JSONObject) forecast.get(0)).getJSONObject("night")).getString("templow"))));

                secondWeather.setText((((JSONObject) forecast.get(1)).getJSONObject("day")).getString("weather") + "\n\n" +
                        ((((JSONObject) forecast.get(1)).getJSONObject("day")).getString("temphigh") + "/" + ((((JSONObject) forecast.get(1)).getJSONObject("night")).getString("templow"))));

                thirdWeather.setText((((JSONObject) forecast.get(2)).getJSONObject("day")).getString("weather") + "\n\n" +
                        ((((JSONObject) forecast.get(2)).getJSONObject("day")).getString("temphigh") + "/" + ((((JSONObject) forecast.get(2)).getJSONObject("night")).getString("templow"))));

                fourthWeather.setText((((JSONObject) forecast.get(3)).getJSONObject("day")).getString("weather") + "\n\n" +
                        ((((JSONObject) forecast.get(3)).getJSONObject("day")).getString("temphigh") + "/" + ((((JSONObject) forecast.get(3)).getJSONObject("night")).getString("templow"))));

                firstDate.setText(((JSONObject) forecast.get(1)).getString("date"));
                secondDate.setText(((JSONObject) forecast.get(2)).getString("date"));
                thirdDate.setText(((JSONObject) forecast.get(3)).getString("date"));
                fourthDate.setText(((JSONObject) forecast.get(4)).getString("date"));
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }


    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals("com.weather.refresh")){
                Toast.makeText(getApplication(),"refresh",Toast.LENGTH_LONG).show();
                Getweather getweather = new Getweather();
                getweather.execute(url, defaultName);
            }
        }


    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, RefreshService.class));
        unregisterReceiver(broadcastReceiver);

    }
}
