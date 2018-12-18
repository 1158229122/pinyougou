package com.pinyougou.search.service.activelistener;

import com.alibaba.fastjson.JSON;
import com.pinyougou.activemq.ActiveMq;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.*;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class ItemSearchListener implements MessageListener {
    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {

        try {


            if (ActiveMq.ADD_INDEX_TO_SOLR.equalsIgnoreCase(message.getJMSType()))
                {//跟新索引
                    TextMessage textMessage = (TextMessage)message;
                    System.out.println("jian听到的消息"+textMessage.getText());
                    List<TbItem> list = JSON.parseArray(textMessage.getText(),TbItem.class);
                    itemSearchService.importList(list);
                }

            if (ActiveMq.DELETE_INDEX_TO_SOLR.equalsIgnoreCase(message.getJMSType())){
                //删除索引
                ObjectMessage objectMessage = (ObjectMessage)message;
                Long[] goodsIds= (Long[]) objectMessage.getObject();
                itemSearchService.deleteByGoodsIds(Arrays.asList(goodsIds));
            }


        } catch (JMSException e) {
            e.printStackTrace();

        }
    }
}
