package cn.android.sample;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.MessageDigest;

/**
 * @author mahongyin
 * raw、assets（项目内的文件）
 * getFilesDir()、getCacheDir()（app私有储存文件目录，app被卸载时文件被删除，不用考虑6.0及以上权限限制）
 * Environment.getExternalStorageDirectory()（外部储存目录，其他应用可以访问，文件不会因为app被卸载而删除，）
 * 项目需要权限，AndroidManifest.xml设置
 *适配6.0及以上机型
 * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
 * <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>

 * final String file_path = getFilesDir().getPath() + File.separator + "files"+File.separator + "adbshell.txt";
 */
class FileUtils {
    /*
     * 使用字符流对文件进行读写操作
     */
    public static void writeToFile(String filePath, String content) throws Exception {
// 向文件中写入内容
        PrintWriter pw = new PrintWriter(filePath, "UTF-8");
        pw.write(content);
        pw.close();

    }

    public static String readFile(String filePath) throws Exception {
        StringBuilder sb = new StringBuilder();
        // 从文件中读取内容
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(filePath), "utf-8"));
        String b;
        while ((b = br.readLine()) != null) { // 按行读取
            System.out.println(b);
            sb.append(b);
        }
        br.close();
        return sb.toString();
    }


    /*
     * 使用字节流对文件进行读写操作
     */
    public static void write2File(String filePath, String content) throws Exception {
        // 向文件中写入内容
        File f = new File(filePath);
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(content.getBytes("UTF-8")); // 可以指定编码
//        fos.write(content.getBytes());
        fos.close();
    }

    public static String read2File(String filePath) throws Exception {

// 从文件中读取内容
        FileInputStream fis = new FileInputStream(filePath);
        byte b[] = new byte[(int) filePath.length()];
        int len = fis.read(b); // 读取后返回长度
        fis.close();
        return (new String(b));
    }

    public static void ioStream(InputStream in, OutputStream out) {
        try {
            InputStream fis = in;
            OutputStream baos = out;
            byte[] buffer = new byte[1024];
            int len = -1;
            while ((len = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            fis.close();
            out.close();
        } catch (Exception e) {
        }
    }

    /**
     * 小写  32位
     * 验证 apk 文件一致性！  需要同一versionCode
     */
    public static String getMD5(File file) {
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            StringBuffer md5 = new StringBuffer();
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] dataBytes = new byte[1024];

            int nread = 0;
            while ((nread = in.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            }
            byte[] mdbytes = md.digest();

            for (int i = 0; i < mdbytes.length; i++) {
                md5.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            return md5.toString().toLowerCase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

