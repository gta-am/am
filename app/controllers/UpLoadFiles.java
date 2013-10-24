package controllers;

import business.CreateESMappingService;
import business.UpLoadExcelService;
import dto.UserInfoDto;
import play.Logger;
import play.mvc.Controller;

import java.io.File;
import java.util.List;

/**
 * User: liuhongjiang
 * Date: 13-10-24
 * Time: 上午9:56
 * 功能说明:
 */
public class UpLoadFiles extends Controller {
    public static final String FILE_PATH = "public/amExcel/am.xls";
    public static void upLoadFile(String localPath){
       // File file = UpLoadExcelService.upLoadExcel("D:\\excel");
        try {
            File file = new File(FILE_PATH);
            List<UserInfoDto> list = UpLoadExcelService.parseUserInfoFromExcel(file);
           // CreateESMappingService.createESMapping(list);
        } catch (Exception e) {
            Logger.info("解析excel失败-->"+e.getMessage());

        }
        renderText("success");
    }


}
