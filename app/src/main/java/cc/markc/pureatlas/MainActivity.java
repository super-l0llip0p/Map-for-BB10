package cc.markc.pureatlas;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.view.RouteOverLay;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DistanceItem;
import com.amap.api.services.route.DistanceResult;
import com.amap.api.services.route.DistanceSearch;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cc.markc.pureatlas.overlay.BusRouteOverlay;

public class MainActivity extends MapActivity implements RadioGroup.OnCheckedChangeListener,
        View.OnKeyListener, AMapLocationListener, AMap.OnMapClickListener, GeocodeSearch.OnGeocodeSearchListener,
        DistanceSearch.OnDistanceSearchListener, PoiSearch.OnPoiSearchListener, RouteSearch.OnRouteSearchListener,
        AMap.OnMarkerClickListener, AdapterView.OnItemClickListener, View.OnClickListener {

    private final int SUCCESS = 1000;
    private String mCityCode;
    private String mKeyword;
    private boolean isJustNavigation = false;

    private AMapNavi mNavi;
    private AMapLocationClient mLocationClient;
    private Marker mCurrentMarker;
    private GeocodeSearch mGeocodeSearch;
    private DistanceSearch mDistanceSearch;
    private RouteSearch mRouteSearch;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initMap();
        initLocationClient();
        setListener();
        initNearbyGridView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestPermission();
    }

    private void requestPermission() {
        AndPermission.with(this)
                .runtime()
                .permission(
                        Permission.ACCESS_FINE_LOCATION,
                        Permission.WRITE_EXTERNAL_STORAGE,
                        Permission.READ_PHONE_STATE
                )
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                    }
                })
                .start();
    }

    private void setListener() {
        mNearbyButton.setOnClickListener(this);
        mSearchBar.setOnKeyListener(this);
        mSearchButton.setOnClickListener(this);
        mRouteTypeGroup.setOnCheckedChangeListener(this);
        mToRouteButton.setOnClickListener(this);
        mToNavigationButton.setOnClickListener(this);
        mToCloseButton.setOnClickListener(this);
    }

    private void initMap() {
        if (mMap == null) {
            mMap = mMapView.getMap();
            mMap.setMapType(AMap.MAP_TYPE_BUS);
            mMap.getUiSettings().setZoomControlsEnabled(false);
            mMap.setOnMapClickListener(this);
            mMap.setOnMarkerClickListener(this);
        }
        mNavi = AMapNavi.getInstance(getApplicationContext());
        mNavi.addAMapNaviListener(this);

        mGeocodeSearch = new GeocodeSearch(this);
        mGeocodeSearch.setOnGeocodeSearchListener(this);
        mDistanceSearch = new DistanceSearch(this);
        mDistanceSearch.setDistanceSearchListener(this);
        mRouteSearch = new RouteSearch(this);
        mRouteSearch.setRouteSearchListener(this);
    }

    private void initLocationClient() {
        AMapLocationClientOption option = new AMapLocationClientOption();
        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        option.setInterval(5000);
        option.setNeedAddress(true);

        mLocationClient = new AMapLocationClient(getApplicationContext());
        mLocationClient.setLocationOption(option);
        mLocationClient.setLocationListener(this);
        mLocationClient.startLocation();
    }

    private void initNearbyGridView() {
        SimpleAdapter adapter = new SimpleAdapter(
                this,
                getNearbyGridViewData(),
                R.layout.item_nearby,
                new String[] { "title" },
                new int[] { R.id.nearby_item_button });
        mNearbyView.setAdapter(adapter);
        mNearbyView.setOnItemClickListener(this);
    }

    private List<HashMap<String, Object>> getNearbyGridViewData() {
        List<HashMap<String, Object>> list = new ArrayList<>();
        String[] table = Common.NEARBY_TABLE;

        for (String title : table) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("title", title);
            list.add(map);
        }
        return list;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        HashMap<String, Object> map = (HashMap<String, Object>) parent.getItemAtPosition(position);
        mKeyword = (String) map.get("title");
        onSearchButtonClick();
    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        if (i == KeyEvent.KEYCODE_ENTER) {
            if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                mKeyword = mSearchBar.getText().toString();
                onSearchButtonClick();
            }
        }
        return false;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (mMyMarker != null && mCurrentMarker != null) {
            clearDriveData();
            clearBusData();

            if (mZoomToSpanPadding == 0) {
                mZoomToSpanPadding = mToView.getHeight();
            }

            switch (checkedId) {
                case R.id.drive_route_button:
                    calculateDriveRoute();
                    break;
                case R.id.bus_route_button:
                    RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
                            Common.getLatLonPoint(mMyMarker.getPosition()),
                            Common.getLatLonPoint(mCurrentMarker.getPosition()));
                    RouteSearch.BusRouteQuery query = new RouteSearch.BusRouteQuery(
                            fromAndTo,
                            RouteSearch.BUS_LEASE_WALK,
                            mCityCode,
                            0);
                    mRouteSearch.calculateBusRouteAsyn(query);
                    break;
            }
        }
    }

    private void calculateDriveRoute() {
        int strategy = 0;
        try {
            strategy = mNavi.strategyConvert(true, false, false, false, true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if (strategy >= 0) {
            mNavi.calculateDriveRoute(mFromList, mToList, mWayList, strategy);
        }
    }

    private void onSearchButtonClick() {
        if (!"".equals(mKeyword)) {
            clearMap();
            searchPoi(mKeyword,
                    (mCurrentMarker != null ? mCurrentMarker : mMyMarker).getPosition());
        }
    }

    private void searchPoi(String keyword, LatLng latLng) {
        PoiSearch.Query query = new PoiSearch.Query(keyword, "", "");
        query.setPageSize(10);

        PoiSearch poiSearch = new PoiSearch(this, query);
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.setBound(new PoiSearch.SearchBound(
                new LatLonPoint(latLng.latitude, latLng.longitude), 5000));
        poiSearch.searchPOIAsyn();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearMap();
        mFromList.clear();
        if (mNavi != null) {
            mNavi.removeAMapNaviListener(this);
            mNavi.destroy();
        }
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                mCurrentLatitude = aMapLocation.getLatitude();
                mCurrentLongitude = aMapLocation.getLongitude();
                mCityCode = aMapLocation.getCityCode();

                mFromList.clear();
                mFromList.add(new NaviLatLng(mCurrentLatitude, mCurrentLongitude));

                LatLng latLng = new LatLng(mCurrentLatitude, mCurrentLongitude);
                if (mMyMarker == null) {
                    mMyMarker = mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker)));
                }
                else {
                    mMyMarker.setPosition(latLng);
                }
                if (isFirstLocation) {
                    isFirstLocation = false;
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM_LEVEL));
                }
            }
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        clearCurrentMarker();
        isJustNavigation = false;
        isDriveRouteCalculateSuccess = false;
        mRouteTypeGroup.clearCheck();

        mToList.add(new NaviLatLng(latLng.latitude, latLng.longitude));
        mCurrentMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_poi)));
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

        LatLonPoint latLonPoint = new LatLonPoint(latLng.latitude, latLng.longitude);
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 100, GeocodeSearch.AMAP);
        mGeocodeSearch.getFromLocationAsyn(query);
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        if (i == SUCCESS) {
            RegeocodeAddress address = regeocodeResult.getRegeocodeAddress();
            String formatAddress = address.getFormatAddress();
            formatAddress = formatAddress.replaceFirst(address.getProvince(), "");
            mToTitleText.setText(formatAddress);

            DistanceSearch.DistanceQuery query = new DistanceSearch.DistanceQuery();
            query.addOrigins(new LatLonPoint(mCurrentLatitude, mCurrentLongitude));
            LatLng latLng = mCurrentMarker.getPosition();
            query.setDestination(new LatLonPoint(latLng.latitude, latLng.longitude));
            query.setType(DistanceSearch.TYPE_DRIVING_DISTANCE);
            mDistanceSearch.calculateRouteDistanceAsyn(query);
        }
    }

    @Override
    public void onDistanceSearched(DistanceResult distanceResult, int i) {
        if (i == SUCCESS) {
            List<DistanceItem> distanceItemList = distanceResult.getDistanceResults();
            if (distanceItemList.size() > 0) {
                float distance = distanceItemList.get(0).getDistance();
                float duration = distanceItemList.get(0).getDuration();
                mToSubtitleText.setText("距您 " + Common.getFormatDistance(distance) +
                        " | 需要约 " + Common.getFormatTime(duration));
                mRouteTypeGroup.setVisibility(View.VISIBLE);
                mToView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        onMapClick(marker.getPosition());
        return true;
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.nearby_button:
                mNearbyView.setVisibility(
                        mNearbyView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                break;
            case R.id.search_button:
                onSearchButtonClick();
                break;
            case R.id.navigation_button:
                if (isDriveRouteCalculateSuccess) {
                    startNavigation();
                    break;
                }
                isJustNavigation = true;
                calculateDriveRoute();
                break;
            case R.id.select_route_button:
                if (mDriveRouteButton.isChecked()) {
                    changeDriveRoute();
                }
                else if (mBusRouteButton.isChecked()) {
                    changeBusRoute();
                }
                break;
            case R.id.close_button:
                clearMap();
                break;
        }
    }

    private void clearMap() {
        clearCurrentMarker();
        clearPoiOverlay();
        clearDriveData();
        clearBusData();
        mNearbyView.setVisibility(View.GONE);
        mRouteTypeGroup.setVisibility(View.GONE);
        mRouteTypeGroup.clearCheck();
        mToRouteButton.setVisibility(View.GONE);
        mToView.setVisibility(View.GONE);
    }

    private void clearCurrentMarker() {
        if (mCurrentMarker != null) {
            mCurrentMarker.hideInfoWindow();
            mCurrentMarker.destroy();
            mCurrentMarker = null;
        }
        mToList.clear();
    }

    private void clearPoiOverlay() {
        for (int i = 0; i < mPoiMarkerList.size(); i++) {
            mPoiMarkerList.get(i).hideInfoWindow();
            mPoiMarkerList.get(i).destroy();
        }
        mToList.clear();
    }

    private void clearDriveData() {
        clearDriveOverlay();
        mDriveRouteIndex = 0;
        mDriveRouteZindex = 1;
        isDriveRouteCalculateSuccess = false;
    }

    private void clearDriveOverlay() {
        for (int i = 0; i < mDriveOverlays.size(); i++) {
            RouteOverLay overlay = mDriveOverlays.valueAt(i);
            overlay.removeFromMap();
        }
        mDriveOverlays.clear();
    }

    private void clearBusData() {
        clearBusOverlay();
        mBusOverlays.clear();
        mBusRouteIndex = 0;
    }

    private void clearBusOverlay() {
        for (int i = 0; i < mBusOverlays.size(); i++) {
            BusRouteOverlay overlay = mBusOverlays.get(i);
            if (overlay != null) {
                overlay.removeFromMap();
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isNeedHideInputMethod(v, ev)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    mKeyword = mSearchBar.getText().toString();
                    mSearchBar.setText("");
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    private boolean isNeedHideInputMethod(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = {0, 0};
            v.getLocationInWindow(leftTop);

            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left &&
                event.getX() < right &&
                event.getY() > top &&
                event.getY() < bottom) {
                return false;
            }
            else
            {
                return isInputMethodActive();
            }
        }
        return false;
    }

    private boolean isInputMethodActive() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        return imm.isActive();
    }

    @Override
    public void onCalculateRouteFailure(int i) {
        isDriveRouteCalculateSuccess = false;
        Common.showToast(getApplicationContext(), "路线规划失败，错误代码：" + i);
    }

    private List<Marker> mPoiMarkerList = new ArrayList<>();
    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {
        if (i == SUCCESS) {
            List<PoiItem> poiItemList = poiResult.getPois();
            for (int j = 0; j < poiItemList.size(); j++) {
                PoiItem item = poiItemList.get(j);
                LatLonPoint latLonPoint = item.getLatLonPoint();
                LatLng latLng = new LatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude());
                Marker marker;
                marker = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.fromView(getPoiMarkerView(item.getTitle()))));
                mPoiMarkerList.add(marker);
                if (j == 0) {
                    onMapClick(latLng);
                }
            }
        }
    }

    public View getPoiMarkerView(String poiName) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View v = inflater.inflate(R.layout.item_poi, null);
        TextView poiNameText = v.findViewById(R.id.marker_title);
        poiNameText.setText(poiName);
        return v;
    }

    // 驾车路线图层 >
    private List<NaviLatLng> mFromList = new ArrayList<>();
    private List<NaviLatLng> mToList = new ArrayList<>();
    private List<NaviLatLng> mWayList = new ArrayList<>();
    private SparseArray<RouteOverLay> mDriveOverlays = new SparseArray<>();
    private int mDriveRouteIndex;
    private int mDriveRouteZindex = 1;
    private boolean isDriveRouteCalculateSuccess = false;

    @Override
    public void onCalculateRouteSuccess(int[] ints) {
        isDriveRouteCalculateSuccess = true;
        if (isJustNavigation) {
            isJustNavigation = false;
            startNavigation();
            return;
        }
        HashMap<Integer, AMapNaviPath> paths = mNavi.getNaviPaths();
        for (int i = 0; i < ints.length; i++) {
            AMapNaviPath path = paths.get(ints[i]);
            if (path != null) {
                drawDriveRoute(ints[i], path);
            }
        }
        changeDriveRoute();
        mToRouteButton.setVisibility(paths.size() > 1 ? View.VISIBLE : View.GONE);
    }

    private void drawDriveRoute(int routeId, AMapNaviPath path) {
        mMap.moveCamera(CameraUpdateFactory.changeTilt(0));
        RouteOverLay overLay = new RouteOverLay(mMap, path, this);
        overLay.setStartPointBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.amap_start));
        overLay.setEndPointBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.amap_end));
        overLay.setTrafficLine(true);
        overLay.addToMap();
        overLay.setTransparency(0.4f);
        mDriveOverlays.put(routeId, overLay);
    }

    private void startNavigation() {
        Intent intent = new Intent(getApplicationContext(), NaviActivity.class);
        intent.putExtra("isGps", true);
        startActivity(intent);
    }

    private void changeDriveRoute() {
        if (mDriveRouteIndex >= mDriveOverlays.size()) {
            mDriveRouteIndex = 0;
        }
        int id = mDriveOverlays.keyAt(mDriveRouteIndex);
        for (int i = 0; i < mDriveOverlays.size(); i++) {
            int key = mDriveOverlays.keyAt(i);
            mDriveOverlays.get(key).setTransparency(0.4f);
        }
        RouteOverLay overLay = mDriveOverlays.get(id);
        if (overLay != null) {
            overLay.setTransparency(1.0f);
            overLay.setZindex(mDriveRouteZindex++);
        }
        mNavi.selectRouteId(id);
        AMapNaviPath path = mNavi.getNaviPath();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(path.getBoundsForPath(), mZoomToSpanPadding));
        mToSubtitleText.setText(
                path.getLabels() + " | " +
                "距您 " + Common.getFormatDistance(path.getAllLength()) + " | " +
                "需要约 " + Common.getFormatTime(path.getAllTime()));
        mDriveRouteIndex++;
    }
    // 驾车路线图层 <

    // 公交路线图层 >
    private List<BusRouteOverlay> mBusOverlays = new ArrayList<>();
    private int mBusRouteIndex;
    private int mZoomToSpanPadding = 0;

    private void zoomToSpan() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(mMyMarker.getPosition());
        builder.include(mCurrentMarker.getPosition());
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), mZoomToSpanPadding));
    }

    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {
        if (i == SUCCESS) {
            List<BusPath> paths = busRouteResult.getPaths();
            if (paths.size() == 0) {
                mRouteTypeGroup.clearCheck();
                return;
            }
            for (int j = 0; j < paths.size(); j++) {
                BusRouteOverlay overlay = new BusRouteOverlay(
                        this,
                        mMap,
                        paths.get(j),
                        Common.getLatLonPoint(mMyMarker.getPosition()),
                        Common.getLatLonPoint(mCurrentMarker.getPosition()));
                mBusOverlays.add(overlay);
                if (j == 0) {
                    overlay.addToMap();
                    zoomToSpan();
                }
            }
            mToRouteButton.setVisibility(paths.size() > 1 ? View.VISIBLE : View.GONE);
        }
        else {
            mRouteTypeGroup.clearCheck();
        }
    }

    private void changeBusRoute() {
        clearBusOverlay();
        if ((++mBusRouteIndex) >= mBusOverlays.size()) {
            mBusRouteIndex = 0;
        }
        BusRouteOverlay overlay = mBusOverlays.get(mBusRouteIndex);
        overlay.addToMap();
        zoomToSpan();
    }
    // 公交路线图层 <

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {}

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {}

    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {}
    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {}
    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {}
}
