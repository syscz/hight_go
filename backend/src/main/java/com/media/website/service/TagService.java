package com.media.website.service;

import com.media.website.entity.Tag;
import com.media.website.mapper.TagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagMapper tagMapper;

    public List<Tag> getTagList() {
        return tagMapper.findAll();
    }
}
