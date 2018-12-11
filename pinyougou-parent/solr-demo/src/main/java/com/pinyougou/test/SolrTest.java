package com.pinyougou.test;

import com.pinyougou.TbItem;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.ScoredPage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

}
