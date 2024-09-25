package com.syj.bi.utils.minio.minio;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.util.DateUtils;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * minio文件上传工具类
 */
@Slf4j
public class MinioUtil {
    //每次上传 限制 每10M上传一次
    private static final Integer partSize =1024*1024*10;

    /**
     * 上传文件
     * @param file 代上传文件
     * @param bizPath 文件服务器路径（如 /abc）
     * @return
     */
    public static String upload(MultipartFile file, String bizPath) throws Exception {
//        String uploadPath = bizPath+"/"+file.getOriginalFilename();
        return  upload(file.getInputStream(),bizPath);
    }
    /**
     * 上传文件到minio
     * @param stream 文件流
     * @param uploadPath 上传文件的全地址 /text/aa.txt
     * @return
     */
    public static String upload(InputStream stream,String uploadPath) throws Exception{
        if(!uploadPath.startsWith("/")){
            throw new RuntimeException("文件地址 要以 / 开头");
        }
        MinioClient minioClient = getMinioClient();
        MinioConfig config = getMinioConfig();
        String bucketName = config.getBucketName();
        uploadPath =dealFileName(uploadPath);
        uploadPath = removeHeadSlash(uploadPath);
        minioClient.putObject(PutObjectArgs.builder().bucket(bucketName)
                                .object(uploadPath)
                                .stream(stream,-1,partSize).contentType("application/octet-stream").build()
                            );
        stream.close();
        return getObjectURL(uploadPath);
    }

    /**
     * 获取文件流
     * @param objectName  文件名全路径 /abc/test.txt
     * @return
     */
    public static InputStream getMinioFile(String objectName) throws Exception {
        MinioClient minioClient = getMinioClient();
        String bucketName = getBucketName();
        objectName = removeHeadSlash(objectName);
        return minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(objectName).build());
    }

    /**
     * 获取文件外链
     * @return
     */
    private static String getObjectURL(String uploadPath) throws Exception {
        MinioClient minioClient = getMinioClient();
        Boolean uploadUrlHostFlag = getMinioConfig().getUploadUrlHostFlag();
        String bucketName = getBucketName();
        String url = minioClient.getPresignedObjectUrl(
                        GetPresignedObjectUrlArgs.builder().bucket(bucketName)
                                .object(uploadPath).
                        method(Method.GET).expiry(60 * 60 * 24 * 7).build()
        );
        url = url.substring(0,url.indexOf("?"));
        if(!uploadUrlHostFlag){
            //不携带域名
            url = url.substring(url.indexOf("/"+bucketName+"/"));
        }
        return URLDecoder.decode(url,"UTF-8");
    }


    /**
     * 删除文件
     * @param objectName  文件名全路径 /abc/test.txt
     */
    public static void removeObject(String objectName) throws Exception {
        MinioClient minioClient = getMinioClient();
        String bucketName = getBucketName();
        objectName = removeHeadSlash(objectName);
        minioClient.removeObject(
                RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build()
        );
    }


    private static MinioClient getMinioClient(){
        MinioClient bean = MinioConfig.getMinioClient();
        if(bean==null){
            throw new RuntimeException("请配置 minio 基础配置");
        }
        return bean;
    }
    private static String getBucketName(){
        return getMinioConfig().getBucketName();
    }
    private static MinioConfig getMinioConfig(){
        MinioConfig bean = MinioConfig.getMinioConfig();
        if(bean==null){
            throw new RuntimeException("请配置 minio 基础配置");
        }
        return bean;
    }
    //去除路径 以斜杠开头
    private static String removeHeadSlash(String path){
        if(path.startsWith("/")){
            path = path.substring(1,path.length());
        }
        return path;
    }

    private static String dealFileName(String filePath){
        if(StrUtil.isBlank(filePath)){
            throw new RuntimeException("文件全路径不能为空!");
        }
        String dateStr = DateUtils.format(new Date(),DateUtils.DATE_FORMAT_14);
        String name = FileUtil.getName(filePath);
        String path = filePath.replace(name,"");
        String type="";
        if(name.indexOf(".")!=-1){
            type = name.substring(name.lastIndexOf("."),name.length());
            name = name.substring(0,name.lastIndexOf("."));
        }
        return path+name+"_"+dateStr+type;
    }

    public static void batchDownLogToZip(Map<String,String> orderId, HttpServletResponse response) throws Exception {
        MinioClient minioClient = getMinioClient();
        Path path = File.createTempFile("网管操作日志", ".zip").toPath();
        ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(path));
        Map<String,String>  tmpUrl1 = new HashMap<>();
        //先循环将工单所有的日志拿到临时文件夹
        for (String url : orderId.keySet()) {
            String orderNum =  orderId.get(url);
            Path tempDirectory = Files.createTempDirectory(orderNum);
            String tempDir = tempDirectory.toAbsolutePath().toString();
            tmpUrl1.put(orderNum,tempDir);
            Iterable < Result < Item >> results = minioClient.listObjects(ListObjectsArgs.builder().bucket(getBucketName()).prefix("instruction/".concat(url).concat("/")).build());
            for (Result<Item> result : results) {
                Item item = result.get();
                String objectName = item.objectName();
                String filename = objectName.split("/")[2];
                File tempFile = File.createTempFile(filename.split("\\.")[0], ".txt", tempDirectory.toFile());
                String tempPath = tempFile.getPath();
                minioClient.downloadObject(DownloadObjectArgs.builder().bucket(getBucketName()).object(objectName).filename(tempPath).build());
            }
        }
        //将文件夹打成一个压缩包
        for (String url : tmpUrl1.keySet()) {
            byte[] bytes = new byte[1024];
            int length;
            String tmp = tmpUrl1.get(url);
            File file = new File(tmp);
            String first_url = url + "/";
            File[] files = file.listFiles();
            ZipEntry zipEntry = new ZipEntry(first_url);
            zos.putNextEntry(zipEntry);
            for (File file1 : files) {
                ZipEntry zipEntry1 = new ZipEntry(first_url + file1.getName());
                zos.putNextEntry(zipEntry1);
                FileInputStream fileInputStream = new FileInputStream(file1);
                while ((length = fileInputStream.read(bytes)) != -1 ){
                    zos.write(bytes,0,length);
                }
                zos.closeEntry();
                fileInputStream.close();
            }
            zos.closeEntry();

        }
        //一定要关闭压缩输出流,不然会显示文件已损坏
        zos.close();
        //返回给前端
        ServletOutputStream outputStream = response.getOutputStream();
        response.setContentType("application/zip");
        response.setCharacterEncoding("GBK");
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        String exportFileName = URLEncoder.encode("网管操作日志","utf-8");
        response.setHeader("Content-disposition","attachment;filename="+exportFileName);
        InputStream inputStream = Files.newInputStream(new File(path.toString()).toPath());
        byte[] bytes = new byte[1024];
        int length;
        while ((length = inputStream.read(bytes)) != -1){
            outputStream.write(bytes,0,length);
        }
        outputStream.close();
    }
}
