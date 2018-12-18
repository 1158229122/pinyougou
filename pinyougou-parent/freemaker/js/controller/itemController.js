app.controller("itemController",function ($scope) {
    $scope.num = 1;//定义购买的数量
    $scope.addNum = function (num) {
        $scope.num = $scope.num + num;
        if ($scope.num<=0) {
            $scope.num = 1;
        }
    }
    //定义用户勾选的选项
    $scope.specificationItems = {};
    $scope.clickAddSpecSelected = function (key,value) {
        $scope.specificationItems[key] = value;
        searchSku();
    }
    //是否选中
    $scope.isSpecSelected = function (k, v) {
        if ($scope.specificationItems[k] == v){
            return true;
        }
            return false;
    }
    //默认加载sku列表
    $scope.loadSuk = function () {

        $scope.sku=skuList[0];
        $scope.specificationItems= JSON.parse(JSON.stringify($scope.sku.spec)) ;

    }
    //匹配两个对象
    matchObject=function(map1,map2){
        for(var k in map1){

            if(map1[k]!=map2[k]){
                return false;
            }
        }
        for(var k in map2){

            if(map2[k]!=map1[k]) {

                return false;
            }
        }
        return true;
    }

    //查询 SKU
    searchSku=function(){
        for(var i=0;i<skuList.length;i++ ){
            if( matchObject(skuList[i].spec ,$scope.specificationItems) ){
                $scope.sku=skuList[i];
                return ;
            }
        }
        $scope.sku={id:0,title:'--------',price:0};//如果没有匹配的
    }

})