package utils;



import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

/**
 * User: liuhongjiang
 * Date: 13-4-27
 * Time: 上午10:05
 */

public class DrawPictrueUtil {

    /*
    *java中对日期的加减操作
    *gc.add(1,-1)表示年份减一.
    *gc.add(2,-1)表示月份减一.
    *gc.add(3.-1)表示周减一.
    *gc.add(5,-1)表示天减一.
    *GregorianCalendar类的add(int field,int amount)方法表示年月日加减.
    *field参数表示年,月.日等.
    *amount参数表示要加减的数量.
    *
    */
    public static Date getTime(Date date ,int d ,int t){
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        GregorianCalendar gc =new GregorianCalendar();
        gc.setTime(date);
        gc.add(d,t);
        gc.set(gc.get(Calendar.YEAR),gc.get(Calendar.MONTH),gc.get(Calendar.DATE));
        return  gc.getTime();
        //return  sdf.format(gc.getTime());

    }

    /**
     * 根据指定格式获取时间 (用户格式化 绘图数据)
     * @param     date 给定时间
     * @return     指定格式的时间
     */
    public static  String[] getFormatDate(Date date){
        String[] formatDate;
        if(date==null){
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        String year =  sdf.format(date);
        // Logger.debug("year=========="+year);
        SimpleDateFormat sdf1 = new SimpleDateFormat("MM");
        String month =sdf1.format(date);
        int m=0;
        try {
            m = Integer.parseInt(month);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        //在highcharts中 0表示1月  需要把实际月份-1 才能在绘图工具中正确显示
        m = m-1;
        //Logger.debug("M=========="+m);
        SimpleDateFormat sdf2 = new SimpleDateFormat("dd");
        String day =sdf2.format(date);
        formatDate=new String[]{year,m+"",day};
        return   formatDate;
    }

    //格式化时间
    public static  String getFormatMaxorMinDate(Date maxDate){
        String[] formatDate = getFormatDate(maxDate)  ;
        if(formatDate.length!=3){
            return null;
        }
        String year =   formatDate[0];
        String month =  formatDate[1];
        String day =   formatDate[2];
        String s = "Date.UTC("+year+","+month+","+day+")";
        return s;
    }



}
