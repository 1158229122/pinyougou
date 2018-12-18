package com.pinyougou.page;

import java.util.List;

public interface ItemPageService {
    /**
     *生成商品详细页
     *@param goodsId
     */
    public boolean genItemHtml(Long goodsId);

    /**
     * 删除页面
     * @param goodsIds
     * @return
     */

    public boolean deleteItemHtml(List<Long> goodsIds);
}
