package org.example.librarymanagementsystem.DTO.ResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.example.librarymanagementsystem.entity.BorrowRecord;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Borrow response data")
public class BorrowResponseDTO implements Serializable {

    private Integer id;
    private LocalDate borrowDate;
    private LocalDate returnDate;
    private LocalDate dueDate;
    private double fine;
    private BorrowRecord.BorrowStatus status;
    private boolean overdue;                // dueDate < today

    private BookSummaryDTO book;       // ✅ was BookResponseDTO
    private MemberSummaryDTO member;   // ✅ was MemberResponseDTO
}
