package com.media.website.service;

import com.media.website.entity.Category;
import com.media.website.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;

    public List<Category> getCategoryList() {
        return categoryMapper.findAll();
    }
}
