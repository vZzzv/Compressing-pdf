//package com.ie.pdf2;
//
//import com.google.gson.Gson;
//import com.ie.pdf2.pdfcomp.PDFDataModel;
//import com.ie.pdf2.pdfcomp.PSModel;
//import com.ie.pdf2.ztools.GsonDoubleInteger;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.redis.core.RedisTemplate;
//
//import javax.annotation.Resource;
//import java.io.File;
//
//
//@Slf4j
//@SpringBootTest
//class Pdf2ApplicationTests {
//
//
//    @Value("${filename}")
//    String filename;
//
//    @Test
//    void contextLoads() {
//        //先创建文件对象
//        File file = new File("/Volumes/Data/Project/2023-07-17亿力小工具/tools/server/pdf2Server/zstatic/pdf/b066b4f271fc42db5f6a76869aac51d1_5.pdf");
//
//
//        //调用相应的方法，得到对应信息
//        System.out.println("文件名=" + file.getName());
//        System.out.println("文件绝对路径=" + file.getAbsolutePath());
//        System.out.println("文件父级目录=" + file.getParent());
//        System.out.println("文件大小(字节)=" + file.length());
//        System.out.println("文件是否存在=" + file.exists());//T
//        System.out.println("文件是不是一个文件=" + file.isFile());//T
//        System.out.println("文件是不是一个目录=" + file.isDirectory());//F
//
//
//    }
//
//
//    @Resource(name = "redis")
//    private RedisTemplate<String, Object> rts;
//
//    @Test
//    void redisFun(){
//        Gson gjson = GsonDoubleInteger.getGson();
//        String str = (String) rts.opsForValue().get("pdf_comp:comp_pdf:b066b4f271fc42db5f6a76869aac51d1.pdf");
//        PDFDataModel pdfDataModel = gjson.fromJson(str, PDFDataModel.class);
//        System.out.println(pdfDataModel.toString());
//        PSModel comp_9 = pdfDataModel.getPdfMap().get("comp_"+9);
//        System.out.println(comp_9.getSize());
//
//    }
//
//    @Test
//    void ccccc(){
//        File file = new File("zstatic/pdf/5_b066b4f271fc42db5f6a76869aac51d1.pdf");
//        float size = (float) file.length() / 1024 / 1024; // 转 MB
//        System.out.println("Compressed Size: " + size);
//    }
//
//
//
//
//
//
//
//}
