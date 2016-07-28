package it.unibs.appwow.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import it.unibs.appwow.NavigationActivity;

/**
 * Created by Alessandro on 04/07/2016.
 */
public class FileUtils {
    public static final String GROUP_IMAGES_DIR = "group_images";
    private static final String GROUP_IMAGE_FILE_NAME = "photo_group_%d.png";
    private static final int PHOTO_SIZE_PX = 400;

    public static String getGroupImageFileName(int idGroup){
        return String.format(GROUP_IMAGE_FILE_NAME, idGroup);
    }

    public static File getGroupImageFile(int idGroup, Context context){
        File res = null;
        String fileName = getGroupImageFileName(idGroup);
        File file = new File(context.getDir(FileUtils.GROUP_IMAGES_DIR, context.MODE_PRIVATE), fileName);
        if(file.exists()){
            res = file;
        }
        return res;
    }
    public static boolean  writeGroupImage(int idGroup, Bitmap bitmap, Context context){
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        String fileName = getGroupImageFileName(idGroup);
        File dir = new File(context.getDir(FileUtils.GROUP_IMAGES_DIR, context.MODE_PRIVATE).getPath());
        if(!dir.exists()){
            try {
                dir.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File destination = new File(dir, fileName);
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

    public static String writeTemporaryBitmap(Bitmap bitmap, Context context){
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        String fileName = "photo_"+System.currentTimeMillis()+".png";
        File destination = new File(context.getCacheDir(), fileName);
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
            //return fileName;
            return destination.getAbsolutePath();
        else return null;
    }

    public static Bitmap readTemporaryBitmap(String fileName, Context context) {
        Bitmap immagine = null;
        if(fileName == null)
            return immagine;

        File toOpen = new File(context.getCacheDir(), fileName);
        if (toOpen.exists()) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            immagine = BitmapFactory.decodeFile(toOpen.getPath(), options);
        } else {
            return null;
        }
        return immagine;
    }


    public static Bitmap readGroupImage(int idGroup, Context context){
        Bitmap immagine = null;
        String fileName = getGroupImageFileName(idGroup);
        File toOpen = new File(context.getDir(FileUtils.GROUP_IMAGES_DIR, context.MODE_PRIVATE), fileName);
        if(toOpen.exists()){
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            immagine= BitmapFactory.decodeFile(toOpen.getPath(), options);
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
        }
        return immagine;
    }

    public static Bitmap readBitmapFromPath(String path, Context context){
        Bitmap immagine = null;
        File toOpen = new File(path);
        if(toOpen.exists()){
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            immagine= BitmapFactory.decodeFile(toOpen.getPath(), options);
        }
        return immagine;
    }

    public static boolean deleteTemporaryFile(String fileName, Context context){
        File file = new File(context.getCacheDir(), fileName);
        if(file.exists()){
            return file.delete();
        }
        return false;
    }

    public static File getImageFile(String fileName, Context context){
        return new File(context.getDir(FileUtils.GROUP_IMAGES_DIR, context.MODE_PRIVATE), fileName);
    }


    public static File getTemporaryImageFile(String fileName, Context context){
        return new File(context.getCacheDir(), fileName);
    }

    public static boolean renameImageFile(String oldImageName, String newImageName, Context context){
        File old = new File(context.getCacheDir(), oldImageName);
        File file = new File(context.getDir(FileUtils.GROUP_IMAGES_DIR, context.MODE_PRIVATE), newImageName);
        if(old.exists()){
            return old.renameTo(file);
        }
        return false;
    }

    public static void clearPhotoDir(Context context) {
        File dir = new File(context.getDir(FileUtils.GROUP_IMAGES_DIR, context.MODE_PRIVATE).getPath());
        if(dir.exists()){
            deleteRecursive(dir);
        }
    }

    public static void clearCache(Context context) {
        deleteRecursive(context.getCacheDir());
    }

    private static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);
        fileOrDirectory.delete();
    }

    public static Bitmap resizeBitmap(Bitmap bm) {

        /*int height = bm.getHeight();
        int width = bm.getWidth();
        if(height < PHOTO_SIZE_PX && width < PHOTO_SIZE_PX){
            return bm;
        }
        int newHeight = PHOTO_SIZE_PX;
        int newWidth = PHOTO_SIZE_PX;
        float scaleHeight = ((float) newHeight) / height;
        float scaleWidth = ((float) newWidth) / width;*/

        float ratio = Math.max(
                (float) PHOTO_SIZE_PX / bm.getWidth(),
                (float) PHOTO_SIZE_PX / bm.getHeight());
        int width = Math.round((float) ratio * bm.getWidth());
        int height = Math.round((float) ratio * bm.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(bm, width,
                height, true);

        return newBitmap;

       // return Bitmap.createScaledBitmap(bm,PHOTO_SIZE_PX,PHOTO_SIZE_PX, true);
    }


}
