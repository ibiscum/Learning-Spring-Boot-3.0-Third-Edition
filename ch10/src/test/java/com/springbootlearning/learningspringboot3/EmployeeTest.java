package com.springbootlearning.learningspringboot3;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class EmployeeTest {

  @Test
  void gettersAndSettersShouldWork() {
    Employee employee = new Employee("alice", "engineering");
    assertNull(employee.getId());
    assertEquals("alice", employee.getName());
    assertEquals("engineering", employee.getRole());

    employee.setId(42L);
    employee.setName("alice smith");
    employee.setRole("engineering lead");

    assertEquals(42L, employee.getId());
    assertEquals("alice smith", employee.getName());
    assertEquals("engineering lead", employee.getRole());
  }

  @Test
  void equalsAndHashCodeShouldRespectIdNameAndRole() {
    Employee first = new Employee("bob", "finance");
    first.setId(1L);
    Employee second = new Employee("bob", "finance");
    second.setId(1L);

    assertEquals(first, second);
    assertEquals(first.hashCode(), second.hashCode());
  }

  @Test
  void equalsShouldReturnFalseForDifferentValues() {
    Employee first = new Employee("bob", "finance");
    first.setId(1L);
    Employee differentName = new Employee("bobby", "finance");
    differentName.setId(1L);
    Employee differentRole = new Employee("bob", "hr");
    differentRole.setId(1L);
    Employee differentId = new Employee("bob", "finance");
    differentId.setId(2L);

    assertNotEquals(first, differentName);
    assertNotEquals(first, differentRole);
    assertNotEquals(first, differentId);
    assertNotEquals(first, null);
    assertNotEquals(first, "not-an-employee");
  }

  @Test
  void equalsAndHashCodeShouldHandleNullFields() {
    Employee emptyOne = new Employee(null, null);
    Employee emptyTwo = new Employee(null, null);

    assertEquals(emptyOne, emptyTwo);
    assertEquals(emptyOne.hashCode(), emptyTwo.hashCode());
  }

  @Test
  void toStringShouldContainFieldValues() {
    Employee employee = new Employee("claire", "sales");
    employee.setId(99L);

    String result = employee.toString();
    assertTrue(result.contains("id=99"));
    assertTrue(result.contains("name='claire'"));
    assertTrue(result.contains("role='sales'"));
  }
}
