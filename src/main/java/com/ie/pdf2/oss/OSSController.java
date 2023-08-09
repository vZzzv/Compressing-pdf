package com.ie.pdf2.oss;


import com.aliyuncs.exceptions.ClientException;
import com.google.gson.Gson;
import com.ie.pdf2.ztools.GsonDoubleInteger;
import com.ie.pdf2.ztools.ZMap;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Api(tags = "OSS接口")
@Slf4j
@RestController
public class OSSController {




    @Autowired
    OSSServer ossServer;


//    //给前端生成 临时 上传文件的 token
//    @ApiOperation("获取 OSSToken")
//    @GetMapping(value = "/getOSSToken")
//    public String getOSSToken(){
//        return ossServer.getOSSToken();
//    }


    @ApiOperation("判断OSS上是否已存在该文件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "要查询的文件名",defaultValue="2315645672156787dsf787.pdf", required = true)
    })
    @PostMapping (value = "/existsOSS")
    public String existsOSS(String name){
        Gson gson = GsonDoubleInteger.getGson();
        if (StringUtils.isEmpty(name)) {
            return gson.toJson(ZMap.pMap(324184,"文件名不能为空"));
        }

        boolean boo = ossServer.existsOSS(name);
        return  gson.toJson(ZMap.pMap(0,"成功",boo));
    }



    // 前端通过微信服把文件分片上传到oss
    @ApiOperation("上传文件切片到oss")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "文件名",defaultValue="2315645672156787dsf787", required = true),
            @ApiImplicitParam(name = "ossUploadId", value = "oss返回的 上传id,index为0时，会返回id", required = true),
            @ApiImplicitParam(name = "index", value = "当前要上传的第几个切片，从0开始", required = true),
            @ApiImplicitParam(name = "allSize", value = "总的切片数量", required = true)
    })
    @PostMapping (value = "/startOSSUpFile", produces = "multipart/form-data")
    public String startUpFile(MultipartFile file,String name, String ossUploadId, int index,int allSize) throws ClientException {
        Gson gson = GsonDoubleInteger.getGson();

        if(file.isEmpty() ){
            return gson.toJson(ZMap.pMap(53230,"file有误"));
        }

        if(StringUtils.isEmpty(name) ){
            return gson.toJson(ZMap.pMap(53231,"name有误"));
        }

        if(allSize < 0 && allSize > 10000){
            return gson.toJson(ZMap.pMap(53232,"allSize有误"));
        }
        if (index < 0 ){
            return gson.toJson(ZMap.pMap(53233,"index有误"));
        }

        if (index > 0 && StringUtils.isEmpty(ossUploadId)){
            return gson.toJson(ZMap.pMap(53234,"ossUploadId不能为空"));
        }

        return ossServer.startUpFile(file,name,ossUploadId,index,allSize);
    }
    @ApiOperation("获取文件链接")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "文件名",defaultValue="2315645672156787dsf787.pdf", required = true)
    })
    @PostMapping (value = "/getLink")
    public String getLink(String name) {
        return ossServer.getLink(name);
    }

}
