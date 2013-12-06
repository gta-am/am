package utils;

import org.apache.poi.ss.usermodel.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * desc: 从excel解析用户信息
 * User: weiguili(li5220008@gmail.com)
 * Date: 13-10-23
 * Time: 下午3:29
 */
public class ParseUserFromExcelUtils {
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
