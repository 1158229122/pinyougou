app.controller("searchController",function ($scope,searchService) {
    $scope.rowsList = {};//返回的数据
    $scope.searchMap= {};

    $scope.search = function(){
        searchService.search($scope.searchMap).success(function (response) {
            $scope.rowsList = response.rows;
        })
    }

})