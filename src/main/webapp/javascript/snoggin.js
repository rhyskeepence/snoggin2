function formatNumber(x) {
    return x.toFixed(1).toString().replace(/\B(?=(?:\d{3})+(?!\d))/g, ",");
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

function notifyNoStats() {
    $("#chart").html("Sorry, no data found!<br/><a href='/'>&lt;&lt; Back</a>")
}

function doPlot(axisDefinition) {
    $(function () {

        if ($.QueryString["aggregate"] == null) {
            if ($.QueryString["series"] == "bars") {
                var series = {
                    bars: { show: true, barWidth: 1000*5, align: 'left' }
                }
            } else {
                var series = {
                    lines: { show: true }
                };
            }
        } else {
            var series = {
                bars: {show: true, barWidth: 1000*60*60*24, align: 'left'}
            };
        }

        var options = {
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
                selection: { mode: "x" },
                legend:{ noColumns: 3, container: "#legend" }
            };

        plot = $.plot($("#chart"),
            axisDefinition,
            options);

        var overview = $.plot($("#overview"),
            axisDefinition,
            {
                series: series,
                legend: { show: false },
                xaxis: { ticks: [], mode: "time" },
                yaxis: { ticks: [], min: 0, autoscaleMargin: 0.1 },
                selection: { mode: "x" }
            });

        $("#chart").bind("plotselected", function (event, ranges) {
            $("#clearSelection").show();
            plot = $.plot($("#chart"), axisDefinition,
                          $.extend(true, {}, options, {
                              xaxis: { min: ranges.xaxis.from, max: ranges.xaxis.to }
                          }));

            overview.setSelection(ranges, true);
        });

        $("#overview").bind("plotselected", function (event, ranges) {
            plot.setSelection(ranges);
        });

        $('#clearSelection').click(clearSelection);
        $("#chart").bind("plotunselected", clearSelection);
        $("#overview").bind("plotunselected", clearSelection);

        var updateLegendTimeout = null;
        var latestPosition = null;

        function clearSelection() {
            $("#clearSelection").hide();
            plot = $.plot($("#chart"), axisDefinition,
                       $.extend(true, {},options, {
                             xaxis: { min: plot.getData()[0].xaxis.datamin, max: plot.getData()[0].xaxis.datamax }
                       }));

            overview.clearSelection();
        }

        function updateLegend() {
            updateLegendTimeout = null;

            var legends = $("#legend .legendLabel");
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
                showDate(d.toLocaleString());
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
