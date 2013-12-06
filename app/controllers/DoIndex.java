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
public class DoIndex extends Controller {
    //创建索引
    public static void doIndex(File file,boolean flag) {
        try {
            if (file != null) {
                if(flag){ //createESClient
                    List<UserInfoDto> list = ParseExcelService.parseUserInfoFromExcel(file);
                    CreateESMappingService.createESMapping(list);
                }else { //upDateESData
                    List<UserInfoDto> list = ParseExcelService.parseUserInfoFromExcel(file);
                    CreateESMappingService.addIndex(list);
                }
            }

        } catch (Exception e) {
            Logger.info("throws exception-->" + e.getMessage());
            renderText("fail");

        }
        render();
    }

}
