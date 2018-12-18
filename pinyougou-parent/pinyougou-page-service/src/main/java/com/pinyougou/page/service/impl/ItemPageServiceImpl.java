package com.pinyougou.page.service.impl;

import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.ItemPageService;

import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemPageServiceImpl implements ItemPageService {
    @Value("${createUrl}")
    private String crateUrl;
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


    @Override
    public boolean genItemHtml(Long goodsId) {
        Writer out = null;
        try {
            System.out.println(crateUrl);
            Template template = freeMarkerConfigurer.getConfiguration().getTemplate("item.html");
            Map map = new HashMap();
            //添加商品

            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(goodsId);
            map.put("goods",tbGoods );
            //添加商品详情
            TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            map.put("goodsDesc",goodsDesc);
            //查询商品分类的名字
            System.out.println(tbGoods);

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

            out = new FileWriter(new File(crateUrl+goodsId+".html"));
            template.process(map,out);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (TemplateException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out!=null){
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    @Override
    public boolean deleteItemHtml(List<Long> goodsIds) {
        try {
            for(Long goodsId:goodsIds){
                new File(crateUrl+goodsId+".html").delete();
            }
            return true;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }
}
