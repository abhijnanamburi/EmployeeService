package com.example.EmployeeService.Controller;

import java.time.LocalDate;
import java.time.Year;
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
			LocalDate currentDate = LocalDate.now();
			Year financialYear = Year.now();

			if (currentDate.getMonthValue() < 4) {
				financialYear = financialYear.minusYears(1);
			}

			LocalDate financialYearStart = LocalDate.of(financialYear.getValue(), 4, 1);
			LocalDate financialYearEnd = LocalDate.of(financialYear.plusYears(1).getValue(), 3, 31);

			LocalDate joiningDate = employee.getDoj();

			if (joiningDate.isAfter(financialYearStart)) {
				financialYearStart = joiningDate;
			}

			double totalSalary = (employee.getSalary() / 30)
					* ChronoUnit.DAYS.between(financialYearStart, financialYearEnd);

			double taxAmount = 0;
			if (totalSalary > 1000000) {
				taxAmount = (totalSalary - 1000000) * 0.2 + 500000 * 0.1 + 250000 * 0.05;
			} else if (totalSalary > 500000) {
				taxAmount = (totalSalary - 500000) * 0.1 + 250000 * 0.05;
			} else if (totalSalary > 250000) {
				taxAmount = (totalSalary - 250000) * 0.05;
			}

			double cessAmount = 0;
			if (totalSalary > 2500000) {
				cessAmount = (totalSalary - 2500000) * 0.02;
			}

			TaxDeduction taxDeduction = new TaxDeduction(employee.getEmployeeId(), employee.getFirstName(),
					employee.getLastName(), totalSalary, taxAmount, cessAmount);

			taxDeductions.add(taxDeduction);
		}

		return ResponseEntity.ok(taxDeductions);
	}

}
