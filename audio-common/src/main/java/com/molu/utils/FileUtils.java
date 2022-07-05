package com.molu.utils;

import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.util.*;

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


    public static Map<String, List<String>> getDirFiles(File... dirs) {
        // 需要返会的map
        Map<String, List<String>> map = new HashMap<>();
        // 判断是否传入可变长参数
        if (ArrayUtils.isNotEmpty(dirs)) {
            // 遍历可变长参数，得到传入的所有目录
            for (File dir : dirs) {
                // 判断传入的目录合法性
                if (dir.isDirectory()) {
                    // 如果是目录 得到他所有的文件
                    File[] files = dir.listFiles();
                    List<String> list = new ArrayList<>();
                    // 如果存在文件
                    if (ArrayUtils.isNotEmpty(files)) {
                        assert files != null;
                        // 迭代器遍历
                        Arrays.stream(files).iterator().forEachRemaining(file -> {
                            list.add(file.getName());
                        });
                    }
                    // 封装信息
                    map.put(dir.getName(), list);
                }
            }
        }
        return map;
    }
}
