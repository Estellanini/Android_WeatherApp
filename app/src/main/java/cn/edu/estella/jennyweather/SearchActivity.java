package cn.edu.estella.jennyweather;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchActivity extends Activity {
    private ListView listView;
    private Button button;
    private EditText editText;
    private String[] cities = {"北京 朝阳","江苏 宿迁","江苏 南京","江苏 徐州","辽宁 朝阳"};
    private List<Map<String, String>> listems = new ArrayList<Map<String, String>>();
    private String url = "http://api.jisuapi.com/weather/city?appkey=fd2507b34c72c646";

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
                String input_text = editText.getText().toString();
                new Getcity().execute(url, input_text);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = getIntent();
                Bundle bundle = intent.getExtras();
                bundle.putString("city", listems.get(position).get("name").toString());
                intent.putExtras(bundle);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }


    public List<Map<String, String>> parseCities(String json, String input_text) {
        Map<String, String> map;
        try {
            JSONObject obj1 = new JSONObject(json);
            JSONArray array = obj1.getJSONArray("result");
            String cityName = input_text;
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj2 = array.getJSONObject(i);
                String parentid = obj2.getString("parentid");
                if (obj2.getString("city").trim().contains (cityName)) {
                    cityName = obj2.getString("city").trim();
                    map = new HashMap<String, String>();
                    if (parentid.equals("0")) {
                        map.put("name", cityName);
                        listems.add(map);
                        break;
                    } else {
                        for (int j = 0; j < array.length(); j++) {
                            JSONObject obj3 = array.getJSONObject(j);
                            if (obj3.getString("cityid").equals(parentid)) {
                                parentid = obj3.getString("parentid");
                                cityName = obj3.getString("city") + " " + cityName;
                                if (parentid.equals("0")) {
                                    map.put("name", cityName);
                                    listems.add(map);
                                    break;
                                }
                                else {
                                    for (int k = 0; k < array.length(); k++) {
                                        JSONObject obj4 = array.getJSONObject(k);
                                        if (obj4.getString("cityid").equals(parentid)) {
                                            cityName = obj4.getString("city") + " " + cityName;
                                            map.put("name", cityName);
                                            listems.add(map);
                                            break;
                                        } else {
                                            continue;
                                        }
                                    }
                                }
                            } else {
                                continue;
                            }
                        }
                    }
                } else {
                    continue;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return listems;
    }




    private class Getcity extends AsyncTask<String, String, String> {
        private String input_text = "";







        @Override
        protected String doInBackground(String... params) {
            input_text = params[1];
            return openConnect(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {


            try {
                listems.clear();
                listems = parseCities(result, input_text);
                listView.setAdapter(new SimpleAdapter(getApplication(), listems,
                        R.layout.activity_search_listview_item, new String[]{"name"},
                        new int[]{R.id.result_text}));
            } catch (Exception e) {
                e.printStackTrace();
            }


        }



    }

    private String openConnect(String address) {
        String result = "";
        try {
            URL url = new URL(address);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            InputStream in = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line = "";
            while ((line = reader.readLine()) != null) {
                result = result + line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }




}