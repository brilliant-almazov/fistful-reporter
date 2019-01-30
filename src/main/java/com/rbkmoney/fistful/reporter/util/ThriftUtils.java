package com.rbkmoney.fistful.reporter.util;

import com.rbkmoney.fistful.reporter.InvalidRequest;
import com.rbkmoney.fistful.reporter.Report;
import com.rbkmoney.fistful.reporter.ReportStatus;
import com.rbkmoney.fistful.reporter.ReportTimeRange;
import com.rbkmoney.geck.common.util.TypeUtil;

import java.util.Arrays;
import java.util.List;

public class ThriftUtils {

    public static InvalidRequest buildInvalidRequest(Throwable throwable) {
        return buildInvalidRequest(throwable.getMessage());
    }

    public static InvalidRequest buildInvalidRequest(String... messages) {
        return new InvalidRequest(Arrays.asList(messages));
    }

    public static Report map(com.rbkmoney.fistful.reporter.domain.tables.pojos.Report report, List<String> fileDataIds) throws IllegalArgumentException {
        Report dReport = new Report();
        dReport.setReportId(report.getId());
        dReport.setTimeRange(createTimeRange(report));
        dReport.setCreatedAt(TypeUtil.temporalToString(report.getCreatedAt()));
        dReport.setReportType(report.getType());
        dReport.setStatus(ReportStatus.valueOf(report.getStatus().getLiteral()));
        dReport.setFileDataIds(fileDataIds);
        return dReport;
    }

    private static ReportTimeRange createTimeRange(com.rbkmoney.fistful.reporter.domain.tables.pojos.Report report) {
        return new ReportTimeRange(
                TypeUtil.temporalToString(report.getFromTime()),
                TypeUtil.temporalToString(report.getToTime())
        );
    }
}
