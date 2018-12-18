package com.pinyougou.search.service;

import com.pinyougou.pojo.TbItem;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ItemSearchService {
    /**
     * 条件查询
     * @param map
     * @return
     */
    public Map<String,Object> search(Map map) ;

    /**
     * 导入数据
     * @param list
     */
    public void importList(List list);

    /**
     * 删除数据
     * @param
     */
    public void deleteByGoodsIds(List goodsIdList);
}
