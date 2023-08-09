package com.ie.pdf2.ztools;

import com.google.gson.Gson;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ZData {

    public static String theme = "pdf_comp:";

    /// 返回当前时间
    public static String getTimer() {
        // 格式化时间
        // SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd
        // HH:mm:ss:SSS");
        // String formatStr2 = formatter2.format(new Date());

        Date current = new Date();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return sdf.format(current);
    }

    // 判断 字符串是否是空
    public static boolean strIsEmpty(String str) {
        boolean boo = true;
        if (str != null
                && !str.trim().isEmpty()
                && !ZData.bjString(str, "undefined")
                && !ZData.bjString(str, "NaN")) {
            boo = false;
        }
        return boo;
    }

    // 生成md5
    public static String getMd5(String str) {
        String md5 = DigestUtils.md5DigestAsHex(str.getBytes());
        return md5;
    }

    // 生成随机数
    public static String getRandom() {
        int max = 10000, min = 1000;
        int ran = (int) (Math.random() * (max - min) + min);
        return String.valueOf(ran);
    }

    /* 获取URL中的参数值 */
    public static String getParameter(String url, String name) {
        if (url == null) {
            return null;
        }
        url = url.trim();
        if (url.equals("")) {
            return null;
        }
        if (url.length() < 3) {
            return null;
        }
        String[] urlParts = url.split("\\?");

        if (urlParts.length <= 1) {
            String a[] = new String[2];
            a[0] = "";
            a[1] = url;
            urlParts = a;
        }
        // 有参数
        String[] params = urlParts[1].split("&");
        Map<String, String> map = new HashMap<String, String>();

        for (String item : params) {
            String[] keyValue = item.split("=");

            try {
                map.put(keyValue[0], keyValue[1] == null ? "" : keyValue[1]);
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
        return map.get(name);
    }

    // 对参数做sql过滤 访sql注入
    public static String SQLToStr(String str) {

        if (str == null) {
            return null;
        }

        // 除了字母数字下划线之外的字符为非法字符
        Pattern pattern =
                Pattern.compile(
                        "%|like|net user|xp_cmdshell|/add|exec master.dbo.xp_cmdshell|net localgroup administrators|from|or|and|exec|insert|select|delete|update|count|--|char|master|drop|truncate|declare|where|'|;|",
                        Pattern.CASE_INSENSITIVE);

        Matcher matcher = pattern.matcher(str);
        StringBuffer buffer = new StringBuffer();
        // 如果找到非法字符
        while (matcher.find()) {
            // 如果里面包含非法字符如冒号双引号等，那么就把他们消去，并把非法字符前面的字符放到缓冲区
            matcher.appendReplacement(buffer, "");
        }
        // 将剩余的合法部分添加到缓冲区
        matcher.appendTail(buffer);
        // ZData.print("您的输入为: " + str);
        // ZData.print("合法的输出为: " + buffer.toString());
        return buffer.toString();
    }

    // 比较两个string是否相等
    public static boolean bjString(String str1, String str2) {
        // 全转为小写
        if (str1 == null) {
            return false;
        }
        if (str2 == null) {
            return false;
        }
        String s1 = str1.toLowerCase();
        String s2 = str2.toLowerCase();
        if (s1.equals(s2)) {
            return true;
        }
        return false;
    }

    // 拼接成字符串
    public static String join(long[] ids, String seperator) {
        if (ids == null || ids.length < 1) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < ids.length; i++) {
            sb.append("" + ids[i]);
            sb.append(seperator);
        }

        return sb.substring(0, sb.length() - seperator.length());
    }

    // 拼接成字符串
    public static String join(byte[] byt, String seperator) {
        if (byt == null || byt.length < 1) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byt.length; i++) {
            sb.append("" + byt[i]);
            sb.append(seperator);
        }

        return sb.substring(0, sb.length() - seperator.length());
    }

    // 拼接成字符串
    public static String join(String[] ids, String seperator) {
        if (ids == null || ids.length < 1) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < ids.length; i++) {
            sb.append("" + ids[i]);
            sb.append(seperator);
        }

        return sb.substring(0, sb.length() - seperator.length());
    }

    public static Map<String, String> objectToMap(Object obj) {
        Gson gjson = GsonDoubleInteger.getGson();
        String objStr = gjson.toJson(obj);
        return gjson.fromJson(objStr, HashMap.class);
    }


//    MultipartFile 转 File
    public static File convertMultipartFileToFile(MultipartFile multipartFile,String path) throws IOException {
        String folderPath = path;
        File folder = new File(folderPath);

        if (!folder.exists()) {
             folder.mkdirs();
        }

        InputStream inputStream = multipartFile.getInputStream();
        String originalFilename = multipartFile.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        File convertedFile = new File(path + UUID.randomUUID().toString() + fileExtension);
        Files.copy(inputStream, convertedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        return convertedFile;
    }

}
