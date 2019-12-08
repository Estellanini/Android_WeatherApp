package cn.edu.estella.jennyweather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CityActivity extends Activity {
    private ListView listView;
    private Button addButton;
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

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = getIntent();
                Bundle bundle = intent.getExtras();
                bundle.putString("city", cities[position]);
                intent.putExtras(bundle);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

        addButton = (Button)findViewById(R.id.add_city_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CityActivity.this, SearchActivity.class);
                Bundle bundle = new Bundle();
                intent.putExtras(bundle);
                startActivityForResult(intent, 0);
            }


        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            Bundle bundle = data.getExtras();
            Toast.makeText(this, bundle.getString("city"), Toast.LENGTH_LONG).show();
        }
    }
}