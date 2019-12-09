package cn.edu.estella.jennyweather;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.util.Timer;
import java.util.TimerTask;

public class RefreshService extends Service {
    Timer timer;
    TimerTask timerTask;



    /*
    * onCreate()方法在服务被开启时调用，只调用一次，这里适合做一些初始化工作，
    * 这里我们定义了timer，用于定时触发timerTask，timerTask中定义了该服务向外发送广播，
    * 以通知MainActivity更新天气信息。sendBroadcast()方法发送了动作为“com.weather.refresh”的广播。
    *
    * */
    @Override
    public void onCreate() {
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                Intent intent = new Intent();
                intent.setAction("com.weather.refresh");
                sendBroadcast(intent);
            }
        };
        super.onCreate();
    }


    //onStartCommand()方法在之后调用，它可以被调用很多次（如果一个服务被多次启动），
    // 这里我们让timer延迟0秒，每隔20秒发送一次广播。
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        timer.schedule(timerTask, 0, 20000);
        return super.onStartCommand(intent, flags, startId);
    }

    //OnDestroy()方法在服务被停止时调用，这里我们销毁timer。
    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        timer = null;
    }


    //这里必须实现onBind()方法，返回null表示该服务不与Activity绑定。
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}