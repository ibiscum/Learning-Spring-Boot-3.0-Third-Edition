package com.springbootlearning.learningspringboot3;

import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class ApiControllerTest {

  ApiController controller;
  @Mock EmployeeRepository repository;

  @BeforeEach
  void setUp() {
    this.controller = new ApiController(repository);
  }

  @Test
  void employeesShouldReturnAllEmployees() {
    // given
    Employee emp1 = new Employee("alice", "management");
    emp1.setId(1L);
    Employee emp2 = new Employee("bob", "engineering");
    emp2.setId(2L);
    given(repository.findAll()).willReturn(Flux.just(emp1, emp2));

    // when
    Flux<Employee> result = controller.employees();

    // then
    StepVerifier.create(result)
        .expectNext(emp1)
        .expectNext(emp2)
        .verifyComplete();

    verify(repository).findAll();
  }

  @Test
  void addShouldSaveEmployeeToRepository() {
    // given
    Employee newEmployee = new Employee("charlie", "engineering");
    newEmployee.setId(3L);
    given(repository.save(any(Employee.class))).willReturn(Mono.just(newEmployee));

    // when
    Mono<Employee> result = controller.add(Mono.just(newEmployee));

    // then
    StepVerifier.create(result)
        .expectNext(newEmployee)
        .verifyComplete();

    verify(repository).save(any(Employee.class));
  }

  @Test
  void addWithMultipleEmployeesShouldSaveEach() {
    // given
    Employee emp1 = new Employee("dave", "finance");
    emp1.setId(4L);
    Employee emp2 = new Employee("eva", "hr");
    emp2.setId(5L);

    given(repository.save(any(Employee.class)))
        .willReturn(Mono.just(emp1))
        .willReturn(Mono.just(emp2));

    // when & then
    StepVerifier.create(controller.add(Mono.just(emp1)))
        .expectNext(emp1)
        .verifyComplete();

    StepVerifier.create(controller.add(Mono.just(emp2)))
        .expectNext(emp2)
        .verifyComplete();
  }
}
