package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import dto.MonthEventDto;
import dto.UserInfoDto;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import play.Logger;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 常用工具类
 * User: wenzhihong
 * Date: 12-9-13
 * Time: 下午12:03
 */
public abstract class CommonUtils {
    public static final String[] DATE_FORMAT_STR_ARR = {"yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyyMMddHHmmss"};
    public static final long AMTIME = getParseDate("08:45");  //GTA上班时间
    public static final long PMTIME = getParseDate("18:00");  //GTA下班时间
    public static final long MIDDLETIME = getParseDate("12:00"); //午休时间

    public static Gson createIncludeNulls(){
        return new GsonBuilder().registerTypeAdapter(Date.class, new TypeAdapter<Date>() {
            public Date read(JsonReader in) throws IOException {
                if (in.peek() == JsonToken.NULL) {
                    in.nextNull();
                    return null;
                }
                //2009-08-29T03:07:13.000Z 目前存的是这种格式的日期
                String value = in.nextString();
                /*  if(value.contains("T") && value.contains("Z")){
                    value =  value.replace("T"," ").replace("Z"," ");
                }*/
                try {
                    //return yyyyMMddHHmmss.parse(value);
                    return DateUtils.parseDate(value, new String[]{"yyyyMMddHHmmss"});
                } catch (ParseException e) {
                    return null;
                }

            }

            public void write(JsonWriter out, Date value) throws IOException {
                if (value == null) {
                    out.nullValue();
                    return;
                }
                //  LOCALFORMAT2.setTimeZone(TimeZone.getTimeZone("UTC"));
                String dateFormatAsString = DateFormatUtils.format(value, "yyyy-MM-dd HH:mm:ss");
                out.value(dateFormatAsString);
            }
        }).serializeNulls().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
    }

    /**
     * 把字符串解析成Date. 支持的字符串格式(yyyy-MM-dd)跟(yyyy-MM-dd hh:mm:ss)
     */
    public static Date parseDate(String d) {
        try {
            return DateUtils.parseDate(d, DATE_FORMAT_STR_ARR);
        } catch (ParseException e) {
            throw new NestableRuntimeException(e);
        }
    }

    public static long getParseDate(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        try {
            return sdf.parse(time).getTime();
        } catch (ParseException e) {
            Logger.info("不正确的日期格式");
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 根据excel得到的时间，判断打卡者状态。
     *
     * 状态（-1周末、0.正常、1.缺少上班打卡记录、2.缺少下班打卡记录、3.迟到、4.早退、5.旷工一天、
     *       6.下午没打卡且迟到,7.上午没打卡且早退,8迟到且早退，）
     * @param list
     * @return
     */
    public static MonthEventDto getStatus(List<UserInfoDto> list){
        final String[]  HOLIDAY = {"2013-10-1","2013-10-2","2013-10-3","2013-10-4","2013-10-5","2013-10-6","2013-10-7"};
        MonthEventDto eventDto = new MonthEventDto();
        for (UserInfoDto u : list) {
            if (u == null) {
                return null;
            }
            long punchTime;          //打卡时间
            int status = -2;            //状态
            String startTime = u.startTime == null ? null : u.startTime.split(",")[0];
            String endTime = u.endTime == null ? null : u.endTime.split(",")[u.endTime.split(",").length - 1];
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(parseDate(u.punchedDate));
            int x = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            getWeeks(u,x);
            eventDto.thisMonth = calendar.get(Calendar.MONTH)+1;
            if (x == 6 || x == 0) {   //周末
                if(!"2013-10-12".equals(u.punchedDate)){
                    u.status = -1;
                    if(startTime!=null || endTime!=null){
                        eventDto.extraWorkDay++;
                    }
                    continue;
                }
            }

            if (startTime == null && endTime == null) { //没有打卡记录

                if (u.punchedDate != null) {
                    status = 5;
                }

            } else if (startTime == null || endTime == null) {  //有一条打卡记录
                if (startTime == null) {
                    punchTime = getParseDate(endTime);

                } else {
                    punchTime = getParseDate(startTime);
                }
                //处理同一天只打了一次卡的情况
                if (punchTime > 0 && punchTime <= AMTIME) {   // 缺少下班打卡记录
                    status = 2;
                } else if (punchTime >= PMTIME) {   //缺少上班打卡记录
                    status = 1;
                } else if (punchTime > AMTIME && punchTime <= MIDDLETIME) {   // 下午没打卡且迟到
                    status = 6;
                } else if (punchTime > MIDDLETIME && punchTime < PMTIME) { // 上午没打卡且早退
                    status = 7;
                }

            } else {//有多条打卡记录
                long workingTime = getParseDate(startTime);  //上班打卡时间
                long restTime = getParseDate(endTime); //下班打卡时间

                if (workingTime <= AMTIME && restTime >= PMTIME) {       //正常
                    status = 0;
                } else if (workingTime <= AMTIME && restTime < PMTIME) {  //早退
                    status = 4;
                } else if (workingTime > AMTIME && restTime >= PMTIME) {   //迟到
                    status = 3;
                    eventDto.defectiveTime+=(workingTime - AMTIME)/(1000*60); //统计迟到总分钟数
                } else if (workingTime > AMTIME && restTime < PMTIME) {  //迟到且早退
                    status = 8;
                    eventDto.defectiveTime+=(workingTime - AMTIME)/(1000*60); //统计迟到总分钟数
                }

            }

            u.status = status;
            for(String s :HOLIDAY){ //过滤节假日
                if(s.equals(u.punchedDate)){
                    u.status = -3;
                    continue;
                }
            }
            if(u.status!=-1 && u.status!=0 && u.status!=-3){   //统计累计打卡异常次数
                eventDto.exceptionTimes++;
            }
        }

        return eventDto;

    }

    /**
     *  记录当天是星期几
     * @param userInfoDto
     * @param x
     */
     private static void getWeeks(UserInfoDto userInfoDto,int x){
         switch (x){
             case 0:
                 userInfoDto.weeks = "星期天";
                 break;
             case 1:
                 userInfoDto.weeks = "星期一";
                 break;
             case 2:
                 userInfoDto.weeks = "星期二";
                 break;
             case 3:
                 userInfoDto.weeks = "星期三";
                 break;
             case 4:
                 userInfoDto.weeks = "星期四";
                 break;
             case 5:
                 userInfoDto.weeks = "星期五";
                 break;
             case 6:
                 userInfoDto.weeks = "星期六";
                 break;
             default:
                 break;

         }

     }

}
