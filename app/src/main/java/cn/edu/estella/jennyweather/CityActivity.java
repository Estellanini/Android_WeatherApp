package cn.edu.estella.jennyweather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CityActivity extends Activity {
    private ListView listView;
    private Button addButton;
    private String[] cities = {"北京 朝阳", "江苏 宿迁", "江苏 南京", "江苏 徐州", "辽宁 朝阳"};
    private List<Map<String, Object>> listems = new ArrayList<Map<String, Object>>();


    private MyAdapter myAdapter;//新的适配器
    private String[] areaIds = {"1","1","1","1","1"};//城市代码（目前还没有任何意义）
    private Button deleteButton;//删除按钮


    private DBHelper dbHelper;//数据库工具
    private String[] name, ids;//存储数据库中城市名称和代码的两个数组

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_layout);

        for (int i = 0; i < cities.length; i++) {
            Map<String, Object> listem = new HashMap<String, Object>();
            listem.put("name", cities[i]);
            listems.add(listem);
        }
        listView = (ListView) findViewById(R.id.city_listview);
        listView.setAdapter(new SimpleAdapter(getApplication(), listems,
                R.layout.activity_search_listview_item, new String[]{"name"},
                new int[]{R.id.result_text}));


        /*
        * 在onCreate()中为listView添加单击事件，将用户点击的城市名称传回上一个Activity:
        *这里通过getIntent()方法获取传来的意图；通过getExtras()获得意图中的数据；
        * 用setResult()方法回退，第一个参数为结果码；用finish()函数结束自己。
        * */

        //数据库部分修改这段代码了
        //最后修改onCreate()中的listview的点击事件，将城市名称传回：
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = getIntent();
                Bundle bundle = intent.getExtras();
                bundle.putString("cityname", name[position]);
                intent.putExtras(bundle);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

        /*
        * 在onCreate()中初始化addButton，并为之添加单击事件，代码与之前类似：
        * 这里请求码我们设置为0。
        * */
        addButton = (Button) findViewById(R.id.add_city_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CityActivity.this, SearchActivity.class);
                Bundle bundle = new Bundle();
                intent.putExtras(bundle);
                startActivityForResult(intent, 0);
            }


        });


        //在onCreate()中初始化：删除按钮、新的适配器
        //这里我们舍弃之前的适配器代码，用新的适配器，
        // 为删除按钮添加点击事件，输出打印已选择的城市数量。
        deleteButton = (Button)findViewById(R.id.delete_city_button);
        myAdapter = new MyAdapter(cities, areaIds, deleteButton);
        listView.setAdapter(myAdapter);

        //数据库部分修改这段代码了
        //这里根据城市代码将相应的记录从数据库中删除，然后更新listView。
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] result = myAdapter.getCheckedCities();
                for(int i=0; i<result.length; i++){
                    dbHelper.deleteDataById(Integer.parseInt(result[i]));
                }
                changeAdapter(dbHelper.queryAllCities());
            }
        });


        dbHelper = DBHelper.getInstance(this);
        changeAdapter(dbHelper.queryAllCities());




    }


    /*
    * 我们也要实现onActivityResult()：
    * 将获得的数据用Toast打印输出。
    * */

    //数据库部分修改这段代码了
    //这里将传回来的城市名和代码存入数据库中，然后更新listview。
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            Bundle bundle = data.getExtras();
            dbHelper.insertData(new String[]{"cityname"},
                    new String[]{bundle.getString("city")});
            changeAdapter(dbHelper.queryAllCities());
        }
    }

    //数据库部分
    //首先是实例化数据库工具，然后是根据数据库中的数据为ListView设置适配器，
    // 其中changeAdapter()方法如下定义：
    private void changeAdapter(List<Map<String, Object>> list){
        ids = new String[list.size()];
        name = new String[list.size()];
        for(int i=0;i<list.size();i++){
            ids[i] = list.get(i).get("id").toString();
            name[i] = list.get(i).get("cityname").toString();
        }
        myAdapter = new MyAdapter(name, ids, deleteButton);
        listView.setAdapter(myAdapter);
    }

    //新建内部类MyAdater，继承自BaseAdapter
    private class MyAdapter extends BaseAdapter {

        //定义该类的内部属性和构造器
        //可以看到该类内部也有一个类ViewSet，
        // 该类只有两个属性，分别代表列表每一项的文字部分和复选框部分。
        private class ViewSet {
            TextView textView;
            CheckBox checkBox;
        }

        private String[] cities;//表示要传入的城市名称
        private int checkedNum = 0;//表示已经勾选的城市数量。
        private Button button;//Button表示要传入的删除城市按钮。
        private boolean[] checkedArray;//CheckedArray存储了每个城市是否被选择的状态。
        private String[] cityIds;//表示要传入的城市ID

        public MyAdapter(String[] cities, String[] cityIds, Button button) {
            this.cities = cities;
            this.button = button;
            this.cityIds = cityIds;
            this.checkedArray = new boolean[cities.length];
            for (int i = 0; i < cities.length; i++) {
                this.checkedArray[i] = false;
            }
        }


        //getCount()返回该适配器中一共有几个条目；
        @Override
        public int getCount() {
            return cities.length;
        }

        //getItem()根据位置返回该位置的内容（具体是什么可自定义）
        @Override
        public Object getItem(int position) {
            return position;
        }

        //getItemId()返回每个条目的Id，一般都返回位置索引
        @Override
        public long getItemId(int position) {
            return position;
        }

        //getView()则返回条目的样式，这是我们重点要实现的地方。
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            //convertView相当于一个缓存，开始为0，当有条目变为不可见，它缓存了它的数据，
            // 后面再出来的条目只需要更新数据就可以了，这样大大节省了系统资料的开销。
            ViewSet viewSet = null;
            if (convertView == null) {
                viewSet = new ViewSet();
                convertView = LayoutInflater.from(getApplication()).inflate(R.layout.activity_city_listview_item, null);
                viewSet.textView = (TextView) convertView.findViewById(R.id.listview_item_textview);
                viewSet.checkBox = (CheckBox) convertView.findViewById(R.id.listview_item_checkbox);
                //当convertView为空时，用setTag()方法为每个View绑定一个存放控件的ViewSet对象。
                convertView.setTag(viewSet);
            } else {
                //当convertView不为空，重复利用已经创建的view的时候，使用getTag()方法获取绑定的VeiwSet对象，
                // 这样就避免了findViewById对控件的层层查询，而是快速定位到控件。
                viewSet = (ViewSet) convertView.getTag();
            }
            viewSet.textView.setText(cities[position]);
            viewSet.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                //这里还为复选框添加了状态改变监听器，使得它被选择状态改变时，
                // 更新checkedNum和checkedArray，并改变删除按钮的状态。
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        checkedArray[position] = true;
                        checkedNum++;
                        if (checkedNum == 1) {
                            button.setEnabled(true);
                        }
                    } else {
                        checkedArray[position] = false;
                        checkedNum--;
                        if (checkedNum == 0) {
                            button.setEnabled(false);
                        }
                    }
                }
            });
            return convertView;
        }


        //返回选择的城市代码
        public String[] getCheckedCities(){
            List<String> checkedCityIdList = new ArrayList<String>();
            for(int i=0;i<checkedArray.length;i++){
                if(checkedArray[i] == true){
                    checkedCityIdList.add(cityIds[i]);
                }
            }
            String[] checkedCityIdArray = new String[checkedCityIdList.size()];
            return checkedCityIdList.toArray(checkedCityIdArray);
        }
    }
}