package controller;

import entity.WorkfieldCategory;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import dto.CreateWorkfieldCategoryRequest;
import service.WorkfieldCategoryService;

@RestController
@RequestMapping("/api/workfield-categories")
public class WorkfieldCategoryController {

    @Autowired
    private WorkfieldCategoryService categoryService;

    @GetMapping("ancestry/{categoryId}")
    public ResponseEntity<?> getAncestry(@PathVariable String categoryId) {
        List<WorkfieldCategory> result = categoryService.getAncestry(categoryId);
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<?> addCategory(@RequestBody CreateWorkfieldCategoryRequest request) {
        WorkfieldCategory category = new WorkfieldCategory();
        category.setParentId(request.getParentId());
        category.setName(request.getName());

        categoryService.addWorkfieldCategory(category);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
