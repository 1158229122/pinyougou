package com.pinyougou.seckill.service.impl;
import java.util.Date;
import java.util.List;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import com.pinyougou.utils.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.pojo.TbSeckillOrderExample;
import com.pinyougou.pojo.TbSeckillOrderExample.Criteria;
import com.pinyougou.seckill.service.SeckillOrderService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service(timeout=10000)
public class SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;

	@Override
	public TbSeckillOrder findOrderByUserIdFromRedis(String userId) {
		return (TbSeckillOrder)
				redisTemplate.boundHashOps("seckillOrder").get(userId);
	}

	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillOrder> findAll() {
		return seckillOrderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSeckillOrder> page=   (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.insert(seckillOrder);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillOrder seckillOrder){
		seckillOrderMapper.updateByPrimaryKey(seckillOrder);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillOrder findOne(Long id){
		return seckillOrderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			seckillOrderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSeckillOrderExample example=new TbSeckillOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(seckillOrder!=null){			
						if(seckillOrder.getUserId()!=null && seckillOrder.getUserId().length()>0){
				criteria.andUserIdLike("%"+seckillOrder.getUserId()+"%");
			}
			if(seckillOrder.getSellerId()!=null && seckillOrder.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+seckillOrder.getSellerId()+"%");
			}
			if(seckillOrder.getStatus()!=null && seckillOrder.getStatus().length()>0){
				criteria.andStatusLike("%"+seckillOrder.getStatus()+"%");
			}
			if(seckillOrder.getReceiverAddress()!=null && seckillOrder.getReceiverAddress().length()>0){
				criteria.andReceiverAddressLike("%"+seckillOrder.getReceiverAddress()+"%");
			}
			if(seckillOrder.getReceiverMobile()!=null && seckillOrder.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+seckillOrder.getReceiverMobile()+"%");
			}
			if(seckillOrder.getReceiver()!=null && seckillOrder.getReceiver().length()>0){
				criteria.andReceiverLike("%"+seckillOrder.getReceiver()+"%");
			}
			if(seckillOrder.getTransactionId()!=null && seckillOrder.getTransactionId().length()>0){
				criteria.andTransactionIdLike("%"+seckillOrder.getTransactionId()+"%");
			}
	
		}
		
		Page<TbSeckillOrder> page= (Page<TbSeckillOrder>)seckillOrderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
	@Autowired
	private RedisTemplate redisTemplate;

	@Override
	public TbSeckillGoods findOneFromRedis(Long seckillGoodsId) {
		TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillGoodsId);

		if (seckillGoods==null){
			throw new RuntimeException("商品没有啦");
		}
		return seckillGoods;
	}
	@Autowired
	private IdWorker idWorker;
	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;
	@Override
	public void submitCreateOrder(Long id, String userID) {
		TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(id);
		if (seckillGoods==null){
			//商品不存在
			throw new RuntimeException("订单不存在");
		}
		if (seckillGoods.getStockCount()<=0){
			throw new RuntimeException("商品已抢购一空");
		}


		//如果用户存在抢购订单,恢复之前订单的库存
		TbSeckillOrder existOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userID);

		recoverSeckillGoods(existOrder);
		//如果用户存在抢购订单,恢复之前订单的库存结束




		//商品存在减1
		seckillGoods.setStockCount(seckillGoods.getStockCount()-1);
		//放入缓存
		redisTemplate.boundHashOps("seckillGoods").put(id,seckillGoods );
		//如果减1后商品变为o插入数据库删除缓存


		if (seckillGoods.getStockCount()==0){
			seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
			//删除缓存
			redisTemplate.boundHashOps("seckillGoods").delete(id);
		}
		//商品还有生成订单
		TbSeckillOrder seckillOrder = new TbSeckillOrder();
		seckillOrder.setId(idWorker.nextId());
		seckillOrder.setSeckillId(seckillGoods.getGoodsId());
		seckillOrder.setCreateTime(new Date());
		seckillOrder.setMoney(seckillGoods.getCostPrice());
		seckillOrder.setUserId(userID);
		seckillOrder.setSellerId(seckillGoods.getSellerId());
		seckillOrder.setStatus("0");//状态
		redisTemplate.boundHashOps("seckillOrder").put(userID,seckillOrder );
	}

	private void recoverSeckillGoods(TbSeckillOrder existOrder) {
		if (existOrder!=null){
			//如果有存在的订单恢复库存
			TbSeckillGoods exitsGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(existOrder.getSeckillId());

			if (exitsGoods!=null){
				//缓存中还存在该商品
				exitsGoods.setStockCount(exitsGoods.getStockCount()+1);
				redisTemplate.boundHashOps("seckillGoods").put(exitsGoods.getGoodsId(),exitsGoods );

			}else {
				//缓存中该商品没有了,在数据库中查
				TbSeckillGoodsExample example = new TbSeckillGoodsExample();
				TbSeckillGoodsExample.Criteria criteria = example.createCriteria();
				criteria.andGoodsIdEqualTo(existOrder.getSeckillId());
				TbSeckillGoods tbSeckillGoods = seckillGoodsMapper.selectByExample(example).get(0);
				System.out.println(tbSeckillGoods+"goods");
				tbSeckillGoods.setStockCount(1);
				redisTemplate.boundHashOps("seckillGoods").put(tbSeckillGoods.getGoodsId(),tbSeckillGoods );
			}
		}
	}

	@Override
	public void saveOrderRedisToDb(String out_trade_no, String userId, String transaction_id) {
		System.out.println("saveOrderFromRedisToDb:"+userId);
		TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
		if (seckillOrder==null){
			//商品不存在
			throw new RuntimeException("商品不存在");
		}
		if (seckillOrder.getId().longValue()!=new Long(out_trade_no).longValue()){
			throw  new RuntimeException("订单不符合");
		}
		seckillOrder.setTransactionId(transaction_id);
		seckillOrder.setPayTime(new Date());
		seckillOrder.setUserId(userId);
		seckillOrder.setStatus("3");
		seckillOrderMapper.insert(seckillOrder);
		//在缓存中删掉订单
		redisTemplate.boundHashOps("seckillOrder").delete(userId);
	}

	@Override
	public void deleteOrderFromRedis(String userId, String orderId) {
		TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
		if (seckillOrder!=null&&seckillOrder.getId().equals(orderId)){
			//合法的订单,在缓存在删除订单
			redisTemplate.boundHashOps("seckillOrder").delete(userId);
			//恢复库存
			recoverSeckillGoods(seckillOrder);
		}
	}

}
