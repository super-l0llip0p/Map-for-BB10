package cc.markc.pureatlas;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapModelCross;
import com.amap.api.navi.model.AMapNaviCameraInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AMapServiceAreaInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.autonavi.tbt.TrafficFacilityInfo;

public class MapActivity extends Activity implements View.OnClickListener, AMapNaviListener {

    public static final int DEFAULT_ZOOM_LEVEL = 16;
    protected double mCurrentLatitude;
    protected double mCurrentLongitude;

    protected ImageButton mNearbyButton;
    protected GridView mNearbyView;
    protected EditText mSearchBar;
    protected Button mSearchButton;
    protected View mRouteTypeView;
    protected RadioGroup mRouteTypeGroup;
    protected RadioButton mDriveRouteButton;
    protected RadioButton mBusRouteButton;
    protected View mToView;
    protected TextView mToTitleText;
    protected TextView mToSubtitleText;
    protected Button mToNavigationButton;
    protected Button mToRouteButton;
    protected Button mToCloseButton;
    private ImageButton mLayersButton;
    private ImageButton mRoadConditionsButton;

    protected MapView mMapView;
    protected AMap mMap;
    protected Marker mMyMarker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Common.setImmerseStatusBar(this, R.color.colorMain);
        setContentView(R.layout.activity_main);
        init(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    private void init(Bundle savedInstanceState) {
        mMapView = findViewById(R.id.mapview);
        mMapView.onCreate(savedInstanceState);
        mNearbyButton = findViewById(R.id.nearby_button);
        mNearbyView = findViewById(R.id.nearby_gridview);
        mSearchBar = findViewById(R.id.search_bar);
        mSearchButton = findViewById(R.id.search_button);
        mToTitleText = findViewById(R.id.to_address);
        mToSubtitleText = findViewById(R.id.to_distance);
        mToRouteButton = findViewById(R.id.select_route_button);
        mToNavigationButton = findViewById(R.id.navigation_button);
        mToCloseButton = findViewById(R.id.close_button);
        mToView = findViewById(R.id.to_windows);
        mRouteTypeView = findViewById(R.id.route_type_view);
        mRouteTypeGroup = findViewById(R.id.route_type_group);
        mDriveRouteButton = findViewById(R.id.drive_route_button);
        mBusRouteButton = findViewById(R.id.bus_route_button);
        mLayersButton = findViewById(R.id.layers_button);
        mLayersButton.setOnClickListener(this);
        mRoadConditionsButton = findViewById(R.id.road_conditions_button);
        mRoadConditionsButton.setOnClickListener(this);

        ImageButton locationButton = findViewById(R.id.location_button);
        ImageButton zoomInButton = findViewById(R.id.zoom_in_button);
        ImageButton zoomOutButton = findViewById(R.id.zoom_out_button);
        ImageButton moreButton = findViewById(R.id.more_button);
        locationButton.setOnClickListener(this);
        zoomInButton.setOnClickListener(this);
        zoomOutButton.setOnClickListener(this);
        moreButton.setOnClickListener(this);
    }

    private boolean isSatelliteMapEnable = false;
    private boolean isTrafficEnabled = false;
    protected boolean isFirstLocation = true;

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.layers_button:
                isSatelliteMapEnable = !isSatelliteMapEnable;
                mLayersButton.setImageResource(
                        isSatelliteMapEnable ? R.drawable.ic_menu_green_24dp : R.drawable.ic_menu_white_24dp);
                mMap.setMapType(isSatelliteMapEnable ? AMap.MAP_TYPE_SATELLITE : AMap.MAP_TYPE_BUS);
                break;
            case R.id.location_button:
                if (isFirstLocation) {
                    break;
                }
                LatLng latLng = new LatLng(mCurrentLatitude, mCurrentLongitude);
                mMyMarker.setPosition(latLng);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM_LEVEL));
                break;
            case R.id.road_conditions_button:
                isTrafficEnabled = !isTrafficEnabled;
                mRoadConditionsButton.setImageResource(
                        isTrafficEnabled ? R.drawable.ic_traffic_light_on : R.drawable.ic_traffic_light_off);
                mMap.setTrafficEnabled(isTrafficEnabled);
                break;
            case R.id.zoom_in_button:
                mMap.animateCamera(CameraUpdateFactory.zoomIn());
                break;
            case R.id.zoom_out_button:
                mMap.animateCamera(CameraUpdateFactory.zoomOut());
                break;
            case R.id.more_button:
                Common.openUrl(this, "https://markc.cc/p/af40ee3c.html");
                break;
        }
    }

    @Override
    public void onInitNaviFailure() {
    }

    @Override
    public void onInitNaviSuccess() {
    }

    @Override
    public void onStartNavi(int i) {
    }

    @Override
    public void onTrafficStatusUpdate() {
    }

    @Override
    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {
    }

    @Override
    public void onGetNavigationText(int i, String s) {
    }

    @Override
    public void onGetNavigationText(String s) {
    }

    @Override
    public void onEndEmulatorNavi() {
    }

    @Override
    public void onArriveDestination() {
    }

    @Override
    public void onCalculateRouteFailure(int i) {
    }

    @Override
    public void onReCalculateRouteForYaw() {
    }

    @Override
    public void onReCalculateRouteForTrafficJam() {
    }

    @Override
    public void onArrivedWayPoint(int i) {
    }

    @Override
    public void onGpsOpenStatus(boolean b) {
    }

    @Override
    public void onNaviInfoUpdate(NaviInfo naviInfo) {
    }

    @Override
    public void onNaviInfoUpdated(AMapNaviInfo aMapNaviInfo) {
    }

    @Override
    public void updateCameraInfo(AMapNaviCameraInfo[] aMapNaviCameraInfos) {
    }

    @Override
    public void onServiceAreaUpdate(AMapServiceAreaInfo[] aMapServiceAreaInfos) {
    }

    @Override
    public void showCross(AMapNaviCross aMapNaviCross) {
    }

    @Override
    public void hideCross() {
    }

    @Override
    public void showModeCross(AMapModelCross aMapModelCross) {
    }

    @Override
    public void hideModeCross() {
    }

    @Override
    public void showLaneInfo(AMapLaneInfo[] aMapLaneInfos, byte[] bytes, byte[] bytes1) {
    }

    @Override
    public void hideLaneInfo() {
    }

    @Override
    public void onCalculateRouteSuccess(int[] ints) {
    }

    @Override
    public void notifyParallelRoad(int i) {
    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {
    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {
    }

    @Override
    public void OnUpdateTrafficFacility(TrafficFacilityInfo trafficFacilityInfo) {
    }

    @Override
    public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {
    }

    @Override
    public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {
    }

    @Override
    public void onPlayRing(int i) {
    }
}
