 //控制层 
app.controller('contentController' ,function($scope,$http,contentService){
	$scope.contentList = [];
	$scope.findByCategoryId = function (categoryId) {
		contentService.findByCategoryId(categoryId).success(
			function (response) {
                $scope.contentList[categoryId] =response;
            }
		)
    }
    $scope.searchLocation = function () {
		location.href="http://localhost:9104/search.html#?keywords="+$scope.search;
    }

});	
