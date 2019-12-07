package cn.edu.estella.jennyweather;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchActivity extends Activity {
    private ListView listView;
    private Button button;
    private EditText editText;
    private String[] cities = {"北京 朝阳","江苏 宿迁","江苏 南京","江苏 徐州","辽宁 朝阳"};
    private List<Map<String, Object>> listems = new ArrayList<Map<String, Object>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_layout);

        listView = (ListView)findViewById(R.id.result_list);
        button = (Button)findViewById(R.id.search_button);
        editText = (EditText)findViewById(R.id.search_text);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listems.clear();
                for(int i=0;i<cities.length;i++){
                    if(cities[i].contains(editText.getText().toString())){
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("name", cities[i]);
                        listems.add(map);
                    }
                }
                listView.setAdapter(new SimpleAdapter(getApplication(), listems,
                        R.layout.activity_search_listview_item, new String[]{"name"},
                        new int[]{R.id.result_text}));
            }
        });
    }
}