package com.example.secondhandbackend.controller;

import com.example.secondhandbackend.dto.*;
import com.example.secondhandbackend.entity.Category;
import com.example.secondhandbackend.service.AdminService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/advertisements/pending")
    public List<PendingAdvertisementResponse> getPendingAdvertisements() {
        return adminService.getPendingAdvertisements();
    }

    @PutMapping("/advertisements/{id}/approve")
    public MessageResponse approveAdvertisement(@PathVariable Long id) {
        adminService.approveAdvertisement(id);
        return new MessageResponse("Advertisement approved");
    }

    @PutMapping("/advertisements/{id}/reject")
    public MessageResponse rejectAdvertisement(@PathVariable Long id, @RequestBody RejectAdvertisementRequest request) {
        adminService.rejectAdvertisement(id, request.getReason());
        return new MessageResponse("Advertisement rejected");
    }

    @GetMapping("/users")
    public List<AdminUserResponse> getAllUsers() {
        return adminService.getAllUsers();
    }

    @PutMapping("/users/{id}/block")
    public MessageResponse blockUser(@PathVariable Long id) {
        adminService.blockUser(id);
        return new MessageResponse("User blocked");
    }

    @PutMapping("/users/{id}/unblock")
    public MessageResponse unblockUser(@PathVariable Long id) {
        adminService.unblockUser(id);
        return new MessageResponse("User unblocked");
    }

    // ---------- مدیریت دسته‌بندی ----------

    @GetMapping("/categories")
    public List<Category> getAllCategories() {
        return adminService.getAllCategories();
    }

    @PostMapping("/categories")
    public Category createCategory(@RequestBody CategoryRequest request) {
        return adminService.createCategory(request.getName());
    }

    @PutMapping("/categories/{id}")
    public Category updateCategory(@PathVariable Long id, @RequestBody CategoryRequest request) {
        return adminService.updateCategory(id, request.getName());
    }

    @DeleteMapping("/categories/{id}")
    public MessageResponse deleteCategory(@PathVariable Long id) {
        adminService.deleteCategory(id);
        return new MessageResponse("Category deleted");
    }
}