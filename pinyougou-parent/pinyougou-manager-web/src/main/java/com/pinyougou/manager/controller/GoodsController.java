package com.pinyougou.manager.controller;
import java.util.List;


import com.alibaba.fastjson.JSON;

import com.pinyougou.activemq.ActiveMq;
import com.pinyougou.pojo.TbItem;

import entity.Goods;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.omg.CORBA.PRIVATE_MEMBER;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import entity.Result;

import javax.jms.*;

/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;
	@Autowired
	private JmsTemplate jmsTemplate;




	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){			
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){			
		return goodsService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody Goods goods){
		try {
			goodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}

	/**
	 * 审核
	 * @param ids
	 * @param status
	 * @return
	 */
	@Autowired
	private Destination queueTextDestination;

	@Autowired
	private Destination topicTextDestination;
	@RequestMapping("updateStatus")
	public Result updateStatus(Long ids[], String status) {
		Result result = new Result();
		try {
			/**
			 * 提交页面
			 */
			//静态页生成
			if ("2".equalsIgnoreCase(status)){
				for(Long goodsId:ids){
					jmsTemplate.send(topicTextDestination, new MessageCreator() {
						@Override
						public Message createMessage(Session session) throws JMSException {
                            TextMessage textMessage = session.createTextMessage();
                            textMessage.setJMSType(ActiveMq.ADD_PAGE_TO_FREEMAKER);
                            textMessage.setText(goodsId+"");
                            return textMessage;
						}
					});
					//itemPageService.genItemHtml(goodsId);
				}
			}


			/**
			 * 跟新索引发送消息队列
			 */
			List<TbItem> itemList = goodsService.findItemListByGoodsIdandStatus(ids, status);
			String jsonString = JSON.toJSONString(itemList);
			if (itemList.size()>0&&itemList!=null){
 				jmsTemplate.send(queueTextDestination, new MessageCreator() {
					@Override

					public Message createMessage(Session session) throws JMSException {
                        TextMessage textMessage = session.createTextMessage();
                        textMessage.setJMSType(ActiveMq.ADD_INDEX_TO_SOLR);
                        textMessage.setText(jsonString);
						System.out.println(jsonString);
                        return textMessage;
					}
				});
			}else {
				System.out.println("没有明细的数据");
			}
			goodsService.updateStatus(ids, status);
			result.setSuccess(true);
			result.setMessage("提交成功");
		} catch (Exception e) {
			e.printStackTrace();
			result.setMessage("提交失败");
		}
		return result;
	}
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){
		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id){
		return goodsService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long [] ids){
		try {
			//删除索引

			jmsTemplate.send(queueTextDestination, new MessageCreator() {
				@Override
				public Message createMessage(Session session) throws JMSException {
                    ObjectMessage objectMessage = session.createObjectMessage();
                    objectMessage.setJMSType(ActiveMq.DELETE_INDEX_TO_SOLR);
                    objectMessage.setObject(ids);
                    return objectMessage;
				}
			});

			//删除静态页面
            //删除每个服务器上的商品详细页
            jmsTemplate.send(topicTextDestination, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    ObjectMessage objectMessage = session.createObjectMessage();
                    objectMessage.setJMSType(ActiveMq.DELETE_PAGE_TO_FREEMAKER);
                    objectMessage.setObject(ids);
                    return objectMessage;
                    //return session.createObjectMessage(ids);
                }
            });


            goodsService.delete(ids);
			return new Result(true, "删除成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param goods
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){

//		String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
//		goods.setSellerId(sellerId);
		return goodsService.findPage(goods, page, rows);		
	}
	
}
