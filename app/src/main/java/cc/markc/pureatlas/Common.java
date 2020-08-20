package cc.markc.pureatlas;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.LatLonPoint;

public class Common {

    public static LatLonPoint getLatLonPoint(LatLng latLng) {
        return new LatLonPoint(latLng.latitude, latLng.longitude);
    }

    public static final String[] NEARBY_TABLE = new String[] {
            "",			"",			"美食畅饮",	"",			"",
            "美食",		"中餐",		"快餐",		"火锅",		"咖啡厅",
            "肯德基",	"自助餐",	"小吃",		"蛋糕房",	"西餐",
            "川菜",		"麦当劳",	"海鲜",		"日本料理",	"湘菜",
            "韩国料理",	"麻辣烫",	"茶餐厅",	"甜品店",	"奶茶",
            "冷饮店",	"必胜客",	"早餐",		"午餐",		"下午茶",

            "",			"",			"酒店住宿",	"",			"",
            "酒店",		"快捷酒店",	"宾馆",		"青年旅社",	"星级酒店",
            "三星级酒店","四星级酒店","五星级酒店","旅馆招待所","主题酒店",
            "商务酒店",	"快捷连锁",	"招待所",	"家庭旅馆",	"",

            "",			"",			"交通出行",	"",			"",
            "公交站",	"地铁站",	"火车站",	"汽车站",	"飞机场",
            "停车场",	"加油站",	"加气站",	"服务区",	"火车票代售",
            "充电桩",	"",			"",			"",			"",

            "",			"",			"景点畅游",	"",			"",
            "景点",		"公园",		"游乐场",	"风景名胜",	"博物馆",
            "寺庙",		"植物园",	"海洋馆",	"动物园",	"纪念馆",
            "水上乐园",	"教堂",		"森林公园",	"主题公园",	"5A 景区",
            "4A 景区",	"度假村",	"",			"",			"",

            "",			"",			"银行",	    "",			"",
            "银行",		"ATM",		"建设银行",	"工商银行",	"农业银行",
            "中国银行",	"招商银行",	"邮政储蓄",	"交通银行",	"浦发银行",
            "民生银行",	"邮储银行",	"中信银行",	"光大银行",	"兴业银行",
            "平安银行",	"北京银行",	"广发银行",	"",			"",

            "",			"",			"逛街购物",	"",			"",
            "超市",		"商场",		"步行街",	"五金店",	"便利店",
            "书店",		"菜市场",	"花店",		"眼镜店",	"建材市场",
            "农贸市场",	"渔具",		"窗帘",		"水果店",	"",

            "",			"",			"生活服务",	"",			"",
            "厕所",		"医院",		"药店",		"诊所",		"汽车维修",
            "洗车",		"汽车美容",	"4S 店",	"理发店",	"美容院",
            "移动营业厅","联通营业厅","电信营业厅","邮局",		"照相馆",
            "小学",		"中学",		"大学",		"驾校",		"打印",
            "旅行社",	"派出所",	"快递",		"彩票",		"房产中介",
            "干洗店",	"",			"",			"",			""
    };

    public static String getFormatTime(float duration) {
        if (duration < 60) {
            return "1 分钟";
        }
        else if (duration < 3600) {
            return ((int) (duration / 60)) + " 分钟";
        }
        else
        {
            int hours = (int) (duration / 3600);
            int minutes = (int) (duration % 3600 / 60);
            return hours + " 小时 " + minutes + " 分钟";
        }
    }

    public static String getFormatDistance(float distance) {
        if (distance < 1000) {
            return ((int) distance) + " 米";
        }
        return Common.saveOneBitOneRound(distance / 1000) + " 公里";
    }

    public static double saveOneBitOneRound(double d) {
        String str = String.format("%.1f", d);
        return Double.parseDouble(str);
    }

    public static void setImmerseStatusBar(Activity activity, int colorId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(activity.getResources().getColor(colorId));
        }
    }

    public static void openUrl(Activity activity, String url) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri uri = Uri.parse(url);
        intent.setData(uri);
        activity.startActivity(intent);
    }

    public static void showToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }
}
