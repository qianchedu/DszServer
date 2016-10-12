package com.dsz.wifi;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import java.lang.reflect.Method;

/**
 * Created by Administrator on 2016/10/12.
 */

public class HPWifiSetup {
    public static final String SETUP_WIFIAP_METHOD = "setWifiApEnabled";
    Context context = null;
    WifiManager wifiManager = null;
    WifiConfiguration wifiConfiguration = new WifiConfiguration();
    static HPWifiSetup hpWifiSetup;

    private HPWifiSetup(){}

    /**
     * 单例模式
     * @param context   上下文
     * @return
     */
    public static HPWifiSetup getInstance(Context context){
        if(hpWifiSetup == null){
            hpWifiSetup = new HPWifiSetup();
            hpWifiSetup.context = context.getApplicationContext();
            hpWifiSetup.wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        }
        return hpWifiSetup;
    }

    /**
     * 创建wifi热点
     * @param name          名称
     * @param password      密码
     * @param status        获取手机信号来区别
     * @throws Exception
     */
    public void setupWifiAp(String name, String password,boolean status)throws  Exception{
        if(name == null || "".equals(name)){
            throw new Exception("wifi热点名称不能为空");
        }

        //在创建wifi热点的时候，要先关闭原来的wifi
        if(wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(false);
        }

        Method setupMethod = wifiManager.getClass().getMethod(SETUP_WIFIAP_METHOD,WifiConfiguration.class,boolean.class);

        //将name中的值赋值给ssid作为热点名称
        wifiConfiguration.SSID = name;

        wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

        if(password != null){
            if(password.length() < 8){
                throw  new Exception("wifi热点的密码不能小于8");
            }

            //创建wifi热点的密码
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wifiConfiguration.preSharedKey = password;
        }

        setupMethod.invoke(wifiManager,wifiConfiguration,status);
    }
}
