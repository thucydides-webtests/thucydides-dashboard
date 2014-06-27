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

        .pie-chart {
            width: 225px;
            height: 225px;
        }

    </style>
    <link href="bootstrap/css/bootstrap-responsive.css" rel="stylesheet">

    <#assign testOutcomes = allTestOutcomes >
    <#assign successfulManualTests = (testOutcomes.count("manual").withResult("SUCCESS") > 0)>
    <#assign pendingManualTests = (testOutcomes.count("manual").withResult("PENDING") > 0)>
    <#assign ignoredManualTests = (testOutcomes.count("manual").withResult("IGNORED") > 0)>
    <#assign failingManualTests = (testOutcomes.count("manual").withResult("FAILURE") > 0)>

    <script class="code" type="text/javascript">$(document).ready(function () {

        var overview_test_results_plot = $.jqplot('project_overview_pie_chart',
                [
                    [
                        ['Passing', ${testOutcomes.proportionOf("automated").withResult("success")}],
                        <#if (successfulManualTests)>['Passing (manual)', ${testOutcomes.proportionOf("manual").withResult("success")}],</#if>
                        ['Pending', ${testOutcomes.proportionOf("automated").withResult("pending")}],
                        <#if (pendingManualTests)>['Pending (manual)', ${testOutcomes.proportionOf("manual").withResult("pending")}],</#if>
                        ['Ignored', ${testOutcomes.proportionOf("automated").withResult("ignored")}],
                        <#if (pendingManualTests)>['Ignored (manual)', ${testOutcomes.proportionOf("manual").withResult("ignored")}],</#if>
                        ['Failing', ${testOutcomes.proportionOf("automated").withResult("failure")}],
                        <#if (failingManualTests)>['Failing (manual)', ${testOutcomes.proportionOf("manual").withResult("failure")}],</#if>
                        ['Errors',  ${testOutcomes.proportionOf("automated").withResult("error")}]
                    ]
                ], {

                    gridPadding:{top:0, bottom:38, left:0, right:0},
                    seriesColors: ['#30cb23',
                        <#if (successfulManualTests)>'#009818',</#if>
                        '#a2f2f2',
                        <#if (pendingManualTests)>'#8bb1df',</#if>
                        '#eeeadd',
                        <#if (ignoredManualTests)>'#d3d3d3',</#if>
                        '#f8001f',
                        <#if (failingManualTests)>'#a20019',</#if>
                        '#fc6e1f'],
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
                            {label: '${testOutcomes.count("automated").withResult("success")} / ${testOutcomes.total} tests passed' },
                        <#if (successfulManualTests)>
                            {label: '${testOutcomes.count("manual").withResult("success")} / ${testOutcomes.total} manual tests passed' },
                        </#if>
                            {label: '${testOutcomes.count("automated").withResult("pending")} / ${testOutcomes.total} tests pending'},
                        <#if (pendingManualTests)>
                            {label: '${testOutcomes.count("manual").withResult("pending")} / ${testOutcomes.total} manual tests pending' },
                        </#if>
                            {label: '${testOutcomes.count("automated").withResult("ignored")} / ${testOutcomes.total} tests not executed'},
                        <#if (ignoredManualTests)>
                            {label: '${testOutcomes.count("manual").withResult("ignored")} / ${testOutcomes.total} manual tests not executed' },
                        </#if>
                            {label: '${testOutcomes.count("automated").withResult("failure")} / ${testOutcomes.total} tests failed'},
                        <#if (failingManualTests)>
                            {label: '${testOutcomes.count("manual").withResult("failure")} / ${testOutcomes.total} manual tests failed' },
                        </#if>
                            {label: '${testOutcomes.count("automated").withResult("error")} / ${testOutcomes.total} errors'}
                    ]
                });

        <#assign sectionTestOutcomes = [] />

        <#list dashboard.sections as section>

            <#assign testOutcomes = allTestOutcomes.withTags(section.tags) >
            <#assign requirements = requirementsFactory.buildRequirementsOutcomesFrom(testOutcomes) >
            <#assign sectionTestOutcomes = sectionTestOutcomes + [ testOutcomes ] />
            var test_results_plot = $.jqplot('project_${section_index}_pie_chart',
            [
                [
                <#if section.chartType == "TESTS">
                    ['Passing', ${testOutcomes.getProportion().withResult("success")}],
                    ['Pending', ${testOutcomes.getProportion().withIndeterminateResult()}],
                    ['Failing', ${testOutcomes.getProportion().withResult("failure")}],
                    ['Errors',  ${testOutcomes.getProportion().withResult("error")}]
                <#else>
                    ['Passing', ${requirements.getProportion().withResult("success")}],
                    ['Pending', ${requirements.getProportion().withIndeterminateResult()}],
                    ['Failing', ${requirements.getProportion().withResult("failure")}],
                    ['Errors',  ${requirements.getProportion().withResult("error")}]
                </#if>
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
                    <#if section.chartType == "TESTS">
                        {label:'${testOutcomes.getTotalTests().withResult("success")} / ${testOutcomes.total} tests passed' },
                        {label:'${testOutcomes.getTotalTests().withIndeterminateResult()} / ${testOutcomes.total} tests pending'},
                        {label:'${testOutcomes.getTotalTests().withResult("failure")} / ${testOutcomes.total} tests failed'},
                        {label:'${testOutcomes.getTotalTests().withResult("error")} / ${testOutcomes.total} errors'}
                    <#else>
                        {label:'${requirements.getTotal().withResult("success")} / ${requirements.requirementCount} requirements done' },
                        {label:'${requirements.getProportion().withIndeterminateResult()}% pending requirements'},
                        {label:'${testOutcomes.getProportion().withResult("failure")}% failing requirements'},
                        {label:'${requirements.getProportion().withResult("error")}% requirements with errors'}
                    </#if>
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
                <h1>${dashboardTitle}</h1>
            </div>

            <div class="row-fluid">
                <h2>Overview</h2>
            </div>
            <div class="row-fluid">
                <div class="span4">
                    <div class="pie-chart" id="project_overview_pie_chart"></div>
                    <p><a class="btn" href="index.html">View detailed reports &raquo;</a></p>
                </div><!--/span-->
                <div class="span4">
                    <#assign testOutcomes = allTestOutcomes />
                    <#include "test-result-summary.ftl"/>
                </div>
            </div><!--/row-->


        <#assign projectNumber = 0>
            <#list dashboard.getSectionRows(2) as sectionRowElements>
            <div class="row-fluid">
                <#list sectionRowElements as section>
                    <div class="span6">
                        <h2>${section.title}</h2>
                        <div id="project_${projectNumber}_pie_chart"></div>
                        <p><a class="btn" href="${section.directoryName}/index.html">View detailed reports &raquo;</a></p>

                        <div>
                            <#assign testOutcomes = sectionTestOutcomes[projectNumber] />
                            <#include "test-result-summary.ftl"/>
                        </div>


                        <#assign projectNumber = projectNumber + 1>
                    </div><!--/span-->
                </#list>
            </div><!--/row-->
            </#list>
        </div><!--/span-->
    </div><!--/row-->


</body>
</html>