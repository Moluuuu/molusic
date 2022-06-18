package com.molu.processing.utils;

import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FileUtils {

    public static Map<String, String> dirClean(File dir, String dirName) {
        Map<String, String> map = new HashMap<>();
        // 得到目录中的文件
        File[] files = dir.listFiles();
        // 如果目录不为空
        if (ArrayUtils.isNotEmpty(files)) {
            assert files != null;
            Iterator<File> iterator = Arrays.stream(files).iterator();
            iterator.forEachRemaining((file) ->
                    map.put(dirName + ": " + file.getName(), (MusicUtils.deleteFile(file.getAbsolutePath()))
                            ? " 删除成功" : " 删除失败，请检查目录、文件是否存在")
            );
        }
        return map;
    }
}
