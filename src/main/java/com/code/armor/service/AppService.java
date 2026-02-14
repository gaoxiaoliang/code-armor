package com.code.armor.service;

import com.code.armor.dto.AppDto;
import com.code.armor.dto.PagedResponse;
import com.code.armor.entity.App;
import com.code.armor.entity.AppStatus;
import com.code.armor.entity.User;
import com.code.armor.exception.ClientIllegalArgumentException;
import com.code.armor.exception.InternalServerErrorException;
import com.code.armor.mapper.AppMapper;
import com.code.armor.repository.AppRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    private final AppRepository appRepository;
    private final AppMapper appMapper;
    private final AppProcessingService appProcessingService;

    private static final String LETTERS = "abcdefghijklmnopqrstuvwxyz";
    private static final String LETTERS_NUMBERS_HYPHEN = "abcdefghijklmnopqrstuvwxyz0123456789-";
    private static final String LETTERS_NUMBERS = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static String generateStorageName(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        sb.append(LETTERS.charAt(random.nextInt(LETTERS.length())));
        for (int i = 1; i < length - 1; i++) {
            sb.append(LETTERS_NUMBERS_HYPHEN.charAt(random.nextInt(LETTERS_NUMBERS_HYPHEN.length())));
        }
        sb.append(LETTERS_NUMBERS.charAt(random.nextInt(LETTERS_NUMBERS.length())));
        return sb.toString();
    }

    public void uploadApp(User user, MultipartFile multipartFile) {
        String storageName = generateStorageName(15);
        Path storagePath = Path.of(uploadDir, storageName);
        try {
            Files.copy(multipartFile.getInputStream(), storagePath);
        } catch (IOException e) {
            throw new InternalServerErrorException("Failed to store file. Please try again later.");
        }
        App app = new App();
        app.setUser(user);
        app.setFileName(multipartFile.getOriginalFilename());
        app.setStorageName(storageName);
        app.setSize(multipartFile.getSize());
        app.setAppType(null);
        app.setStatus(AppStatus.UPLOADED);
        appRepository.save(app);
        appProcessingService.processApp(app.getId());
    }

    public void deleteFile(User user, long id) {
        App file = appRepository.findById(id)
                .orElseThrow(() -> new ClientIllegalArgumentException("File not found"));
        if(!file.getUser().getId().equals(user.getId())){
            throw new ClientIllegalArgumentException("File not found");
        }
        try {
            Files.deleteIfExists(Path.of(uploadDir, file.getStorageName()));
        } catch (IOException e) {
            throw new InternalServerErrorException("Failed to delete file. Please try again later.");
        }
        appRepository.delete(file);
    }

    public PagedResponse<AppDto> listFiles(User user, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize,
                Sort.by(Sort.Direction.DESC, "uploadedAt"));
        Page<App> appPage = appRepository.findByUserId(user.getId(), pageable);
        List<AppDto> appDtoList = appPage.stream()
                .map(appMapper::toDto)
                .toList();
        return new PagedResponse<>(appDtoList, appPage.getTotalElements());
    }
}
