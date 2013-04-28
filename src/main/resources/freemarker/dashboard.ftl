<!DOCTYPE html>
<html>
<head>
    <title>Project Dashboard</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <!-- Bootstrap -->
    <!-- Le styles -->
    <script src="scripts/jquery.js"></script>
    <script src="bootstrap/js/bootstrap.min.js"></script>
    <link rel="stylesheet" type="text/css" href="jqplot/jquery.jqplot.min.css"/>
    <script type="text/javascript" src="jqplot/jquery.jqplot.min.js"></script>
    <script type="text/javascript" src="jqplot/plugins/jqplot.pieRenderer.min.js"></script>

    <link href="bootstrap/css/bootstrap.min.css" rel="stylesheet" media="screen">
    <style type="text/css">
        body {
            padding-top: 60px;
            padding-bottom: 40px;
        }
        .sidebar-nav {
            padding: 9px 0;
        }

        .hero-unit.dashboard-header {
            padding: 30px;
        }

    </style>
    <link href="bootstrap/css/bootstrap-responsive.css" rel="stylesheet">

    <script class="code" type="text/javascript">$(document).ready(function () {
        <#list dashboard.sections as section>

            <#assign testOutcomes = allTestOutcomes.withTags(section.tags) >

            var test_results_plot = $.jqplot('project_${section_index}_pie_chart', [
                [
                    ['Passing', ${testOutcomes.decimalPercentagePassingTestCount}],
                    ['Pending', ${testOutcomes.decimalPercentagePendingTestCount}],
                    ['Failing', ${testOutcomes.decimalPercentageFailingTestCount}],
                    ['Errors',  ${testOutcomes.decimalPercentageErrorTestCount}]
                ]
            ], {

                gridPadding:{top:0, bottom:38, left:0, right:0},
                seriesColors:['#30cb23', '#a2f2f2', '#f8001f','#fc6e1f'],
                seriesDefaults:{
                    renderer:$.jqplot.PieRenderer,
                    trendline:{ show:false },
                    rendererOptions:{ padding:8, showDataLabels:true }
                },
                legend:{
                    show:true,
                    placement:'outside',
                    rendererOptions:{
                        numberRows:1
                    },
                    location:'s',
                    marginTop:'15px'
                },
                series:[
                    {label:'${testOutcomes.successCount} / ${testOutcomes.total} tests passed' },
                    {label:'${testOutcomes.pendingCount} / ${testOutcomes.total} tests pending'},
                    {label:'${testOutcomes.failureCount} / ${testOutcomes.total} tests failed'},
                    {label:'${testOutcomes.errorCount} / ${testOutcomes.total} errors'}
                ]
            });
        </#list>

    })
    ;
    </script>
</head>
<body>

<#macro write_subsections(subsections)>
    <#assign level = level + 1>
    <#list subsections as subsection>
        <li class="nav-subsection" style="padding-left: ${level * 10}px;"><a href="${subsection.directoryName}/index.html">${subsection.title}</a></li>
        <@write_subsections subsections=subsection.subsections/>
    </#list>
    <#assign level = level - 1>
</#macro>

<div class="container-fluid">
    <div class="row-fluid">
        <div class="span3">
            <div class="well sidebar-nav">
                <ul class="nav nav-list">
                    <#assign level = 1>
                    <#list dashboard.sections as section>
                        <li class="nav-header"><a href="${section.directoryName}/index.html">${section.title}</a></li>
                        <#assign level=1>
                        <@write_subsections subsections=section.subsections/>
                    </#list>
                </ul>
            </div><!--/.well -->
        </div><!--/span-->

        <div class="span9">
            <div class="hero-unit dashboard-header">
                <h1>Project Dashboard</h1>
            </div>

            <#assign projectNumber = 0>
            <#list dashboard.getSectionRows(3) as sectionRowElements>
            <div class="row-fluid">
                <#list sectionRowElements as section>
                    <div class="span6">
                        <h2>${section.title}</h2>
                        <div id="project_${projectNumber}_pie_chart"></div>
                        <p><a class="btn" href="${section.directoryName}/index.html">View detailed reports &raquo;</a></p>
                        <#assign projectNumber = projectNumber + 1>
                    </div><!--/span-->
                </#list>
            </div><!--/row-->
            </#list>
        </div><!--/span-->
    </div><!--/row-->


</body>
</html>