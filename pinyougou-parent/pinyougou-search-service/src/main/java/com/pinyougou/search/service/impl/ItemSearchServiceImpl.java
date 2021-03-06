package com.pinyougou.search.service.impl;

import com.alibaba.druid.sql.ast.expr.SQLCaseExpr;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.Group;
import org.apache.solr.client.solrj.response.GroupCommand;
import org.apache.solr.client.solrj.response.GroupResponse;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.GroupParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SolrDataQuery;
import sun.java2d.pipe.SpanShapeRenderer;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service(timeout=3000)
public class ItemSearchServiceImpl implements ItemSearchService {
    @Value("${solrUrl}")
    private String solrUrl;

    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private HttpSolrServer solrServer;
    @Autowired
    private RedisTemplate redisTemplate;

    public void updateIndex(TbItem item){
       solrTemplate.saveBean(item);
    }

    /**
     * 条件查询
     * @param searchMap
     * @return
     */
    @Override
    public Map<String, Object> search(Map searchMap)  {
        Map<String, Object> map = new HashMap<String, Object>();
        //获取搜索的条件
        String keywords = (String) searchMap.get("keywords");
        if (!StringUtils.isEmpty(keywords)){
            keywords = keywords.replace(" ", "");//处理空字符串
        }

        if("".equals(keywords)){
            keywords="*";//如果搜索条件为空,查询所有
            System.out.println(keywords);
        }
        SolrQuery query = new SolrQuery("item_keywords:"+keywords);
        //获取搜索条件结束

        //设置过滤条件
        addFilterSearch(query,searchMap);
        //过滤条件设置结束

        //获取条件查询的列表
        map.putAll(getListSearchMap(query,keywords));

        //分组查询

        if(!"*".equals(keywords)){
            map.put("category", searchGroup(keywords));//如果没有输入条件的时候不启用分组查询
            List<String> categoryList = searchGroup(keywords);

            //条件查询获取品牌和规格
            if (categoryList.size()>0){
                Map brandAndSpec = searchBrandAndSpec(categoryList.get(0));
                map.putAll(brandAndSpec);
            }
        }
        return map;
    }

    @Override
    public void importList(List list) {
        for (Object obj : list) {
            TbItem item = (TbItem) obj;
            Map map = JSON.parseObject(item.getSpec(), Map.class);
            item.setSpecMap(map);
        }
        solrTemplate.saveBeans(list);
        solrTemplate.commit();

    }

    @Override
    public void deleteByGoodsIds(List goodsIdList) {
        Query query = new SimpleQuery();
        Criteria criteria=new Criteria("item_goodsid").in(goodsIdList);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    //设置用户需要过滤的条件
    private void addFilterSearch(SolrQuery query,Map searchMap){
        //设置价格排序过滤
        Map<String,String> sort = (Map<String, String>) searchMap.get("sort");//获取排序对象
       if (sort!=null&&sort.size()>0){//entity.getkey是域字段entity.get.getGetValue是排序字段
           for (Map.Entry<String, String> entry : sort.entrySet()) {
               if (!StringUtils.isEmpty(entry.getKey())&&!StringUtils.isEmpty(entry.getValue())){
                        if ("asc".equalsIgnoreCase(entry.getValue())){
                            query.addSort(entry.getKey(), SolrQuery.ORDER.asc);
                        }
                        if ("desc".equalsIgnoreCase(entry.getValue())){
                            query.addSort(entry.getKey(), SolrQuery.ORDER.desc);
                        }

               }
           }
       }




        //设置品牌过滤
        String brand = (String) searchMap.get("brand");
        if (!"".equals(brand)){
            query.addFilterQuery("item_brand:"+brand);
        }
        //设置分类过滤
        String category = (String) searchMap.get("category");
        if(!"".equals(category)){
            query.addFilterQuery("item_category:"+category);
        }
        //设置规格选项
        Map<String,String> spec = (Map<String,String>) searchMap.get("spec");
        if(spec!=null){
            for (Map.Entry<String, String> entry : spec.entrySet()) {
                query.addFilterQuery("item_spec_"+entry.getKey()+":"+entry.getValue());
            }
        }
        //过滤价格
        String priceStr = (String) searchMap.get("price");
        if (!"".equals(priceStr)){
            String[] price = priceStr.split("-");
            query.addFilterQuery("item_price:["+price[0]+" TO "+price[1]+"]");
        }
        Integer currentPage = (Integer) searchMap.get("currentPage");//获取当前页
        if (currentPage==null){
            currentPage = 1;
        }
        Integer pageRows = (Integer) searchMap.get("pageRows");
        if (pageRows==null){
            pageRows = 20;
        }
        query.setStart((currentPage-1)*pageRows);//设置起始索引
        query.setRows(pageRows);//设置每页的条数


    }


    /***************** 通过分类获取品牌列表和规格列表 ***************/
    private Map searchBrandAndSpec(String category){
        Map map = new HashMap();
        long templateId = (long) redisTemplate.boundHashOps("itemCat").get(category);
        if(templateId!=0){
            System.out.println(templateId);
            List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(templateId);
            map.put("brandList", brandList);
            List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(templateId);
            map.put("specList", specList);
        }
        return map;
    }



    //获取条件查询的列表
    private Map<String, Object> getListSearchMap(SolrQuery query, String search) {
        List<TbItem> itemList = new ArrayList<TbItem>();
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            /******************** 高亮开始 ************************/
            query.setHighlight(true);//开启高亮
            query.setHighlightSimplePre("<em style= 'color: red'>");//设置前缀
            query.setHighlightSimplePost("</em>");//设置后缀
            query.addHighlightField("item_title");//添加需要被高亮的域
            QueryResponse queryResponse = solrServer.query(query);
            Map<String, Map<String, List<String>>> highlighting = queryResponse.getHighlighting();//获取到高亮的字段集合
            /******************** 高亮条件设置参数结束 ************************/
            SolrDocumentList results = queryResponse.getResults();//获取到所有的元素结果集合

            for (SolrDocument document : results) {
                String item_id = (String) document.get("id");

                /******************** 设置高亮的标题 ************************/
                String item_title = null;
                if (highlighting!=null&&highlighting.get(item_id)!=null&&highlighting.get(item_id).get("item_title")!=null&&!"*".equals(search)){
                    item_title = highlighting.get(item_id).get("item_title").get(0);
                }else {
                    item_title = (String) document.get("item_title");
                }
                /******************** 高亮标题设置借宿 ************************/

                Double item_price = (Double) document.get("item_price");
                String item_image = (String) document.get("item_image");
                Long item_goodsid = (Long) document.get("item_goodsid");
                String item_category = (String) document.get("item_category");
                String item_brand = (String) document.get("item_brand");
                //添加值
                TbItem item = new TbItem();
                item.setId((long) StringUtils.parseInteger(item_id));
                item.setTitle(item_title);
                item.setPrice(new BigDecimal(item_price));
                item.setImage(item_image);
                item.setBrand(item_brand);
                item.setGoodsId(item_goodsid);
                item.setCategory(item_category);
                itemList.add(item);

            }
            long numFound = results.getNumFound();
            map.put("totalPage",numFound);
            map.put("rows",itemList );
        } catch (SolrServerException e) {
            e.printStackTrace();
        }

        return map;
    }

    //分组查询
    private List<String> searchGroup(String keywords){
        SolrQuery query = new SolrQuery("item_keywords:"+keywords);
        query.setParam(GroupParams.GROUP,true);//是否分组
        query.setParam(GroupParams.GROUP_FIELD,"item_category");//分组的域
        // query.setParam(GroupParams.GROUP_LIMIT,"1");//每组显示的个数，默认为1
        QueryResponse response = null;
        List<String> categoryList = new ArrayList<String>();//用于接收分组的值
        try {
            response = solrServer.query(query, SolrRequest.METHOD.POST);
            GroupResponse groupResponse = response.getGroupResponse();
            if(groupResponse != null) {
                List<GroupCommand> groupList = groupResponse.getValues();
                for(GroupCommand groupCommand : groupList) {
                    List<Group> groups = groupCommand.getValues();

                    for(Group group : groups) {
                        categoryList.add(group.getGroupValue());
                    }
                }
            }
        }catch (SolrServerException e) {
            e.printStackTrace();
        }

        return categoryList;
    }

}
