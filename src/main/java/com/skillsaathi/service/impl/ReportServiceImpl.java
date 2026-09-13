package com.skillsaathi.service.impl;

import com.skillsaathi.dto.report.ReportResponse;
import com.skillsaathi.dto.report.SubmitReportRequest;
import com.skillsaathi.entity.Report;
import com.skillsaathi.entity.User;
import com.skillsaathi.entity.enums.ReportStatus;
import com.skillsaathi.exception.BadRequestException;
import com.skillsaathi.exception.ResourceNotFoundException;
import com.skillsaathi.repository.ReportRepository;
import com.skillsaathi.repository.UserRepository;
import com.skillsaathi.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    @Override
    public ReportResponse submitReport(Long reportedByUserId, SubmitReportRequest request) {
        if (reportedByUserId.equals(request.getReportedUserId())) {
            throw new BadRequestException("You can't report yourself");
        }

        User reportedBy = getUserOrThrow(reportedByUserId);
        User reportedUser = getUserOrThrow(request.getReportedUserId());

        Report report = Report.builder()
                .reportedBy(reportedBy)
                .reportedUser(reportedUser)
                .reason(request.getReason())
                .description(request.getDescription())
                .status(ReportStatus.OPEN)
                .build();

        reportRepository.save(report);
        return toResponse(report);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportResponse> listReports(ReportStatus status) {
        List<Report> reports = status != null ? reportRepository.findByStatus(status) : reportRepository.findAll();
        return reports.stream().map(this::toResponse).toList();
    }

    @Override
    public ReportResponse resolveReport(Long reportId, ReportStatus newStatus) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));
        report.setStatus(newStatus);
        reportRepository.save(report);
        return toResponse(report);
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private ReportResponse toResponse(Report r) {
        return ReportResponse.builder()
                .id(r.getId())
                .reportedByUserId(r.getReportedBy().getId())
                .reportedByName(r.getReportedBy().getName())
                .reportedUserId(r.getReportedUser().getId())
                .reportedUserName(r.getReportedUser().getName())
                .reason(r.getReason())
                .description(r.getDescription())
                .status(r.getStatus().name())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
