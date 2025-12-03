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

    public void addWorkfieldCategory(String name, String parentId) {
        if (categoryRepository.isDirectChildOf(name, parentId)) {
            throw new IllegalArgumentException("Category with name '" + name + "' already exists in this path.");
        }

        categoryRepository.add(name, parentId);
    }

    public boolean categoryExists(String categoryId) {
        return categoryRepository.existsById(categoryId);
    }

    public WorkfieldCategory getCategoryById(String categoryId) {
        return categoryRepository.getById(categoryId);
    }
}
