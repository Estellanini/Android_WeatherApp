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
    //声明控件，设置访问地址。
    //百度天气API，根据城市代号查询历史及未来天气的地址
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

        //再在onCreate()中初始化控件：
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



        /*这里为按钮添加点击事件：跳转到CityActivity中去。
          从这个activity要前往另一个Activity就需要用到Intent。
          这里所用的Intent的构造函数有两个参数，分别代表始发地和目的地。
          Bundle主要用于传递数据；它保存的数据，是以key-value(键值对)的形式存在的，
          可以在这里用Bundle.putString("Data", "data from TestBundle")这样的方法传递数据。
          而intent.putExtras(bundle)把整个Bundle传递过去。
          Intent的发送可以用startActivity(intent)，
          本例中因为CityActivity需要回传数据给MainActivity，所以我们使用startActivityForResult(intent, 1)，
          这里第二个参数1代表请求码，数值可以自己设定。
        */
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


        //onCreate()中开启网络访问活动：

        Getweather getweather = new Getweather();
        getweather.execute(url, defaultName);


        //注册该广播接收器，并且开启服务：

        IntentFilter inf = new IntentFilter();
        inf.addAction("com.weather.refresh");
        registerReceiver(broadcastReceiver, inf);

        startService(new Intent(this, RefreshService.class));


    }

    /*onActivityResult方法
    * 该方法会在CityActivity回退的时候被调用。这里我们判断请求码是否是1，
    * 结果码是否是Activity.RESULT_OK（值为-1），
    * 条件满足则捕获CityActivity传来的数据，然后用Toast打印输出。
    * */

    //数据库部分修改这段代码了
    //这里修改请求的城市代码。
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            Bundle bundle = data.getExtras();
            String cityname = bundle.getString("cityname");
            String[] params = cityname.split(" ");
            defaultName = params[params.length - 1];
        }
    }


    //访问网络的内部类
    /*
    * AsyncTask,是android提供的轻量级的异步类,可以直接继承AsyncTask,在类中实现异步操作,
    * 并提供接口反馈当前异步执行的程度(可以通过接口实现UI进度更新),最后反馈执行的结果给UI主线程。
    * AsyncTask定义了三种泛型类型 Params，Progress和Result。
    * Params 启动任务执行的输入参数，比如HTTP请求的URL。（本例中类型为String）
    * Progress 后台任务执行的百分比。（void）
    * Result 后台执行任务最终返回的结果，比如String。（本例为String）
    * 然后要实现它的两个方法：
    * doInBackground()，该方法内实现需要后台完成的工作，一般比较耗时，本例中是访问网络；
    * onPostExecute()，该方法在后台工作完成后调用，并把doInBackground()返回的结果作为输入参数，这里可以进行UI更新等操作。
    * */
    private class Getweather extends AsyncTask<String, String, String> {


        /*
        * 我们先实现doInBackground()：
        * 这里将传入的参数params[0], params[1]传入openConnection中，作为地址和城市Id，
        * 访问天气API的根据城市代码查询天气的方法，最后返回得到的字符串。
        * */
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


        /*
        * 接下来实现onPostExecute()来更新UI：
        * 这里上一个函数的返回值作为参数result传入，将它转化为JSONObject，
        * 根据API提供的它的结构，天气有关的数据存储在了名为“result”的JSONObject中，
        * 它包含：名为“today”的JSONObject，它存储着今天的天气情况，名为“forecast”的JSONObject，
        * 它存储着未来四天天气。
        *
        * */
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


    //定义广播接收器：
    //这里我们判断接收到的intent是否是我们想要的，通过判断action是否是“com.weather.refresh”。
    // 如果是，打印输出“refresh”，并且再次请求天气情况。

    //数据库部分修改这段代码了
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("com.weather.refresh")) {
                //  Toast.makeText(getApplication(), "refresh", Toast.LENGTH_LONG).show();
                Getweather getweather = new Getweather();
                getweather.execute(url, defaultName);
            }
        }
    };


    //实现onDestroy()方法，在Activity结束时结束广播接收器服务，并注销接受者：
    //实际上，服务的生命周期可以独立于Activity，
    // 如果不实现该方法，Activity结束后，在任务管理器中还可以看到该服务还在后台运行。
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, RefreshService.class));
        unregisterReceiver(broadcastReceiver);

    }
}
