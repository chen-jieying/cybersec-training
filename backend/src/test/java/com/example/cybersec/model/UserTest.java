package com.example.cybersec.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserTest {

  @Test
  void shouldStoreAndRetrieveUserFields() {
    User user = new User();
    user.setId(7L);
    user.setUsername("alice");
    user.setPassword("secret");
    user.setRole("student");
    user.setFullName("Alice Wang");
    user.setGrade("七年�?);

    assertEquals(7L, user.getId());
    assertEquals("alice", user.getUsername());
    assertEquals("secret", user.getPassword());
    assertEquals("student", user.getRole());
    assertEquals("Alice Wang", user.getFullName());
    assertEquals("七年�?, user.getGrade());
  }

  @Test
  void shouldCreateUserWithDefaultValues() {
    User user = new User();

    assertNull(user.getId());
    assertNull(user.getUsername());
    assertNull(user.getPassword());
    assertNull(user.getRole());
    assertNull(user.getFullName());
    assertNull(user.getGrade());
  }
}

