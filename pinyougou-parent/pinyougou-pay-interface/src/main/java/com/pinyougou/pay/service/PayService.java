package com.pinyougou.pay.service;

import com.pinyougou.pojo.TbPayLog;

import java.util.Map;

public interface PayService {
    /**
     * 支付链接
     * @return
     */
    Map<String, String> createNative(String out_trade_no,String total_fee);
    /**
     * 查询支付状态
     * @param out_trade_no
     */
    public Map queryPayStatus(String out_trade_no);

    /**
     * 根据用户查询 payLog
     * @param userId
     * @return
     */
    public TbPayLog searchPayLogFromRedis(String userId);

    /**
     * 关闭支付
     * @param out_trade_no
     * @return
     */
    public Map closePay(String out_trade_no);
}
