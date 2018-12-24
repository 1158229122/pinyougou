app.service("orderInfoService",function ($http) {

    this.findAddressByUserId=function(){
        return $http.get("address/findAddressByUserId.do");
    }
})