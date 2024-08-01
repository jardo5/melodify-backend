package com.melodify.Melodify.Repositories;

import com.melodify.Melodify.Models.ConnectedAccount;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConnectedAccountsRepo extends MongoRepository<ConnectedAccount, String> {
}
