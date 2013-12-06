package controllers;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Chars;
import com.google.common.primitives.Ints;
import com.google.inject.internal.Sets;
import dto.MonthEventDto;
import dto.UserInfoDto;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.common.collect.Maps;
import play.*;
import play.libs.F;
import play.mvc.*;

import java.io.File;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import service.AMService;
import service.DefaultAMServiceImpl;
import utils.CommonUtils;
import utils.DrawPictrueUtil;
import utils.FileUtil;

public class Application extends Controller {
    public static final String[] DATE_FORMAT_STR_ARR = {"yyyy-MM-dd", "HH:mm"};
    public static AMService am = null;
    static {
         am = new DefaultAMServiceImpl();
    }

    public static void index(String name, final int flag) {
        //request.cookies.clear();
        MonthEventDto eventDto = new MonthEventDto();
        if (!StringUtils.isNotBlank(name)) {
            name  = URLDecoder.decode(request.cookies.get("name").value);
        }else {
            response.setCookie("name", URLEncoder.encode(name), null);
        }

        List<UserInfoDto> list = getUserInfoList(name);
        CommonUtils.getStatus(list,eventDto);
        Collections.sort(list, new Comparator<UserInfoDto>() {
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
        render(list, name, eventDto);
    }

    public static List<UserInfoDto> getUserInfoList(String name) {
        List<UserInfoDto> list = am.searchInfoByName(name);
        return list;
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

    public static void drawChart(String name) {
        String data = "";
        String data2 = "";
        if (StringUtils.isNotBlank(name)) {
            List<UserInfoDto> list = getUserInfoList(name);
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
                            endTime = endTime.split(",")[0];
                        }
                        hours2 = DateUtils.parseDate(endTime, DATE_FORMAT_STR_ARR).getHours();
                        minutes2 = DateUtils.parseDate(endTime, DATE_FORMAT_STR_ARR).getMinutes();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (startTime != null) {
                    data += "[Date.UTC(" + year + "," + month + "," + day + "),Date.UTC(" + year + "," + month + "," + 1 + "," + hours + "," + minutes + ")],";

                }
                if (endTime != null) {
                    data2 += "[Date.UTC(" + year + "," + month + "," + day + "),Date.UTC(" + year + "," + month + "," + 1 + "," + hours2 + "," + minutes2 + ")],";
                }

            }
            data = data.substring(0, data.length() - 1) + "]";
            data2 = data2.substring(0, data2.length() - 1) + "]";
        }
        Logger.info("data==========" + data);
        Logger.info("data2==========" + data2);
        render(data,data2);
    }

}