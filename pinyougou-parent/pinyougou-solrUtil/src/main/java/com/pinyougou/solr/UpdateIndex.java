package com.pinyougou.solr;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class UpdateIndex {


    public static void main(String[] args) {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
        TbItemMapper itemMapper = applicationContext.getBean(TbItemMapper.class);
        SolrTemplate solrTemplate = applicationContext.getBean(SolrTemplate.class);

        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");
        List<TbItem> items = itemMapper.selectByExample(example);
        for (TbItem item : items) {
            Map map = JSON.parseObject(item.getSpec(), Map.class);
            item.setSpecMap(map);
        }
        solrTemplate.saveBeans(items);
        solrTemplate.commit();


    }
}
