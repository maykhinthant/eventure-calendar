package com.eventure.calendar_app.user.repo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.eventure.calendar_app.user.model.Users;

@DataJpaTest
@ActiveProfiles("test")
class UserRepoTest {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private TestEntityManager entityManager;

    private Users persistedUser;

    @BeforeEach
    void setUp() {
        persistedUser = new Users();
        persistedUser.setUsername("john_doe");
        persistedUser.setEmail("john.doe@example.com");
        persistedUser.setName("John Doe");
        persistedUser.setPassword("password");
        persistedUser.setProvider("local");
        persistedUser.setProviderId("local-id");
        persistedUser.setRoles("ROLE_USER");

        entityManager.persist(persistedUser);
        entityManager.flush();
    }

    @Test
    void findByEmail_returnsUser_whenEmailExists() {
        Users found = userRepo.findByEmail("john.doe@example.com");

        assertThat(found)
                .as("Should return persisted user for matching email")
                .isNotNull()
                .extracting(Users::getUsername)
                .isEqualTo("john_doe");
    }

    @Test
    void findByEmail_returnsNull_whenEmailDoesNotExist() {
        Users found = userRepo.findByEmail("missing@example.com");

        assertThat(found).as("Should be null when email is not in database").isNull();
    }

    @Test
    void findByUsername_returnsUser_whenUsernameExists() {
        Users found = userRepo.findByUsername("john_doe");

        assertThat(found)
                .as("Should return persisted user for matching username")
                .isNotNull()
                .extracting(Users::getEmail)
                .isEqualTo("john.doe@example.com");
    }

    @Test
    void findByUsername_returnsNull_whenUsernameDoesNotExist() {
        Users found = userRepo.findByUsername("missing_user");

        assertThat(found).as("Should be null when username is not in database").isNull();
    }
}
