package com.melodify.Melodify.Repositories;

import com.melodify.Melodify.Models.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepo extends MongoRepository<User, String> {
}
