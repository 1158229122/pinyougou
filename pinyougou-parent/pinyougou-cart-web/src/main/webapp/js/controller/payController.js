app.controller("payController",function ($scope,payService,$location) {

    /**
     * 生成二維碼
     */
    $scope.createPayUri=function(){
        payService.createPayUri().success(
            function(response){
                $scope.money=  (response.total_fee/100).toFixed(2) ; //金额
                $scope.out_trade_no= response.out_trade_no;//订单号
                //二维码
                var qr = new QRious({
                    element:document.getElementById('qrious'),
                    size:250,
                    level:'H',
                    value:response.code_url
                });
                queryPayStatus($scope.out_trade_no);
            });
    }
    //查询支付状态
    queryPayStatus=function(out_trade_no){
        payService.queryPayStatus(out_trade_no).success(
            function(response){
                if(response.success){
                    location.href="paysuccess.html#?money="+$scope.money;
                }else{
                    if (response.message=="支付超時") {
                        $scope.createPayUri();
                    }else {
                        ocation.href="payfail.html";
                    }

                }
            }
        );
    }

    //獲取金錢
    $scope.getMoney = function () {
      return  $location.search()["money"];
    }

})