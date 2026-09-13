package com.skillsaathi.service;

import com.skillsaathi.dto.report.ReportResponse;
import com.skillsaathi.dto.report.SubmitReportRequest;
import com.skillsaathi.entity.enums.ReportStatus;

import java.util.List;

public interface ReportService {
    ReportResponse submitReport(Long reportedByUserId, SubmitReportRequest request);
    List<ReportResponse> listReports(ReportStatus status); // admin use
    ReportResponse resolveReport(Long reportId, ReportStatus newStatus); // admin use
}
