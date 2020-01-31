package com.im.ui;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.codesaid.lib_framework.adapter.CommonAdapter;
import com.codesaid.lib_framework.adapter.CommonViewHolder;
import com.codesaid.lib_framework.base.BaseBackActivity;
import com.codesaid.lib_framework.entity.Constants;
import com.codesaid.lib_framework.map.MapManager;
import com.codesaid.lib_framework.view.DialogManager;
import com.codesaid.lib_framework.view.DialogView;
import com.codesaid.lib_framework.view.LoadingView;
import com.im.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created By codesaid
 * On :2020-01-27
 * Package Name: com.im.ui
 * desc : 地图定位
 */
public class LocationActivity extends BaseBackActivity implements View.OnClickListener, PoiSearch.OnPoiSearchListener {

    private AMap mAMap;
    private boolean isShow;
    private DialogView mPoiView;
    private PoiSearch mPoiSearch;
    private PoiSearch.Query mQuery;
    private LoadingView mLoadingView;

    private double mLa;
    private double mLo;
    private String mAddress;

    private int ITEM = -1;

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

    private RecyclerView mConstellationnView;
    private TextView tv_cancel;

    private CommonAdapter<PoiItem> mAdapter;
    private List<PoiItem> mList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        initPoiView();
        initView(savedInstanceState);
    }

    private void initPoiView() {

        mLoadingView = new LoadingView(LocationActivity.this);
        mLoadingView.setLoadingText("正在搜索中...");

        mPoiView = DialogManager.getInstance()
                .initView(LocationActivity.this, R.layout.dialog_select_constellation, Gravity.BOTTOM);
        mPoiView.setCancelable(false);
        mConstellationnView = mPoiView.findViewById(R.id.mConstellationnView);
        tv_cancel = mPoiView.findViewById(R.id.tv_cancel);

        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogManager.getInstance().hide(mPoiView);
            }
        });

        mConstellationnView.setLayoutManager(new LinearLayoutManager(LocationActivity.this));
        mConstellationnView.addItemDecoration(
                new DividerItemDecoration(LocationActivity.this, DividerItemDecoration.VERTICAL));

        mAdapter = new CommonAdapter<>(mList, new CommonAdapter.onBindDataListener<PoiItem>() {
            @Override
            public void onBindViewHolder(final PoiItem model, CommonViewHolder holder, int type, int position) {

                ITEM = position;

                holder.setText(R.id.tv_age_text, model.toString());

                // 点击跳转到对应位置
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // 先隐藏 Dialog
                        DialogManager.getInstance().hide(mPoiView);

                        MapManager.getInstance()
                                .address2poi(model.toString())
                                .setOnGeocodeListener(new MapManager.onGeocodeListener() {
                                    @Override
                                    public void poi2address(String address) {

                                    }

                                    @Override
                                    public void address2poi(double la, double lo, String address) {
                                        mLa = la;
                                        mLo = lo;
                                        mAddress = address;

                                        updatePoi(mLa, mLo, mAddress);
                                    }
                                });
                    }
                });
            }

            @Override
            public int getLayoutId(int type) {
                return R.layout.layout_me_age_item;
            }
        });

        mConstellationnView.setAdapter(mAdapter);
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

            updatePoi(la, lo, address);
        } else {

        }

        mAMap.setOnMyLocationChangeListener(new AMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {

            }
        });
    }

    /**
     * 更新位置
     *
     * @param la      经度
     * @param lo      纬度
     * @param address 地址
     */
    private void updatePoi(double la, double lo, String address) {
        mAMap.setMyLocationEnabled(false);
        supportInvalidateOptionsMenu();
        // 显示位置
        LatLng latLng = new LatLng(la, lo);
        // 清除当前位置
        mAMap.clear();
        mAMap.addMarker(new MarkerOptions().position(latLng).title("位置").snippet(address));
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

        if (item.getItemId() == R.id.menu_send) {
            Intent intent = new Intent();

            if (ITEM > 0) {
                // 直接点击
                intent.putExtra("la", mLa);
                intent.putExtra("lo", mLo);
                intent.putExtra("address", mAddress);
            } else {
                // 直接点击
                intent.putExtra("la", mAMap.getMyLocation().getLatitude());
                intent.putExtra("lo", mAMap.getMyLocation().getLongitude());
                intent.putExtra("address", mAMap.getMyLocation().getExtras().getString("desc"));
            }
            setResult(Activity.RESULT_OK, intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_poi:
                String keyWord = et_search.getText().toString().trim();
                if (TextUtils.isEmpty(keyWord)) {
                    return;
                }
                poiSearch(keyWord);
                break;
        }
    }

    /**
     * 关键字搜索
     *
     * @param keyWord 关键字
     */
    private void poiSearch(String keyWord) {
        mLoadingView.show();
        //第一个参数表示搜索字符串，
        //第二个参数表示POI搜索类型，二者选填其一，选用POI搜索类型时建议填写类型代码，码表可以参考下方（而非文字）
        //第三个参数表示POI搜索区域，可以是城市编码也可以是城市名称，也可以传空字符串，空字符串代表全国在全国范围内进行搜索
        mQuery = new PoiSearch.Query(keyWord, "", "");
        // 设置每页最多返回多少条
        mQuery.setPageSize(7);
        //设置查询页码
        mQuery.setPageNum(1);

        // 构造 PoiSearch 对象，并设置监听
        mPoiSearch = new PoiSearch(LocationActivity.this, mQuery);
        mPoiSearch.setOnPoiSearchListener(this);
        // 发送请求
        mPoiSearch.searchPOIAsyn();
    }

    /**
     * 得到搜索的结果
     * 1）可以在回调中解析result，获取POI信息。
     * <p>
     * 2）result.getPois()可以获取到PoiItem列表，Poi详细信息可参考PoiItem类。
     * <p>
     * 3）若当前城市查询不到所需POI信息，可以通过result.getSearchSuggestionCitys()获取当前Poi搜索的建议城市。
     * <p>
     * 4）如果搜索关键字明显为误输入，则可通过result.getSearchSuggestionKeywords()方法得到搜索关键词建议。
     * <p>
     * 5）返回结果成功或者失败的响应码。1000为成功，其他为失败
     */
    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {
        mLoadingView.hide();
        if (mList.size() > 0) {
            mList.clear();
        }
        //解析result获取POI信息
        mList.addAll(poiResult.getPois());
        mAdapter.notifyDataSetChanged();
        DialogManager.getInstance().show(mPoiView);
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }
}
