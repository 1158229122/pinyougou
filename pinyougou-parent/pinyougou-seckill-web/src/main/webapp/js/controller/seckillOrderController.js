 //控制层 
app.controller('seckillOrderController' ,function($scope,$controller   ,seckillOrderService,$location,$interval){
	

	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		seckillOrderService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		seckillOrderService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体
	getSeckillGoodsId = function(){

        return $location.search()['seckillGoodsId'];
	}
	$scope.findOne=function(){
		var id = getSeckillGoodsId();
		seckillOrderService.findOne(id).success(
			function(response){
				$scope.entity= response;
                allsecond =Math.floor( (  new Date($scope.entity.endTime).getTime()-
                    (new Date().getTime())) /1000); //总秒数

                time= $interval(function(){
                    if(allsecond>0){
                        allsecond =allsecond-1;
                        $scope.timeString=convertTimeToString(allsecond);//转换时间字符串
                    }else{
                        $interval.cancel(time);
                        alert("秒杀服务已结束");
                    }
                },1000);
            }
		);				
	}



	//时间格式转换
	convertTimeToString = function(allsecound){
        var days= Math.floor( allsecond/(60*60*24));//天数
        var hours= Math.floor( (allsecond-days*60*60*24)/(60*60) );//小时数
        var minutes= Math.floor(  (allsecond -days*60*60*24 - hours*60*60)/60    );//分钟数
		var seconds= allsecond -days*60*60*24 - hours*60*60 -minutes*60; //秒数
        var timeString="";
        if(days>0){
            timeString=days+"天 ";
        }
        return timeString+hours+":"+minutes+":"+seconds;
    }



	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=seckillOrderService.update( $scope.entity ); //修改  
		}else{
			serviceObject=seckillOrderService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	$scope.reloadList();//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		seckillOrderService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		seckillOrderService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

	$scope.submitOrder = function () {
		//提交订单se
        var id = getSeckillGoodsId();
		seckillOrderService.submitOrder($scope.entity.id).success(
            function(response){
                if(response.success){
                    alert("下单成功，请在 1 分钟内完成支付");
                    location.href="pay.html";
                }else{

                	alert(response.message);

                }
            }
        )
    }
});	
