package com.molu.processing.service;

import com.alibaba.fastjson.JSON;
import com.molu.dictionary.MFD;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class UploadServiceImpl implements UploadService {

    @Value("${oss.qiniu.accessKey}")
    private String accessKey;

    @Value("${oss.qiniu.secretKey}")
    private String secretKey;

    @Value("${oss.qiniu.url}")
    private String url;

    @Value("${oss.qiniu.bucketName}")
    private String bucketName;


    @Override
    public Map<String, String> uploadToQiNiu(File file) {
        Map<String, String> map = new HashMap<>();

        Region region = new Region.Builder()
                .region("z0")
                .accUpHost("up-cn-east-2.qiniup.com")
                .srcUpHost("up-cn-east-2.qiniup.com")

                /*.iovipHost("http://iovip.qbox.me")
                .rsHost("http://rs.qiniu.com")
                .rsfHost("http://rsf.qiniu.com")
                .apiHost("http://api.qiniu.com")*/
                .build();
        //构造一个带指定 Region 对象的配置类
        Configuration configuration = new Configuration(region);
        configuration.useHttpsDomains = false;
        UploadManager uploadManager = new UploadManager(configuration);

        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucketName);


        try {
            String name = file.getName().substring(0, file.getName().lastIndexOf("."));
            String processedName = new String(name.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
            Response response = uploadManager.put(MFD.UPLOADFILEPATH + processedName + MFD.MP3, "music/" + processedName + MFD.MP3, upToken);
            Response response1 = uploadManager.put(MFD.UPLOADFILEPATH + processedName + MFD.LRC, "music/" + processedName + MFD.LRC, upToken);
            //解析上传成功的结果
            DefaultPutRet putRet = JSON.parseObject(response.bodyString(), DefaultPutRet.class);
            DefaultPutRet putRet1 = JSON.parseObject(response1.bodyString(), DefaultPutRet.class);
            map.put("musicUploadState", putRet.key + " 上传成功，hash值是: " + putRet.hash + "远程地址: " + response.url());
            map.put("lyricUploadState", putRet1.key + " 上传成功，hash值是: " + putRet1.hash + "远程地址: " + response1.url());
        } catch (
                QiniuException ex) {
            Response r = ex.response;
            System.err.println(r.toString());
        }
        return map;
    }

    @Override
    public Map<String, String> uploadToMinio(File file) {
        return null;
    }
}
