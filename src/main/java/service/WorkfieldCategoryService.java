package service;

import entity.WorkfieldCategory;

import java.util.List;

import org.springframework.stereotype.Service;
import repository.WorkfieldCategoryRepository;

@Service
public class WorkfieldCategoryService {
    private final WorkfieldCategoryRepository categoryRepository;

    public WorkfieldCategoryService(WorkfieldCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<WorkfieldCategory> getAncestry(String categoryId) {
        return  categoryRepository.getAncestry(categoryId);
    }

    public void addWorkfieldCategory(WorkfieldCategory category) {
        if (categoryRepository.isDirectChildOf(category.getName(), category.getParentId())) {
            throw new IllegalArgumentException("Category with name '" + category.getName() + "' already exists in this path.");
        }

        categoryRepository.add(category);
    }

    public boolean categoryExists(String categoryId) {
        return categoryRepository.existsById(categoryId);
    }

    public WorkfieldCategory getCategoryById(String categoryId) {
        return categoryRepository.getById(categoryId);
    }
}
