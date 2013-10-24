package utils;

import play.libs.IO;

import java.io.File;

/**
 * User: Administrator
 * Date: 13-10-24
 * Time: 下午1:11
 * To change this template use File | Settings | File Templates.
 */
public class FileUtil {
    public static final String FILE_PATH = "public/amExcel/am.xls";

    public static void uploadFile(File file) {
        File newFile = new File(FILE_PATH);
        IO.copyDirectory(file, newFile);
    }
}
