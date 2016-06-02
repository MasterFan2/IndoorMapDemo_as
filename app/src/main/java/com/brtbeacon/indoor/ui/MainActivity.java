package com.brtbeacon.indoor.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.brtbeacon.indoor.R;
import com.brtbeacon.indoor.bean.Menu;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<Menu> menuList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        Intent intentBase = new Intent(MainActivity.this, BaseMapActivity.class);
                        startActivity(intentBase);
                        break;
                    case 1:
                        Intent intentPop = new Intent(MainActivity.this, PopviewActivity.class);
                        startActivity(intentPop);
                        break;
                    case 2:
                        Intent intentNav = new Intent(MainActivity.this, NavActivity.class);
                        startActivity(intentNav);
                        break;
                }
            }
        });

        menuList.add(new Menu("显示地图", "一个基础地图的展示"));
        menuList.add(new Menu("地图覆盖物", "在地图上弹出框"));
        menuList.add(new Menu("路径规划", "在地图上显示导航路径"));

        listView.setAdapter(new MyAdapter());
    }

    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return menuList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Menu menu = menuList.get(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.two_line_layout, parent, false);
            }
            TextView text1 = (TextView)  convertView.findViewById(R.id.text1);
            TextView text2 = (TextView) convertView.findViewById(R.id.text2);

            text1.setText(menu.getTitle());
            text2.setText(menu.getDesc());

            return convertView;
        }
    }
}
