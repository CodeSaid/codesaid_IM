package com.im.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.codesaid.lib_framework.base.BaseBackActivity;
import com.codesaid.lib_framework.entity.Constants;
import com.im.R;

/**
 * Created By codesaid
 * On :2020-01-27
 * Package Name: com.im.ui
 * desc : 地图定位
 */
public class LocationActivity extends BaseBackActivity implements View.OnClickListener {

    private AMap mAMap;
    private boolean isShow;

    /**
     * 跳转到当前 Activity
     *
     * @param context
     * @param isShow
     * @param la
     * @param lo
     * @param address
     * @param requestCode
     */
    public static void startActivity(Activity context, boolean isShow,
                                     double la, double lo, String address, int requestCode) {
        Intent intent = new Intent(context, LocationActivity.class);
        intent.putExtra(Constants.INTENT_MAP_SHOW, isShow);
        intent.putExtra("la", la);
        intent.putExtra("lo", lo);
        intent.putExtra("address", address);
        context.startActivityForResult(intent, requestCode);
    }

    private MapView mMapView;
    private EditText et_search;
    private ImageView iv_poi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        initView(savedInstanceState);
    }

    private void initView(Bundle savedInstanceState) {
        mMapView = findViewById(R.id.mMapView);
        et_search = findViewById(R.id.et_search);
        iv_poi = findViewById(R.id.iv_poi);

        iv_poi.setOnClickListener(this);

        // 初始化地图
        mMapView.onCreate(savedInstanceState);

        if (mAMap == null) {
            mAMap = mMapView.getMap();
        }

        MyLocationStyle myLocationStyle = new MyLocationStyle();
        // 连续定位
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);
        myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        mAMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        mAMap.getUiSettings().setMyLocationButtonEnabled(true);
        mAMap.setMyLocationEnabled(true);
        // 缩放
        mAMap.moveCamera(CameraUpdateFactory.zoomTo(18));

        Intent intent = getIntent();
        isShow = intent.getBooleanExtra(Constants.INTENT_MAP_SHOW, false);
        if (isShow) {
            // 如果不显示，则作为展示类地图，接受外界传递的地址显示
            double la = intent.getDoubleExtra("la", 0);
            double lo = intent.getDoubleExtra("lo", 0);
            String address = intent.getStringExtra("address");

            mAMap.setMyLocationEnabled(false);
            supportInvalidateOptionsMenu();
            // 显示位置
            LatLng latLng = new LatLng(la, lo);
            // 清除当前位置
            mAMap.clear();
            mAMap.addMarker(new MarkerOptions().position(latLng).title("位置").snippet(address));
        } else {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 销毁地图
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 重新绘制加载地图
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 暂停地图的绘制
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // 保存当前地图的绘制
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isShow) {
            getMenuInflater().inflate(R.menu.location_menu, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_poi:

                break;
        }
    }
}
