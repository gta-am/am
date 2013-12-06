package controllers;

import business.ParseExcelService;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import com.google.inject.internal.Sets;
import dto.MonthEventDto;
import dto.UserInfoDto;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.elasticsearch.common.collect.Lists;
import play.*;
import play.data.binding.As;
import play.mvc.*;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.*;

import service.AMService;
import service.DefaultAMServiceImpl;
import utils.CommonUtils;
import utils.DrawPictrueUtil;

public class Application extends Controller {
    public static final String[] DATE_FORMAT_STR_ARR = {"yyyy-MM-dd", "HH:mm"};
    public static AMService am = null;

    static {
        am = new DefaultAMServiceImpl();
    }

    public static void index(String name) {
        if (!StringUtils.isNotBlank(name) &&request.cookies.get("name")!=null) {
            name = URLDecoder.decode(request.cookies.get("name").value);
        } else {
            response.setCookie("name", URLEncoder.encode(name==null?"":name), null);
        }

        render(name);
    }


    /**
     * 自动补全 拼音  汉字
     *
     * @param pinyin
     */
    public static void showName(String pinyin) {
        Set<String> nameSet = Sets.newHashSet();
        if (pinyin == null) {
            pinyin = "";
        }
        AMService am = new DefaultAMServiceImpl();
        List<UserInfoDto> list = am.searchInfoByPinyin(pinyin);
        if (list == null || list.size() == 0) {
            list = am.searchInfoByName(pinyin);
        }
        if (list == null || list.size() == 0) {
            list = am.searchInfoByQP(pinyin);
        }
        // CommonUtils.getStatus(list);
        for (UserInfoDto u : list) {
            nameSet.add(u.name);
        }
        renderJSON(nameSet);
    }



    public static void drawChart(String name,int[] months) {
        String data = "";
        String data2 = "";
        String _start = null  ;
        String _end = null ;

        if (!StringUtils.isNotBlank(name)) {
            name  = URLDecoder.decode(request.cookies.get("name").value);
        }

        List<UserInfoDto> list = ParseExcelService.oOo(am,months,name);
        Collections.sort(list, new Comparator<UserInfoDto>() {
            @Override
            public int compare(UserInfoDto o1, UserInfoDto o2) {
                return Ordering.natural().nullsLast().compare(CommonUtils.parseDate(o1.punchedDate), CommonUtils.parseDate(o2.punchedDate));
            }
        });

        data += "[";
        data2 = "[";
        for (int i = 0; i < list.size(); i++) {
            String[] formatDate = DrawPictrueUtil.getFormatDate(list.get(i).parsePunchedDate());
            String year = formatDate[0];
            String month = formatDate[1];
            String day = formatDate[2];
            int hours = 0;
            int minutes = 0;
            int hours2 = 0;
            int minutes2 = 0;
            String startTime = list.get(i).startTime;
            String endTime = list.get(i).endTime;
            try {
                if (startTime != null) {
                    if (startTime.contains(",")) {
                        startTime = startTime.split(",")[0];
                    }
                    hours = DateUtils.parseDate(startTime, DATE_FORMAT_STR_ARR).getHours();
                    minutes = DateUtils.parseDate(startTime, DATE_FORMAT_STR_ARR).getMinutes();
                }
                if (endTime != null) {
                    if (endTime.contains(",")) {
                        endTime = endTime.split(",")[endTime.split(",").length-1];
                    }
                    hours2 = DateUtils.parseDate(endTime, DATE_FORMAT_STR_ARR).getHours();
                    minutes2 = DateUtils.parseDate(endTime, DATE_FORMAT_STR_ARR).getMinutes();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (startTime != null) {
                data += "[Date.UTC(" + year + "," + month + "," + day + "),Date.UTC(" + 0 + "," + 0 + "," + 0 + "," + hours + "," + minutes + ")],";

            }
            if (endTime != null) {
                data2 += "[Date.UTC(" + year + "," + month + "," + day + "),Date.UTC(" + 0 + "," + 0 + "," + 0 + "," + hours2 + "," + minutes2 + ")],";
            }
            if(i == 0){
                _start = "Date.UTC(" + year + "," + month + "," + day +")";
            }
            if(i == (list.size()-1)){
                _end = "Date.UTC(" + year + "," + month + "," + day +")";
            }

        }
        data = data.substring(0, data.length() - 1) + "]";
        data2 = data2.substring(0, data2.length() - 1) + "]";
        Logger.info("data==========" + data);
        Logger.info("data2==========" + data2);
        Logger.info("_start==========" + _start);
        Logger.info("_end==========" + _end);
        render(data, data2,_start,_end);
    }

    /**
     * @param months 用户选择的月份
     */
    public static void showDetail(int[] months, String name, final int flag) {
        int totalExceptionTimes = 0; //总异常打卡次数
        long totalDefectiveTime = 0; //总迟到分钟数
        int totalextraWorkDay = 0;   //总加班数
        List<UserInfoDto> biggerList = Lists.newArrayList();
        List<MonthEventDto> eventList = Lists.newArrayList();
        if (!StringUtils.isNotBlank(name)) {
            name = URLDecoder.decode(request.cookies.get("name").value);
        } else {
            response.setCookie("name", URLEncoder.encode(name), null);
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int thisMonth = calendar.get(Calendar.MONTH);

        Set<Integer> monthSet = ParseExcelService.parseMonth(months,thisMonth);
        if (monthSet.size()>0) {
            for (int month : monthSet) {
                List<UserInfoDto> list = am.searchInfoByNameAndMonth(name, month);
                if(list!=null && list.size()>0){
                    MonthEventDto monthEventDto = CommonUtils.getStatus(list);
                    eventList.add(monthEventDto);
                    totalExceptionTimes += monthEventDto.exceptionTimes;
                    totalDefectiveTime += monthEventDto.defectiveTime;
                    totalextraWorkDay += monthEventDto.extraWorkDay;
                    biggerList.addAll(list);
                }
            }
        } else {
            List<UserInfoDto> list = am.searchInfoByNameAndMonth(name, thisMonth);
            if(list!=null && list.size()>0){
                MonthEventDto monthEventDto = CommonUtils.getStatus(list);
                eventList.add(monthEventDto);
                totalExceptionTimes += monthEventDto.exceptionTimes;
                totalDefectiveTime += monthEventDto.defectiveTime;
                totalextraWorkDay += monthEventDto.extraWorkDay;
                biggerList.addAll(list);
            }

        }

         int count = monthSet.size()<1?1:monthSet.size();
         int level;
        if(totalExceptionTimes == 0 && totalDefectiveTime == 0){
            level =1;
        }else if(totalExceptionTimes/count <= 2 && totalDefectiveTime == 0){
            level = 2;
        }else if(totalExceptionTimes/count <= 3 && totalDefectiveTime/count <= 10){
            level = 3;
        }else if(totalExceptionTimes/count > 10 && totalDefectiveTime/count > 45){
            level = 5;
        }else {
            level = 4;
        }


        Collections.sort(biggerList, new Comparator<UserInfoDto>() {
            @Override
            public int compare(UserInfoDto o1, UserInfoDto o2) {
                if (flag == 1) {
                    return Ordering.natural().reverse().nullsLast().compare(CommonUtils.parseDate(o1.punchedDate), CommonUtils.parseDate(o2.punchedDate));
                } else if (flag == 2) {
                    return Ordering.natural().reverse().nullsLast().compare(o1.department, o2.department);
                } else {
                    return Ints.compare(o2.status, o1.status);
                }

            }
        });
        Collections.sort(eventList, new Comparator<MonthEventDto>() {
            @Override
            public int compare(MonthEventDto o1, MonthEventDto o2) {
                return Ints.compare(o2.thisMonth, o1.thisMonth);
            }

        });
        render(biggerList, name, eventList, totalExceptionTimes, totalDefectiveTime, totalextraWorkDay,level);
    }

}