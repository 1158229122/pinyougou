package com.pinyougou.activemq;

public interface ActiveMq {
    String ADD_INDEX_TO_SOLR = "pinyougou_queue_solr";
    String DELETE_INDEX_TO_SOLR = "pinyougou_queue_delete_solr";
    String ADD_PAGE_TO_FREEMAKER = "pinyougou_topic_add_page";
    String DELETE_PAGE_TO_FREEMAKER = "pinyougou_topic_delete_page";
}
