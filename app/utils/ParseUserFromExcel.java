package utils;

import models.InfomationModel;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

/**
 * desc: 从excel解析用户信息
 * User: weiguili(li5220008@gmail.com)
 * Date: 13-10-23
 * Time: 下午3:29
 */
public class ParseUserFromExcel {
    static String EXCELLFile = "xx.xls";

    public ParseUserFromExcel() {
    }

    public static List<InfomationModel> parse(File excelFile) throws Exception {
        String fileName = excelFile.getName();
        InputStream is = new FileInputStream(excelFile);
        if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")){
            Workbook wb = WorkbookFactory.create(is);
            for(int i=0; i<wb.getNumberOfSheets(); i++){//循环工作簿
                Sheet sheet = wb.getSheetAt(0);
                Iterator<Row> rows = sheet.rowIterator();
                while (rows.hasNext()){//循环行
                    Row row = rows.next();

                }
            }
        }else {
            throw new Exception("格式解析错误。");//暂时还没做容错处理。
        }
        return null;
    }

}
