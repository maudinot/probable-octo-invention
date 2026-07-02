package com.github.maudinot.octo_invention.repository;

import com.github.maudinot.octo_invention.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByName(String name);
}
