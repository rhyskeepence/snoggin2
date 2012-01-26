function formatNumber(x) {
    if ((x > -50) && (x < 50)) {
        return x.toFixed(2);
    }
    return x.toFixed(0).toString().replace(/\B(?=(?:\d{3})+(?!\d))/g, ",");
}
var plot;

function doPlot(axisDefinition) {
    $(function () {
        plot = $.plot($("#chart"),
            axisDefinition,
            {
                xaxes:[
                    { mode:'time' }
                ],
                yaxes:[
                    { min:0, tickFormatter: formatNumber }
                ],
                series: {
                    lines: { show: true }
                },
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

                var y = formatNumber(series.data[j - 1][1]);
                legends.eq(i).text(series.label.replace(/=.*/, "= " + y));

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