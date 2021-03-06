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
import com.codesaid.lib_framework.utils.log.LogUtils;

/**
 * Created By codesaid
 * On :2020-01-28
 * Package Name: com.codesaid.lib_framework.map
 * desc : 地图管理类
 */
public class MapManager {
    private static volatile MapManager mInstance = null;
    private GeocodeSearch mGeocodeSearch;

    private onAddress2poiGeocodeListener address2poi;
    private onPoi2addressGeocodeListener poi2address;

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
                    if (poi2address != null) {
                        poi2address.poi2address(regeocodeResult.getRegeocodeAddress()
                                .getFormatAddress());
                    }
                }
            }
        }

        @Override
        public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
            if (i == AMapException.CODE_AMAP_SUCCESS) {
                if (geocodeResult != null) {
                    if (address2poi != null) {
                        if (geocodeResult.getGeocodeAddressList() != null &&
                                geocodeResult.getGeocodeAddressList().size() > 0) {
                            GeocodeAddress address = geocodeResult.getGeocodeAddressList().get(0);
                            address2poi.address2poi(
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
    public MapManager address2poi(String address, onAddress2poiGeocodeListener listener) {
        this.address2poi = listener;
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
    public MapManager poi2address(double la, double lo, onPoi2addressGeocodeListener listener) {
        this.poi2address = listener;

        /**
         * 第二个参数： 时间
         * 第三个参数： 需要转换的坐标系类型
         */
        RegeocodeQuery query = new RegeocodeQuery(
                new LatLonPoint(la, lo), 3000, GeocodeSearch.AMAP);
        mGeocodeSearch.getFromLocationAsyn(query);
        return mInstance;
    }

    public interface onPoi2addressGeocodeListener {
        void poi2address(String address);
    }

    public interface onAddress2poiGeocodeListener {
        void address2poi(double la, double lo, String address);
    }

    /**
     * 获取静态地图Url
     *
     * @param la
     * @param lo
     * @return
     */
    public String getMapUrl(double la, double lo) {
        String url = "https://restapi.amap.com/v3/staticmap?location=" + lo + "," + la +
                "&zoom=17&scale=2&size=150*150&markers=mid,,A:" + lo + ","
                + la + "&key=" + "b8e77d76adf0bb168f4f41b83b46d38a";
        LogUtils.i("url:" + url);
        return url;
    }
}
