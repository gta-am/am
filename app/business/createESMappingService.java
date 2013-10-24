package business;

import com.tom.springutil.StopWatch;
import models.InfomationModel;
import play.Logger;
import service.AMService;
import service.DefaultAMServiceImpl;
import utils.ElasticsearchHelper;

import java.util.List;

/**
 * User: liuhongjiang
 * Date: 13-10-23
 * Time: 上午9:38
 * 功能说明:
 */
public class createESMappingService {

    public void createESMapping(List<InfomationModel> list) {
        try{
            AMService am = new DefaultAMServiceImpl();
            if(ElasticsearchHelper.isIndexExist(am.index_name)){
                Logger.info("索引已存在，不创建，任务退出");
            }else{
                StopWatch sw = new StopWatch("索引数据");
                sw.start("建索引库");
                am.createIndexLib();
                sw.stop();
                sw.start("建索引表");
                am.createNewsInfoMapping();
                sw.stop();
                sw.start("开始索引");
                am.doIndex(list);
                sw.stop();
            }
        }catch (Exception e){
            System.out.println("createESMappingService--> :" + e.getMessage());
        }
    }
}