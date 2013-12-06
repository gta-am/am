package dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import utils.CommonUtils;

import java.util.Date;

/**
 * User: liuhongjiang
 * Date: 13-10-23
 * Time: 下午5:39
 * 功能说明:
 */
public class UserInfoDto {

    @Expose
    public String name;

    @Expose
    public String department;

    @Expose
    public String punchedDate;

    @Expose
    public String startTime;

    @Expose
    public String endTime;

    @Expose
    public int status;

    public String result;
    /*
        记录星期几
     */
    public String weeks;

    public Date parsePunchedDate() {
        return CommonUtils.parseDate(this.punchedDate);
    }



    public UserInfoDto showStatus( UserInfoDto userInfoDto){
        int status = userInfoDto.status;
        userInfoDto.result = "未知状态";
        if(status == 0){
            userInfoDto.result="正常";
        }else if(status == 1){
            userInfoDto.result="上班没打卡 (@﹏@)~ ";
            userInfoDto.startTime = "缺";
        }else if(status == 2){
            userInfoDto.result="下班没打卡 o(>﹏<)o";
            userInfoDto.endTime = "缺";
        }else if(status == 3){
            userInfoDto.result="迟到 ⊙﹏⊙";
            if(!userInfoDto.startTime.contains("迟到"))  {
                userInfoDto.startTime += "(迟到)";
            }
        }else if(status == 4){
            userInfoDto.result="早退 ⊙﹏⊙";
            if(!userInfoDto.endTime.contains("早退"))  {
                userInfoDto.endTime += "(早退)";
            }
        }else if(status == 5){
            userInfoDto.result="旷工一天 ⊙﹏⊙";
            userInfoDto.startTime = "缺";
            userInfoDto.endTime = "缺";
        }else if(status == 6){
            userInfoDto.result="下午没打卡且迟到 ⊙﹏⊙";
            if(!userInfoDto.startTime.contains("迟到"))  {
                userInfoDto.startTime += "(迟到)";
            }
            userInfoDto.endTime = "缺";
        }else if(status == 7){
            userInfoDto.result="上午没打卡且早退 ⊙﹏⊙";
            userInfoDto.startTime = "缺";
            if(!userInfoDto.endTime.contains("早退"))  {
                userInfoDto.endTime += "(早退)";
            }
        }else if(status == 8){
            userInfoDto.result="迟到且早退 ⊙﹏⊙";
            if(!userInfoDto.startTime.contains("迟到"))  {
                userInfoDto.startTime += "(迟到)";
            }
            if(!userInfoDto.endTime.contains("早退"))  {
                userInfoDto.endTime += "(早退)";
            }
        } else if(status == -1){
            userInfoDto.result = "周末  O(∩_∩)O~~";
            if(userInfoDto.startTime==null){
                userInfoDto.startTime = "--";
            }
            if(userInfoDto.endTime==null){
                userInfoDto.endTime = "--";
            }
        } else if(status == -3){
            userInfoDto.result = "国庆  O(∩_∩)O~~";
            userInfoDto.startTime = "--";
            userInfoDto.endTime = "--";
        }
        return userInfoDto;
    }
}
