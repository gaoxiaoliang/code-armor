package com.code.armor.controller;

import com.code.armor.dto.AppDto;
import com.code.armor.dto.PagedResponse;
import com.code.armor.entity.User;
import com.code.armor.security.CustomUserDetails;
import com.code.armor.service.AppService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/app")
@RequiredArgsConstructor
public class AppController {

    private final AppService appService;

    @PostMapping
    public void uploadApp(@AuthenticationPrincipal CustomUserDetails userDetails,
                          @RequestParam("file") MultipartFile multipartFile){
        appService.uploadApp(userDetails.getUser(), multipartFile);
    }

    @GetMapping
    public PagedResponse<AppDto> listFiles(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam int pageNo,
            @RequestParam int pageSize
    ){
        User user = userDetails.getUser();
        return appService.listFiles(user, pageNo, pageSize);
    }

    @DeleteMapping("/{id}")
    public void deleteFile(@PathVariable long id,
                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        appService.deleteFile(userDetails.getUser(), id);
    }
}
