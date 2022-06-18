package com.molu.processing.utils;

import java.util.HashMap;
import java.util.Map;


public class DataUtils {
    public static Map<String, Object> selectMap(String selectKey, Object value) {
        Map<String, Object> selectMap = new HashMap<>();
        selectMap.put(selectKey, value);
        return selectMap;
    }
}
