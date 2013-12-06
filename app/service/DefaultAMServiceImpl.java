package service;

import dto.UserInfoDto;
import org.apache.commons.lang.time.DateUtils;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.AndFilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import play.Logger;
import play.libs.F;
import utils.CommonUtils;
import utils.ElasticsearchHelper;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * User: liuhongjiang
 * Date: 13-10-22
 * Time: 下午5:59
 * 功能说明: 新建自定义ES客户端和索引
 */
public class DefaultAMServiceImpl implements AMService {
    public static final String[] DATE_FORMAT_STR_ARR = {"yyyy-MM-dd", "HH:mm"};
    /**
     * 创建ES客户端
     */
    @Override
    public void createIndexLib() {
        try {
            Builder settings = ImmutableSettings.settingsBuilder()
                    .loadFromSource(getAnalysisSettings());
            ElasticsearchHelper.createIndex(index_name, settings);
        } catch (Exception e) {
            Logger.info("创建ES客户端时出错！");
            e.printStackTrace();
        }

    }

    /**
     * {
     * "index" : {
     * "analysis" : {
     * "analyzer" : {
     * "pinyin_analyzer" : {
     * "tokenizer" : "my_pinyin",
     * "filter" : ["standard"]
     * }
     * },
     * "tokenizer" : {
     * "my_pinyin" : {
     * "type" : "pinyin",
     * "first_letter" : "only",
     * "padding_char" : " "
     * }
     * }
     * }
     * }
     * }
     * first_letter即拼音首字母，可以设置为(默认为none)：
     * prefix , append , only 和none ，对应如“刘德华”的分词效果分别为”ldh liu de hua","liu de hua ldh","ldh","liu de hua"
     *
     * @return
     * @throws Exception
     */
    private static String getAnalysisSettings() throws Exception {
        XContentBuilder mapping = jsonBuilder()
                .startObject()
                .startObject("index")
                .startObject("analysis")
                .startObject("analyzer")
                .startObject("pinyin_analyzer")
                .field("tokenizer", "my_pinyin")
                .field("filter", new String[]{"standard"})
                .endObject()
                .startObject("qp_analyzer")
                .field("tokenizer", "my_qp")
                .field("filter", new String[]{"standard"})
                .endObject()
                .endObject()
                .startObject("tokenizer")    //支持拼音首字母检索
                .startObject("my_pinyin")
                .field("type", "pinyin")
                .field("first_letter", "only")
                .field("padding_char", "")
                .endObject()
                .startObject("my_qp") //支持全拼检索
                .field("type", "pinyin")
                .field("first_letter", "none")
                .field("padding_char", "")
                .endObject()
                .endObject()
                .endObject()
                .endObject()
                .endObject();
        System.out.println(mapping.string());
        return mapping.string();
    }

    /**
     * 自定义索引类型
     *
     * @throws Exception
     */
    @Override
    public void createNewsInfoMapping() throws Exception {
        XContentBuilder mapping = XContentFactory.jsonBuilder()
                .startObject()
                .startObject(index_type_news_info)
                .startObject("properties")
                .startObject(InfomationFieldMapping.NAME)   //打卡人姓名
                .field("type", "multi_field")
                .startObject("fields")
                .startObject(InfomationFieldMapping.NAME)
                .field("analyzer", "pinyin_analyzer")
                .field("boost", 10)
                .field("term_vector", "with_positions_offsets")
                .field("type", "string")
                .endObject()
                .startObject("qp")
                .field("analyzer", "qp_analyzer")
                .field("boost", 10)
                .field("term_vector", "with_positions_offsets")
                .field("type", "string")
                .endObject()
                .startObject("primitive")
                .field("include_in_all", "false")
                .field("store", "yes")
                .field("analyzer", "keyword")
                .field("type", "string")
                .endObject()
                .endObject()
                .endObject()
                .startObject(InfomationFieldMapping.DEPARTMENT)    //打卡人部门
                .field("type", "string")
                .field("store", "yes")
                .field("index", "not_analyzed")
                .endObject()
                .startObject(InfomationFieldMapping.PUNCHEDDATE)    //打卡日期
                .field("type", "date")
                .field("store", "yes")
                .field("index", "not_analyzed")
                .endObject()
                .startObject(InfomationFieldMapping.STARTTIME)  //  上班打卡时间
                .field("type", "string")
                .field("store", "yes")
                .field("index", "not_analyzed")
                .endObject()
                .startObject(InfomationFieldMapping.ENDTIME)   //下班打卡时间
                .field("type", "string")
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
        System.out.println(mapping.string());
        ElasticsearchHelper.createMapping(index_name, index_type_news_info, mapping);
    }

    /**
     * 开始创建索引
     *
     * @param newsList
     * @throws Exception
     */
    @Override
    public void doIndex(List<UserInfoDto> newsList) throws Exception {
        if (newsList == null || newsList.size() < 1) {
            return;
        }
        BulkRequestBuilder bulkRequestBuilder = ElasticsearchHelper.getClient().prepareBulk();
        for (UserInfoDto news : newsList) {
            bulkRequestBuilder.add(ElasticsearchHelper.getClient().prepareIndex(index_name, index_type_news_info)
                    .setSource(jsonBuilder()
                            .startObject()
                            .field(InfomationFieldMapping.NAME, news.name)
                            .field(InfomationFieldMapping.DEPARTMENT, news.department)
                            .field(InfomationFieldMapping.PUNCHEDDATE, news.punchedDate)
                            .field(InfomationFieldMapping.STARTTIME, news.startTime)
                            .field(InfomationFieldMapping.ENDTIME, news.endTime)
                            .field(InfomationFieldMapping.STATUS, news.status)
                            .endObject()
                    ));
        }
        Logger.info("索引完成");
        if (bulkRequestBuilder.numberOfActions() > 0) {
            ElasticsearchHelper.indexByBulk(bulkRequestBuilder);
        } else {
            Logger.info("这个批次没有相应的记录要索引");
        }
    }

    /**
     * 查询入口，输入姓名拼音首字母，得到response
     *
     * @param pinyin
     * @return
     */
    @Override
    public List<UserInfoDto> searchInfoByPinyin(String pinyin) {
        AndFilterBuilder newsFileFilterBuilder = FilterBuilders.andFilter().cache(false);
        //按更新时间倒序排
        SortBuilder sortBuilder = SortBuilders.fieldSort(InfomationFieldMapping.PUNCHEDDATE).order(SortOrder.DESC);
        newsFileFilterBuilder.add(FilterBuilders.prefixFilter(InfomationFieldMapping.NAME, pinyin));
        SearchResponse searchResponse = ElasticsearchHelper.doSearchByFilterWithSort(index_name, newsFileFilterBuilder, sortBuilder, index_type_news_info);
        List<UserInfoDto> list = ElasticsearchHelper.parseHits2List(searchResponse.hits(), UserInfoDto.class);
        return list;
    }

    /**
     * 查询入口，输入姓名，得到response
     * @param name
     * @return
     */
    @Override
    public List<UserInfoDto> searchInfoByName(String name) {
        AndFilterBuilder newsFileFilterBuilder = FilterBuilders.andFilter().cache(false);
        //按更新时间倒序排
        SortBuilder sortBuilder = SortBuilders.fieldSort(InfomationFieldMapping.PUNCHEDDATE).order(SortOrder.DESC);
//        QueryBuilder queryBuilder = QueryBuilders.fieldQuery(InfomationFieldMapping.PRIMITIVENAME,name);
//        newsFileFilterBuilder.add(FilterBuilders.queryFilter(queryBuilder));
        newsFileFilterBuilder.add(FilterBuilders.prefixFilter(InfomationFieldMapping.PRIMITIVENAME, name));
        SearchResponse searchResponse = ElasticsearchHelper.doSearchByFilterWithSort(index_name, newsFileFilterBuilder, sortBuilder, index_type_news_info);
        List<UserInfoDto> list = ElasticsearchHelper.parseHits2List(searchResponse.hits(), UserInfoDto.class);
        return list;
    }

    /**
     * 根据姓名，月份 得到response
     * @param name
     * @return
     */
    @Override
    public List<UserInfoDto> searchInfoByNameAndMonth(String name,int month) {
        Date startDate = CommonUtils.parseDate("2013-"+month+"-01");
        Date endDate = CommonUtils.parseDate("2013-"+month+"-31");
        AndFilterBuilder newsFileFilterBuilder = FilterBuilders.andFilter().cache(false);
        newsFileFilterBuilder.add(FilterBuilders.rangeFilter(InfomationFieldMapping.PUNCHEDDATE).from(startDate).to(endDate));
        newsFileFilterBuilder.add(FilterBuilders.prefixFilter(InfomationFieldMapping.PRIMITIVENAME, name));
        SearchResponse searchResponse = ElasticsearchHelper.doSearchByFilterWithoutSort(index_name, newsFileFilterBuilder, index_type_news_info);
        List<UserInfoDto> list = ElasticsearchHelper.parseHits2List(searchResponse.hits(), UserInfoDto.class);
        return list;
    }

    /**
     * 查询入口，输入姓名全拼，得到response
     *
     * @param qp
     * @return
     */
    @Override
    public List<UserInfoDto> searchInfoByQP(String qp) {
        AndFilterBuilder newsFileFilterBuilder = FilterBuilders.andFilter().cache(false);
        //按更新时间倒序排
        SortBuilder sortBuilder = SortBuilders.fieldSort(InfomationFieldMapping.PUNCHEDDATE).order(SortOrder.DESC);
        newsFileFilterBuilder.add(FilterBuilders.prefixFilter(InfomationFieldMapping.QPNAME, qp));
        SearchResponse searchResponse = ElasticsearchHelper.doSearchByFilterWithSort(index_name, newsFileFilterBuilder, sortBuilder, index_type_news_info);
        List<UserInfoDto> list = ElasticsearchHelper.parseHits2List(searchResponse.hits(), UserInfoDto.class);
        return list;
    }

    @Override
    public void del() {
        ElasticsearchHelper.deleteByQuery(index_name, index_type_news_info);
    }

    @Override
    public F.T2<Integer, Integer> getLateNumAndTime(List<UserInfoDto> list) {
        int lateNum = 0;
        int lateMinutes = 0;
        for (int i = 0; i < list.size(); i++) {
            int minutes = 0;
            String startTime = list.get(i).startTime;
            if (startTime != null) {
                if (startTime.contains(",")) {
                    startTime = startTime.split(",")[0];
                }
                try {
                    minutes = DateUtils.parseDate(startTime, DATE_FORMAT_STR_ARR).getMinutes();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (minutes > 45) {
                    lateNum = lateNum +1;
                    lateMinutes = lateMinutes + (minutes - 45);
                }
            }
        }
        return F.T2(lateNum, lateMinutes);
    }
}
