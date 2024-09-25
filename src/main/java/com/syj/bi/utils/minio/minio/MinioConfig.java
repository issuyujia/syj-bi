package com.syj.bi.utils.minio.minio;

import cn.hutool.core.util.StrUtil;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Minio文件上传配置文件
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "minio.enable", havingValue = "true")
@Data
public class MinioConfig {
    @Value(value = "${minio.minio_url}")
    private String minioUrl;
    @Value(value = "${minio.minio_name}")
    private String minioName;
    @Value(value = "${minio.minio_pass}")
    private String minioPass;
    @Value(value = "${minio.bucketName}")
    private String bucketName;
    /**
     * 上传url 是否携带 host 默认不携带
     */
    @Value(value = "${minio.uploadUrlHost:false}")
    private Boolean uploadUrlHostFlag;
    /**
     * 多个用逗号分隔
     * 例如 /temp/*,/test/*
     */
    @Value(value = "${minio.open_dir:}")
    private String openDir;

    private static MinioClient minioClient;
    private static MinioConfig config;


    protected static MinioClient getMinioClient(){
        return MinioConfig.minioClient;
    }
    protected static MinioConfig getMinioConfig(){
        return MinioConfig.config;
    }

    @Bean
    public MinioClient initMinio() throws Exception {
        String url = minioUrl;
        if(!minioUrl.startsWith("http")){
            url = "http://" + minioUrl;
        }
        if(!url.endsWith("/")){
            url = url.concat("/");
        }
        MinioClient minioClient = new MinioClient.Builder().endpoint(url)
                .credentials(minioName, minioPass)
                .build();

        //检查 minio 上是否有该存储桶
        if(!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())){
            log.info("创建文件服务桶--->{}",bucketName);
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
        //设置存储同权限
        if(StrUtil.isNotBlank(openDir)){
            // \"arn:aws:s3:::"+bucketName+"/*\"
            List<String> openStr=new ArrayList<>();
            for(String open :openDir.split(",")){
                if(!open.startsWith("/")){
                    open ="/"+open;
                }
                openStr.add("\"arn:aws:s3:::"+bucketName+open+"\"");
            }
            String policy = "{\n" +
                    "  \"Statement\": [\n" +
                    "        {\n" +
                    "            \"Action\": \"s3:GetObject\",\n" +
                    "            \"Effect\": \"Allow\",\n" +
                    "            \"Principal\": \"*\",\n" +
                    "            \"Resource\": ["+String.join(",",openStr)+"]\n" +
                    "        }\n" +
                    "    ],\n" +
                    "    \"Version\": \"2012-10-17\"\n" +
                    "}";
            //设置 存储桶只读
            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder().config(policy).bucket(bucketName).build());
        }else{
            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder().config("{\"Statement\":[],\"Version\":\"2012-10-17\"}").bucket(bucketName).build());
        }


        MinioConfig.minioClient = minioClient;
        MinioConfig.config =this;

        return minioClient;
    }


}
