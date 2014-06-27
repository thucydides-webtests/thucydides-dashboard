
<#assign totalCount = testOutcomes.totalTests.total >
<#assign successCount = testOutcomes.totalTests.withResult("success") >
<#assign pendingCount = testOutcomes.totalTests.withResult("pending") >
<#assign ignoredCount = testOutcomes.totalTests.withResult("ignored") >
<#assign failureCount = testOutcomes.totalTests.withResult("failure") >
<#assign errorCount = testOutcomes.totalTests.withResult("error") >
<#assign failureOrErrorCount = testOutcomes.totalTests.withFailureOrError() >

<#assign autoTotalCount = testOutcomes.count("AUTOMATED").total >
<#assign autoSuccessCount = testOutcomes.count("AUTOMATED").withResult("success") >
<#assign autoPendingCount = testOutcomes.count("AUTOMATED").withResult("pending") >
<#assign autoIgnoredCount = testOutcomes.count("AUTOMATED").withResult("ignored") >
<#assign autoFailureCount = testOutcomes.count("AUTOMATED").withResult("failure") >
<#assign autoErrorCount = testOutcomes.count("AUTOMATED").withResult("error") >

<#if (autoTotalCount > 0)>
    <#assign autoPercentageSuccessCount = autoSuccessCount / autoTotalCount >
    <#assign autoPercentagePendingCount = autoPendingCount / autoTotalCount  >
    <#assign autoPercentageIgnoredCount = autoIgnoredCount / autoTotalCount  >
    <#assign autoPercentageFailureCount = autoFailureCount / autoTotalCount  >
    <#assign autoPercentageErrorCount = autoErrorCount / autoTotalCount  >
<#else>
    <#assign autoPercentageSuccessCount = 0.0 >
    <#assign autoPercentagePendingCount = 0.0 >
    <#assign autoPercentageIgnoredCount = 0.0 >
    <#assign autoPercentageFailureCount = 0.0 >
    <#assign autoPercentageErrorCount = 0.0 >
</#if>

<#assign manualTotalCount = testOutcomes.count("MANUAL").total >
<#assign manualSuccessCount = testOutcomes.count("MANUAL").withResult("success") >
<#assign manualPendingCount = testOutcomes.count("MANUAL").withResult("pending") >
<#assign manualIgnoredCount = testOutcomes.count("MANUAL").withResult("ignored") >
<#assign manualFailureCount = testOutcomes.count("MANUAL").withResult("failure") >
<#assign manualErrorCount = testOutcomes.count("MANUAL").withResult("error") >

<#if (manualTotalCount > 0)>
    <#assign manualPercentageSuccessCount = manualSuccessCount / manualTotalCount >
    <#assign manualPercentagePendingCount = manualPendingCount / manualTotalCount  >
    <#assign manualPercentageIgnoredCount = manualIgnoredCount / manualTotalCount  >
    <#assign manualPercentageFailureCount = manualFailureCount / manualTotalCount  >
    <#assign manualPercentageErrorCount = manualErrorCount / manualTotalCount  >
<#else>
    <#assign manualPercentageSuccessCount = 0.0 >
    <#assign manualPercentagePendingCount = 0.0 >
    <#assign manualPercentageIgnoredCount = 0.0 >
    <#assign manualPercentageFailureCount = 0.0 >
    <#assign manualPercentageErrorCount = 0.0 >
</#if>

<#if (totalCount > 0)>
    <#assign percentageSuccessCount = successCount / totalCount >
    <#assign percentagePendingCount = pendingCount / totalCount  >
    <#assign percentageIgnoredCount = ignoredCount / totalCount  >
    <#assign percentageFailureCount = failureCount / totalCount  >
    <#assign percentageErrorCount = errorCount / totalCount  >
<#else>
    <#assign percentageSuccessCount = 0.0 >
    <#assign percentagePendingCount = 0.0 >
    <#assign percentageIgnoredCount = 0.0 >
    <#assign percentageFailureCount = 0.0 >
    <#assign percentageErrorCount = 0.0 >
</#if>

<div>
    <table class="table">
        <head>
            <tr>
                <th>Test Type</th>
                <th>Total</th>
                <th>Success&nbsp;<i class="icon-check"/> </th>
                <th>Pending&nbsp;<i class="icon-calendar"/></th>
                <th>Ignored&nbsp;<i class="icon-ban-circle"/></th>
                <th>Failure&nbsp;<i class="icon-thumbs-down"/></th>
                <th>Error&nbsp;&nbsp;&nbsp;<i class="icon-remove"/></th>
            </tr>
        </head>
        <body>
        <tr>
            <td class="summary-leading-column">Automated</td>
            <td>${autoTotalCount}</td>
            <td>${autoSuccessCount} (${autoPercentageSuccessCount?string.percent})</td>
            <td>${autoPendingCount} (${autoPercentagePendingCount?string.percent})</td>
            <td>${autoIgnoredCount} (${autoPercentageIgnoredCount?string.percent})</td>
            <td>${autoFailureCount} (${autoPercentageFailureCount?string.percent})</td>
            <td>${autoErrorCount} (${autoPercentageErrorCount?string.percent})</td>
        </tr>
        <tr>
            <td class="summary-leading-column">Manual</td>
            <td>${manualTotalCount}</td>
            <td>${manualSuccessCount} (${manualPercentageSuccessCount?string.percent})</td>
            <td>${manualPendingCount} (${manualPercentagePendingCount?string.percent})</td>
            <td>${manualIgnoredCount} (${manualPercentageIgnoredCount?string.percent})</td>
            <td>${manualFailureCount} (${manualPercentageFailureCount?string.percent})</td>
            <td>${manualErrorCount} (${manualPercentageErrorCount?string.percent})</td>
        </tr>
        <tr>
            <td class="summary-leading-column">Total</td>
            <td>${totalCount}</td>
            <td>${successCount} (${percentageSuccessCount?string.percent})</td>
            <td>${pendingCount} (${percentagePendingCount?string.percent})</td>
            <td>${ignoredCount} (${percentageIgnoredCount?string.percent})</td>
            <td>${failureCount} (${percentageFailureCount?string.percent})</td>
            <td>${errorCount} (${percentageErrorCount?string.percent})</td>
        </tr>
        </body>
    </table>
</div>