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
 * desc: 考勤Excell解析器
 * User: weiguili(li5220008@gmail.com)
 * Date: 13-10-23
 * Time: 下午3:29
 */
public class AttendanceExcelParser {

    private final long AMTIME = getParseLong("08:45");  //GTA上班时间
    private final long PMTIME = getParseLong("18:00");  //GTA下班时间
    private final long MIDDLETIME = getParseLong("12:00"); //午休时间

    //要解析的excel文件
    private File excelFile;
    private List<InfomationModel> infomationModelList; //返回的解析实体

    private static AttendanceExcelParser instance = null;
    private AttendanceExcelParser(File excelFile){ //单例模式
        setExcelFile(excelFile);
    }
    public static List<InfomationModel> getParser(File excelFile) throws Exception{
        if(instance == null){
            instance = new AttendanceExcelParser(excelFile);
        }
        instance.parse();
        return instance.getInfomationModelList();
    }

    public File getExcelFile() {
        return excelFile;
    }

    public void setExcelFile(File excelFile) {
        this.excelFile = excelFile;
    }

    public List<InfomationModel> getInfomationModelList() {
        return infomationModelList;
    }
    /**
     * 主解析方法入口
     * @return
     * @throws Exception
     */
    public void parse() throws Exception {
        String fileName = getExcelFile().getName();
        InputStream is = new FileInputStream(getExcelFile());
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
                    InfomationModel infomationModel = new InfomationModel();
                    // 循环列Cell
                    // 0考勤号码 1姓名 2部门 3日期 4时间
                    Cell cell = row.getCell(0);
                    infomationModel.number = getStringValue(cell);
                    Cell cell1 = row.getCell(1);
                    infomationModel.name = getStringValue(cell);
                    Cell cell2 = row.getCell(2);
                    infomationModel.department = getStringValue(cell);
                    Cell cell3 = row.getCell(3);
                    infomationModel.punchedDate = getDate(getStringValue(cell3));

                    Cell cell4 = row.getCell(4);
                    String startTime = getAmPmTimeFromPunchTime(getStringValue(cell4))._1;
                    String endTime = getAmPmTimeFromPunchTime(getStringValue(cell4))._2;
                    infomationModel.startTime = getDate(startTime);
                    infomationModel.endTime = getDate(endTime);
                    infomationModel.status = getStatus(startTime,endTime);
                    infomationModelList.add(infomationModel);//插入
                }
            }
        }else {
            Logger.info("格式解析错误。");
            throw new Exception("格式解析错误。");//暂时还没做容错处理。
        }
    }

    /**
     * 根据上午打卡时间和下午打卡时间判断考勤状态
     * @param pmTimes
     * @param amTimes
     * @return
     */
    public int getStatus(String pmTimes,String amTimes) {
        Long amTime = getParseLong(amTimes);
        Long pmTime = getParseLong(pmTimes);
        int status = -1;
        //穷举法判断所有情况
        if(StringUtils.isNotBlank(amTimes) && StringUtils.isNotBlank(pmTimes)){
            if(amTime<=AMTIME && pmTime>=PMTIME){
                status = 0;
            }else if(amTime>AMTIME && pmTime>=PMTIME){
                status = 3;
            }else if(amTime>AMTIME && pmTime<PMTIME){
                status =8;
            }else if(amTime<=AMTIME && pmTime<PMTIME){
                status =4;
            }
        }else if(StringUtils.isBlank(amTimes) && StringUtils.isNotBlank(pmTimes)){
            if(pmTime>=PMTIME){
                status = 1;
            }else{
                status = 7;
            }
        }else if(StringUtils.isBlank(amTimes) && StringUtils.isBlank(pmTimes)){
            status = 5;
        }else if(StringUtils.isNotBlank(amTimes) && StringUtils.isBlank(pmTimes)){
            if(amTime<=AMTIME){
                status = 2;
            }else{
                status = 6;
            }
        }
        return status;
    }

    /**
     * 根据打卡时间，得到上班时间和下班时间。（T1是上午时间，T2是下午时间）
     * @param punchTime
     * @return
     */
    public F.T2<String,String> getAmPmTimeFromPunchTime(String punchTime){
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
    public boolean isAmTime(String timeStr){
        if(getParseLong(timeStr)<= MIDDLETIME){
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
    public boolean validateSheetHead(Row row){
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

    public String getStringValue(Cell xssfCell){
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
    public Long getParseLong(String time, String fomat) {
        SimpleDateFormat sdf = new SimpleDateFormat(fomat);
        try {
            return sdf.parse(time).getTime();
        } catch (ParseException e) {
            Logger.info("不正确的日期格式");
            return null;
        }
    }

    /**
     * 固定格式 HH:mm
     * @param time
     * @return
     */
    public  Long getParseLong(String time) {
        return getParseLong(time,"HH:mm");
    }

    public Date getDate(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd");
        try {
            return sdf.parse(time);
        } catch (ParseException e) {
            Logger.info("不正确的日期格式");
            return null;
        }
    }
}
