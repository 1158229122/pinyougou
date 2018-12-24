app.controller("orderInfoController",function ($scope,orderInfoService,cartService) {
    $scope.findAddress = function () {
        orderInfoService.findAddressByUserId().success(
            function (response) {
                $scope.addressList = response;
                for(var i=0;i<$scope.addressList.length;i++){
                    if ($scope.addressList[i].isDefault=="1"){
                        $scope.address = $scope.addressList[i];
                        break;
                    }
                }
            }
        )
    }

    //支付方式選擇
    $scope.order={paymentType:'1'};//默認為微信支付
    //选择支付方式
    $scope.selectPayType=function(type){
        $scope.order.paymentType= type;
    }

    //选择地址
    $scope.selectAddress=function(address){
        $scope.address=address;
    }

    //判断是否是当前选中的地址
    $scope.isSelectedAddress=function(address){
        if(address==$scope.address){
            return true;
        }else{
            return false;
        }
    }

    //查詢購物車詳情
    $scope.findCartList = function () {
        cartService.findAll().success(
            function (response) {
                $scope.cartList = response;
                $scope.totalValue=cartService.sum($scope.cartList);//求合计数
            }
        )
    }

})