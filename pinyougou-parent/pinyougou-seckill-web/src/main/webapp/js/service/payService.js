app.service("payService",function ($http) {
    //请求后端生成二维码url
    this.createPayUri = function () {
        return $http.get("pay/createPayUri.do");
    }
    this.queryPayStatus=function(out_trade_no){
        return $http.get('pay/queryPayStatus.do?out_trade_no='+out_trade_no);
    }
})