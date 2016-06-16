图呀地图api开发指南
一、简介
室内定位开发包是基于ArcGIS框架和GEOS几何计算开源库，为开发者提供了的室内地图显	示、路径规划、室内定位等相关GIS功能。
二、准备工作
本开发包支持的Android版本为18或更高
a)新建android工程，将下载好的jar包和动态库(*.so)复制到项目的libs文件夹中,AndroidStudio需要在build.gradle中指定so库的位置信息,如下：
android {
sourceSets {
main {
jniLibs.srcDir([‘libs’])
}
}
}

b)复制地图数据到工程的assets文件下

c)复制地图中用的到资源图片到res下的drawable-hdpi文件夹下

三、基础地图展示
i.新建actvity类，并加支持库
static {
    	System.loadLibrary("TYMapSDK");
	    }

ii.在Activity的布局文件中添加地图控件
	<com.ty.mapsdk.TYMapView
    	android:id="@+id/map"
    	android:layout_width="match_parent"
    	android:layout_height="match_parent" />

iii.初始化mapView,并按步骤初始化地图[代码详见demo]
	//1.设置地图数据保存在SD卡的位置
	//2.copy测试地图数据到SD卡
	//3.获得城市数据、建筑数据
	//4.初始化地图并显示
	//显示地图需要用到城市数据、建筑信息，并传人授权OpenId和License以验证地图使用权限
	MapView.init(TYBuilding building, String userID, String license)
	//参数building:建筑信息
	//参数userId:开发者账号
	//参数license:地图使用授权
	申请userId和license地址入口http://map.brtbeacon.com/



iv.以上工作完成后，地图就可以正常显示地图了


四、展示弹窗
i.复制弹窗样式配置文件callout_style.xml到res文件夹下的xml文件夹
<resources>
	<calloutViewStyle
   	backgroundColor="#ffffff"//弹窗背景颜色
   	backgroundAlpha="255"    //弹窗背景透明度
   	frameColor="#66FFCC"     //弹窗边框颜色
   	cornerCurve="10"         //弹窗边框拐角度数
   	anchor="5" />  //弹窗锚 指的方向 【1：上，2:右上，3：右，4：右下, ....】
</resources>

ii.Activity实现TYMapView.TYMapViewListener接口[代码详见demo]
@Override
public void onClickAtPoint(TYMapView tyMapView, Point point) {
//点击地图上的点回调方法
}
@Override
public void onPoiSelected(TYMapView tyMapView, List<TYPoi> list) {
//选择poi回调方法
popview.setStyle(R.xml.callout_style);
if (popview != null && popview.isShowing()) {
    popview.hide();
}
if (list != null && list.size() > 0) {
    TYPoi poi = list.get(0);

    Point position;
    if (poi.getGeometry().getClass() == Polygon.class) {
        position = GeometryEngine.getLabelPointForPolygon((Polygon)
                poi.getGeometry(),TYMapEnvironment.defaultSpatialReference());
    } else {
        position = (Point) poi.getGeometry();
    }

    String title = poi.getName();
    String detail = poi.getPoiID();

    //设置弹出view的大小
    popview.setMaxWidth(dip2px(PoiActivity.this, 160));
    popview.setMaxHeight(dip2px(PoiActivity.this, 120));

    popview.setContent(poiView(title, detail));
    popview.show(position);//显示弹窗
}

}
@Override
public void onFinishLoadingFloor(TYMapView tyMapView, TYMapInfo tyMapInfo) {
//楼层加载完毕回调方法
}
@Override
public void mapViewDidZoomed(TYMapView tyMapView) {
//地图缩放回调方法
}

iii.显示效果


五、线路规划
i.设置路径起点
startPoint = tempPoint;
mapView.setStartSymbol(null);
pt1 = new TYLocalPoint(startPoint.getX(), startPoint.getY(), currentMapInfo.getFloorNumber());
TYPictureMarkerSymbol start = new TYPictureMarkerSymbol(getResources().getDrawable(R.drawable.qidian));
start.setWidth(dip2px(PoiActivity.this, 12)); //设置起点图标的宽、高
start.setHeight(dip2px(PoiActivity.this, 16));

mapView.setRouteStart(pt1);  //设置起点 位置
mapView.setStartSymbol(start);//设置起点位置的图标

mapView.showRouteStartSymbolOnCurrentFloor(pt1);//显示起点图标在当前楼层地图上

popview.hide();

if (endPoint != null && pt2 != null) {
    routeManager.requestRoute(pt1, pt2);//请求线路
}

ii.设置路径终点
endPoint = tempPoint;
mapView.setEndSymbol(null);
if (startPoint == null) {
    Toast.makeText(PoiActivity.this, "请选择起点", Toast.LENGTH_SHORT).show();
    return;
}

pt2 = new TYLocalPoint(endPoint.getX(), endPoint.getY(), currentMapInfo.getFloorNumber());

if (pt1.getFloor() != pt2.getFloor()) {
    TYPictureMarkerSymbol pic_floor = new TYPictureMarkerSymbol(getResources().getDrawable(R.drawable.green_pushpin));
    pic_floor.setWidth(dip2px(PoiActivity.this, 12));
    pic_floor.setHeight(dip2px(PoiActivity.this, 12));
    mapView.setSwitchSymbol(pic_floor);
}
//
routeManager.requestRoute(pt1, pt2);
TYPictureMarkerSymbol pic = new TYPictureMarkerSymbol(getResources().getDrawable(R.drawable.zhongdian));
pic.setWidth(dip2px(PoiActivity.this, 12));
pic.setHeight(dip2px(PoiActivity.this, 16));
mapView.setRouteEnd(pt2);
mapView.setEndSymbol(pic);

mapView.showRouteEndSymbolOnCurrentFloor(pt2);//显示终点图标在当前楼层地图上
popview.hide();


iii.实现接口TYOfflineRouteManager.TYOfflineRouteManagerListener[代码详见demo]
@Override
public void didSolveRouteWithResult(TYOfflineRouteManager tyOfflineRouteManager, TYRouteResult tyRouteResult) {
   //设置线路
mapView.setRouteResult(tyRouteResult);//设置线路
mapView.showRouteResultOnCurrentFloor();//地图上显示线路

}
@Override
public void didFailSolveRouteWithError(TYOfflineRouteManager tyOfflineRouteManager, Exception e) {
	//线路规划错误
}
iv.显示效果


六、室内定位
实现定位导航之前，请先配置好导航的beacon信息，配置端的下载地址为
http://fir.im/cfg

在AndroidManifest.xml中配置相关权限和服务
//添加定位服务
	<service android:name="com.ty.locationengine.ibeacon.BeaconService" />
//添加蓝牙权限
	<uses-permission android:name="android.permission.BLUETOOTH" />
	<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />

i.添加定位支持库
	System.loadLibrary("TYLocationEngine");

ii.初始化定位引擎
//locationManager
String uuid = "FDA50693-A4E2-4FB1-AFCF-C6EB07647825", major = "10046";
// 初始化定位引擎
locationManager = new TYLocationManager(this, currentBuilding);

// 设置Beacon定位参数，并传递给定位引擎
if (uuid != null && major != null) {
    locationManager.setBeaconRegion(new BeaconRegion("TuYa", uuid, Integer.parseInt(major), null));
} else {
    locationManager.setBeaconRegion(new BeaconRegion("TuYa", null, null, null));
}

// 添加回调监听
locationManager.addLocationEngineListener(this);

iii.实现接口TYLocationManager.TYLocationManagerListener接口[代码详见demo]
@Override
public void didRangedBeacons(TYLocationManager tyLocationManager, List<TYBeacon> list) {
	     //Beacon扫描结果事件回调，返回符合扫描参数的所有Beacon
}
@Override
public void didRangedLocationBeacons(TYLocationManager tyLocationManager, List<TYPublicBeacon> list) {
	     //定位Beacon扫描结果事件回调，返回符合扫描参数的定位Beacon，定位Beacon包含坐标信息
}
@Override
public void didUpdateLocation(TYLocationManager tyLocationManager, TYLocalPoint tyLocalPoint) {
	     //位置更新事件回调，位置更新并返回新的位置结果
if (mapView.getCurrentMapInfo().getFloorNumber() != tyLocalPoint.getFloor()) {
    TYMapInfo targetMapInfo = TYMapInfo.searchMapInfoFromArray(
            allMapInfos, tyLocalPoint.getFloor());
    mapView.setFloor(targetMapInfo);
}
// 在地图当前楼层上显示定位结果
mapView.showLocation(tyLocalPoint);

}
@Override
public void didFailUpdateLocation(TYLocationManager tyLocationManager) {
    //更新位置错误
}
@Override
public void didUpdateDeviceHeading(TYLocationManager tyLocationManager, double v) {
	    //设备方向改变事件回调}
iv.显示效果

七、联系
有任何问题请联系QQ:00000000