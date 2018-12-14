app.controller("searchController",function ($scope,searchService) {
    $scope.rowsList = {};//返回的数据
    $scope.searchMap= {"keywords":"","brand":"","category":"","price" : '',"currentPage":'1',"pageRows":'20',spec:{},sort:{}};

    function isNum(a){
        var regexp =  /^[0-9]+.?[0-9]*$/;
        return regexp.test(a);
    }
    //判断关键字是否包含品牌
    $scope.keywordsIsBrand = function(){

    }

    //排序查询
    $scope.orderSearch = function(key,value){
        $scope.searchMap.sort[key] = value;
        $scope.search();//调用查询
    }
    //去哪页
    $scope.goToPage = function(toPage){
        $scope.searchMap.currentPage = toPage;
        $scope.search();
    }

    $(document).keydown(function(event){
　　　　if(event.keyCode == 13){
            $scope.search();
    　　}
    });

    //主线查询
    //搜索,所有的方法都走这条主线
    $scope.search = function(){

        $scope.searchMap.currentPage = parseInt($scope.searchMap.currentPage);
        $scope.searchMap.pageRows = parseInt($scope.searchMap.pageRows);
        $scope.toPage = $scope.searchMap.currentPage;//每次收索完显示前台
        if($scope.searchMap.currentPage>$scope.page){
            $scope.searchMap.currentPage = $scope.page;//如果当前页数超过总页数相等总页数
        }
        if($scope.searchMap.currentPage<1){
            $scope.searchMap.currentPage = 1;//当前页数相等于1//优化参数
        }

        if(!isNum($scope.searchMap.currentPage)){

            $scope.searchMap.currentPage = 1;//当前页数相等于1//优化参数如果是字符串当前页数就是等于1
        }


        searchService.search($scope.searchMap).success(function (response) {
            $scope.resultMap = response;
            $scope.buildLimitPage();
            window.scrollTo(0,200);
        })
    }
    
    $scope.buildSearchItem = function (key,value) {
        if (key == "category" || key == "brand"||key == "price"||key == "currentPage"){//不是分类就是品牌
            $scope.searchMap[key] = value;
        } else {//否者就是规格选项
            $scope.searchMap.spec[key] = value;
        }
        //添加条件查询
        $scope.search();
    }
    //删除key和value
    $scope.removeSearch = function (key,value) {
        if (key == "category" || key == "brand"||key=='price'){//不是分类就是品牌
            $scope.searchMap[key] = "";
        } else {//否者就是规格选项
            delete $scope.searchMap.spec[key];
        }
        $scope.search();
    }


    $scope.buildLimitPage = function () {
        $scope.firstDot = true;//前面有点
        $scope.lastFot = true;//后面有点
        var startPage ;//开始页
        var endPage;//结束页
        $scope.limitPage = [];
        var currentPage = parseInt($scope.searchMap.currentPage);//当前页面
        $scope.searchMap.pageRows = parseInt($scope.searchMap.pageRows);
        $scope.page = $scope.resultMap.totalPage/$scope.searchMap.pageRows;//总页数
        $scope.page = Math.ceil($scope.page);//总页数

        //总页面不足5
        if ($scope.page<=5) {
            startPage = 1;
            endPage = $scope.page;
            $scope.firstDot = false;//前面无点
            $scope.lastFot = false;//后面无点
        }else {
            //总页面超过5个
            startPage = currentPage -2;
            endPage =currentPage+2;


            if (startPage<=1){
                //前面不足2个
                startPage =1;
                endPage = 5;
                $scope.firstDot = false;//前面无点
            }
            if (endPage >=$scope.page ) {
                //后面不足2个
                startPage = $scope.page-4;
                endPage = $scope.page
                $scope.lastFot = false;//后面无点
            }

        }


        for(var i =startPage;i<=endPage ;i++){
            $scope.limitPage.push(i);
        }
    }
    //是否相同页
    $scope.isActive = function(item){
        if ($scope.searchMap.currentPage==item) {
            return true;
        }
        return false;
    }

    //是否是第一页
    $scope.isfirstPage = function () {

        if ($scope.searchMap.currentPage==1) {

            return true;
        }

        return false;
    }

    //是否最后一页
    $scope.islastPage = function () {

        if ($scope.page==$scope.searchMap.currentPage) {

            return true;
        }

        return false;
    }

    $scope.queryByPage = function (currentPage) {

        //页码验证
        if(currentPage<1 || currentPage>$scope.resultMap.page){
            return;}

        $scope.searchMap.currentPage=currentPage;
        $scope.search();
    }




})