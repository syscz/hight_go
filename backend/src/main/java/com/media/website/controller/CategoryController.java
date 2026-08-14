package com.media.website.controller;

import com.media.website.service.CategoryService;
import com.media.website.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryVO>> getCategoryList() {
        List<CategoryVO> categories = categoryService.getCategoryList().stream()
                .map(CategoryVO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }
}
