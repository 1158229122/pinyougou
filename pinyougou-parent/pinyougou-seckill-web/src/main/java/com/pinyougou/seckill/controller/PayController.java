package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;

import com.pinyougou.pay.service.PayService;
import com.pinyougou.pojo.TbPayLog;


import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
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
    @Reference
    private SeckillOrderService seckillOrderService;




    /**
     * 查询支付状态
     * @param out_trade_no
     * @return
     */
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        //获取当前用户
        String
                userId=SecurityContextHolder.getContext().getAuthentication().getName();
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
                //orderService.updateOrderStatus(out_trade_no,map.get("transaction_id") );交易流水号
                //支付成功后保存订单
                seckillOrderService.saveOrderRedisToDb(out_trade_no,userId,map.get("transaction_id"));
                break;
            }

            try {
                Thread.sleep(3000);//间隔三秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //不让一直等待支付状态
            if (count>=20){
                //关闭支付服务
                result=new  Result(false, "二维码超时");
                Map payresult = payService.closePay(out_trade_no);
                if (!!"SUCCESS".equals(payresult.get("result_code"))){
                    //正常关闭
                    if("ORDERPAID".equals(payresult.get("err_code"))){
                        result=new Result(true, "支付成功");
                        //保存订单
                        seckillOrderService.saveOrderRedisToDb(out_trade_no, userId,  map.get("transaction_id"));
                    }
                }
                if (result.getSuccess()==false){
                    //直接删除
                    System.out.println("deletePaying......");
                    seckillOrderService.deleteOrderFromRedis(userId,out_trade_no );
                }
                break;
            }
            System.out.println("queryPaying.....");
        }
        return result;
    }

//    @Reference
//    private SOrderService orderService;
    /**
     * 生成二维码
     * @return
     */

    @RequestMapping("/createPayUri")
    public Map createNative(){
        //获取当前用户
        String userId= SecurityContextHolder.getContext().getAuthentication().getName();
        // 根据用户id查询订单
        TbSeckillOrder seckillOrder = seckillOrderService.findOrderByUserIdFromRedis(userId);
        System.out.println(seckillOrder);
        if (seckillOrder!=null){
            //计算金钱
            long fen=  (long)(seckillOrder.getMoney().doubleValue()*100);//金额（分）
            return payService.createNative(seckillOrder.getId()+"", fen+"");
        }else {
            return new HashMap();
        }



    }
}
