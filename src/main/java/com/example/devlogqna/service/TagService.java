package com.example.devlogqna.service;

import com.example.devlogqna.dto.request.TagRequest;
import com.example.devlogqna.dto.response.TagResponse;
import com.example.devlogqna.entity.Tag;
import com.example.devlogqna.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;

    public List<TagResponse> getAllTags() {
        return tagRepository.findAll().stream()
                .map(tag -> new TagResponse(tag.getId(), tag.getName()))
                .collect(Collectors.toList());
    }

    public List<TagResponse> getActiveTags() {
        return tagRepository.findTagsUsedInPublicQuestions()
                .stream()
                .map(tag -> new TagResponse(tag.getId(), tag.getName()))
                .collect(Collectors.toList());
    }

    @Transactional
    public TagResponse createTag(TagRequest request) {
        Tag tag = tagRepository.save(new Tag(request.getName()));
        return new TagResponse(tag.getId(), tag.getName());
    }

    @Transactional
    public void deleteTag(Long id) {
        tagRepository.deleteById(id);
    }
}
