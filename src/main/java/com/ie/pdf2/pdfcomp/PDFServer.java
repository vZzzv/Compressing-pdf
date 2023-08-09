package com.ie.pdf2.pdfcomp;


import com.google.gson.Gson;
import com.ie.pdf2.ztools.GsonDoubleInteger;
import com.ie.pdf2.ztools.ZData;
import com.ie.pdf2.ztools.ZMap;
import com.ie.pdf2.oss.OSSServer;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.parser.PdfImageObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
import java.text.DecimalFormat;

import java.util.*;

import java.util.List;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
public class PDFServer {


    @Resource(name = "redis")
    private RedisTemplate<String, Object> rts;

    public static String prefix = ZData.theme + "comp_pdf:";

    @Value("${filename}")
    String filename;
    @Value("${pdf}")
    String pdf;

    int ptime= 3600*2;


    @Autowired
    OSSServer ossServer;

    //读取压缩pdf文件的进度
    public String getCompProgress(String pkey) {
        Boolean keyBoo = rts.hasKey(prefix +"progress:" + pkey);
        Gson gson = GsonDoubleInteger.getGson();
        if (keyBoo) {
            try{
                String str = (String) rts.opsForValue().get(prefix + "progress:" + pkey);
                Map<String,String> map = gson.fromJson(str,HashMap.class);
                return  gson.toJson(ZMap.pMap(0,"进度查询成功", map));
            }catch (Exception e){

            }
            return  gson.toJson(ZMap.pMap(35224,"进度查询失败"));
        }

        return  gson.toJson(ZMap.pMap(35224,"没有这个key"));
    }


    @Data
    class BinaryTree {
        TreeMode root;
        public BinaryTree() {
            root = null;
        }
        public void insert(int data) {
            root = insertRec(root, data);
        }
        private TreeMode insertRec(TreeMode root, int data) {
            if (root == null) {
                root = new TreeMode(data);
                return root;
            }

            if (data < root.data) {
                root.left = insertRec(root.left, data);
            }else if (data > root.data) {
                root.right = insertRec(root.right, data);
            }
            return root;
        }

        // 查询小于给定浮点数 A 的最大正整数并打印经过的节点
        public int findLargestSmallerThanA(PDFDataModel rdata, String name, String  path, int compSize,String redisKey) {

            log.info("开始从树中查询最接近的值");
            Gson gjson = GsonDoubleInteger.getGson();
            Map<String,String> progress = new HashMap<>();

            if (root == null) {
                return -1;
            }

            TreeMode current = root;
            boolean whileBoo  = true;
            TreeMode prev = null;  // 记录 满足条件的节点
            String[] arrStr = name.split("\\.");
            int i = 3;
            while (current != null && whileBoo) {
                progress.put("state", String.valueOf(i));
                progress.put("msg","压缩"+i);
                rts.opsForValue().set(prefix +"progress:" + redisKey,gjson.toJson(progress),ptime,TimeUnit.SECONDS);

                PSModel psm = rdata.getPdfMap().get("comp_"+ current.data);
                if(psm.getProgress() != 100){
                    // 压缩数据
                    log.info( current.data + "  还没压缩");
                    try {
                        File file = new File(path + name);
                        if (!file.exists()) {
                            //本地没有这个文件，去 oss上找
                            try {
                                boolean ossFileDown = ossServer.getOSSFileDown(name);
                                if (ossFileDown == false){
                                    log.info("oss上也没有这个文件");
                                    progress.put("state", "-1");
                                    progress.put("msg","oss上也没有这个文件");
                                    rts.opsForValue().set(prefix +"progress:" + redisKey,gjson.toJson(progress),ptime,TimeUnit.SECONDS);
                                    whileBoo = false;
                                }
                            }catch (Exception e){
                                log.info("oss上也没有这个文件");
                                progress.put("state", "-1");
                                progress.put("msg","oss上也没有这个文件");
                                rts.opsForValue().set(prefix +"progress:" + redisKey,gjson.toJson(progress),ptime,TimeUnit.SECONDS);
                                whileBoo = false;
                            }
                        }

                        compressPdf(path + name, path + arrStr[0]  + "_" +  current.data + ".pdf" , current.data,redisKey);

                        // 压缩后 获取文件大小
                        file = new File(path + arrStr[0]  + "_" +  current.data + ".pdf" );
                        float size = (float) file.length() / 1024 / 1024; // 转 MB
                        psm.setProgress(100);
                        psm.setSize(size);
                        rdata.getPdfMap().put("comp_"+current.data, psm);


                        if(!ossServer.existsOSS(arrStr[0] + "_" + current.data  + ".pdf")){
                            // oss上没有这个文件，
                            log.info(" oss上没有 1  上传" );
                            // 异步，把压缩过的文件都上传到oss上
                            ossServer.upDateOSSFile0( arrStr[0] + "_" + current.data + ".pdf");
                        }


                        //redis 时间不变
                        long keyExpire = rts.getExpire(prefix + name, TimeUnit.SECONDS);
                        rts.opsForValue().set(prefix + name, gjson.toJson(rdata), keyExpire  ,TimeUnit.SECONDS);
                    }catch (Exception e){
                    }
                }
                i++;
                //有数据后
                current.size = psm.getSize();
                if (current.size < compSize) {
                    prev = current;
                    current = current.right;
                } else {
                    current = current.left;
                }
            }
            if (prev != null) {
                return prev.data;
            } else {
                return -1;
            }
        }
    }




    // 准备开始压缩pdf
    @Async("ieTaskExecutor")
    public void startCompressingPDF(String name, int compSize, String redisKey)  {
        Gson gson = GsonDoubleInteger.getGson();
        String path = filename  + "/"+ pdf + "/";
        String pathName = path + name;
        File file = new File(pathName);

        Map<String,String> progress = new HashMap<>();
        progress.put("state","0");
        progress.put("msg","准备开始");
        rts.opsForValue().set(prefix +"progress:" + redisKey,gson.toJson(progress),ptime,TimeUnit.SECONDS);
        compPDF(name,path,compSize,redisKey);
        return;
    }


    // 1 准备开始压缩
//    @Async("ieTaskExecutor")
    public void  compPDF(String name, String path, int compSize,String redisKey){
        Gson gson = GsonDoubleInteger.getGson();
        Map<String,String> progress = new HashMap<>();
        Gson gjson = GsonDoubleInteger.getGson();
        log.info("准备开始，从redis读对应数据");


        // 从redis里获取压缩后的pdf文件 数据
        String pdfStr = (String) rts.opsForValue().get(prefix + name);

        PDFDataModel rdata = null;
        if (pdfStr == null){
            log.info("没有数据据 创建");
            // 数据库里没有，开始压缩
            // 创建空数据
            rdata = new PDFDataModel();
            for (int i = 1; i < 10; i++) {
                PSModel psModel = new PSModel();
                psModel.setProgress(0);
                psModel.setSize(0f);
                rdata.pdfMap.put("comp_" + i, psModel);
            }
            //获取redis里 key的过期时间
            //long key = rts.getExpire("key", TimeUnit.SECONDS);

            // 在redis里缓存7天
            rts.opsForValue().set(prefix + name, gjson.toJson(rdata), 3600 * 24 * 7  ,TimeUnit.SECONDS);
        }else{
            rdata = gjson.fromJson(pdfStr, PDFDataModel.class);
        }


        progress.put("state","1");
        progress.put("msg","redis数据获取成功");
        rts.opsForValue().set(prefix +"progress:" + redisKey,gson.toJson(progress),ptime,TimeUnit.SECONDS);

        // 数据库里有 解析
        startCompressing(rdata,name,path, compSize,redisKey);

    }


    // 生成二叉树
    //                 5
    //             3       7
    //          2    4   6    8
    //        1                 9

    private void startCompressing(PDFDataModel rdata,String name,String path,int compSize,String redisKey){
        log.info("生成二叉树");
        Gson gson = GsonDoubleInteger.getGson();
        Map<String,String> progress = new HashMap<>();

        BinaryTree tree = new BinaryTree();
        tree.insert(5);
        tree.insert(3);
        tree.insert(7);
        tree.insert(2);
        tree.insert(4);
        tree.insert(6);
        tree.insert(8);
        tree.insert(1);
        tree.insert(9);


        progress.put("state","2");
        progress.put("msg","树生成在功");
        rts.opsForValue().set(prefix +"progress:" + redisKey,gson.toJson(progress),ptime,TimeUnit.SECONDS);

        int result = tree.findLargestSmallerThanA(rdata, name, path,compSize,redisKey);
        // 记录，比 compSize小的 最大的pdf 文件大小的 序号
        String[] arrStr = name.split("\\.");


        progress.put("state","8");
        progress.put("msg","压缩完成 "+ result );
        rts.opsForValue().set(prefix +"progress:" + redisKey,gson.toJson(progress),ptime,TimeUnit.SECONDS);

        boolean cboo = false;
        log.info("有结果" + result);
        if(ossServer.existsOSS(arrStr[0] + "_" + result + ".pdf")){
            // oss上有了
            log.info(" oss上有了" + arrStr[0] + "_" + result + ".pdf" );
            progress.put("state","10");
            progress.put("msg","压缩后上传oss 成功 ");
            progress.put("data", String.valueOf(result));
            cboo = true;
        }else{
            log.info(" oss上没有  上传" );
            // oss上没有
            // 准备上传oss
            boolean boo = ossServer.upDateOSSFile( arrStr[0] + "_" + result + ".pdf");
            if (boo) {
                progress.put("state","10");
                progress.put("msg","压缩后上传oss 成功 ");
                progress.put("data", String.valueOf(result));
                cboo = true;
            }else{
                progress.put("state","-2");
                progress.put("msg","压缩后上传oss 失败 "+ result );
            }
        }
        if (cboo) {
            String pdfStr = (String) rts.opsForValue().get(prefix + name);
            PDFDataModel pdfM = gson.fromJson(pdfStr, PDFDataModel.class);
            PSModel psModel = pdfM.getPdfMap().get("comp_" + result);
            progress.put("size",  String.valueOf(psModel.getSize()));
        }


        rts.opsForValue().set(prefix +"progress:" + redisKey,gson.toJson(progress),ptime,TimeUnit.SECONDS);


    }

    // 压缩pdf
    public String compressPdf(String src, String out, float dpi,String redisKey) throws IOException, DocumentException {
        Gson gson = GsonDoubleInteger.getGson();

        System.out.println(src);
        System.out.println(out);
        System.out.println(dpi);
        float dpi1 = 0.1f * dpi;
        float zdpi = dpi1;

                PdfReader reader = null;
                PdfStamper stamper = null;
                ByteArrayOutputStream imgBytes = null;
                try {
                    // 读取PDF文件
                    reader = new PdfReader(src);
                    int n = reader.getXrefSize();
                    PdfObject object;
                    PRStream stream;


                    DecimalFormat df = new DecimalFormat("#.00");
                    // 遍历PDF文件中的所有对象

//                    int imgNum = 0;
                    for (int i = 0; i < n; i++) {
                        object = reader.getPdfObject(i);
                        if (object == null || object.isStream()==false) {
                            continue;
                        }
                        stream = (PRStream) object;
                        // 判断对象是否为图片
                        PdfObject pdfSubByte = stream.get(PdfName.SUBTYPE);
                        if (pdfSubByte != null && pdfSubByte.toString().equals(PdfName.IMAGE.toString())) {
//                            imgNum ++;
//                            System.out.println(imgNum);
                            // 获取图片对象
                            PdfImageObject imageobj = new PdfImageObject(stream);
                            BufferedImage bimg = imageobj.getBufferedImage();

                            if (bimg == null) {
                                continue;
                            }

//                            ImageIO.write(bimg, "JPEG", new File("./img/" + imgNum + ".jpg"));

                            int width = bimg.getWidth();
                            int height = bimg.getHeight();

//                            System.out.println(width + ":::"+ height);
                            // 对大图做第一次 尺寸限制
                            double P1 = 1;
                            if(width > 1024){
                                P1 = (double) 1024/width;

                                width = 1024;
                                height = (int) (height*P1);
//                                System.out.println(1024/width + ":::"+ height*P1);
                            }


                            /////////////////////////////////////////////////////////////////////////
                            // 根据缩放比例计算新的宽度和高度
                            AffineTransform atform = AffineTransform.getScaleInstance(1, 1);
                            if ((int) (width * zdpi) > 0 && (int) (height * zdpi) > 0) {
                                width = (int) (width * zdpi);
                                height = (int) (height * zdpi);
                                atform = AffineTransform.getScaleInstance(P1*zdpi, P1*zdpi);
                            }

//                            System.out.println(width + ":::"+ height);



                            Image image = bimg.getScaledInstance(width, height, Image.SCALE_SMOOTH);

                            BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);




                            Graphics2D g = tag.createGraphics();
                            g.drawRenderedImage(tag, atform);
                            g.setColor(Color.white);

                            g.drawImage(image, 0, 0, null); // 绘制处理后的图
                            g.dispose();



                            Iterator<ImageWriter> iterator = ImageIO.getImageWritersByFormatName("jpg");
                            ImageWriter writer = iterator.next();
                            ImageWriteParam param = writer.getDefaultWriteParam();
                            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);

                            float qdpi = 0.88f;
                            if(zdpi < qdpi){
                                qdpi = zdpi;
                            }

                            param.setCompressionQuality(qdpi);


                            imgBytes = new ByteArrayOutputStream();
//                            ImageIO.write(tag, "JPG", imgBytes);
                            writer.setOutput(new MemoryCacheImageOutputStream(imgBytes));
                            writer.write(null, new IIOImage(tag, null, null), param);



//
                            // 替换原有的图片流
                            stream.clear();
                            stream.setData(imgBytes.toByteArray(), false, PRStream.BEST_COMPRESSION);
                            //设置XObject的类型为图像
                            stream.put(PdfName.TYPE, PdfName.XOBJECT);
                            // 设置过滤器为DCTDECODE,用于解码JPEG、GIF等压缩过的图像
                            stream.put(PdfName.FILTER, PdfName.DCTDECODE);
                            // 设置子类型为IMAGE,表示这是一个图像对象
                            stream.put(PdfName.SUBTYPE, PdfName.IMAGE);
                            // 设置图像的宽度和高度
                            stream.put(PdfName.WIDTH, new PdfNumber(width));
                            stream.put(PdfName.HEIGHT, new PdfNumber(height));
                            // 设置颜色空间为DEVICERGB,表示使用设备RGB颜色空间
                            stream.put(PdfName.COLORSPACE, PdfName.DEVICERGB);
                            // 设置每个像素的颜色位数为8位，即24位真彩色
                            stream.put(PdfName.BITSPERCOMPONENT, new PdfNumber(8));
                        }


                        // 进度写入 redis
                        String proStr = (String) rts.opsForValue().get(prefix + "progress:" + redisKey);

                        if( proStr != null && proStr.length() > 0){
                            Map<String,String> progress = new HashMap<>();
                            progress = gson.fromJson(proStr, HashMap.class);
                            progress.put("p2", String.valueOf((float) (i+1)/n * 100));
                            rts.opsForValue().set(prefix +"progress:" + redisKey,gson.toJson(progress),ptime,TimeUnit.SECONDS);
                        }

                        log.info( " 压缩进度 = " + df.format(((float) (i+1)/n * 100)) + "%");
                    }

                    stamper = new PdfStamper(reader, new FileOutputStream(out));


                    log.info(out + " 压缩进度 = " + "100.00%");
                    log.info("完成");
                } catch (Exception e) {
                    log.error("PDF压缩错误：{}=>{}", src, out);
                    log.error("PDF压缩错误 e ：", e);
                } finally {
                    // 关闭流
                    try {
                        if (imgBytes != null) {
                            imgBytes.close();
                        }
                    } catch (IOException e) {
                        log.error("imgBytes 关闭失败：", e);
                    }

                    try {
                        if (stamper != null) {
                            stamper.close();
                        }
                    } catch (Exception e) {
                        log.error("stamper 关闭失败：", e);
                    }

                    try {
                        if (reader != null) {
                            reader.close();
                        }
                    } catch (Exception e) {
                        log.error("reader 关闭失败：", e);
                    }
                }

        return "ok";
    }




}
