package utils;

import models.InfomationModel;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.*;
import play.Logger;
import play.libs.F;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * desc: 从excel解析用户信息
 * User: weiguili(li5220008@gmail.com)
 * Date: 13-10-23
 * Time: 下午3:29
 */
public class ParseUserFromExcel {
    static String EXCELLFile = "xx.xls";
    private static final String MIDDLETIME = "12:00";
    //public static final long AMTIME = getParseDate("08:45");  //GTA上班时间
    //public static final long PMTIME = getParseDate("18:00");  //GTA下班时间
    //public static final long MIDDLETIME = getParseDate("12:00"); //午休时间
    private ParseUserFromExcel parseUserFromExcel = null;//单例模式
    private ParseUserFromExcel() {
    }

    public static List<InfomationModel> parse(File excelFile) throws Exception {
        String fileName = excelFile.getName();
        InputStream is = new FileInputStream(excelFile);
        InfomationModel infomationModel = null;
        if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")){
            Workbook wb = WorkbookFactory.create(is);
            for(int sheetNum=0; sheetNum<wb.getNumberOfSheets(); sheetNum++){//循环工作簿
                Sheet sheet = wb.getSheetAt(sheetNum);
                //添加表头校验2013-03-12 和数据有效性校验
                if (sheet == null ) {
                    continue;
                }
                if(sheetNum == 0 && !validateSheetHead(sheet.getRow(1))){
                    continue;
                }
                int rowNum = 2;//从第二行开始循环。
                for (; rowNum <= sheet.getLastRowNum(); rowNum++) {
                    Row row = sheet.getRow(rowNum);
                    if (row == null) {
                        continue;
                    }
                    infomationModel = new InfomationModel();
                    // 循环列Cell
                    // 0考勤号码 1姓名 2部门 3日期 4时间
                    Cell cell = row.getCell(0);
                    infomationModel.number = getStringValue(cell);
                    Cell cell1 = row.getCell(1);
                    infomationModel.name = getStringValue(cell);
                    Cell cell2 = row.getCell(2);
                    infomationModel.department = getStringValue(cell);
                    Cell cell3 = row.getCell(3);
                    infomationModel.punchedDate = getDate(getStringValue(cell3), "yy-MM-dd");

                    Cell cell4 = row.getCell(4);
                    infomationModel.number = getStringValue(cell);
                }
            }
        }else {
            throw new Exception("格式解析错误。");//暂时还没做容错处理。
        }
        return null;
    }

    /**
     * 根据打卡时间，得到上班时间和下班时间。（T1是上午时间，T2是下午时间）
     * @param punchTime
     * @return
     */
    private F.T2<String,String> getAmPmTimeFromPunchTime(String punchTime){
        String amTime = "";
        String pmTime = "";
        if(StringUtils.isNotBlank(punchTime)){
            String[] time = punchTime.trim().split(" ");
            if(time.length==1){//只有一个时间
                if(isAmTime(time[0])){//判断是否是上午时间。
                    amTime = time[0];
                }else{
                    pmTime = time[0];
                }
            }else if(time.length>=2){
                if(isAmTime(time[0])){//如果是上午时间
                    amTime = time[0];
                }
                if(!isAmTime(time[time.length-1])){//数组最后一个数如果是下午时间的话
                    pmTime = time[time.length-1];
                }
            }
        }
        return F.T2(amTime,pmTime);
    }

    /**
     * 判断一个时间是否是上午时间
     * @param timeStr
     * @return
     */
    private static boolean isAmTime(String timeStr){
        if(getParseDate(timeStr,"HH:mm")<=getParseDate(MIDDLETIME,"HH:mm")){
            return true;
        }else {
            return false;
        }
    }

    public enum FieldName{
        NUMBER("考勤号码"),NAME("姓名"),DEPARTMENT("部门"),DATE("日期"),TIME("时间");
        private String name;
        FieldName(String name){
            this.name = name;
        }
        public String getName(){
            return name;
        }
    }

    //表头校验
    private static boolean validateSheetHead(Row row){
        if(row == null ){
            return false;
        }
        //10至20个字符
        if(!FieldName.NUMBER.getName().equals(getStringValue(row.getCell(0)))){
            return false;
        }
        if(!FieldName.NAME.getName().equals(getStringValue(row.getCell(0)))){
            return false;
        }
        if(!FieldName.DEPARTMENT.getName().equals(getStringValue(row.getCell(0)))){
            return false;
        }
        if(!FieldName.DATE.getName().equals(getStringValue(row.getCell(0)))){
            return false;
        }
        if(!FieldName.TIME.getName().equals(getStringValue(row.getCell(0)))){
            return false;
        }
        return true;
    }

    private static String getStringValue(Cell xssfCell){
        if(xssfCell.getCellType() == xssfCell.CELL_TYPE_BOOLEAN){
            return String.valueOf( xssfCell.getBooleanCellValue());
        }else if(xssfCell.getCellType() == xssfCell.CELL_TYPE_NUMERIC){
            return String.valueOf( (int)(xssfCell.getNumericCellValue()));
        }else{
            return String.valueOf( xssfCell.getStringCellValue());
        }
    }

    /**
     * 把字符串转换成特定格式的时间转换成时间
     * @param time 时间字符串
     * @param fomat 时间格式
     * @return
     */
    public static Long getParseDate(String time, String fomat) {
        SimpleDateFormat sdf = new SimpleDateFormat(fomat);
        try {
            return sdf.parse(time).getTime();
        } catch (ParseException e) {
            Logger.info("不正确的日期格式");
            return null;
        }
    }

    public static Date getDate(String time, String fomat) {
        SimpleDateFormat sdf = new SimpleDateFormat(fomat);
        try {
            return sdf.parse(time);
        } catch (ParseException e) {
            Logger.info("不正确的日期格式");
            return null;
        }
    }
}
