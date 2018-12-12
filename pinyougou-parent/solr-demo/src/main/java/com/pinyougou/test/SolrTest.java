package com.pinyougou.test;

import com.pinyougou.TbItem;
import org.apache.http.client.HttpClient;
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
import org.apache.solr.common.params.SolrParams;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.ScoredPage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/applicationContext-solr.xml")
public class SolrTest {
    @Autowired
    private SolrTemplate solrTemplate;
    @Test
    public void add(){
        //添加
        List list  = new ArrayList();

        for (int i = 0; i < 100; i++) {
            TbItem item = new TbItem();
            item.setId(i+1L);
            item.setBrand("华为"+i);
            item.setCategory("手机");
            item.setGoodsId(i+1L);
            item.setSeller("华为 2 号专卖店");
            item.setTitle(i+"华为 Mate9");
            item.setPrice(new BigDecimal(2000));
            list.add(item);
        }

        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }
    //根据id查询
    @Test
    public void findByid(){
        TbItem byId = solrTemplate.getById(1, TbItem.class);
        System.out.println(byId);
    }
    @Test
    public void search(){
        //条件查询
        Query query = new SimpleQuery();//查询所有
        //是个坑
        Criteria criteria = new Criteria("item_title").contains("华为");
        criteria = criteria.and("item_title").contains("0");
        query.addCriteria(criteria);

        ScoredPage<TbItem> items = solrTemplate.queryForPage(query, TbItem.class);
        for (TbItem item : items) {
            System.out.println(item);
        }



    }
    @Test
    public void delete (){
        Query query=new SimpleQuery("*:*");
        solrTemplate.delete(query);
        solrTemplate.commit();

    }
    //原生查询
    @Test
    public void rootSearch() throws SolrServerException {
        HttpSolrServer solrServer = new HttpSolrServer("http://127.0.0.1:8080/solr");
        SolrQuery query = new SolrQuery("item_keywords:三星");
        query.setHighlight(true);
        query.addHighlightField("item_title");
        query.setHighlightSimplePre("<em color='red'>");
        query.setHighlightSimplePost("</em>");
        //分组=========================
        query.setParam(GroupParams.GROUP,true);//是否分组
        query.setParam(GroupParams.GROUP_FIELD,"item_category");//分组的域
       // query.setParam(GroupParams.GROUP_LIMIT,"1");//每组显示的个数，默认为1

        QueryResponse response = null;
        try {
            response = solrServer.query(query, SolrRequest.METHOD.POST);
            Map<String, Integer> info = new HashMap<String, Integer>();
            GroupResponse groupResponse = response.getGroupResponse();
            if(groupResponse != null) {
                List<GroupCommand> groupList = groupResponse.getValues();
                for(GroupCommand groupCommand : groupList) {
                    List<Group> groups = groupCommand.getValues();
                    for(Group group : groups) {

                        System.out.println(group.getGroupValue());
                    }
                }
            }
        }catch (SolrServerException e) {
            e.printStackTrace();
        }






//        QueryResponse queryResponse = solrServer.query(query);
//        Map<String, Map<String, List<String>>> highlighting = queryResponse.getHighlighting();
//
//
//
//        SolrDocumentList results = queryResponse.getResults();
//        for (SolrDocument document : results) {
//            String item_id = (String) document.get("id");
//            String item_title = highlighting.get(item_id).get("item_title").get(0);
//            System.out.println(item_title);
//
//            Double item_price = (Double) document.get("item_price");
//            String item_image = (String) document.get("item_image");
//            Long item_goodsid = (Long) document.get("item_goodsid");
//            String item_category = (String) document.get("item_category");
//            String item_brand = (String) document.get("item_brand");
//           // String item_spec_网络 = (String) document.get("item_spec_网络");
//            // String item_spec_机身内存 = (String) document.get("item_spec_机身内存");
//            TbItem item = new TbItem();
//            //item.setId(item_id);
//            item.setTitle(item_title);
//            item.setPrice(new BigDecimal(item_price));
//            item.setImage(item_image);
//            item.setBrand(item_brand);
//            System.out.println(item);
//        }




//        Map ma = new HashMap();
//        Object obj = ma.get("obj");
//        System.out.println(obj);
    }


}
