package it.unibs.appwow.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Alessandro on 04/07/2016.
 */
public class FileUtils {
    public static final String GROUP_IMAGES_DIR = "group_images";
    private static final String GROUP_IMAGE_FILE_NAME = "photo_group_%d.png";

    public static String getGroupImageFileName(int idGroup){
        return String.format(GROUP_IMAGE_FILE_NAME, idGroup);
    }
    public static boolean  writeGroupImage(int idGroup, Bitmap bitmap, Context context){
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        String fileName = getGroupImageFileName(idGroup);
        File destination = new File(context.getDir(FileUtils.GROUP_IMAGES_DIR, context.MODE_PRIVATE), fileName);
        FileOutputStream fos;
        if(destination.exists()){
            boolean delete = destination.delete();
            Log.d("writeGroupImage", "Immagine cancellata: "+ delete);
        }
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
        return destination.exists();
    }

    public static String writeBitmap(Bitmap bitmap, Context context){
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        String fileName = "photo_"+System.currentTimeMillis()+".png";
        File destination = new File(context.getDir(FileUtils.GROUP_IMAGES_DIR, context.MODE_PRIVATE), fileName);
        FileOutputStream fos;
        if(destination.exists()){
            destination.delete();
        }
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

        if(destination.exists())
            return fileName;
        else return null;
    }

    public static Bitmap readGroupImage(int idGroup, Context context){
        Bitmap immagine = null;
        String fileName = getGroupImageFileName(idGroup);
        File toOpen = new File(context.getDir(FileUtils.GROUP_IMAGES_DIR, context.MODE_PRIVATE), fileName);
        if(toOpen.exists()){
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            immagine= BitmapFactory.decodeFile(toOpen.getPath(), options);
        } else {
            return null;
        }
        return immagine;
    }

    public static Bitmap readBitmap(String fileName, Context context){
        Bitmap immagine = null;
        File toOpen = new File(context.getDir(FileUtils.GROUP_IMAGES_DIR, context.MODE_PRIVATE), fileName);
        if(toOpen.exists()){
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            immagine= BitmapFactory.decodeFile(toOpen.getPath(), options);
        } else {
            return null;
        }
        return immagine;
    }

    public static boolean deleteTemporaryFile(String fileName, Context context){
        File file = new File(context.getDir(FileUtils.GROUP_IMAGES_DIR, context.MODE_PRIVATE), fileName);
        if(file.exists()){
            return file.delete();
        }
        return false;
    }

    public static File getImageFile(String fileName, Context context){
        return new File(context.getDir(FileUtils.GROUP_IMAGES_DIR, context.MODE_PRIVATE), fileName);
    }

    public static boolean renameImageFile(String oldImageName, String newImageName, Context context){
        File old = new File(context.getDir(FileUtils.GROUP_IMAGES_DIR, context.MODE_PRIVATE), oldImageName);
        File file = new File(context.getDir(FileUtils.GROUP_IMAGES_DIR, context.MODE_PRIVATE), newImageName);
        if(old.exists()){
            return old.renameTo(file);
        }
        return false;
    }
}
