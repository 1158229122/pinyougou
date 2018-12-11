package com.pinyougou.search.service;

import java.util.Map;

public interface ItemSearchService {
    /**
     * 条件查询
     * @param map
     * @return
     */
    public Map<String,Object> search(Map map);
}
