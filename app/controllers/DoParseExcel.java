package controllers;

import business.CreateESMappingService;
import business.ParseExcelService;
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
public class DoParseExcel extends Controller {
    private static List<UserInfoDto> list = null;
    public static void upLoadFile(File file){
       // File file = ParseExcelService.upLoadExcel("D:\\excel");
        try {
            //File file = new File("D:\\excel\\11.xlsx");
            if(file!=null){
                if(list==null ){
                    list = ParseExcelService.parseUserInfoFromExcel(file);
                }
                CreateESMappingService.createESMapping(list);
            }

        } catch (Exception e) {
            Logger.info("there is a exception-->"+e.getMessage());
            renderText("fail");

        }
        render();
    }



}
