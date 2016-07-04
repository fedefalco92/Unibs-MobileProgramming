package it.unibs.appwow.utils;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Alessandro on 04/07/2016.
 */
public class FileUtils {
    public static final String GROUP_IMAGES_DIR = "group_images";

    public static String writeBitmap(Bitmap bitmap, Context context){
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        String fileName = "photo_" + System.currentTimeMillis() + ".png";
        File destination = new File(context.getDir(FileUtils.GROUP_IMAGES_DIR, context.MODE_PRIVATE), fileName);
        FileOutputStream fos;
        try {
            destination.createNewFile();
            fos = new FileOutputStream(destination);
            fos.write(bytes.toByteArray());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(destination.exists()){
            return fileName;
        } else return "";
    }
}
