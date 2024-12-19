package ru.practicum.ewm.categories.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.categories.dto.CategoryDto;
import ru.practicum.ewm.categories.dto.NewCategoryDto;
import ru.practicum.ewm.categories.entity.Category;
import ru.practicum.ewm.categories.mapper.CategoryMapper;
import ru.practicum.ewm.categories.repository.CategoryRepository;
import ru.practicum.ewm.events.repository.EventRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryDto addCategory(NewCategoryDto newCategoryDto) {

        String nameCategory = newCategoryDto.getName();

        if (categoryRepository.existsByName(nameCategory)) {
            throw new ConflictException("Категория c именем " + nameCategory + " уже существует");
        }

        return categoryMapper.toCategoryDto(
                categoryRepository.save(categoryMapper.toCategory(newCategoryDto)));
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long categoryId, CategoryDto categoryDto) {

        String nameCategory = categoryDto.getName();

        Category category = getCategory(categoryId);

        if (categoryRepository.existsByName(nameCategory)) {
            if (!nameCategory.equals(category.getName())) {
                throw new ConflictException("Категория c именем " + nameCategory + " уже существует");
            }
        }

        category.setName(categoryDto.getName());

        return categoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        return categoryRepository.findAll(PageRequest.of(from / size, size)).stream()
                .map(categoryMapper::toCategoryDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Long categoryId) {
        return categoryMapper.toCategoryDto(getCategory(categoryId));
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {

        Category category = categoryRepository.findById(categoryId).orElseThrow(() ->
                new NotFoundException("Категория с id=" + categoryId + " не найдена"));

        if (eventRepository.existsByCategory(category)) {
            throw new ConflictException("Категория не доступна");
        }

        categoryRepository.deleteById(categoryId);

    }

    @Transactional(readOnly = true)
    private Category getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(() ->
                new NotFoundException("Категория с id=" + categoryId + " не найдена"));
    }

}
