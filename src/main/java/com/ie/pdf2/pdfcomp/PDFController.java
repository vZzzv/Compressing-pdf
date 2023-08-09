package com.ie.pdf2.pdfcomp;

import com.aliyuncs.exceptions.ClientException;
import com.google.gson.Gson;
import com.ie.pdf2.ztools.ZData;
import com.ie.pdf2.ztools.ZMap;
import com.ie.pdf2.oss.OSSServer;
import com.ie.pdf2.ztools.GsonDoubleInteger;
import com.ie.pdf2.ztools.ZMath;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;


@Api(tags = "pdf压缩")
@Slf4j
@RestController
public class PDFController {



    @Autowired
    PDFServer pdfServer;

    @Autowired
    OSSServer ossServer;



//    @ApiOperation("上传pdf文件 大文件切片上传")
//    @ApiImplicitParams({
//            @ApiImplicitParam(name = "name", value = "上传pdf文件后服务器上pdf文件名", required = true),
//            @ApiImplicitParam(name = "size", value = "需要压综的文件大小", required = true)
//    })
//    @PostMapping(value = "/generatingPDF", produces = "application/x-www-form-urlencoded")
//    public String generatingPDF(String name, String size) throws ClientException {
//
//        return  "ok";
//    }




    @ApiOperation("开始压缩pdf")
        @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "上传pdf文件后服务器上pdf文件名",defaultValue="2315645672156787dsf787.pdf", required = true),
            @ApiImplicitParam(name = "compSize", value = "需要压综的文件大小 ( 1 ~ 500 ) MB",defaultValue="50", required = true)
    })
    @PostMapping(value = "/startCompressingPDF")
    public String startCompressingPDF(String name, String compSize) throws ClientException {
        Gson gson = GsonDoubleInteger.getGson();

        if (StringUtils.isEmpty(name)) {
            return gson.toJson(ZMap.pMap(124656,"name 不能为空"));
        }
        if (!ZMath.isDigital(compSize)){
            return gson.toJson(ZMap.pMap(124657,"compSize 类型错误"));
        }
        int compSize2 =  ZMath.stringToInt(compSize);
        if(compSize2 < 1 || compSize2 > 500){
            return gson.toJson(ZMap.pMap(124658,"compSize 超过范围"));
        }
        String  redisKey = ZData.getMd5(name + compSize2 + new Date().getTime());


        boolean isName = ossServer.existsOSS(name);
        if (!isName){
            return gson.toJson(ZMap.pMap(124659,"文件不存在"));
        }

        pdfServer.startCompressingPDF(name,compSize2,redisKey);

        try {
            //  压缩是异常   有一定的延时
            // 可能redis 数据还没创建好，就被查询到了，会出现查不到的情况
            Thread.sleep(500);
        }catch (Exception e){}


        return new Gson().toJson(ZMap.pMap(0,"准备开始压缩",redisKey));
    }

    @ApiOperation("获取压缩pdf文件的进度")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pkey", value = "查询压缩进度要用到的 key (由开始压缩接口提供)",defaultValue="2315645672156787dsf787", required = true)
    })
    @PostMapping(value = "/getCompProgress")
    public String getCompProgress(String pkey) throws ClientException {
        Gson gson = GsonDoubleInteger.getGson();
        if (StringUtils.isEmpty(pkey)) {
            return gson.toJson(ZMap.pMap(324184,"pkey 不能为空"));
        }






        return  pdfServer.getCompProgress(pkey);
    }

}
