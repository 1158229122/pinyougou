 //控制层 
app.controller('userController' ,function($scope,$controller   ,userService){
    $scope.entity = {
    	password : ""
	}
	//保存
	$scope.save=function(){
		if (!$scope.isEqualPassword){
			return;//两次密码不一致
		}
		userService.add($scope.entity,$scope.smsCode).success(
			function (response) {
				if (response.success) {
					alert(response.message);
				}else {
                    alert(response.message);
				}
            }
		)
	}

	//判断密码是否同步
	$scope.password = "";
	$scope.isEqualPassword  = function (){
        if ($scope.entity.password != $scope.password){
            //两次密码不一致
            $scope.errorMessage = "两次输入的密码不一致";
            return false;
        }else {
        	return true;
		}

	}
    /**
	 * 生成验证码
     */
	$scope.createSmsCode = function () {
		userService.createSmsCode($scope.entity.phone).success(
			function (response) {
                if (response.success) {
                    alert(response.message);
                }else {
                    alert(response.message);
                }
            }
		)
    }
	 

    
});	
