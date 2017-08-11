package com.jbkweather.android.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import com.jbkweather.android.R;
import com.jbkweather.android.libcore.io.DiskLruCache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by sunny on 2017/7/17.
 * 自己完成ImageLoader的加载，哈哈，看看行不行
 */

public class ImageLoader {

    private static final String TAG = "ImageLoader";

    public static final int MESSAGE_POST_RESULT = 0X001;

    //获取系统CPU数
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    //线程池数量
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;

    //最大线程数量
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT + 1;

    //活跃线程数
    private static final long KEEP_ALIVE = 10L;

    private static final int TAG_KEY_URI = R.id.main_activity;

    //缓存大小
    private static final long DISK_CACHE_SIZE = 1024 * 1024 * 50;

    //读写IO大小
    private static final int IO_BUFFER_SIZE = 8 * 1024;

    //缓存下标
    private static final int DISK_CACHE_INDEX = 0;

    //缓存是否已经创建
    private boolean mIsDiskLruCacheCreated = false;


    //通过工厂类创建线程对象
    private static final ThreadFactory sTHREAD_FACTORY = new ThreadFactory() {

        //AtomicInteger是一个提供原子操作的Integer类，通过线程安全的方式操作加减。
        private final AtomicInteger mCount = new AtomicInteger(1);

        @Override
        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r, "ImageLoader#" + mCount.getAndIncrement());
        }
    };

    //初始化线程池对象
    /**
     * 阻塞队列LinkedBlockingQueue,这是一个线程安全的数据结构
     */
    public static final Executor THREAD_POLL_EXECUTOR = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAXIMUM_POOL_SIZE,
            KEEP_ALIVE,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(),
            sTHREAD_FACTORY);


    //在UI线程中更行界面
    private Handler mMainHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            LoaderResult result = (LoaderResult) msg.obj;
            ImageView imageView = result.imageView;
            String uri = (String) imageView.getTag(TAG_KEY_URI);
            //判断是防止错位
            if(uri.equals(result.uri)){
                imageView.setImageBitmap(result.bitmap);
            }else{
                Log.w(TAG, "set image bitmap,but url has changed,igored" );
            }
        }
    };

    private Context mContext;
    //图片压缩工具
    private ImageResizer mImageResizer = new ImageResizer();
    //内存缓存
    private LruCache<String,Bitmap> mMemoryCache;
    //磁盘缓存
    private DiskLruCache mDiskLruCache;

    private ImageLoader(Context context){
        mContext = context.getApplicationContext();
        int maxMemory = (int) (Runtime.getRuntime().maxMemory()/1024);
        int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String uri, Bitmap bitmap) {
                return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
            }
        };

        File diskCacheDir = getDiskCacheDir(mContext,"bitmap");
        if(!diskCacheDir.exists()){
            diskCacheDir.mkdirs();
        }

        //创建内存卡缓存
        if(getUsableSpace(diskCacheDir) > DISK_CACHE_SIZE){
            try {
                mDiskLruCache = DiskLruCache.open(diskCacheDir,1,1,DISK_CACHE_SIZE);
                mIsDiskLruCacheCreated = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ImageLoader使用单例调用
     * @param context
     * @return
     */
    public static ImageLoader build (Context context){
        return new ImageLoader(context);
    }

    /**
     * 添加的时候判断这个Key对应bitmap是不是null，
     * null的时候才进行添加，方式数据重复
     * @param key
     * @param bitmap
     */
    private void addBitmapToMemoryCache(String key,Bitmap bitmap){
        if(getBitmapFromMemoryCache(key) == null){
            mMemoryCache.put(key,bitmap);
        }
    }

    /**
     * 内存缓存
     * @param key
     * @return
     */

    private Bitmap getBitmapFromMemoryCache(String key) {
        return mMemoryCache.get(key);
    }

    /**
     * 加载图片,提供给外部调用的方法
     * @param uri
     * @param imageView
     */
    public void bindBitmap(final String uri,final ImageView imageView){
        bindBitmap(uri,imageView,0,0);
    }

    public void bindBitmap(final String uri, final ImageView imageView, final int reqWidth, final int reqHeight) {
        imageView.setTag(TAG_KEY_URI,uri);
        //内存缓存中获取bitmap
        Bitmap bitmap = loadBitmapFromMemoryCache(uri);
        if(bitmap != null){
            imageView.setImageBitmap(bitmap);
            return;
        }

        Runnable loadBitmapTask = new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = loadBitmap(uri,reqWidth,reqHeight);
                if(bitmap != null){
                    LoaderResult result = new LoaderResult(imageView,uri,bitmap);
                    //message 从handler 类获取，从而可以直接向该handler 对象发送消
                    mMainHandler.obtainMessage(MESSAGE_POST_RESULT,result).sendToTarget();
                }
            }
        };

        //加入线程池执行请求
        THREAD_POLL_EXECUTOR.execute(loadBitmapTask);
    }

    /**
     * load bitmap form memory cache or disk cache or network
     * @param uri http url
     * @param reqWidth ImageView的宽度
     * @param reqHeight ImageView的高度
     * @return 位图对象
     */
    public Bitmap loadBitmap(String uri, int reqWidth, int reqHeight) {

        //内存中读取
        Bitmap bitmap = loadBitmapFromMemoryCache(uri);
        if(bitmap != null){
            Log.d(TAG,"loadBitmapFromMemoryCache,url=="+uri);
            return bitmap;
        }

        try{
            //手机存储内容中读取
            bitmap = loadBitmapFromDiskCache(uri,reqWidth,reqHeight);
            if(bitmap != null){
                Log.d(TAG,"loadBitmapFromDiskCache,url=="+uri);
                return bitmap;
            }
            bitmap = loadBitmapFromHttp(uri,reqWidth,reqHeight);
        }catch (IOException e){
            e.printStackTrace();
        }

        //内存和SD卡都没有，就发送网络请求获取Bitmap
        if(bitmap == null && !mIsDiskLruCacheCreated){
            Log.d(TAG,"loadBitmapFromHttp,url=="+uri);
            bitmap = downLoadBitmapFromHttp(uri);
        }
        return bitmap;
    }



    /**
     * 在内存缓存中读取Bitmap
     * @param uri
     * @return
     */
    private Bitmap loadBitmapFromMemoryCache(String uri) {
        final String key = hashKeyFromUrl(uri);
        Bitmap bitmap = getBitmapFromMemoryCache(key);
        return bitmap;
    }

    /**
     * 在存储缓存中读取Bitmap
     * @param url
     * @param reqWidth
     * @param reqHeight
     * @return
     * @throws IOException
     */
    private Bitmap loadBitmapFromDiskCache(String url, int reqWidth, int reqHeight) throws IOException{
        if(Looper.myLooper() == Looper.getMainLooper()){
            throw new RuntimeException("can not visit network from UI Thread.");
        }
        if(mDiskLruCache == null){
            return null;
        }
        Bitmap bitmap = null;
        String key = hashKeyFromUrl(url);
        DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
        if(snapshot != null){
            FileInputStream fileInputStream = (FileInputStream) snapshot.getInputStream(DISK_CACHE_INDEX);
            //文件描述
            FileDescriptor fileDescriptor = fileInputStream.getFD();
            bitmap = mImageResizer.decodeSampleeBitmapFromFileDescriptor(fileDescriptor,reqWidth,reqHeight);
            if(bitmap != null){
                //添加到内存缓存
                addBitmapToMemoryCache(key,bitmap);
            }
        }
        return bitmap;
    }

    /**
     * 在网络上下载bitmap
     * @param url
     * @param reqWidth
     * @param reqHeight
     * @return
     * @throws IOException
     */
    private Bitmap loadBitmapFromHttp(String url,int reqWidth,int reqHeight)
            throws IOException{
        if(Looper.myLooper() == Looper.getMainLooper()){
            throw new RuntimeException("can not visit network from UI Thread.");
        }
        if(mDiskLruCache == null){
            return null;
        }
        String key = hashKeyFromUrl(url);
        DiskLruCache.Editor editor = mDiskLruCache.edit(key);
        if(editor != null){
            OutputStream outputStream = editor.newOutputStream(DISK_CACHE_INDEX);
            if(downloadUrlToStream(url,outputStream)){
                editor.commit();
            }else{
                editor.abort();
            }
            mDiskLruCache.flush();
        }
        return loadBitmapFromDiskCache(url,reqWidth,reqHeight);
    }


    private Bitmap downLoadBitmapFromHttp(String urlString) {
        Bitmap bitmap = null;
        HttpURLConnection urlConnection = null;
        BufferedInputStream inputStream = null;
        try {
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            inputStream = new BufferedInputStream(urlConnection.getInputStream(),IO_BUFFER_SIZE);
            bitmap = BitmapFactory.decodeStream(inputStream);

        } catch (IOException e) {
            Log.e(TAG, "downLoadBitmapFromHttp: failed..."+e );
        }finally {
            if(urlConnection != null){
                urlConnection.disconnect();
            }
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmap;
    }

    /**
     * 执行网络请求,下载图片
     * @param urlString http url
     * @param outputStream
     * @return
     */
    private boolean downloadUrlToStream(String urlString, OutputStream outputStream) {
        HttpURLConnection urlConnection = null;
        BufferedOutputStream out = null;
        BufferedInputStream in = null;
        try {
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream(),IO_BUFFER_SIZE);
            out = new BufferedOutputStream(outputStream,IO_BUFFER_SIZE);

            int b;
            while((b=in.read()) != -1){
                out.write(b);
            }
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "downloadBitmap failed "+e);
        }finally {
            if(urlConnection != null){
                urlConnection.disconnect();
            }
            try {
                if(in != null){
                    in.close();
                }
                if(out != null){
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    private String hashKeyFromUrl(String url) {
        String cacheKey;
        try {
            //信息摘要--一种算法
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(url.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(url.hashCode());
            e.printStackTrace();
        }
        return cacheKey;
    }

    /**
     * 将二进制转换为字符串
     * @param bytes
     * @return
     */
    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xff & bytes[i]);
            if (hex.length() == 1){
                sb.append("0");
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     *  获取文件大小
     * @param diskCacheDir
     * @return
     */
    private long getUsableSpace(File diskCacheDir) {
        return diskCacheDir.getUsableSpace();
    }

    /**
     * 获取文件的存储路径
     * @param context
     * @param uniqueName 文件名称
     * @return
     */
    private File getDiskCacheDir(Context context, String uniqueName) {
        //SD卡是否存在
        boolean externalStorageAvailable =
                Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        final String cachePath;
        if(externalStorageAvailable){
            cachePath = context.getExternalCacheDir().getPath();
        }else{
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath+File.separator+uniqueName);
    }

    /**
     * 基本参数
     */
    private static class LoaderResult{
        public ImageView imageView;
        public String uri;
        public Bitmap bitmap;

        public LoaderResult(ImageView imageView, String uri, Bitmap bitmap) {
            this.imageView = imageView;
            this.uri = uri;
            this.bitmap = bitmap;
        }
    }

}
