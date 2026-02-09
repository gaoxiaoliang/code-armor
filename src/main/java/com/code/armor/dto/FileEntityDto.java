package com.code.armor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import com.code.armor.entity.FileStatus;
import com.code.armor.entity.ProjectType;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class FileEntityDto {

    private Long id;
    private String fileName;
    private Long size;
    private ProjectType projectType;
    private FileStatus status;
    private LocalDateTime uploadedAt;
}
