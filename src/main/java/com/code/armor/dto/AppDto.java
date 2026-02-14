package com.code.armor.dto;

import com.code.armor.entity.AppStatus;
import com.code.armor.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;

import com.code.armor.entity.AppType;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AppDto {

    private String fileName;
    private Long size;
    private AppType appType;
    private AppStatus status;
    private String siteURL;
    private LocalDateTime uploadedAt;

}
