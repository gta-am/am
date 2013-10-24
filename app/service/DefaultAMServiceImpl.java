package service;

import models.InfomationModel;
import org.apache.commons.lang.time.DateFormatUtils;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.AndFilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import play.Logger;
import utils.ElasticsearchHelper;

import java.util.List;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * User: liuhongjiang
 * Date: 13-10-22
 * Time: 下午5:59
 * 功能说明: 新建自定义ES客户端和索引
 */
public class DefaultAMServiceImpl implements AMService {
    /**
     * 创建ES客户端
     */
    @Override
    public void createIndexLib() {
        ElasticsearchHelper. createIndex(index_name);
    }

    /**
     * 自定义索引类型
     * @throws Exception
     */
    @Override
    public void createNewsInfoMapping() throws Exception {
        XContentBuilder mapping= XContentFactory.jsonBuilder()
                .startObject()
                .startObject(index_type_news_info)
                .startObject("properties")
                .startObject(InfomationFieldMapping.NAME)   //打卡人姓名
                .field("type", "string")
                .field("store", "yes")
                .field("indexAnalyzer", "ik")
                .field("searchAnalyzer", "ik")
                .endObject()
                .startObject(InfomationFieldMapping.DEPARTMENT)    //打卡人部门
                .field("type", "string")
                .field("store", "yes")
                .field("index", "not_analyzed")
                .endObject()
                .startObject(InfomationFieldMapping.PUNCHEDDATE)    //打卡日期
                .field("type", "long")
                .field("store", "yes")
                .field("index", "not_analyzed")
                .endObject()
                .startObject(InfomationFieldMapping.STARTTIME)  //  上班打卡时间
                .field("type", "long")
                .field("store", "yes")
                .field("index", "not_analyzed")
                .endObject()
                .startObject(InfomationFieldMapping.ENDTIME)   //下班打卡时间
                .field("type", "long")
                .field("store", "yes")
                .field("index", "not_analyzed")
                .endObject()
                .startObject(InfomationFieldMapping.STATUS)     //状态 0.正常、1.缺少上班打卡记录、2.缺少下班打卡记录、3.迟到、4.早退、5.旷工一天
                .field("type", "integer")
                .field("store", "yes")
                .field("index", "not_analyzed")
                .endObject()
                .endObject()
                .endObject()
                .endObject();
        ElasticsearchHelper.createMapping(index_name, index_type_news_info, mapping);
    }

    /**
     * 开始创建索引
     * @param newsList
     * @throws Exception
     */
    @Override
    public void doIndex(List<InfomationModel> newsList) throws Exception {
        if (newsList == null || newsList.size() < 1) {
            return ;
        }
        int index = 0;
        BulkRequestBuilder bulkRequestBuilder = ElasticsearchHelper.getClient().prepareBulk();
        for (InfomationModel news : newsList) {
            bulkRequestBuilder.add(ElasticsearchHelper.getClient().prepareIndex(index_name, index_type_news_info)
                    .setSource(jsonBuilder()
                            .startObject()
                            .field(InfomationFieldMapping.NAME, news.name)
                            .field(InfomationFieldMapping.DEPARTMENT, news.department)
                            .field(InfomationFieldMapping.PUNCHEDDATE, Long.parseLong(DateFormatUtils.format(news.punchedDate, "yyyyMMdd")))
                            .field(InfomationFieldMapping.STARTTIME, Long.parseLong(DateFormatUtils.format(news.startTime, "HHmm")))
                            .field(InfomationFieldMapping.ENDTIME, Long.parseLong(DateFormatUtils.format(news.endTime, "HHmm")))
                            .field(InfomationFieldMapping.STATUS, news.status)
                            .endObject()
                    ));
        }
        if (bulkRequestBuilder.numberOfActions() > 0) {
            ElasticsearchHelper.indexByBulk(bulkRequestBuilder);
        } else {
            Logger.info("这个批次没有相应的记录要索引");
        }
    }

    /**
     * 查询入口，在这里得到response
     * @param name
     * @return
     */
    @Override
    public List<InfomationModel> searchInfoByName(String name) {
        AndFilterBuilder newsFileFilterBuilder = FilterBuilders.andFilter().cache(false);
        //按更新时间倒序排
        SortBuilder sortBuilder = SortBuilders.fieldSort(InfomationFieldMapping.PUNCHEDDATE).order(SortOrder.DESC);
        newsFileFilterBuilder.add(FilterBuilders.inFilter(InfomationFieldMapping.NAME,name));
        SearchResponse searchResponse =  ElasticsearchHelper.doSearchByFilterWithSort(index_name, newsFileFilterBuilder, sortBuilder, index_type_news_info);
        List<InfomationModel> list =  ElasticsearchHelper.parseHits2List(searchResponse.hits(), InfomationModel.class);
        return list;
    }
}
