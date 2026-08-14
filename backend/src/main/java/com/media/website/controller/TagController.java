package com.media.website.controller;

import com.media.website.dto.TagCreateRequestDto;
import com.media.website.service.TagService;
import com.media.website.vo.ApiErrorVO;
import com.media.website.vo.TagVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public ResponseEntity<List<TagVO>> getTagList() {
        List<TagVO> tags = tagService.getTagList().stream()
                .map(TagVO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(tags);
    }

    @PostMapping
    public ResponseEntity<?> createTag(@Valid @RequestBody TagCreateRequestDto request) {
        try {
            return ResponseEntity.ok(TagVO.from(tagService.createTag(request.getName())));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiErrorVO(e.getMessage()));
        }
    }
}
