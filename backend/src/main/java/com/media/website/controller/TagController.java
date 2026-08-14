package com.media.website.controller;

import com.media.website.service.TagService;
import com.media.website.vo.TagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
