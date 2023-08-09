package com.ie.pdf2.ztools;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;


@Slf4j
public class ZMap {

    public static Map<String, Object> pMap(int code, String msg) {
        Map<String, Object> map = new HashMap<String, Object>();
        Gson json = GsonDoubleInteger.getGson();
        map.put("code", code);
        map.put("msg", msg);
        log.info(msg + "=>" + code);
        return map;

    }

    public static Map<String, Object> pMap(int code, String msg, Object data) {
        Map<String, Object> map = new HashMap<String, Object>();
        Gson json = GsonDoubleInteger.getGson();
        map.put("code", code);
        map.put("msg", msg);
        map.put("data", data);
        log.info(msg + "=>" + code);
        return map;
    }


    public static Map<String, Object> pMap(int code, String msg, Object data, String name1, Object data1) {
        Map<String, Object> map = new HashMap<String, Object>();
        Gson json = GsonDoubleInteger.getGson();
        map.put("code", code);
        map.put("msg", msg);
        map.put("data", data);
        map.put(name1, data1);
        log.info(msg + "=>" + code);
        return map;
    }

    public static Map<String, Object> pMap(int code, String msg, Object data, String name1, Object data1, String name2, Object data2) {
        Map<String, Object> map = new HashMap<String, Object>();
        Gson json = GsonDoubleInteger.getGson();
        map.put("code", code);
        map.put("msg", msg);
        map.put("data", data);
        map.put(name1, data1);
        map.put(name2, data2);
        log.info(msg + "=>" + code);
        return map;
    }

}
