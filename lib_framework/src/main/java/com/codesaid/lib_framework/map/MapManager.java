package com.codesaid.lib_framework.map;

import android.content.Context;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;

/**
 * Created By codesaid
 * On :2020-01-28
 * Package Name: com.codesaid.lib_framework.map
 * desc : 地图管理类
 */
public class MapManager {
    private static volatile MapManager mInstance = null;
    private GeocodeSearch mGeocodeSearch;

    private onGeocodeListener mOnGeocodeListener;

    public void setOnGeocodeListener(onGeocodeListener onGeocodeListener) {
        mOnGeocodeListener = onGeocodeListener;
    }

    private MapManager() {

    }

    public static MapManager getInstance() {
        if (mInstance == null) {
            synchronized (MapManager.class) {
                if (mInstance == null) {
                    mInstance = new MapManager();
                }
            }
        }
        return mInstance;
    }

    public void initMap(Context context) {
        mGeocodeSearch = new GeocodeSearch(context);
        mGeocodeSearch.setOnGeocodeSearchListener(listener);
    }

    private GeocodeSearch.OnGeocodeSearchListener listener = new GeocodeSearch.OnGeocodeSearchListener() {
        @Override
        public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
            if (i == AMapException.CODE_AMAP_SUCCESS) {
                if (regeocodeResult != null) {
                    if (mOnGeocodeListener != null) {
                        mOnGeocodeListener.poi2address(regeocodeResult.getRegeocodeAddress()
                                .getFormatAddress());
                    }
                }
            }
        }

        @Override
        public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
            if (i == AMapException.CODE_AMAP_SUCCESS) {
                if (geocodeResult != null) {
                    if (mOnGeocodeListener != null) {
                        if (geocodeResult.getGeocodeAddressList() != null &&
                                geocodeResult.getGeocodeAddressList().size() > 0) {
                            GeocodeAddress address = geocodeResult.getGeocodeAddressList().get(0);
                            mOnGeocodeListener.address2poi(
                                    address.getLatLonPoint().getLatitude(),
                                    address.getLatLonPoint().getLongitude(),
                                    address.getFormatAddress()
                            );
                        }
                    }
                }
            }
        }
    };

    /**
     * 地址 转 poi
     *
     * @param address 地址
     */
    public MapManager address2poi(String address) {
        GeocodeQuery query = new GeocodeQuery(address, "");
        mGeocodeSearch.getFromLocationNameAsyn(query);
        return mInstance;
    }

    /**
     * poi 转地址
     *
     * @param la 经度
     * @param lo 纬度
     */
    public MapManager poi2address(double la, double lo) {
        /**
         * 第二个参数： 时间
         * 第三个参数： 需要转换的坐标系类型
         */
        RegeocodeQuery query = new RegeocodeQuery(
                new LatLonPoint(la, lo), 3000, GeocodeSearch.AMAP);
        mGeocodeSearch.getFromLocationAsyn(query);
        return mInstance;
    }

    public interface onGeocodeListener {
        void poi2address(String address);

        void address2poi(double la, double lo, String address);
    }
}
