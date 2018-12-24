app.controller("cartController",function ($scope,cartService) {
    /**
     * 查詢所有
     */
      $scope.findAll = function () {
          cartService.findAll().success(
              function (response) {
                  $scope.cartList = response;
                  $scope.totalValue=cartService.sum($scope.cartList);//求合计数
              }
          )
      }
    /**
     * 添加到購物車
     */
    $scope.addGoodsToCartList = function (itemId,num) {
        cartService.addGoodsToCartList(itemId,num).success(
            function (response) {
                if (response.success) {
                    $scope.findAll();
                } else {
                    alert(response.message);
                }
            }
        )
    }

    $scope.addNum = function (num) {
        $scope.item.num = num;
    }

})