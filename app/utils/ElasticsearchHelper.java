package utils;

import com.google.gson.Gson;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.mapping.delete.DeleteMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.deletebyquery.DeleteByQueryResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilder;
import play.Logger;
import play.Play;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User: 刘建力(liujianli@gtadata.com))
 * Date: 13-3-28
 * Time: 上午11:22
 * 功能描述:
 */
public class ElasticsearchHelper {
    private static TransportClient client = null;
    public static int DEFAULT_PAGE_NO = 1;
    public static int DEFAULT_PAGE_SIZE = 50;

    static {
        client = new TransportClient().addTransportAddress(
                new InetSocketTransportAddress(Play.configuration.getProperty("elasticsearch.server", "localhost"),
                        Integer.valueOf(Play.configuration.getProperty("elasticsearch.port", "9300"))));
        //client.connectedNodes();

    }


    /**
     * 注:带高亮处理
     * @param hits
     * @param T
     * @param <T>
     * @return
     */
    public static <T> List<T> parseHits2List(SearchHits hits, Class<T> T) {
        List<T> results = new ArrayList<T>();
        for (SearchHit hit : hits) {
            Map<String, HighlightField> result = hit.highlightFields();
            for (String field : result.keySet()) {
                Text[] titleTexts = result.get(field).fragments();
                String source = "";
                for (Text text : titleTexts) {
                    source += text;
                }
                hit.getSource().put(field, source);
                hit.getSource().put("id", hit.id());
            }
            Gson jsonBuilder = CommonUtils.createIncludeNulls();
            String json =  CommonUtils.createIncludeNulls().toJson(hit.getSource());
            //String json = JSON.toJSONString(hit.getSource());
            T object =  jsonBuilder.fromJson(json, T);
          //  T object = JSON.parseObject(json, T);
            results.add(object);
        }
        return results;
    }

    public static SearchResponse doSearchByQuery(String index, QueryBuilder qb, int pageNo, int pageSize, String... types) {
        SearchResponse searchResponse = client.prepareSearch(index)
                .setQuery(qb)
                .setTypes(types)
               // .addHighlightedField("title")
                .setFrom((pageNo - 1) * pageSize)
                .setSize(pageSize)
                .setExplain(true)
                .execute()
                .actionGet();
        return searchResponse;
    }

    public static SearchResponse doSearchByQueryWithSort(String index, QueryBuilder qb,SortBuilder sortBuilder, int pageNo, int pageSize, String... types) {
        SearchResponse searchResponse = client.prepareSearch(index)
                .setQuery(qb)
                .setTypes(types)
                .setFrom((pageNo - 1) * pageSize)
                .setSize(pageSize)
                .addSort(sortBuilder)
                .setExplain(true)
                .execute()
                .actionGet();
        return searchResponse;
    }

    public static SearchResponse doSearchByFilter(String index, FilterBuilder filterBuilder, int pageNo, int pageSize, String... types) {
        SearchResponse searchResponse = client.prepareSearch(index)
                .setFilter(filterBuilder)
                .setTypes(types)
                .setFrom((pageNo - 1) * pageSize)
                .setSize(pageSize)
                .setExplain(true)
                .execute()
                .actionGet();
        return searchResponse;
    }
    public static SearchResponse doSearchByFilterWithSort(String index, FilterBuilder filterBuilder,SortBuilder sortBuilder, int pageNo, int pageSize, String... types) {
        SearchResponse searchResponse = client.prepareSearch(index)
                .setFilter(filterBuilder)
                .setTypes(types)
                .setFrom((pageNo - 1) * pageSize)
                .setSize(pageSize)
                .addSort(sortBuilder)
                .setExplain(true)
                .execute()
                .actionGet();
        return searchResponse;
    }

    public static SearchResponse doSearchByFilterWithSort(String index, FilterBuilder filterBuilder,SortBuilder sortBuilder, String... types) {
        SearchResponse searchResponse = client.prepareSearch(index)
                .setFilter(filterBuilder)
                .setTypes(types)
                .setSize(500)
                .addSort(sortBuilder)
                .setExplain(true)
                .execute()
                .actionGet();
        return searchResponse;
    }

    public static SearchResponse doSearchByFilterWithoutSort(String index, FilterBuilder filterBuilder, String... types) {
        SearchResponse searchResponse = client.prepareSearch(index)
                .setFilter(filterBuilder)
                .setTypes(types)
                .setSize(500)
                .setExplain(true)
                .execute()
                .actionGet();
        return searchResponse;
    }

    public static PutMappingResponse createMapping(String indexName, String indexType, XContentBuilder mapping) {
        try {
            PutMappingRequest mappingRequest = Requests.putMappingRequest(indexName)
                    .type(indexType)
                    .source(mapping);
            PutMappingResponse response = client.admin().indices().putMapping(mappingRequest).actionGet();
            return response;
        } catch (Exception e) {
            Logger.error("建立mapping出错");
            e.printStackTrace();
        }
        return null;
    }

    public static BulkResponse indexByBulk( BulkRequestBuilder bulkRequestBuilder) {
        BulkResponse bulkResponse = bulkRequestBuilder.execute().actionGet();
        if (bulkResponse.hasFailures()) {
            //处理错误
            Logger.error("建立mapping出错" + bulkResponse.buildFailureMessage());
        }
        return bulkResponse;
    }

    public static DeleteByQueryResponse deleteByQuery(String index,String type,QueryBuilder queryBuilder){
      // DeleteByQueryResponse response= new DeleteByQueryRequestBuilder(client).setQuery(queryBuilder).execute().actionGet();
        DeleteByQueryResponse response=client.prepareDeleteByQuery(index).setQuery(queryBuilder).setTypes(type).execute().actionGet();
       // client.deleteByQuery()
        return response;
    }
    public static void deleteByQuery(String index,String type){
        client.admin().indices().prepareDelete(index,type).execute().actionGet();
    }
    public static boolean isIndexExist(String indexName){
        return client.admin().indices().exists( new IndicesExistsRequest(indexName)).actionGet().exists();
    }
    public static CreateIndexResponse createIndex( String indexName) {
        return client.admin().indices().prepareCreate(indexName).execute().actionGet();
    }
    public static CreateIndexResponse createIndex( String indexName,Builder settings) {
        return client.admin().indices().prepareCreate(indexName).setSettings(settings).execute().actionGet();
    }

    public static Client getClient() {
        return client;
    }

    public static void close() {
        client.close();
    }

}
