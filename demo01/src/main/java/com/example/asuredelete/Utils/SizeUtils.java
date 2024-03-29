package com.example.asuredelete.Utils;

import com.example.DDSD.domain.DDMSK;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;

public class SizeUtils {

    public static String getSize(int  size) {
//        String byteCount=ClassLayout.parseInstance(e).toPrintable();
//        int size=Integer.valueOf(byteCount);
        //获取到的size为：1705230
        int GB = 1024 * 1024 * 1024;//定义GB的计算常量
        int MB = 1024 * 1024;//定义MB的计算常量
        int KB = 1024;//定义KB的计算常量
        DecimalFormat df = new DecimalFormat("0.00");//格式化小数
        String resultSize = "";
        if (size / GB >= 1) {
            //如果当前Byte的值大于等于1GB
            resultSize = df.format(size / (float) GB) + "GB   ";
        } else if (size / MB >= 1) {
            //如果当前Byte的值大于等于1MB
            resultSize = df.format(size / (float) MB) + "MB   ";
        } else if (size / KB >= 1) {
            //如果当前Byte的值大于等于1KB
            resultSize = df.format(size / (float) KB) + "KB   ";
        } else {
            resultSize = size + "B   ";
        }
        return resultSize;
    }

    @SneakyThrows
    public static void main(String[] args) {
        DDMSK msk=new DDMSK();
        msk.setLeafNum(10);
        msk.setBeta(FuncUtils.getRandomFromZp());
        System.out.println(msk.toString().getBytes().length);

        Files.write(Paths.get("D:\\Desktop\\a.txt"), msk.toString().getBytes());
    }
}
