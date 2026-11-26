package com.vibesnapshot.repository;

import com.vibesnapshot.model.ArenaState;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ArenaStateRepository extends MongoRepository<ArenaState, String> {
}
