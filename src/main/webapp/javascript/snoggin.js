function formatNumber(x) {
    if ((x > -50) && (x < 50)) {
        return x.toFixed(2);
    }
    return x.toFixed(0).toString().replace(/\B(?=(?:\d{3})+(?!\d))/g, ",");
}


(function($) {
    $.QueryString = (function(a) {
        if (a == "") return {};
        var b = {};
        for (var i = 0; i < a.length; ++i)
        {
            var p=a[i].split('=');
            if (p.length != 2) continue;
            b[p[0]] = decodeURIComponent(p[1].replace(/\+/g, " "));
        }
        return b;
    })(window.location.search.substr(1).split('&'))
})(jQuery);

var plot;

function doPlot(axisDefinition) {
    $(function () {

        if ($.QueryString["aggregate"] == null) {
            var series = {
                lines: { show: true }
            };
        } else {
            var series = {
                bars: {show: true, barWidth: 1000*60*60*24, align: 'left'}
            };
        }

        plot = $.plot($("#chart"),
            axisDefinition,
            {
                xaxes:[
                    { mode:'time' }
                ],
                yaxes:[
                    { min:0, tickFormatter: formatNumber }
                ],
                series: series,
                grid: {
                    hoverable: true, autoHighlight: false
                },
                crosshair: { mode: "x" },
                legend:{ container: "#legend" }
            });

        var legends = $("#legend .legendLabel");
        var updateLegendTimeout = null;
        var latestPosition = null;

        function updateLegend() {
            updateLegendTimeout = null;

            var pos = latestPosition;

            var axes = plot.getAxes();
            if (pos.x < axes.xaxis.min || pos.x > axes.xaxis.max ||
                pos.y < axes.yaxis.min || pos.y > axes.yaxis.max)
                return;

            var i, j, dataset = plot.getData();
            for (i = 0; i < dataset.length; ++i) {
                var series = dataset[i];

                // find the nearest points, x-wise
                for (j = 0; j < series.data.length; ++j)
                    if (series.data[j][0] > pos.x)
                        break;

                var y = series.data[j - 1];
                if (y != null) {
                    legends.eq(i).text(series.label.replace(/=.*/, "= " + formatNumber(y[1])));
                } else {
                    legends.eq(i).text(series.label.replace(/=.*/, "= 0"));
                }

                var d = new Date();
                d.setTime(pos.x);
                showDate(d.toGMTString());
            }
        }

        function showDate(contents) {
            $("#date").html(contents);
        }

        $("#chart").bind("plothover",  function (event, pos, item) {
            latestPosition = pos;
            if (!updateLegendTimeout)
                updateLegendTimeout = setTimeout(updateLegend, 50);

        });
    });
}
