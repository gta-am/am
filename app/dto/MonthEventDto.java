package dto;

/**
 * User: liuhongjiang
 * Date: 13-12-2
 * Time: 上午10:43
 * 功能说明:
 */
public class MonthEventDto {
    /*
        当前月份
     */
    public String thisMonth;
    /*
     本月打卡异常次数
    */
    public int exceptionTimes;

    /*
      本月早上累计迟到时间
     */
    public long defectiveTime;
    /*
        加班天数
     */
    public int extraWorkDay;

}
