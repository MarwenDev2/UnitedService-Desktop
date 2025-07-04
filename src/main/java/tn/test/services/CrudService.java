package tn.test.services;

import java.util.List;

public interface CrudService<T> {
    boolean add(T t);
    void update(T t);
    void delete(int id);
    T findById(int id);
    List<T> findAll();
}