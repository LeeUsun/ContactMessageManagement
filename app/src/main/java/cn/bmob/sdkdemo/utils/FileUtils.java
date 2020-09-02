package cn.bmob.sdkdemo.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2019/2/21.
 */

public class FileUtils {


    public static boolean OutLogFile = true;    // 输出log信息到文件

    /**
     * 输出log信息到文件中
     */
    public static String FileLog(String info) {
        if (OutLogFile) {

            String crashPath = Environment.getExternalStorageDirectory() + "";
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            String date = formatter.format(new Date());
            DateFormat formatter2 = new SimpleDateFormat("HH:mm:ss");
            String time = formatter2.format(new Date()) + "  ";

            String fileName = "log-" + date + ".txt";
            info = "\r\n" + time + info;

            String path = Environment.getExternalStorageDirectory() + "/GxPoliceLog";
            File file = new File(path);
            //创建对应的文件夹
            if (!file.exists()) {
                file.mkdirs();
            }

            try {
                createFile(path, fileName);
                FileOutputStream fos = new FileOutputStream(path + "/" + fileName, true);
                fos.write(info.getBytes());
                fos.close();
            } catch (Exception e) {


            }
            return path + "/" + fileName;
        } else return "";
    }

    //创建文件
    public static void createFile(String path, String filename) throws IOException {
        File file = new File(path + "/" + filename);
        if (!file.exists())
            file.createNewFile();
    }


    //flie：要删除的文件夹的所在位置
    private void deleteFile(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                deleteFile(f);
            }
            //  file.delete();//如要保留文件夹，只删除文件，请注释这行
        } else if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 输出log信息到文件中
     */
    public static String FileLog(String fileName, String info) {
        if (OutLogFile) {

            String crashPath = Environment.getExternalStorageDirectory() + "";
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            String date = formatter.format(new Date());
            DateFormat formatter2 = new SimpleDateFormat("HH:mm:ss");
            String time = formatter2.format(new Date()) + "  ";

            fileName = fileName + ".txt";
//            info = "\r\n" + time + info;

            String path = Environment.getExternalStorageDirectory() + "/ContactApp";
            File file = new File(path);
            //创建对应的文件夹
            if (!file.exists()) {
                file.mkdirs();
            }

            try {
                createFile(path, fileName);
                FileOutputStream fos = new FileOutputStream(path + "/" + fileName, false);
                fos.write(info.getBytes());
                fos.close();
            } catch (Exception e) {


            }
            return path + "/" + fileName;
        } else return "";
    }
}

