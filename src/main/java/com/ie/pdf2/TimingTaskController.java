package com.ie.pdf2;



import com.google.gson.Gson;
import com.ie.pdf2.ztools.GsonDoubleInteger;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.parser.PdfImageObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.DecimalFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@Async
@RestController
public class TimingTaskController {

    @Value("${filename}")
    String filename;

    @Value("${temp}")
    private String temp;

    // # Redis服务器连接密码（默认为空）
    @Value("${pdf}")
    private String pdf ;

    // 每天 清除一次
    // 过期 pdf 文件
    @Scheduled(cron = "0 0 23 * * ?")
//    @Scheduled(cron = "* * * * * ?")
    @Async
    public void deleteFile() {
        log.info("deleteFile");
        daysWeekFun(filename + "/" + temp + "/");
        daysWeekFun(filename + "/" + pdf + "/");
    }

    // 每天 清除一次
    // 过期 pdf 文件


    public void daysWeekFun(String path) {
        try {
            // 替换为你的文件路径
//            String path = filename + "/pdf/";
            File _file = new File( path);


            long nowTime = new Date().getTime()/1000;
            System.out.println(nowTime);

            // 十天，时间差
            long day = 3600 *24 * 10;

            long moreThanTenDays = nowTime - day;
            File[] cflie = _file.listFiles();
            for (int i = 0; i < cflie.length; i++) {
                if (!cflie[i].isDirectory()) { // 判断非文件夹
//                    System.out.println("============================");
                    String name = cflie[i].getName();
//                    System.out.println(name);
                    Path filePath = Paths.get(path + name);
                    BasicFileAttributes attributes = Files.readAttributes(filePath, BasicFileAttributes.class);
                    // 获取创建时间
                    FileTime creationTime = attributes.creationTime();
                    ZonedDateTime serverTime = ZonedDateTime.ofInstant(creationTime.toInstant(), ZoneId.systemDefault());
                    long cTime = serverTime.toEpochSecond();
                    if (cTime < moreThanTenDays) {
                        log.info("文件：" + name + " 创建时间 10天 外 " + cTime + "::"+ moreThanTenDays);
                        // 删除
                        cflie[i].delete();
                    }

                    // 获取更新时间
//                    FileTime lastModifiedTime = attributes.lastModifiedTime();
//                    serverTime = ZonedDateTime.ofInstant(lastModifiedTime.toInstant(), ZoneId.systemDefault());
//                    System.out.println("文件更新时间： " + serverTime + ":" + serverTime.toEpochSecond());
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



}
