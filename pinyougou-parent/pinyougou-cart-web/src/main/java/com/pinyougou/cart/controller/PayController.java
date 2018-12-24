package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.PayService;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.utils.IdWorker;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("pay")
@RestController
public class PayController {
    @Reference
    private PayService payService;
    @Autowired
    private IdWorker idWorker;



    /**
     * 查询支付状态
     * @param out_trade_no
     * @return
     */
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        Result result=null;
        int count = 1;
        while(true){
            //调用查询接口
            count++;
            if (count>=300){
                result=new  Result(false, "支付超時");
                break;
            }
            Map<String,String> map = payService.queryPayStatus(out_trade_no);
            if(map==null){//出错
                result=new  Result(false, "支付出错");
                break;
            }
            if(map.get("trade_state").equals("SUCCESS")){//如果成功
                result=new  Result(true, "支付成功");
                orderService.updateOrderStatus(out_trade_no,map.get("transaction_id") );
                break;
            }


            try {
                Thread.sleep(3000);//间隔三秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @Reference
    private OrderService orderService;
    /**
     * 生成二维码
     * @return
     */
    @RequestMapping("/createPayUri")
    public Map createNative(){
        //获取当前用户
        String userId= SecurityContextHolder.getContext().getAuthentication().getName();
        //到 redis 查询支付日志
        TbPayLog payLog = payService.searchPayLogFromRedis(userId);
        //判断支付日志存在
        if(payLog!=null){
            return payService.createNative(payLog.getOutTradeNo(),payLog.getTotalFee()+"");
        }else{
            return new HashMap();
        }
    }
}
