package com.ie.pdf2;

import com.itextpdf.text.DocumentException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.PropertySource;

import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.io.File;

import java.io.IOException;

@SpringBootApplication
@EnableDiscoveryClient  // nacos
@EnableSwagger2  // api文档
@EnableScheduling    //开启定时任务
@PropertySource(value = {"file:./application.properties"})
public class Pdf2Application {

    public static void main(String[] args) throws IOException, DocumentException {
        System.setProperty("log4j2.formatMsgNoLookups", "true");
        SpringApplication.run(Pdf2Application.class, args);

    }
}
