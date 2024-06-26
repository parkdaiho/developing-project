package me.parkdaiho.project.dto.notice;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class ModifyNoticeRequest {

    private Long id;
    private String title;
    private String text;
    private Boolean isFixed;
    private List<MultipartFile> files;
}
