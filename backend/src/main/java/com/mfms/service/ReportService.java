package com.mfms.service;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.mfms.dto.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final MemberService memberService;
    private final ContributionService contributionService;
    private final LoanService loanService;
    private final ExternalBorrowerService borrowerService;
    private final FundCalculationService fundCalculationService;

    public byte[] exportMembersExcel() throws Exception {
        List<MemberResponse> members = memberService.getAllMembers();
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Members");
            Row header = sheet.createRow(0);
            String[] cols = {"Code", "Name", "Phone", "Join Date", "Status", "Total Deposit",
                    "Paid Months", "Pending Months", "Last Paid Month", "Current Loan", "Total Interest Paid"};
            for (int i = 0; i < cols.length; i++) header.createCell(i).setCellValue(cols[i]);
            int rowNum = 1;
            for (MemberResponse m : members) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(m.getMemberCode());
                row.createCell(1).setCellValue(m.getFullName());
                row.createCell(2).setCellValue(m.getPhoneNumber());
                row.createCell(3).setCellValue(m.getJoinDate().toString());
                row.createCell(4).setCellValue(m.getStatus().name());
                row.createCell(5).setCellValue(m.getTotalDeposit().doubleValue());
                row.createCell(6).setCellValue(m.getPaidMonths());
                row.createCell(7).setCellValue(m.getPendingMonths());
                row.createCell(8).setCellValue(m.getLastPaidMonth() != null ? m.getLastPaidMonth() : "");
                row.createCell(9).setCellValue(m.getCurrentLoanAmount().doubleValue());
                row.createCell(10).setCellValue(m.getTotalInterestPaid().doubleValue());
            }
            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportContributionsExcel(LocalDate from, LocalDate to) throws Exception {
        List<ContributionResponse> contributions = contributionService.getAll();
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Contributions");
            Row header = sheet.createRow(0);
            String[] cols = {"Member", "Month/Year", "Amount", "Status", "Payment Date"};
            for (int i = 0; i < cols.length; i++) header.createCell(i).setCellValue(cols[i]);
            int rowNum = 1;
            for (ContributionResponse c : contributions) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(c.getMemberName());
                row.createCell(1).setCellValue(c.getMonth() + "/" + c.getYear());
                row.createCell(2).setCellValue(c.getAmount().doubleValue());
                row.createCell(3).setCellValue(c.getStatus().name());
                row.createCell(4).setCellValue(c.getPaymentDate() != null ? c.getPaymentDate().toString() : "");
            }
            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportFundReportPdf() throws Exception {
        DashboardResponse fund = fundCalculationService.buildAdminDashboard();
        com.itextpdf.text.Document document = new com.itextpdf.text.Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();
        document.add(new Paragraph("Fund Report", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
        document.add(new Paragraph("Generated: " + LocalDate.now()));
        document.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(2);
        addPdfRow(table, "Total Members", String.valueOf(fund.getTotalMembers()));
        addPdfRow(table, "Total Deposits", "₹" + fund.getTotalContributions());
        addPdfRow(table, "Pending Deposits", "₹" + fund.getTotalPendingContributionAmount());
        addPdfRow(table, "Interest Earned", "₹" + fund.getTotalInterestEarned());
        addPdfRow(table, "Money Available", "₹" + fund.getAvailableFund());
        addPdfRow(table, "Money Given On Loan", "₹" + fund.getMoneyLoanedOut());
        addPdfRow(table, "Active Loans", String.valueOf(fund.getActiveLoans()));
        addPdfRow(table, "Expected Months", String.valueOf(fund.getExpectedContributionMonths()));
        document.add(table);
        document.close();
        return out.toByteArray();
    }

    public byte[] exportLoansExcel() throws Exception {
        List<LoanResponse> loans = loanService.getAllLoans();
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Loans");
            Row header = sheet.createRow(0);
            String[] cols = {"ID", "Borrower", "Type", "Amount", "Outstanding", "Rate", "Status"};
            for (int i = 0; i < cols.length; i++) header.createCell(i).setCellValue(cols[i]);
            int rowNum = 1;
            for (LoanResponse l : loans) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(l.getId());
                row.createCell(1).setCellValue(l.getBorrowerName());
                row.createCell(2).setCellValue(l.getBorrowerType().name());
                row.createCell(3).setCellValue(l.getLoanAmount().doubleValue());
                row.createCell(4).setCellValue(l.getOutstandingAmount().doubleValue());
                row.createCell(5).setCellValue(l.getInterestRate().doubleValue());
                row.createCell(6).setCellValue(l.getStatus().name());
            }
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private void addPdfRow(PdfPTable table, String label, String value) {
        PdfPCell c1 = new PdfPCell(new Phrase(label));
        PdfPCell c2 = new PdfPCell(new Phrase(value));
        table.addCell(c1);
        table.addCell(c2);
    }
}
