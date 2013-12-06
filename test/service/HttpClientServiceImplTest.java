package service;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * desc:
 * User: weiguili(li5220008@gmail.com)
 * Date: 13-11-28
 * Time: 上午9:19
 */
public class HttpClientServiceImplTest {
    public static HCS hcs;
    @Before
    public void setUp() throws Exception {
        hcs = new HttpClientServiceImpl();
    }
    @Test
    public void testService(){
        try {
            //第一步 登陆
            String url0="http://oa.gtadata.com/C6/JHSoft.Web.Login/AjaxForLogin.aspx";
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("loginCode", "d2VpZ2w="));
            nvps.add(new BasicNameValuePair("pwd", "MTIzNDU2"));
            nvps.add(new BasicNameValuePair("type", "login"));
            System.out.println(hcs.url(url0).nvp(nvps).post());

            //第二步 创建流程
            String url1="http://oa.gtadata.com/C6/JHsoft.Web.Workflow/WorkFlowTemplate/InstanceEntry.aspx";
            String html1 = hcs.url(url1).get();
            Document doc2 = Jsoup.parse(html1);
            Element li = doc2.select(".normal").first().child(0);
            String param = li.attr("onclick");
            Pattern p = Pattern.compile("^CreateInstance\\(\"(.+)\",\"(.+)\",\"(.+)\"\\)(.+)$");
            Matcher m = p.matcher(param);
            if(m.find()){
                System.out.println(m.groupCount());
                System.out.println(m.group(1));
                System.out.println(m.group(2));
                System.out.println(m.group(3));
                System.out.println(m.group(4));
            }

            //第三步 生成最终提交的表单
            StringBuilder urll3 = new StringBuilder(m.group(2).replace("JHSoft.Web.Module/fceform/common/djframe.htm","http://oa.gtadata.com/C6/JHSoft.Web.Module/ToolBar/toolbarwf.aspx"))
                    .append("&_FlowTemplateID=").append(m.group(1))
                    .append("&Version=").append(m.group(3))
                    .append("&_FlowInstanceID=0");
            String html3 = hcs.url(urll3.toString()).get();

            //第四步 转下一步
            Document doc3 = Jsoup.parse(html3);
            String urlDJ = "http://oa.gtadata.com/C6/JHSoft.Web.Module/eformaspx/WebBill.aspx?djupdate";//生成单据url
            String djNo = hcs.url(urlDJ).body("<root><add1>JHC</add1><no>SW5zZXJ0IGludG8ga3FqbGdnIChNYWluSUQsZGVwdCx4bSxycSxkcixkcnJxLGdnc2Jzancsc2Jzaix5eTEseXkyLGdneGJzancseGJzaixjYyxjY3NqLHosZCxiaCkgVmFsdWVzICgnOmdldF9rZXlmaWVsZCcsJ+acuuaehOW6lOeUqOaKgOacr+mDqCcsJ+mtj+i0teekvCcsJzIwMTMtMTItMDMnLCfmmK8nLCcyMDEzLTEyLTI1Jywn5pivJywnJywn5b+Y6K6w5omT5Y2hMTEwJywnJywnJywnJywnJywnJywnJywnJywnMTA2NTInKQ==</no></root>").post();
            System.out.println("单据号："+djNo);
            String param4 = doc3.select("#btn6").first().attr("onclick");
            Pattern p4 = Pattern.compile("^.*funDialogNext\\('(.*)','(.*)','(.*)','(.*)','(.*)','(.*)','(.*)','(.*)','(.*)','(.*)','(.*)','(.*)'\\).*$");
            Matcher m4 = p4.matcher(param4);
            if(m4.find()){
                System.out.println(m4.group(1));
                System.out.println(m4.group(2));
                System.out.println(m4.group(3));
                System.out.println(m4.group(4));
                System.out.println(m4.group(5));
                System.out.println(m4.group(6));
                System.out.println(m4.group(7));
                System.out.println(m4.group(8));
                System.out.println(m4.group(9));
                System.out.println(m4.group(10));
                System.out.println(m4.group(11));
                System.out.println(m4.group(12));
            }
            StringBuilder urll4 = new StringBuilder("http://oa.gtadata.com/C6/JHSoft.Web.WorkFlow/WorkFlow/NextStepSelectNew.aspx")
                    .append("?httAppTID=").append(m4.group(1))
                    .append("&httAppDID=").append(m4.group(2))
                    .append("&httCurrProcessName=").append(m4.group(3))
                    .append("&httCurrBtnText=").append(m4.group(4))
                    .append("&httAppID=").append(m4.group(6))
                    .append("&httDaType=").append(m4.group(7))
                    .append("&httCurUserID=").append(m4.group(8)/*.append("10652")这里可以改变提交的人，比如我的员工号 10652*/)
                    .append("&GroupCode=").append(m4.group(9))
                    .append("&Condition=").append(doc2.select("#app_hiddenFlowCondition").val())
                    .append("&_FlowInstanceID=").append(doc2.select("#hid_FlowInstanceTitle").val())
                    .append("&_FlowVersion=").append(m4.group(11))
                    .append("&httOperationType=").append(m4.group(5));
            String html4 = hcs.url(urll4.toString()).get();

            //第五步 提交表单
            Document doc4 = Jsoup.parse(html4);
            String[] receiveObjs = doc4.select("#InitReceiveObjs").first().child(0).val().split(",");

            doc3.select("#hidReceiveMan").val("<receiveMan><recobj><rorder>0</rorder><recid><![CDATA["+receiveObjs[1]+"]]></recid><deptid><![CDATA["+receiveObjs[0]+"]]></deptid></recobj></receiveMan>");//设置部门 和接收人
            doc3.select("#hidNextStep").val("3540");
            doc3.select("#hidPromptContent").val("考勤记录更改申请");
            doc3.select("#hidSms").val("N");
            doc3.select("#hidMail").val("1");
            doc3.select("#hidDel").val("0");
            doc3.select("#hidTransactMode").val("2");
            doc3.select("#hidDAType").val("6");
            doc3.select("#hidCall").val("1");
            doc3.select("#hidCurVersion").val("2.0");
            //doc3.select("#hid_value").val(djNo); //单据号，如何利用输入的考勤信息，生成这个单据号还没有破解。
            doc3.select("#hid_value").val("JHC00032046"); //写死了
            doc3.select("#hidTableName").val("kqjlgg");
            doc3.select("#hidAppID").val("-1");
            doc3.select("#hidTempAttributes").val("<root></root>");
            doc3.select("#hidFormName").val("考勤");
            doc3.select("#hidFormSN").val("kqjlggsq_"+doc3.select("#hidAppTID").val());

            FormElement form3 = (FormElement)doc3.select("#Form1").first();//强制转换成一个form
            Elements elements = form3.elements();
            List<Connection.KeyVal> lists = form3.formData();
            List<NameValuePair> nvps5 = new ArrayList<NameValuePair>(); //创建提交的键值参数
            for(Connection.KeyVal list : lists){
                nvps5.add(new BasicNameValuePair(list.key(),list.value()));
                System.out.println(list.key()+":"+list.value());
            }
            nvps5.add(new BasicNameValuePair("__EVENTTARGET","ToolBar1"));
            nvps5.add(new BasicNameValuePair("ToolBar1","6"));
            nvps5.add(new BasicNameValuePair("_FlowInstanceTitle","考勤记录更改"));
            //String html5 = hcs.url(url3.toString()).nvp(nvps5).post(); //这里提交的url第三步一样，只是提交的方法要用post方法。千万别随便提交，不然就搞大了。

            //第六步 退出系统
            hcs.url("http://oa.gtadata.com/C6/jhsoft.web.workflat/UserDispose.aspx?userid="+m4.group(8)).get();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {//保证释放链接
            hcs.close();
        }
    }
}
