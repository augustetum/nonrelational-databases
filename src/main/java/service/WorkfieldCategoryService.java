package service;

import entity.WorkfieldCategory;
import org.springframework.stereotype.Service;
import repository.WorkfieldCategoryRepository;

@Service
public class WorkfieldCategoryService {
    private final WorkfieldCategoryRepository categoryRepository;

    public WorkfieldCategoryService(WorkfieldCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public void addCategory(WorkfieldCategory category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new IllegalArgumentException("Category with name '" + category.getName() + "' already exists.");
        }
        categoryRepository.add(category);
    }

    public boolean categoryExists(String categoryId) {
        return categoryRepository.existsById(categoryId);
    }

    public WorkfieldCategory getCategoryById(String categoryId) {
        return categoryRepository.findById(categoryId);
    }
}
