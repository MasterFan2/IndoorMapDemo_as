package com.brtbeacon.indoor.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.brtbeacon.indoor.R;
import com.brtbeacon.indoor.util.FileHelper;
import com.esri.android.map.Callout;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.ty.mapdata.TYBuilding;
import com.ty.mapdata.TYCity;
import com.ty.mapsdk.TYBuildingManager;
import com.ty.mapsdk.TYCityManager;
import com.ty.mapsdk.TYMapEnvironment;
import com.ty.mapsdk.TYMapInfo;
import com.ty.mapsdk.TYMapView;
import com.ty.mapsdk.TYPoi;

import java.io.File;
import java.util.List;

public class PopviewActivity extends AppCompatActivity implements TYMapView.TYMapViewListenser {

    static {
        System.loadLibrary("TYMapSDK");
        System.loadLibrary("TYLocationEngine");
    }

    private TYMapView mapView;

    private final String CITY_ID = "0021";
    private final String BUILD_ID= "00210018";

    private String mapRootDir;

    protected TYCity currentCity;
    protected TYBuilding currentBuilding;
    protected TYMapInfo currentMapInfo;
    protected List<TYMapInfo> allMapInfos;

    protected int currentFloor = 0;

    private Point tempPoint;
    private Callout popview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popview);

        mapView = (TYMapView) findViewById(R.id.map);
        mapView.addMapListener(this);

        init();
    }

    private void init(){

        //1.设置地图数据保存在SD卡的位置
        TYMapEnvironment.initMapEnvironment();
        mapRootDir = Environment.getExternalStorageDirectory() + "/MapDemo/MapFiles";
        TYMapEnvironment.setRootDirectoryForMapFiles(mapRootDir);

        //2.copy测试地图数据到SD卡
        if(isFirst()){
            copyMapFiles();
        }

        //3.获得城市数据、建筑数据
        //获得城市信息
        currentCity = TYCityManager.parseCityFromFilesById(this, CITY_ID);

        //获得建筑信息
        currentBuilding = TYBuildingManager.parseBuildingFromFilesById(this, CITY_ID, BUILD_ID);

        //获得建筑的所有信息列表
        try{
            allMapInfos = TYMapInfo.parseMapInfoFromFiles(this, CITY_ID, BUILD_ID);
        }catch (Exception e){
            Toast.makeText(PopviewActivity.this, "暂无定位", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return;
        }
        currentMapInfo = allMapInfos.get(currentFloor);

        //4.初始化地图并显示
        mapView.init(currentBuilding, "ty4e13f85911a44a75", "26db2af1g0772n53`dd9`666101ec55a");
        mapView.setFloor(currentMapInfo);

        popview = mapView.getCallout();
    }

    private boolean isFirst(){
        SharedPreferences settings = getSharedPreferences("first", 0);
        return settings.getBoolean("first", true);
    }

    void copyMapFiles() {
        String sourcePath = "MapResource";
        String targetPath = TYMapEnvironment.getRootDirectoryForMapFiles();
        FileHelper.deleteFile(new File(targetPath));
        FileHelper.copyFolderFromAsset(this, sourcePath, targetPath);
    }

    @Override
    public void onClickAtPoint(TYMapView tyMapView, Point point) {
        tempPoint = point;
    }

    @Override
    public void onPoiSelected(TYMapView tyMapView, List<TYPoi> list) {
        popview.setStyle(R.xml.callout_style);
        if (popview != null && popview.isShowing()) {
            popview.hide();
        }
        if (list != null && list.size() > 0) {
            TYPoi poi = list.get(0);

            Point position;
            if (poi.getGeometry().getClass() == Polygon.class) {
                position = GeometryEngine.getLabelPointForPolygon((Polygon) poi.getGeometry(),TYMapEnvironment.defaultSpatialReference());
            } else {
                position = (Point) poi.getGeometry();
            }

            String title = poi.getName();
            String detail = poi.getPoiID();

            popview.setMaxWidth(dip2px(PopviewActivity.this, 160));
            popview.setMaxHeight(dip2px(PopviewActivity.this, 120));

            popview.setContent(poiView(title, detail));
            popview.show(position);
        }
    }

    @Override
    public void onFinishLoadingFloor(TYMapView tyMapView, TYMapInfo tyMapInfo) {

    }

    @Override
    public void mapViewDidZoomed(TYMapView tyMapView) {

    }

    /**
     * @param title
     * @return
     */
    public View poiView(String title, String content) {
        View view = getLayoutInflater().inflate(R.layout.pop_layout, null);
        TextView titleText = (TextView) view.findViewById(R.id.poi_title_txt);
        TextView contentText = (TextView) view.findViewById(R.id.poi_content_txt);
        Button startBtn = (Button) view.findViewById(R.id.poi_start_btn);
        Button endBtn = (Button) view.findViewById(R.id.poi_end_btn);

        titleText.setText(title);
        contentText.setText(content);

        return view;
    }
    public int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
