package com.travel.BizTravel360.employee.controller;

import com.travel.BizTravel360.employee.domain.EmployeeService;
import com.travel.BizTravel360.employee.model.dto.AnalyticsDTO;
import com.travel.BizTravel360.employee.model.dto.EmployeeDTO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Slf4j
@Controller
public class EmployeeController {
    
    private static final String PAGE_DEFAULT_VALUE = "0";
    private static final String SIZE_DEFAULT_VALUE = "10";
    
    private final EmployeeService employeeService;
    
    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }
    
    //URL will have first and last name after login
    @GetMapping("/hello/employee")
    public String hello(Model model) {
        String currentEmployeeEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        
        EmployeeDTO employeeDTO = employeeService.getEmployeeByEmail(currentEmployeeEmail);
        
        model.addAttribute("firstName", employeeDTO.getFirstName());
        model.addAttribute("lastName", employeeDTO.getLastName());
        
        return "employee/helloEmployee";
    }
    
    @GetMapping("/profile/employee")
    public String profile() {
        return "authorization/profileEmployee";
    }
    
    @GetMapping("/employees/employee")
    public String getAllEmployees(@RequestParam(value = "page", defaultValue = PAGE_DEFAULT_VALUE) int page,
                                  @RequestParam(value = "size", defaultValue = SIZE_DEFAULT_VALUE) int size,
                                  Model model) {
        
        Page<EmployeeDTO> employees = employeeService.findAll(PageRequest.of(page, size));
        log.info("Fetched: {} employees", employees.getTotalElements());
        model.addAttribute("employees", employees);
        
        int totalPages = employees.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.range(0, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }
        return "employee/employees";
    }
    
    @GetMapping("/employee/employee")
    public String showSaveEmployeeForm(Model model) {
        model.addAttribute("employee", new EmployeeDTO());
        return "employee/createEmployeeForm";
    }
    
    @PostMapping("/employee")
    public String saveEmployee(@Valid @ModelAttribute("employee") EmployeeDTO employeeDTO,
                               BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "employee/createEmployeeForm";
        }
        employeeService.save(employeeDTO);
        redirectAttributes.addFlashAttribute("successMessage", renderSuccessMessage(employeeDTO, "created"));
        return "redirect:/employees/employee";
    }
    
    @GetMapping("/employee/employee/{id}")
    public String showUpdateEmployeeForm(@PathVariable("id") Long employeeId, Model model) {
        EmployeeDTO employee = employeeService.getById(employeeId);
        model.addAttribute("employee", employee);
        return "employee/updateEmployeeForm";
    }
    
    @PostMapping("update-employee/employee")
    public String updateEmployee(@Valid @ModelAttribute("employee") EmployeeDTO employeeDTO, BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "employee/updateEmployeeForm";
        }
        employeeService.updateEmployee(employeeDTO);
        redirectAttributes.addFlashAttribute("successMessage", renderSuccessMessage(employeeDTO, "updated"));
        return "redirect:/employees/employee";
    }
    
    @PostMapping("/delete-employee/{employeeId}")
    public String deleteEmployee(@PathVariable("employeeId") Long employeeId,
                                 RedirectAttributes redirectAttributes) {
        employeeService.deleteById(employeeId);
        redirectAttributes.addFlashAttribute("successMessage", String.format("Successfully deleted employee with ID: %s", employeeId));
        return "redirect:/employees/employee";
    }
    
    @GetMapping("/employee/analytics/{id}")
    public String getEmployeeAnalytics(@PathVariable Long id, Model model) {
        AnalyticsDTO analyticsDTO = employeeService.getAnalyticsForEmployee(id);
        EmployeeDTO employeeDTO = employeeService.getById(id);
        
        model.addAttribute("analytics", analyticsDTO);
        model.addAttribute("employeeName", employeeDTO.getFirstName() + " " + employeeDTO.getLastName());
        
        return "employee/analytics";
    }
    
    
    
    @GetMapping("/employee/analytics/download/{id}")
    public ResponseEntity<ByteArrayResource> downloadEmployeeAnalytics(@PathVariable Long id) {
        AnalyticsDTO analyticsDTO = employeeService.getAnalyticsForEmployee(id);
        
        StringBuilder content = new StringBuilder();
        content.append("Popular Types of Transport:\n");
        analyticsDTO.getPopularTypeAccommodations().forEach((key, value) -> {
            content.append(key);
            content.append(": ");
            content.append(value);
            content.append("\n");
        });
        
        content.append("\nAccommodation Costs:\n");
        analyticsDTO.getAccommodationCosts().forEach((key, value) -> {
            content.append(key);
            content.append(": ");
            content.append(value);
            content.append("\n");
        });
        
        content.append("\nTransport Costs:\n");
        analyticsDTO.getTransportCosts().forEach((key, value) -> {
            content.append(key);
            content.append(": ");
            content.append(value);
            content.append("\n");
        });
        
        content.append("\nTotal Cost: ").append(analyticsDTO.getTotalCost()).append("\n");
        byte[] bytes = content.toString().getBytes();
        ByteArrayResource resource = new ByteArrayResource(bytes);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"analytics.txt\"")
                .contentType(MediaType.TEXT_PLAIN)
                .contentLength(bytes.length)
                .body(resource);
    }
    
    
    @GetMapping("/search-employee")
    public String searchEmployees(@RequestParam(value = "page", defaultValue = PAGE_DEFAULT_VALUE) int page,
                                  @RequestParam(value = "size", defaultValue = SIZE_DEFAULT_VALUE) int size,
                                  @RequestParam(value = "keyword", defaultValue = "") String keyword, Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<EmployeeDTO> employees = employeeService.searchEmployee(keyword, pageable);
        
        model.addAttribute("employees", employees);
        model.addAttribute("keyword", keyword);
        
        int totalPages = employees.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.range(0, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }
        return "employee/employees";
    }
    
    @GetMapping("/sending-password/{token}")
    public String showCreatePasswordForm(@PathVariable("token") String employeeToken, Model model) {
        EmployeeDTO employee = employeeService.getEmployeeByToken(employeeToken);
        model.addAttribute("employee", employee);
        return "email/formToSendPassword";
    }
    
    @PostMapping("/sending-password/password")
    public String sendPassword(@Valid @ModelAttribute("token") EmployeeDTO employeeDTO, BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "email/formToSendPassword";
        }
        employeeService.sendEmployeePassword(employeeDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Successfully added employee password" );
        return "redirect:/employees/employee";
    }
    
    private String renderSuccessMessage(EmployeeDTO employeeDTO, String action) {
        String successMessage = String.format("Successfully %s accommodation: %s",
                action,
                employeeDTO.getFirstName(),
                employeeDTO.getLastName());
        log.info(successMessage);
        return successMessage;
    }
}
