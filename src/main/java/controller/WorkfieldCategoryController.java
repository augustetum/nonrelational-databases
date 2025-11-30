package controller;

import entity.WorkfieldCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.WorkfieldCategoryService;

@RestController
@RequestMapping("/api/workfield-categories")
public class WorkfieldCategoryController {

    @Autowired
    private WorkfieldCategoryService categoryService;

    @PostMapping
    public ResponseEntity<?> addCategory(@RequestBody WorkfieldCategory category) {
        categoryService.addCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
