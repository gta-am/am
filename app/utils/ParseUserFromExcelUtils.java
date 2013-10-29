package utils;

import dto.UserInfoDto;
import models.InfomationModel;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.*;
import play.data.validation.Validation;
import play.libs.IO;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * desc: 从excel解析用户信息
 * User: weiguili(li5220008@gmail.com)
 * Date: 13-10-23
 * Time: 下午3:29
 */
public class ParseUserFromExcelUtils {
    static String EXCELLFile = "xx.xls";

    public ParseUserFromExcelUtils() {
    }

    public static List<InfomationModel> parse(File excellFile) throws Exception {
        String fileName = excellFile.getName();
        InputStream is = new FileInputStream(excellFile);
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
            throw new IndexOutOfBoundsException();//暂时还没做容错处理。
        }
        return null;
    }



    public static String getStringValue(Cell xssfCell) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        if(xssfCell.getCellType() == xssfCell.CELL_TYPE_BOOLEAN){
            return String.valueOf( xssfCell.getBooleanCellValue());
        }else if(xssfCell.getCellType() == xssfCell.CELL_TYPE_NUMERIC){
            return sdf.format(xssfCell.getDateCellValue());
            //return String.valueOf( (int)(xssfCell.getNumericCellValue()));
        }else{
            return String.valueOf( xssfCell.getStringCellValue());
        }
    }
}
