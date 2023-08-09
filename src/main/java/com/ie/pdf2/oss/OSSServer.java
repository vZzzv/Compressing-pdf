package com.ie.pdf2.oss;


import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;

import com.aliyun.oss.model.*;

import com.aliyuncs.exceptions.ClientException;

import com.google.gson.Gson;
import com.ie.pdf2.ztools.GsonDoubleInteger;
import com.ie.pdf2.ztools.ZData;
import com.ie.pdf2.ztools.ZMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class OSSServer {

    //地区id
//    @Value("${regionId}")
//    String regionId;

    @Value("${accessKeyId}")
    String accessKeyId;

    @Value("${accessKeySecret}")
    String accessKeySecret;

    //角色
//    @Value("${roleArn}")
//    String roleArn;
//
//    @Value("${roleSessionName}")
//    String roleSessionName;


    @Value("${endpoint}")
    String endpoint;

    @Value("${bucketName}")
    String bucketName;

    @Value("${filename}")
    String filename;

    @Value("${pdf}")
    String pdf;
    @Value("${OSSpdf}")
    String OSSpdf;



    public static String prefix = ZData.theme + "oss:sharding:";

    @Resource(name = "redis")
    private RedisTemplate<String, Object> rts;

//
//    //给前端生成 临时 上传文件的 token
//    public String getOSSToken() {
//        DefaultProfile profile = DefaultProfile.getProfile(regionId, accessKeyId, accessKeySecret);
//        IAcsClient client = new DefaultAcsClient(profile);
//
//        //构造请求，设置参数。关于参数含义和设置方法，请参见API参考。
//        AssumeRoleRequest request = new AssumeRoleRequest();
//        request.setRoleArn(roleArn);
//        request.setRoleSessionName(roleSessionName);
//
//        try {
//            AssumeRoleResponse response = client.getAcsResponse(request);
////            System.out.println(new Gson().toJson(response.getCredentials()));
//            return new Gson().toJson(response.getCredentials());
//        } catch (ClientException e) {
//            return new Gson().toJson(ZMap.pMap(24124, e.getErrMsg()));
//        }
//
//    }

    //从oss  下载文件
    public boolean getOSSFileDown(String name) throws ClientException {
        log.info("oss  下载文件" + name);
        // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
        // 填写不包含Bucket名称在内的Object完整路径，例如testfolder/exampleobject.txt。
        String objectName = OSSpdf + "/" + name;
        String pathName = filename + "/" + pdf + "/" + name;

        // 本地没有文件，从oss下载
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            // 下载Object到本地文件，并保存到指定的本地路径中。如果指定的本地文件存在会覆盖，不存在则新建。
            // 如果未指定本地路径，则下载后的文件默认保存到示例程序所属项目对应本地路径中。
            ossClient.getObject(new GetObjectRequest(bucketName, objectName), new File(pathName));
        } catch (OSSException oe) {
//                System.out.println("捕获了一个OSSException异常，这意味着你的请求进入了OSS,但由于某种原因被拒绝了，并给出了错误的响应");
//                System.out.println("Error Message:" + oe.getErrorMessage());
//                System.out.println("Error Code:" + oe.getErrorCode());
//                System.out.println("Request ID:" + oe.getRequestId());
//                System.out.println("Host ID:" + oe.getHostId());
            log.info("捕获了一个OSSException异常，这意味着你的请求进入了OSS,但由于某种原因被拒绝了，并给出了错误的响应");
            ZMap.pMap(21456, oe.getErrorCode() + "::" + oe.getRequestId() + "::" + oe.getHostId());
            return false;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        return true;
    }


    // 上传文件到 OSS上
    @Async("ieTaskExecutor")
    public void upDateOSSFile0(String name) {
        upDateOSSFile(name);
    }

    // 上传文件到 OSS上
    public boolean upDateOSSFile(String name) {
        //要上传的文件
        // 填写Object完整路径，例如exampledir/exampleobject.txt。Object完整路径中不能包含Bucket名称。
        String objectName = OSSpdf + "/" + name;
        // 本地文件路径
        String pathName = filename + "/"+ pdf +"/" + name;
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 创建InitiateMultipartUploadRequest对象。
            InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, objectName);
            // 初始化分片。
            InitiateMultipartUploadResult upresult = ossClient.initiateMultipartUpload(request);
            // 返回uploadId，它是分片上传事件的唯一标识。您可以根据该uploadId发起相关的操作，例如取消分片上传、查询分片上传等。
            String uploadId = upresult.getUploadId();

            // partETags是PartETag的集合。PartETag由分片的ETag和分片号组成。
            List<PartETag> partETags = new ArrayList<PartETag>();
            // 每个分片的大小，用于计算文件有多少个分片。单位为字节。
            final long partSize = 1 * 1024 * 1024L;   //1 MB。

            // 根据上传的数据大小计算分片数。以本地文件为例，说明如何通过File.length()获取上传数据的大小。
            final File sampleFile = new File(pathName);
            long fileLength = sampleFile.length();
            int partCount = (int) (fileLength / partSize);
            if (fileLength % partSize != 0) {
                partCount++;
            }
            // 遍历分片上传。
            for (int i = 0; i < partCount; i++) {
                long startPos = i * partSize;
                long curPartSize = (i + 1 == partCount) ? (fileLength - startPos) : partSize;
                UploadPartRequest uploadPartRequest = new UploadPartRequest();
                uploadPartRequest.setBucketName(bucketName);
                uploadPartRequest.setKey(objectName);
                uploadPartRequest.setUploadId(uploadId);
                // 设置上传的分片流。
                // 以本地文件为例说明如何创建FIleInputstream，并通过InputStream.skip()方法跳过指定数据。
                InputStream instream = new FileInputStream(sampleFile);
                instream.skip(startPos);
                uploadPartRequest.setInputStream(instream);
                // 设置分片大小。除了最后一个分片没有大小限制，其他的分片最小为100 KB。
                uploadPartRequest.setPartSize(curPartSize);
                // 设置分片号。每一个上传的分片都有一个分片号，取值范围是1~10000，如果超出此范围，OSS将返回InvalidArgument错误码。
                uploadPartRequest.setPartNumber(i + 1);
                // 每个分片不需要按顺序上传，甚至可以在不同客户端上传，OSS会按照分片号排序组成完整的文件。
                UploadPartResult uploadPartResult = ossClient.uploadPart(uploadPartRequest);
                // 每次上传分片之后，OSS的返回结果包含PartETag。PartETag将被保存在partETags中。
                partETags.add(uploadPartResult.getPartETag());
            }


            // 创建CompleteMultipartUploadRequest对象。
            // 在执行完成分片上传操作时，需要提供所有有效的partETags。OSS收到提交的partETags后，会逐一验证每个分片的有效性。当所有的数据分片验证通过后，OSS将把这些分片组合成一个完整的文件。
            CompleteMultipartUploadRequest completeMultipartUploadRequest =
                    new CompleteMultipartUploadRequest(bucketName, objectName, uploadId, partETags);

            // 如果需要在完成分片上传的同时设置文件访问权限，请参考以下示例代码。
            // completeMultipartUploadRequest.setObjectACL(CannedAccessControlList.Private);
            // 指定是否列举当前UploadId已上传的所有Part。仅在Java SDK为3.14.0及以上版本时，支持通过服务端List分片数据来合并完整文件时，将CompleteMultipartUploadRequest中的partETags设置为null。
            // Map<String, String> headers = new HashMap<String, String>();
            // 如果指定了x-oss-complete-all:yes，则OSS会列举当前UploadId已上传的所有Part，然后按照PartNumber的序号排序并执行CompleteMultipartUpload操作。
            // 如果指定了x-oss-complete-all:yes，则不允许继续指定body，否则报错。
            // headers.put("x-oss-complete-all","yes");
            // completeMultipartUploadRequest.setHeaders(headers);

            // 完成分片上传。
            CompleteMultipartUploadResult completeMultipartUploadResult = ossClient.completeMultipartUpload(completeMultipartUploadRequest);
            //上传完成
//            System.out.println("上传完成 " + completeMultipartUploadResult.getETag());
            log.info("上传完成 " + completeMultipartUploadResult.getETag());
            return true;
        } catch (OSSException oe) {
//            System.out.println("Caught an OSSException, which means your request made it to OSS, "
//                    + "but was rejected with an error response for some reason.");
//            System.out.println("Error Message:" + oe.getErrorMessage());
//            System.out.println("Error Code:" + oe.getErrorCode());
//            System.out.println("Request ID:" + oe.getRequestId());
//            System.out.println("Host ID:" + oe.getHostId());
            log.info(String.valueOf(oe));
            return false;
        } catch (FileNotFoundException e) {
            log.info(String.valueOf(e));
            return false;
        } catch (IOException e) {
            log.info(String.valueOf(e));
            return false;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

    }


    // 判断OSS上是否存在文件
    public boolean existsOSS(String name) {
        String objectName = OSSpdf + "/"  + name;
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            // 判断文件是否存在。如果返回值为true，则文件存在，否则存储空间或者文件不存在。
            // 设置是否进行重定向或者镜像回源。默认值为true，表示忽略302重定向和镜像回源；如果设置isINoss为false，则进行302重定向或者镜像回源。
            //boolean isINoss = true;
            boolean found = ossClient.doesObjectExist(bucketName, objectName);
            //boolean found = ossClient.doesObjectExist(bucketName, objectName, isINoss);
//            System.out.println(found);
            return found;
        } catch (OSSException oe) {
//            System.out.println("Caught an OSSException, which means your request made it to OSS, "
//                    + "but was rejected with an error response for some reason.");
//            System.out.println("Error Message:" + oe.getErrorMessage());
//            System.out.println("Error Code:" + oe.getErrorCode());
//            System.out.println("Request ID:" + oe.getRequestId());
//            System.out.println("Host ID:" + oe.getHostId());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        return false;
    }


    //分片上传 oss
    public String startUpFile(MultipartFile mfile, String name,String _uploadId,int index,int allSize) throws ClientException {
        String uploadId = _uploadId;
        Gson gson = GsonDoubleInteger.getGson();

        // 填写Object完整路径，例如exampledir/exampleobject.txt。Object完整路径中不能包含Bucket名称。
        String objectName = OSSpdf + "/"  + name + ".pdf";

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            // 创建InitiateMultipartUploadRequest对象。
            InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, objectName);

            // 如果需要在初始化分片时设置请求头，请参考以下示例代码。
//            ObjectMetadata metadata = new ObjectMetadata();

            // 根据文件自动设置ContentType。如果不设置，ContentType默认值为application/oct-srream。
//            if (metadata.getContentType() == null) {
//                metadata.setContentType(Mimetypes.getInstance().getMimetype(new File(filePath), objectName));
//            }

            // 初始化分片。
            InitiateMultipartUploadResult upresult = ossClient.initiateMultipartUpload(request);
            // 返回uploadId，它是分片上传事件的唯一标识。您可以根据该uploadId发起相关的操作，例如取消分片上传、查询分片上传等。

            if ( index == 0){
                uploadId = upresult.getUploadId();
            }
//            System.out.println("uploadId:" + uploadId);
            // partETags是PartETag的集合。PartETag由分片的ETag和分片号组成。
//            List<PartETag> partETags = new ArrayList<PartETag>();


            // 根据上传的数据大小计算分片数。以本地文件为例，说明如何通过File.length()获取上传数据的大小。
            final File sampleFile = ZData.convertMultipartFileToFile(mfile,filename + "/temp/");

            UploadPartRequest uploadPartRequest = new UploadPartRequest();
            uploadPartRequest.setBucketName(bucketName);
            uploadPartRequest.setKey(objectName);
            uploadPartRequest.setUploadId(uploadId);
            // 设置上传的分片流。
            // 以本地文件为例说明如何创建FIleInputstream，并通过InputStream.skip()方法跳过指定数据。
            InputStream instream = new FileInputStream(sampleFile);

            uploadPartRequest.setInputStream(instream);
            // 设置分片大小。除了最后一个分片没有大小限制，其他的分片最小为100 KB。
            uploadPartRequest.setPartSize(sampleFile.length());
            // 设置分片号。每一个上传的分片都有一个分片号，取值范围是1~10000，如果超出此范围，OSS将返回InvalidArgument错误码。
            uploadPartRequest.setPartNumber(index + 1);
            // 每个分片不需要按顺序上传，甚至可以在不同客户端上传，OSS会按照分片号排序组成完整的文件。
            UploadPartResult uploadPartResult = ossClient.uploadPart(uploadPartRequest);
            // 每次上传分片之后，OSS的返回结果包含PartETag。PartETag将被保存在partETags中。

            //把上传后的PartETag 放入 redis

            gson.toJson(uploadPartResult.getPartETag());
            rts.opsForValue().set(prefix + ":" + uploadId + ":" + index, gson.toJson(uploadPartResult.getPartETag()),3600*5, TimeUnit.SECONDS);



            if(index+1 == allSize){
                List<PartETag> partETags = new ArrayList<PartETag>();
                // 准备删除key 用的
                List<String> redisKey = new ArrayList<>();
                for(int i = 0; i < allSize; i++){
                    redisKey.add(prefix + ":" + uploadId + ":" + i);
                    String StrPartETag = (String) rts.opsForValue().get(prefix + ":" + uploadId + ":" + i);
                    gson.fromJson(StrPartETag,PartETag.class);
                    partETags.add(gson.fromJson(StrPartETag,PartETag.class));

                }


                // 每个分片的大小，用于计算文件有多少个分片。单位为字节。
                CompleteMultipartUploadRequest completeMultipartUploadRequest =
                        new CompleteMultipartUploadRequest(bucketName, objectName, uploadId, partETags);


                // 完成分片上传。
                CompleteMultipartUploadResult completeMultipartUploadResult = ossClient.completeMultipartUpload(completeMultipartUploadRequest);
//                System.out.println("完成分片合并：" + completeMultipartUploadResult.getETag());

                // 删除redis 上的key
                rts.delete(prefix + ":" + uploadId);
                redisKey.forEach(el->{
                    rts.delete(el);
                });
            }
        } catch (FileNotFoundException ex) {
            return gson.toJson(ZMap.pMap(532144,"分片上传失败",ex));
        } catch (IOException ex) {
            return gson.toJson(ZMap.pMap(532145,"分片上传失败",ex));
        }

     return gson.toJson(ZMap.pMap(0,"成功",uploadId));

    }


    public String getLink(String name) {
        Gson gson = GsonDoubleInteger.getGson();
        String objectName = OSSpdf + "/"  + name;
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        long expirationTimeMillis = 10 * 3600 * 1000;  // URL过期时间，单位为毫秒
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, objectName);
        request.setExpiration(new Date(System.currentTimeMillis() + expirationTimeMillis));
        log.info("文件URL: " + ossClient.generatePresignedUrl(request));
        return gson.toJson(ZMap.pMap(0,"成功",ossClient.generatePresignedUrl(request)));
    }


}
