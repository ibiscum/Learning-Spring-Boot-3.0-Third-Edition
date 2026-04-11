package com.springbootlearning.learningspringboot3;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class ApiControllerTest {

  ApiController controller;

  @BeforeEach
  void setUp() {
    this.controller = new ApiController();
    ApiController.DATABASE.clear();
  }

  @Test
  void employeesShouldReturnListOfEmployees() {
    // when
    var result = controller.employees();

    // then
    StepVerifier.create(result)
        .expectNext(new Employee("alice", "management"))
        .expectNext(new Employee("bob", "payroll"))
        .verifyComplete();
  }

  @Test
  void addShouldStoreEmployeeInDatabase() {
    // given
    Employee newEmployee = new Employee("charlie", "engineering");
    Mono<Employee> employeeMono = Mono.just(newEmployee);

    // when
    Mono<Employee> result = controller.add(employeeMono);

    // then
    StepVerifier.create(result)
        .expectNext(newEmployee)
        .verifyComplete();

    assertThat(ApiController.DATABASE).containsEntry("charlie", newEmployee);
  }

  @Test
  void addMultipleEmployeesShouldStoreAllInDatabase() {
    // given
    Employee emp1 = new Employee("dave", "finance");
    Employee emp2 = new Employee("eva", "hr");

    // when
    controller.add(Mono.just(emp1)).block();
    controller.add(Mono.just(emp2)).block();

    // then
    assertThat(ApiController.DATABASE)
        .hasSize(2)
        .containsEntry("dave", emp1)
        .containsEntry("eva", emp2);
  }
}
