package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.common.json.JSON;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Service;
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
    @Override
    public Map<String, Object> search(Map searchMap)  {
        Map<String, Object> map = new HashMap<String, Object>();
        //获取搜索的条件
        String search = (String) searchMap.get("search");
        if(StringUtils.isEmpty(search)){
            search="*";//如果搜索条件为空,查询所有
        }
        SolrQuery query = new SolrQuery("item_keywords:"+search);
        //获取搜索条件结束

        //获取条件查询的列表
        map.put("rows",getListSearchMap(query,search));

        //分组查询

        if(!"*".equals(search)){
            map.put("category", searchGroup(query));//如果没有输入条件的时候不启用分组查询
            List<String> categoryList = searchGroup(query);

            //条件查询获取品牌和规格
            for (String str : categoryList) {
                Map brandAndSpec = searchBrandAndSpec(str);
                String json = null;
                try {
                    json = JSON.json(brandAndSpec);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println(json);
                System.out.println(str);
                if (brandAndSpec.size()>0){
                    map.putAll(brandAndSpec);
                }
            }


        }
        return map;
        }



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
    private List<TbItem> getListSearchMap(SolrQuery query, String search) {
        List<TbItem> itemList = new ArrayList<TbItem>();

        try {

            query.setHighlight(true);//开启高亮
            query.setHighlightSimplePre("<em color=red>");//设置前缀
            query.setHighlightSimplePost("</em>");//设置后缀
            query.addHighlightField("item_title");//添加需要被高亮的域
            QueryResponse queryResponse = solrServer.query(query);
            Map<String, Map<String, List<String>>> highlighting = queryResponse.getHighlighting();//获取到高亮的字段集合

            SolrDocumentList results = queryResponse.getResults();//获取到所有的元素结果集合
            for (SolrDocument document : results) {
                String item_id = (String) document.get("id");

                String item_title = null;
                if (highlighting!=null&&highlighting.get(item_id)!=null&&highlighting.get(item_id).get("item_title")!=null&&!"*".equals(search)){
                    item_title = highlighting.get(item_id).get("item_title").get(0);
                }else {
                    item_title = (String) document.get("item_title");
                }
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

        } catch (SolrServerException e) {
            e.printStackTrace();
        }
        return itemList;
    }

    //分组查询
    private List<String> searchGroup(SolrQuery query){
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
