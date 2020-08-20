package cc.markc.pureatlas;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;
import com.amap.api.navi.AMapNaviViewOptions;
import com.amap.api.navi.enums.NaviType;
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

public class NaviActivity extends Activity implements AMapNaviViewListener, AMapNaviListener {

    private AMapNaviView mNaviView;
    private AMapNavi mNavi;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Common.setImmerseStatusBar(this, R.color.colorNaviStatusBar);
        setContentView(R.layout.activity_navi);

        mNaviView = findViewById(R.id.naviview);
        mNaviView.onCreate(savedInstanceState);
        mNaviView.setAMapNaviViewListener(this);

        AMapNaviViewOptions options = mNaviView.getViewOptions();
        options.setCompassEnabled(false);
        options.setTrafficLayerEnabled(false);
        options.setTrafficBarEnabled(false);
        mNaviView.setViewOptions(options);

        mNavi = AMapNavi.getInstance(getApplicationContext());
        mNavi.addAMapNaviListener(this);
        mNavi.setUseInnerVoice(true);
        mNavi.setEmulatorNaviSpeed(60);

        boolean isGps = getIntent().getBooleanExtra("isGps", false);
        if (isGps) {
            mNavi.startNavi(NaviType.GPS);
        }
        else {
            mNavi.startNavi(NaviType.EMULATOR);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mNaviView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mNaviView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNaviView.onDestroy();
        mNavi.stopNavi();
        mNavi.removeAMapNaviListener(this);
    }

    @Override
    public void onNaviSetting() {}

    @Override
    public void onNaviCancel() {
        this.finish();
    }

    @Override
    public boolean onNaviBackClick() {
        return false;
    }

    @Override
    public void onNaviMapMode(int i) {}
    @Override
    public void onNaviTurnClick() {}
    @Override
    public void onNextRoadClick() {}
    @Override
    public void onScanViewButtonClick() {}
    @Override
    public void onLockMap(boolean b) {}
    @Override
    public void onNaviViewLoaded() {}
    @Override
    public void onInitNaviFailure() {}
    @Override
    public void onInitNaviSuccess() {}
    @Override
    public void onStartNavi(int i) {}
    @Override
    public void onTrafficStatusUpdate() {}
    @Override
    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {}
    @Override
    public void onGetNavigationText(int i, String s) {}
    @Override
    public void onGetNavigationText(String s) {}
    @Override
    public void onEndEmulatorNavi() {}
    @Override
    public void onArriveDestination() {}
    @Override
    public void onCalculateRouteFailure(int i) {}
    @Override
    public void onReCalculateRouteForYaw() {}
    @Override
    public void onReCalculateRouteForTrafficJam() {}
    @Override
    public void onArrivedWayPoint(int i) {}
    @Override
    public void onGpsOpenStatus(boolean b) {}
    @Override
    public void onNaviInfoUpdate(NaviInfo naviInfo) {}
    @Override
    public void onNaviInfoUpdated(AMapNaviInfo aMapNaviInfo) {}
    @Override
    public void updateCameraInfo(AMapNaviCameraInfo[] aMapNaviCameraInfos) {}
    @Override
    public void onServiceAreaUpdate(AMapServiceAreaInfo[] aMapServiceAreaInfos) {}
    @Override
    public void showCross(AMapNaviCross aMapNaviCross) {}
    @Override
    public void hideCross() {}
    @Override
    public void showModeCross(AMapModelCross aMapModelCross) {}
    @Override
    public void hideModeCross() {}
    @Override
    public void showLaneInfo(AMapLaneInfo[] aMapLaneInfos, byte[] bytes, byte[] bytes1) {}
    @Override
    public void hideLaneInfo() {}
    @Override
    public void onCalculateRouteSuccess(int[] ints) {}
    @Override
    public void notifyParallelRoad(int i) {}
    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {}
    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {}
    @Override
    public void OnUpdateTrafficFacility(TrafficFacilityInfo trafficFacilityInfo) {}
    @Override
    public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {}
    @Override
    public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {}
    @Override
    public void onPlayRing(int i) {}
}
