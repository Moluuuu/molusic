package com.molu.processing.utils.minio;

import com.alibaba.fastjson.JSONObject;
import com.molu.processing.pojo.Minio;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MinioUtils {

    @Autowired
    private MinioClient client;
    @Autowired
    private Minio minio;


    /**
     * 查看存储bucket是否存在
     * @param bucketName 存储bucket
     * @return boolean
     */
    @SneakyThrows
    public Boolean bucketExists(String bucketName) {
        return client.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
    }

    /**
     * 创建存储bucket
     * @param bucketName 存储bucket名称
     * @return Boolean
     */
    public Boolean makeBucket(String bucketName) {
        try {
            client.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 删除存储bucket
     * @param bucketName 存储bucket名称
     * @return Boolean
     */
    public Boolean removeBucket(String bucketName) {
        try {
            client.removeBucket(RemoveBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 文件上传
     * @param file 文件
     * @param bucketName 存储bucket
     * @return Boolean
     */
    public Boolean upload(MultipartFile file, String bucketName) {
        try {
            PutObjectArgs objectArgs = PutObjectArgs.builder().bucket(bucketName).object(file.getOriginalFilename())
                    .stream(file.getInputStream(),file.getSize(),-1).contentType(file.getContentType()).build();
            //文件名称相同会覆盖
            client.putObject(objectArgs);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 文件下载
     * @param bucketName 存储bucket名称
     * @param fileName 文件名称
     * @param res response
     */
    public void download(String bucketName, String fileName, HttpServletResponse res) {
        GetObjectArgs objectArgs = GetObjectArgs.builder().bucket(bucketName)
                .object(fileName).build();
        try (GetObjectResponse response = client.getObject(objectArgs)){
            byte[] buf = new byte[1024];
            int len;
            try (FastByteArrayOutputStream os = new FastByteArrayOutputStream()){
                while ((len=response.read(buf))!=-1){
                    os.write(buf,0,len);
                }
                os.flush();
                byte[] bytes = os.toByteArray();
                res.setCharacterEncoding("utf-8");
                //设置强制下载不打开
                res.setContentType("application/force-download");
                res.addHeader("Content-Disposition", "attachment;fileName=" + fileName);
                try (ServletOutputStream stream = res.getOutputStream()){
                    stream.write(bytes);
                    stream.flush();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    /**
//     * 查看文件对象
//     * @param bucketName 存储bucket名称
//     * @return 存储bucket内文件对象信息
//     */
//    public List<> listObjects(String bucketName) {
//        Iterable<Result<Item>> results = client.listObjects(
//                ListObjectsArgs.builder().bucket(bucketName).build());
//        List<com.msun.supermind.common.utils.Result> objectItems = new ArrayList<>();
//        try {
//            for (Result<Item> result : results) {
//                Item item = result.get();
//                com.msun.supermind.common.utils.Result objectItem = new com.msun.supermind.common.utils.Result();
//                objectItem.setMsg(item.objectName());
//                objectItems.add(objectItem);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//        return objectItems;
//    }

    /**
     * 批量删除文件对象
     * @param bucketName 存储bucket名称
     * @param objects 对象名称集合
     */
    public Iterable<Result<DeleteError>> removeObjects(String bucketName, List<String> objects) {
        List<DeleteObject> dos = objects.stream().map(DeleteObject::new).collect(Collectors.toList());
        return client.removeObjects(RemoveObjectsArgs.builder().bucket(bucketName).objects(dos).build());
    }
}