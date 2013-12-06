package service;

import dto.UserInfoDto;
import play.libs.F;

import java.util.Arrays;
import java.util.List;

/**
 * User: 刘建力(liujianli@gtadata.com))
 * Date: 13-3-20
 * Time: 上午9:46
 * 功能描述:
 */
public interface AMService {
    public static final String index_name = "am_data_test";
    public static final String index_type_news_info = "am_info_test";

    /**
     * 创建索引库,相当于创建数据库
     */
    public void createIndexLib();

    public void createNewsInfoMapping() throws Exception;

    public void doIndex(List<UserInfoDto> newsList) throws Exception;

    public List<UserInfoDto> searchInfoByName(String name);

    public List<UserInfoDto> searchInfoByNameAndMonth(String name,int month);

    public List<UserInfoDto> searchInfoByPinyin(String pinyin);

    public List<UserInfoDto> searchInfoByQP(String qp);

    public F.T2<Integer, Integer> getLateNumAndTime(List<UserInfoDto> list);

    public void del();

    /**
     * 索引字段名描述
     */
    public static class InfomationFieldMapping {
        /**
         * 打卡人姓名
         */
        public static final String NAME = "name";
        public static final String PRIMITIVENAME = "name.primitive";
        public static final String QPNAME = "name.qp";
        /**
         * 打卡人部门
         */
        public static final String DEPARTMENT = "department";
        /**
         * 打卡日期
         */
        public static final String PUNCHEDDATE   = "punchedDate";
        /**
         * 上班打卡时间
         */
        public static final String STARTTIME  = "startTime";
        /**
         * 下班打卡时间
         */
        public static final String ENDTIME  = "endTime";
        /**
         * 状态（-1.未知状态、0.正常、1.缺少上班打卡记录、2.缺少下班打卡记录、3.迟到、4.早退、5.旷工一天,6.下午没打卡且迟到,7.上午没打卡且早退,8迟到且早退）
         */
        public static final String STATUS  = "status";
    }
}
