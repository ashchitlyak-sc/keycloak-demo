package com.scand.keycloak.service;

import com.scand.keycloak.domain.User;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.util.List;

public class UserService {

    private final EntityManager entityManager;

    public UserService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public User create(User user) {
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        entityManager.persist(user);
        transaction.commit();
        return user;
    }

    public void delete(User user) {
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        entityManager.remove(user);
        transaction.commit();
    }

    public User update(User userEntity) {
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        entityManager.merge(userEntity);
        transaction.commit();
        return userEntity;
    }

    public List<User> getUsers() {
        return getUsers(null, null);
    }

    public List<User> getUsers(Integer start, Integer max) {
        TypedQuery<User> query = entityManager.createNamedQuery("getUsers", User.class);
        if (start != null) {
            query.setFirstResult(start);
        }
        if (max != null) {
            query.setMaxResults(max);
        }
        return query.getResultList();
    }

    public User getUserById(String id) {
        return entityManager.find(User.class, Long.parseLong(id));
    }

    public User getUserByLogin(String login) {
        TypedQuery<User> query = entityManager.createNamedQuery("getUserByLogin", User.class);
        query.setParameter("login", login);
        return query.getResultList().stream().findFirst().orElse(null);
    }

    public User getUserByEmail(String email) {
        TypedQuery<User> query = entityManager.createNamedQuery("getUserByEmail", User.class);
        query.setParameter("email", email);
        return query.getResultList().stream().findFirst().orElse(null);
    }

    public List<User> searchForUserByUsernameOrEmail(String searchString) {
        return searchForUserByUsernameOrEmail(searchString, null, null);
    }

    public List<User> searchForUserByUsernameOrEmail(String searchString, Integer start, Integer max) {
        TypedQuery<User> query = entityManager.createNamedQuery("searchByLoginOrEmail", User.class);
        query.setParameter("search", "%" + searchString + "%");

        if (start != null) {
            query.setFirstResult(start);
        }
        if (max != null) {
            query.setMaxResults(max);
        }

        return query.getResultList();
    }

    public Integer getCount() {
        return entityManager.createNamedQuery("getUserCount", Integer.class).getSingleResult();
    }

    public void close() {
        entityManager.close();
    }
}
