//策略对比JS
$(function () {
    $.qicLoading({
        target:'body',
        text:"努力加载中...",
        modal:false,
        width:220,
        top:'35%',
        left:'40%',
        postion:"absolute",
        zIndex:2000
    });
    $('#container').highcharts({
        chart:{
            type:'line',
            width:800,
            height:200
        },
        title:{
            text:'上班时间走势',
            x:-20 //center
        },
        xAxis:{
            type:'datetime',
            min:Date.UTC(2013, 10, 01),
            max:Date.UTC(2013, 10, 31),
            //ckInterval:1000*60*60
            // showFirstLabel: false,
            labels:{
                //align: 'left',
                formatter:function () {
                    return Highcharts.dateFormat('%Y-%m-%d', this.value);
                }
            }

        },
        yAxis:{
            /*min:Date.UTC(2013, 8, 1, 08, 8, 0), //0表示1月 以此类推
             max:Date.UTC(2013, 8, 1, 09, 20, 0),*/
            //ckInterval:1000*60*60
            // showFirstLabel: false,
            labels:{
                //align: 'left',
                formatter:function () {
                    return Highcharts.dateFormat('%H:%M', this.value);
                }
            },
            plotLines: [{
                color: '#FF0000',
                width: 2,
                value: Date.UTC(2013, 10, 1, 08, 45, 0)
            }]
        },
        tooltip:{
            formatter:function () {
                return Highcharts.dateFormat('%m月%d日', this.x) + ' ' + Highcharts.dateFormat('%H:%M', this.y);
            }
        },
        legend:{
            enabled:true,
            layout:'vertical',
            align:'right',
            verticalAlign:'middle',
            borderWidth:0
        },
        plotOptions: {
            line: {
                dataGrouping: {
                    enabled: false
                }
            },
            series: {
                lineWidth:2,
                marker: {
                    radius: 2 //鼠标接触时 显示点的大小
                }
            }

        },
        series:[
            {
                name:'上班打卡',
                data:data,
                lineColor:'#53b6f4'

            }
        ]
    });
    $('#container2').highcharts({
        chart:{
            type:'line',
            width:800,
            height:200
        },
        title:{
            text:'下班时间走势',
            x:-20 //center
        },
        xAxis:{
            type:'datetime',
            min:Date.UTC(2013, 10, 01),
            max:Date.UTC(2013, 10, 31),
            //ckInterval:1000*60*60
            // showFirstLabel: false,
            labels:{
                //align: 'left',
                formatter:function () {
                    return Highcharts.dateFormat('%Y-%m-%d', this.value);
                }
            }

        },
        yAxis:{
            /*min:Date.UTC(2013, 8, 1, 08, 8, 0), //0表示1月 以此类推
             max:Date.UTC(2013, 8, 1, 09, 20, 0),*/
            //ckInterval:1000*60*60
            // showFirstLabel: false,
            labels:{
                //align: 'left',
                formatter:function () {
                    return Highcharts.dateFormat('%H:%M', this.value);
                }
            },
            plotLines: [{
                color: '#FF0000',
                width: 2,
                value: Date.UTC(2013, 10, 1, 18, 0, 0)
            }]
        },
        tooltip:{
            formatter:function () {
                return Highcharts.dateFormat('%m月%d日', this.x) + ' ' + Highcharts.dateFormat('%H:%M', this.y);
            }
        },
        legend:{
            enabled:true,
            layout:'vertical',
            align:'right',
            verticalAlign:'middle',
            borderWidth:0
        },
        plotOptions: {
            line: {
                dataGrouping: {
                    enabled: false
                }
            },
            series: {
                lineWidth:2,
                marker: {
                    radius: 2 //鼠标接触时 显示点的大小
                }
            }

        },
        series:[
            {
                name:'下班打卡',
                data:data2,
                lineColor:'#53b6f4'

            }
        ]
    });
    $.qicLoading({remove:true});//移除loading。。。
});
