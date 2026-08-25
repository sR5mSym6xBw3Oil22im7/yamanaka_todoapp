package com.example.todoapp;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TodoMapper {

    List<Todo> findAll();

    Todo findById(Long id);

    void insert(Todo todo);

    void update(Todo todo);
}
