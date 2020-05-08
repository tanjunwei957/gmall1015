package com.atguigu.gmall.manager.util;

import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.multipart.MultipartFile;

/**
 * GmallUploadUtil
 *
 * @Author: 谭俊伟
 * @CreateTime: 2020-03-04
 * @Description:
 */
public class GmallUploadUtil {
    public static String imageUpload(MultipartFile multipartFile) {
        //通过地址访问nginx +fast集合的图片内容
        String responseUrl = "http://192.168.254.135";
        //读取配置文件的路径
        String path = GmallUploadUtil.class.getResource("/tracker.conf").getPath();
        //客户端初始化
        try {
            ClientGlobal.init(path);

            //连接traker
            TrackerClient trackerClient = new TrackerClient();
            TrackerServer trackerServer = trackerClient.getTrackerServer();
            //获得storage
            StorageClient storageClient = new StorageClient(trackerServer);
            //上传文件
            String originalFilename = multipartFile.getOriginalFilename();
            int lastIndexOf = originalFilename.lastIndexOf(".");
            String fileName = originalFilename.substring(lastIndexOf);
            String[] urls = storageClient.upload_file(multipartFile.getBytes(), fileName, null);
            //解析返回图片的的路径的url信息
            for (String url : urls) {
                responseUrl = responseUrl + "/" + url;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseUrl;
    }
}
