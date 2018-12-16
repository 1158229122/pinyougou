package com.pinyougou;

import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:spring/applicationContext*.xml")
public class freeMaker {
    @Autowired
    private TbGoodsMapper goodsMapper;
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private TbItemCatMapper itemCatMapper;
    @Autowired
    private TbItem item;
    @Test
    public void test() throws Exception {
        Template template = freeMarkerConfigurer.getConfiguration().getTemplate("item.html");
        Long goodsId = 149187842867980L;
        Map map = new HashMap();
        //添加商品
        TbGoods tbGoods = goodsMapper.selectByPrimaryKey(goodsId);
        map.put("goods",tbGoods );
        //添加商品详情
        TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
        map.put("goodsDesc",goodsDesc);
        //查询商品分类的名字
        String itemCat1 = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory1Id()).getName();
        String itemCat2 = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory2Id()).getName();
        String itemCat3 = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id()).getName();
        map.put("itemCat1", itemCat1);
        map.put("itemCat2", itemCat2);
        map.put("itemCat3", itemCat3);

        //查询商品的sku列表
        TbItemExample ex = new TbItemExample();
        TbItemExample.Criteria criteria = ex.createCriteria();
        criteria.andGoodsIdEqualTo(goodsId);//设置商品的id
        criteria.andStatusEqualTo("2");//设置有效状态
        ex.setOrderByClause("is_default desc");

        List<TbItem> itemList = itemMapper.selectByExample(ex);
        map.put("itemList",itemList );

        Writer out =new FileWriter(new File(goodsId+".html"));

        template.process(map,out  );
        out.close();
    }

}
