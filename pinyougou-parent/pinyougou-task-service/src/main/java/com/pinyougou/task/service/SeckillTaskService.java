package com.pinyougou.task.service;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class SeckillTaskService {
    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Scheduled(cron = "0 * * * * ?")
    public void taskUpdateSeckill(){
        Set keys = redisTemplate.boundHashOps("seckillGoods").keys();//获取每一个key
        List ids = new ArrayList(keys);//将key转化为list集合
        TbSeckillGoodsExample ex = new TbSeckillGoodsExample();
        TbSeckillGoodsExample.Criteria criteria = ex.createCriteria();
        criteria.andStockCountGreaterThan(0);//剩余库存大于 0
        criteria.andStartTimeLessThanOrEqualTo(new Date());//开始时间小于等于当前时间
        criteria.andEndTimeGreaterThan(new Date());//结束时间大于当前时间
        criteria.andStatusEqualTo("2");//审核通过
        criteria.andIdNotIn(ids);
        List<TbSeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(ex);
        //saveToRedis
        if (seckillGoodsList!=null&&seckillGoodsList.size()>0){
            for (TbSeckillGoods seckillGoods : seckillGoodsList) {
                redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(),seckillGoods );
            }
        }
        System.out.println("定时执行更新秒杀商品的服务");
    }
    @Scheduled(cron = "* * * * * ?")
    public void deleteOverDue(){
        List<TbSeckillGoods> seckillGoodsList = redisTemplate.boundHashOps("seckillGoods").values();
        for (TbSeckillGoods tbSeckillGoods : seckillGoodsList) {
            if (tbSeckillGoods.getEndTime().getTime()<new Date().getTime()){//过期商品
                redisTemplate.boundHashOps("seckillGoods").delete(tbSeckillGoods.getId());
            }
        }
        System.out.println("定时执行删除秒杀商品过期的任务");
    }
}
