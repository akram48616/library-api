package org.example.librarymanagementsystem.Service;


import lombok.extern.slf4j.Slf4j;
import org.example.librarymanagementsystem.Repository.BookRepository;
import org.example.librarymanagementsystem.Repository.BorrowRecordRepository;
import org.example.librarymanagementsystem.Repository.MemberRepository;
import org.example.librarymanagementsystem.entity.BorrowRecord;
import org.example.librarymanagementsystem.entity.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@Slf4j
public class DailyReportService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BorrowRecordRepository borrowRepository;

    @Autowired
    private EmailService emailService;

    @Scheduled(cron = "0 0 6 * * *")   // TEMPORARY — every minute, for testing only   // every day at 6:00 AM server time
    public void sendDailyReport() {
        log.info("Generating daily report");

        long totalBooks = bookRepository.count();
        long activeMembers = memberRepository.countByMembershipStatus(Member.MembershipStatus.ACTIVE);
        long overdueCount = borrowRepository.countByDueDateBeforeAndStatusNot(
                LocalDate.now(), BorrowRecord.BorrowStatus.RETURNED);

        emailService.sendDailyReportEmail("admin@library.com", totalBooks, activeMembers, overdueCount);

        log.info("Daily report generated - books={}, activeMembers={}, overdue={}",
                totalBooks, activeMembers, overdueCount);
    }
}