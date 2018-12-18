package com.pinyougou.page.service.pageLitstener;


import com.pinyougou.activemq.ActiveMq;
import com.pinyougou.page.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.*;
import java.io.Serializable;
import java.util.Arrays;

public class PageListener implements MessageListener {
    @Autowired
    private ItemPageService itemPageService;
    @Override
    public void onMessage(Message message) {
        try {
                if (ActiveMq.ADD_PAGE_TO_FREEMAKER.equalsIgnoreCase(message.getJMSType())){
                //新建页面
                    TextMessage textMessage = (TextMessage)  message;
                    String id = textMessage.getText();
                    itemPageService.genItemHtml(Long.parseLong(id));
                }
                if (ActiveMq.DELETE_PAGE_TO_FREEMAKER.equalsIgnoreCase(message.getJMSType())){
                    ObjectMessage objectMessage = (ObjectMessage)  message;
                    Long [] goodsId =(Long []) objectMessage.getObject();
                    itemPageService.deleteItemHtml(Arrays.asList(goodsId));
                }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
