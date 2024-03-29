package com.edward.calculoapi.utils.fixtures;

import com.edward.calculoapi.api.models.*;
import com.edward.calculoapi.database.repositories.CategoryRepository;
import com.edward.calculoapi.database.repositories.RoleRepository;
import com.edward.calculoapi.database.repositories.ExpenseRepository;
import com.edward.calculoapi.database.repositories.UserRepository;
import com.edward.calculoapi.exceptions.RoleNotValidException;
import com.edward.calculoapi.api.models.*;
import com.github.javafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Component
public class DataFixture implements CommandLineRunner {

    @Autowired
    UserRepository userRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    ExpenseRepository expenseRepository;

    @Autowired
    RoleRepository roleRepository;

    private final Faker faker = new Faker(new Locale("en-GB"));

    @Autowired
    PasswordEncoder encoder;

    @Override
    public void run(String... args)
    {
        createUserData();
    }

    private void createUserData()
    {
        createRolesAndCategories();
        createUsers();
        createExpenses();
    }

    private void createUsers()
    {
        if (userRepository.count() == 0) {
            Set<Role> roles = new HashSet<>();
            Role adminRole = roleRepository
                    .findByName(ERole.ROLE_ADMIN)
                    .orElseThrow(() -> new RoleNotValidException("This is not a valid role"));
            roles.add(adminRole);

            User user = new User("Edward",
                    "Heaver",
                    "admin@calculo.com",
                    encoder.encode("password"));
            user.setRoles(roles);
            userRepository.save(user);
        }

        if (userRepository.count() == 0) {
            Set<Role> roles = new HashSet<>();
            Role adminRole = roleRepository
                    .findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RoleNotValidException("This is not a valid role"));
            roles.add(adminRole);

            User user = new User("User",
                    "Account",
                    "user@calculo.com",
                    encoder.encode("password"));
            user.setRoles(roles);
            userRepository.save(user);
        }
    }

    private void createRolesAndCategories()
    {
        if (roleRepository.count() == 0) {
            Set<Role> roles = new HashSet<>();
            for (ERole eRole : ERole.values()) {
                Role role = new Role(eRole);
                roles.add(role);
            }
            roleRepository.saveAll(roles);
        }

        if (categoryRepository.count() == 0) {
            Set<Category> categories = new HashSet<>();
            for (ECategory eCategory : ECategory.values()){
                Category category = new Category(eCategory);
                categories.add(category);
            }
            categoryRepository.saveAll(categories);
        }
    }

    private void createExpenses()
    {
        if (expenseRepository.count() == 0) {
            User user = userRepository.findById(1L).orElseThrow();
            for (int i = 0; i <= 10; i++) {
                Expense expense = new Expense(
                        faker.food().fruit(),
                        faker.gameOfThrones().quote(),
                        (float) faker.number().randomDouble(2,5,15),
                        user
                );

                Category category = categoryRepository
                        .findByName(ECategory.values()[2])
                        .orElseThrow();

                expense.setCategories(
                        Set.of(category)
                );

                expenseRepository.save(expense);
            }
        }
    }
}