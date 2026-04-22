package com.Airtel.Inventory.controller;

import com.Airtel.Inventory.model.*;
import com.Airtel.Inventory.repository.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class InventoryController {
    
    private final DeviceRepository deviceRepository;
    private final EmployeeRepository employeeRepository;
    private final AssignmentRepository assignmentRepository;
    
    public InventoryController(DeviceRepository deviceRepository,
                               EmployeeRepository employeeRepository,
                               AssignmentRepository assignmentRepository) {
        this.deviceRepository = deviceRepository;
        this.employeeRepository = employeeRepository;
        this.assignmentRepository = assignmentRepository;
    }
    
    @GetMapping("/")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        model.addAttribute("username", username);
        
        model.addAttribute("totalDevices", deviceRepository.count());
        model.addAttribute("availableDevices", deviceRepository.countByStatus("Available"));
        model.addAttribute("assignedDevices", deviceRepository.countByStatus("Assigned"));
        model.addAttribute("totalEmployees", employeeRepository.count());
        model.addAttribute("activeAssignments", assignmentRepository.findByStatus("Active").size());
        model.addAttribute("recentDevices", deviceRepository.findAll().stream().limit(5).toList());
        return "dashboard";
    }
    
    @GetMapping("/devices")
    public String listDevices(Model model) {
        model.addAttribute("devices", deviceRepository.findAll());
        model.addAttribute("deviceTypes", getDeviceTypes());
        model.addAttribute("statuses", getStatuses());
        return "devices/list";
    }
    
    @GetMapping("/devices/add")
    public String showAddDeviceForm(Model model) {
        model.addAttribute("device", new Device());
        model.addAttribute("deviceTypes", getDeviceTypes());
        model.addAttribute("conditions", getConditions());
        model.addAttribute("statuses", getStatuses());
        return "devices/add";
    }
    
    @PostMapping("/devices/save")
    public String saveDevice(@ModelAttribute Device device, RedirectAttributes redirectAttributes) {
        device.setUpdatedAt(LocalDateTime.now());
        deviceRepository.save(device);
        redirectAttributes.addFlashAttribute("success", "Device added successfully!");
        return "redirect:/devices";
    }
    
    @GetMapping("/devices/edit/{id}")
    public String showEditDeviceForm(@PathVariable Long id, Model model) {
        Optional<Device> device = deviceRepository.findById(id);
        if (device.isPresent()) {
            model.addAttribute("device", device.get());
            model.addAttribute("deviceTypes", getDeviceTypes());
            model.addAttribute("conditions", getConditions());
            model.addAttribute("statuses", getStatuses());
            return "devices/edit";
        }
        return "redirect:/devices";
    }
    
    @PostMapping("/devices/update")
    public String updateDevice(@ModelAttribute Device device, RedirectAttributes redirectAttributes) {
        device.setUpdatedAt(LocalDateTime.now());
        deviceRepository.save(device);
        redirectAttributes.addFlashAttribute("success", "Device updated successfully!");
        return "redirect:/devices";
    }
    
    @GetMapping("/devices/delete/{id}")
    public String deleteDevice(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        deviceRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Device deleted successfully!");
        return "redirect:/devices";
    }
    
    @GetMapping("/employees")
    public String listEmployees(Model model) {
        model.addAttribute("employees", employeeRepository.findAll());
        return "employees/list";
    }
    
    @GetMapping("/employees/add")
    public String showAddEmployeeForm(Model model) {
        model.addAttribute("employee", new Employee());
        model.addAttribute("departments", getDepartments());
        return "employees/add";
    }
    
    @PostMapping("/employees/save")
    public String saveEmployee(@ModelAttribute Employee employee, RedirectAttributes redirectAttributes) {
        employee.setJoinedDate(LocalDateTime.now());
        employeeRepository.save(employee);
        redirectAttributes.addFlashAttribute("success", "Employee added successfully!");
        return "redirect:/employees";
    }
    
    @GetMapping("/assignments")
    public String listAssignments(Model model) {
        model.addAttribute("assignments", assignmentRepository.findAll());
        return "assignments/list";
    }
    
    @GetMapping("/assignments/assign")
    public String showAssignForm(Model model) {
        model.addAttribute("assignment", new Assignment());
        model.addAttribute("devices", deviceRepository.findByStatus("Available"));
        model.addAttribute("employees", employeeRepository.findByIsActiveTrue());
        return "assignments/assign";
    }
    
    @PostMapping("/assignments/save")
    public String saveAssignment(@RequestParam Long deviceId,
                                 @RequestParam Long employeeId,
                                 @RequestParam String purpose,
                                 @RequestParam String assignedBy,
                                 RedirectAttributes redirectAttributes) {
        
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
        
        if (deviceOpt.isPresent() && employeeOpt.isPresent()) {
            Assignment assignment = new Assignment();
            assignment.setDevice(deviceOpt.get());
            assignment.setEmployee(employeeOpt.get());
            assignment.setPurpose(purpose);
            assignment.setAssignedBy(assignedBy);
            assignment.setConditionAtAssignment(deviceOpt.get().getDeviceCondition());
            
            assignmentRepository.save(assignment);
            
            Device device = deviceOpt.get();
            device.setStatus("Assigned");
            deviceRepository.save(device);
            
            redirectAttributes.addFlashAttribute("success", "Device assigned successfully!");
        }
        return "redirect:/assignments";
    }
    
    @GetMapping("/assignments/return/{id}")
    public String returnDevice(@PathVariable Long id,
                               @RequestParam String conditionAtReturn,
                               RedirectAttributes redirectAttributes) {
        
        Optional<Assignment> assignmentOpt = assignmentRepository.findById(id);
        if (assignmentOpt.isPresent()) {
            Assignment assignment = assignmentOpt.get();
            assignment.setReturnDate(LocalDateTime.now());
            assignment.setConditionAtReturn(conditionAtReturn);
            assignment.setStatus("Returned");
            assignmentRepository.save(assignment);
            
            Device device = assignment.getDevice();
            device.setStatus("Available");
            device.setDeviceCondition(conditionAtReturn);
            deviceRepository.save(device);
            
            redirectAttributes.addFlashAttribute("success", "Device returned successfully!");
        }
        return "redirect:/assignments";
    }
    
    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAttribute("totalDevices", deviceRepository.count());
        model.addAttribute("availableDevices", deviceRepository.countByStatus("Available"));
        model.addAttribute("assignedDevices", deviceRepository.countByStatus("Assigned"));
        
        Map<String, Long> devicesByType = new HashMap<>();
        for (String type : getDeviceTypes()) {
            devicesByType.put(type, (long) deviceRepository.findByDeviceType(type).size());
        }
        model.addAttribute("devicesByType", devicesByType);
        
        Map<String, Long> devicesByCondition = new HashMap<>();
        for (String condition : getConditions()) {
            devicesByCondition.put(condition, (long) deviceRepository.findByDeviceCondition(condition).size());
        }
        model.addAttribute("devicesByCondition", devicesByCondition);
        
        model.addAttribute("activeAssignments", assignmentRepository.findByStatus("Active"));
        return "reports/index";
    }
    
    private List<String> getDeviceTypes() {
        return List.of("Laptop", "Desktop", "Phone", "Tablet", "Router", "Printer", "Other");
    }
    
    private List<String> getConditions() {
        return List.of("New", "Good", "Fair", "Poor", "Damaged");
    }
    
    private List<String> getStatuses() {
        return List.of("Available", "Assigned", "Under Repair", "Retired");
    }
    
    private List<String> getDepartments() {
        return List.of("IT", "HR", "Finance", "Sales", "Marketing", "Operations", "Customer Service");
    }
}