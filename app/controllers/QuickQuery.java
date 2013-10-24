package controllers;

import business.UpLoadExcelService;
import dto.UserInfoDto;
import models.InfomationModel;
import play.mvc.Controller;
import service.AMService;
import service.DefaultAMServiceImpl;

import java.util.List;

/**
 * User: liuhongjiang
 * Date: 13-10-23
 * Time: 上午9:23
 * 功能说明:
 */
public class QuickQuery extends Controller{

    public static void query(String name){
        AMService am = new DefaultAMServiceImpl();
        List<UserInfoDto> list = am.searchInfoByName(name);
        render(list);
    }




}
