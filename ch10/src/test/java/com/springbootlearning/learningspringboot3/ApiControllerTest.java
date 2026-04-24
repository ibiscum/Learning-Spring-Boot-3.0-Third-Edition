package com.springbootlearning.learningspringboot3;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

  @Test
  void employeesShouldReturnEmptyFluxWhenNoEmployees() {
    // given
    given(repository.findAll()).willReturn(Flux.empty());

    // when
    Flux<Employee> result = controller.employees();

    // then
    StepVerifier.create(result)
        .verifyComplete();

    verify(repository).findAll();
  }

  @Test
  void addShouldIgnoreRequestIdAndSaveFreshEmployee() {
    // given
    Employee request = new Employee("zoe", "marketing");
    request.setId(99L);
    ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
    Employee saved = new Employee("zoe", "marketing");
    saved.setId(7L);
    given(repository.save(any(Employee.class))).willReturn(Mono.just(saved));

    // when
    Mono<Employee> result = controller.add(Mono.just(request));

    // then
    StepVerifier.create(result)
        .expectNext(saved)
        .verifyComplete();

    verify(repository).save(captor.capture());
    Employee argument = captor.getValue();
    assertNull(argument.getId());
    assertEquals("zoe", argument.getName());
    assertEquals("marketing", argument.getRole());
  }

  @Test
  void addShouldPropagateRepositoryError() {
    // given
    Employee request = new Employee("fred", "operations");
    given(repository.save(any(Employee.class))).willReturn(Mono.error(new IllegalStateException("save failure")));

    // when
    Mono<Employee> result = controller.add(Mono.just(request));

    // then
    StepVerifier.create(result)
        .expectErrorMatches(throwable -> throwable instanceof IllegalStateException
            && throwable.getMessage().equals("save failure"))
        .verify();

    verify(repository).save(any(Employee.class));
  }
}
