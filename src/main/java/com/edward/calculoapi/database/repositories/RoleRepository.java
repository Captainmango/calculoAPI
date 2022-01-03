package com.edward.calculoapi.database.repositories;

import com.edward.calculoapi.models.ERole;
import com.edward.calculoapi.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
}
