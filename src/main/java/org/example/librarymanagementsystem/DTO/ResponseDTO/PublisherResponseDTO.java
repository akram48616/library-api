package org.example.librarymanagementsystem.DTO.ResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "publisher response data")
public class PublisherResponseDTO implements Serializable {

    private Integer id;
    private String name;
    private String address;
    private String phone;
    // books NOT included — avoids circular response
}
