package com.pinyougou.seckill.service;
import java.util.List;

import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;

import entity.PageResult;
/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface SeckillOrderService {

	TbSeckillOrder findOrderByUserIdFromRedis(String userId);


	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbSeckillOrder> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);
	
	
	/**
	 * 增加
	*/
	public void add(TbSeckillOrder seckillOrder);
	
	
	/**
	 * 修改
	 */
	public void update(TbSeckillOrder seckillOrder);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbSeckillOrder findOne(Long id);
	
	
	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long[] ids);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize);

	/**
	 * 在redis中查询秒杀商品
	 * @param seckillGoodsId
	 * @return
	 */
	TbSeckillGoods findOneFromRedis(Long seckillGoodsId);

	/**
	 * 生成订单
	 * @param id
	 * @param userID
	 */
	void submitCreateOrder(Long id, String userID);

	/**
	 * 保存订单到数据库
	 * @param out_trade_no
	 * @param userId
	 * @param transaction_id
	 */
	void saveOrderRedisToDb(String out_trade_no, String userId, String transaction_id);

	/**
	 * 在缓存中删除订单的逻辑
	 * @param userId
	 * @param orderId
	 */
	void deleteOrderFromRedis(String userId,String orderId);
}
