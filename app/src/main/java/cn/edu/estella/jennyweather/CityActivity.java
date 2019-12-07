package cn.edu.estella.jennyweather;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CityActivity extends Activity {
    private ListView listView;
    private String[] cities = {"北京 朝阳","江苏 宿迁","江苏 南京","江苏 徐州","辽宁 朝阳"};
    private List<Map<String, Object>> listems = new ArrayList<Map<String, Object>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_layout);

        for(int i=0;i<cities.length;i++){
            Map<String, Object> listem = new HashMap<String, Object>();
            listem.put("name", cities[i]);
            listems.add(listem);
        }
        listView = (ListView)findViewById(R.id.city_listview);
        listView.setAdapter(new SimpleAdapter(getApplication(), listems,
                R.layout.activity_search_listview_item, new String[]{"name"},
                new int[]{R.id.result_text}));
    }
}