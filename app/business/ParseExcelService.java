package business;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import dto.MonthEventDto;
import dto.UserInfoDto;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import play.Logger;
import play.Play;
import play.libs.IO;
import play.vfs.VirtualFile;
import service.AMService;
import utils.CommonUtils;
import utils.ParseUserFromExcelUtils;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

/**
 * User: liuhongjiang
 * Date: 13-10-23
 * Time: 下午5:33
 * 功能说明:
 */
public class ParseExcelService {
    /**
     * 从本地上传
     *
     * @param localPath
     */
    public static File upLoadExcel(String localPath) {
        VirtualFile vf = Play.getVirtualFile("/public/amExcel");
        File playFile = new File(vf.getRealFile().getAbsolutePath());
        File localFile = new File(localPath);
        IO.copyDirectory(localFile, playFile);
        return playFile;
    }

    /**
     * 解析考勤EXCEL
     *
     * @param excelFile
     * @return
     * @throws Exception
     */
    public static List<UserInfoDto> parseUserInfoFromExcel(File excelFile) throws Exception {
        InputStream is = new FileInputStream(excelFile);
        Workbook workbook = WorkbookFactory.create(is);
        UserInfoDto userInfoDto;
        List<UserInfoDto> list = new ArrayList<UserInfoDto>();
        Sheet hssfSheet = workbook.getSheetAt(0);//选择第一个工作表
        for (int rowNum = 1; rowNum <= hssfSheet.getLastRowNum(); rowNum++) {
            Logger.info("-------------开始解析EXCEL第%s行-----------------", rowNum);
            Row hssfRow = hssfSheet.getRow(rowNum);
            if (hssfRow == null) {
                continue;
            }
            userInfoDto = new UserInfoDto();
            // 循环列Cell
            // 0 1姓名 2部门 3打卡日期 4 打卡时间
            Cell xh = hssfRow.getCell(1);
            if (xh == null || StringUtils.isBlank(ParseUserFromExcelUtils.getStringValue(xh))) {
                continue;
            }
            userInfoDto.name = ParseUserFromExcelUtils.getStringValue(xh);
            Logger.info("用户名 %s", userInfoDto.name);

            xh = hssfRow.getCell(2);
            if (xh == null) {
                continue;
            }
            userInfoDto.department = ParseUserFromExcelUtils.getStringValue(xh);
            Logger.info("部门 %s", userInfoDto.department);

            xh = hssfRow.getCell(3);
            if (xh == null) {
                continue;
            }
            userInfoDto.punchedDate = ParseUserFromExcelUtils.getStringValue(xh);
            if(userInfoDto.punchedDate.equals("00:00")){
                userInfoDto.punchedDate = ParseUserFromExcelUtils.getDateValue(xh);
            }
            Logger.info("打卡日期 %s", userInfoDto.punchedDate);

            xh = hssfRow.getCell(4);
            if (xh == null) {
                continue;
            }
            String time = ParseUserFromExcelUtils.getStringValue(xh);
            showTime(time, userInfoDto);
            Logger.info("上班打卡时间 %s", userInfoDto.startTime);
            Logger.info("下班打卡时间 %s", userInfoDto.endTime);
            list.add(userInfoDto);
        }
        if (is != null) {
            is.close();
        }
        Logger.info("-----------解析excel完成!----------------");
        return list;
    }

    public static Workbook create(InputStream inp) throws IOException, InvalidFormatException {
        if (!inp.markSupported()) {
            inp = new PushbackInputStream(inp, 8);
        }

        if (POIFSFileSystem.hasPOIFSHeader(inp)) {
            return new HSSFWorkbook(inp);
        }
        if (POIXMLDocument.hasOOXMLHeader(inp)) {
            return new XSSFWorkbook(OPCPackage.open(inp));
        }
        throw new IllegalArgumentException("Your InputStream was neither an OLE2 stream, nor an OOXML stream");
    }

    /**
     * 根据EXCEL得到的打卡时间 区分上下班打卡记录
     *
     * @param times
     * @param userInfoDto
     * @return
     */
    private static UserInfoDto showTime(String times, UserInfoDto userInfoDto) {
        String[] time = times.split(" ");
        StringBuilder startTime = new StringBuilder();
        StringBuilder endTime = new StringBuilder();
        if (time == null || time.length == 0 || "".equals(time[0].trim())) {
            userInfoDto.startTime = null;
            userInfoDto.endTime = null;
        } else {
            for (String t : time) {
                long parseTime = CommonUtils.getParseDate(t);
                if (parseTime < CommonUtils.MIDDLETIME) {
                    startTime.append(t + ",");
                } else {
                    endTime.append(t + ",");
                }
            }
            if (startTime.length() > 0) {
                userInfoDto.startTime = startTime.substring(0, startTime.lastIndexOf(","));
            }
            if (endTime.length() > 0) {
                userInfoDto.endTime = endTime.substring(0, endTime.lastIndexOf(","));
            }
        }
        return userInfoDto;
    }

    //页面点击”最近三个月“ 会传过来13，这时要转换为真正的最近三个月的月份
    public static Set<Integer> parseMonth(int[] months,int thisMonth){
        Set<Integer> monthSet = Sets.newHashSet();
        if(months==null||months.length<1){
            return monthSet;
        }
        for (int month:months){
            if(month == 13){
               monthSet.add(thisMonth);
               monthSet.add(thisMonth-1>0?thisMonth-1:1);
               monthSet.add(thisMonth-2>0?thisMonth-2:1);
            } else {
                monthSet.add(month==0?thisMonth:month);
            }
        }
        return monthSet ;
    }

    /**
     * (取个玩的名字:)
     * 注释还是要写清楚：根据用户选择的月份和用户名得到考勤信息
     * @param am
     * @param months
     * @param name
     * @return
     */
    public static List<UserInfoDto> oOo(AMService am,int[] months,String name ){
        List<UserInfoDto> biggerList = Lists.newArrayList();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int thisMonth = calendar.get(Calendar.MONTH);

        Set<Integer> monthSet = parseMonth(months,thisMonth);
        if (monthSet.size()>0) {
            for (int month : monthSet) {
                List<UserInfoDto> list = am.searchInfoByNameAndMonth(name, month);
                if(list!=null && list.size()>0){
                    biggerList.addAll(list);
                }
            }
        } else {
            List<UserInfoDto> list = am.searchInfoByNameAndMonth(name, thisMonth);
            if(list!=null && list.size()>0){
                biggerList.addAll(list);
            }

        }
        return biggerList;
    }

}
