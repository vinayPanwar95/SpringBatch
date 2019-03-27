package com.example.Repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.entity.User;

public interface UserRepo extends JpaRepository<User, Integer>{

}
