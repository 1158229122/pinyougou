var app = angular.module("app",[]);

app.filter("trust",['$sce',function ($sce) {
    return function (data) {
        return $sce.trustAsHtml(data);
    }
}])