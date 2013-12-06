//策略对比JS
$(function () {
    $('#container').highcharts({
        chart:{
            type:'line',
            width:800,
            height:200
        },
        title:{
            text:'上班打卡时间走势',
            x:-20 //center
        },
        xAxis:{
            type:'datetime',
            min:_start,
            max:_end,
            tickInterval:interval,
            labels:{
                //align: 'left',
                formatter:function () {
                    return Highcharts.dateFormat('%Y-%m-%d', this.value);
                }
            }

        },
        yAxis:{
            min: Date.UTC(0, 0, 0, 08, 05, 0),
            max: Date.UTC(0, 0, 0, 10, 0, 0),
            tickInterval:1000*60*15,
            title: {
                enabled: false
            },
            labels:{
                formatter:function () {
                    return Highcharts.dateFormat('%H:%M', this.value);
                }
            },
            plotLines: [{
                color: '#FF0000',
                width: 2,
                value: Date.UTC(0, 0, 0, 08, 45, 0)
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
            text:'下班打卡时间走势',
            x:-20 //center
        },
        xAxis:{
            type:'datetime',
            min:_start,
            max:_end,
            tickInterval:interval,
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
            min: Date.UTC(0, 0, 0, 17, 30, 0),
            max: Date.UTC(0, 0, 0, 20, 40, 0),
            tickInterval:1000*60*20,
            title: {
                enabled: false
            },
            labels:{
                formatter:function () {
                    return Highcharts.dateFormat('%H:%M', this.value);
                }
            },
            plotLines: [{
                color: '#FF0000',
                width: 2,
                value: Date.UTC(0, 0, 0, 18, 0, 0)
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
});
