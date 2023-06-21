package com.example.EmployeeService.Controller;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.EmployeeService.model.Employee;
import com.example.EmployeeService.model.TaxDeduction;
import com.example.EmployeeService.repo.EmployeeRepository;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

	@Autowired
	private EmployeeRepository employeeRepository;

	@PostMapping("/add")
	public ResponseEntity<String> createEmployee(@RequestBody Employee employee) {

		boolean inValid = validateEmployeeDetails(employee);

		if (inValid) {
			return ResponseEntity.badRequest().body("Invalid employee data");
		}

		employeeRepository.save(employee);

		return ResponseEntity.status(HttpStatus.CREATED).body("Employee details stored successfully");
	}

	private boolean validateEmployeeDetails(Employee employee) {

		return employee.getEmployeeId() == null || employee.getFirstName() == null || employee.getLastName() == null
				|| employee.getEmail() == null || employee.getPhoneNumbers().isEmpty() || employee.getDoj() == null
				|| employee.getSalary() <= 0;
	}

	@GetMapping("/tax-deductions")
	public ResponseEntity<List<TaxDeduction>> calculateTaxDeductions() {
		List<Employee> employees = employeeRepository.findAll();

        List<TaxDeduction> taxDeductions = new ArrayList<>();
        for (Employee employee : employees) {
            double totalSalary = calculateTotalSalary(employee);
            double taxAmount = calculateTax(totalSalary);
            double cessAmount = calculateCess(totalSalary);
            TaxDeduction taxDeduction = new TaxDeduction(employee.getEmployeeId(), employee.getFirstName(),
                    employee.getLastName(), employee.getSalary() * 12, taxAmount, cessAmount);
            taxDeductions.add(taxDeduction);
        }
        return ResponseEntity.ok(taxDeductions);
	}

	private double calculateTotalSalary(Employee employee) {
	    double totalSalary = employee.getSalary();
	    LocalDate doj = LocalDate.parse(employee.getDoj(), DateTimeFormatter.ISO_DATE);
	    int joiningYear = doj.getYear();
	    int joiningMonth = doj.getMonthValue();

	    if (joiningMonth >= 4) {
	        totalSalary *= 12;
	    } else {
	        totalSalary *= 12 - 1;
	        joiningYear -= 1;
	    }

	    LocalDate financialYearStart = LocalDate.of(joiningYear, Month.APRIL, 1);

	    if (doj.isBefore(financialYearStart)) {
	        totalSalary -= employee.getSalary() * (financialYearStart.until(doj, ChronoUnit.DAYS) / 30.0);
	    }

	    return totalSalary;}

    private double calculateTax(double totalSalary) {
        if (totalSalary <= 250000) {
            return 0;
        } else if (totalSalary <= 500000) {
            return (totalSalary - 250000) * 0.05;
        } else if (totalSalary <= 1000000) {
            return 12500 + (totalSalary - 500000) * 0.1;
        } else {
            return 12500 + 50000 + (totalSalary - 1000000) * 0.2;
        }
    }

    private double calculateCess(double totalSalary) {
        if (totalSalary > 2500000) {
            return (totalSalary - 2500000) * 0.02;
        } else {
            return 0;
        }
    }
}
