package com.example.intenllignetbabyshakerjava.utils;

import android.os.Handler;

import com.itfitness.mqttlibrary.MQTTHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class Common {
    public static String Port = "6002";
    public static String Sever = "tcp://183.230.40.39" + ":" + Port;
//    public static String Port = "1883";
//    public static String Sever = "tcp://192.168.101.77" + ":" + Port;

    public static String ReceiveTopic = "babyapp_topic";
    public static String PushTopic = "baby_topic";
    public static String DriveID = "1183943269";
    public static String DriveName = "625921";
    public static String DrivePassword = "9";
    public static String Drive2ID = "1183943270";
    public static String api_key = "OVfi7n9Rwecd1uA0SuRz=mbjK2Y=";
    public static boolean DeviceOnline = false;
    public static String LatestOnlineDate = "离线";
    public static MQTTHelper mqttHelper = null;
    public static Handler mHandler;

    public static String modelRemoveNull(String send) {
        try {
            JSONObject jsonObject = new JSONObject(send);
            JSONObject dataObject = jsonObject.getJSONObject("data");
            JSONObject filteredDataObject = new JSONObject();
            Iterator<String> keys = dataObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = dataObject.opt(key);
                if (value != null && value != JSONObject.NULL) {
                    filteredDataObject.put(key, value);
                }
            }
            JSONObject filteredJsonObject = new JSONObject()
                    .put("cmd", jsonObject.get("cmd"))
                    .put("data", filteredDataObject);
            return filteredJsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }
}
