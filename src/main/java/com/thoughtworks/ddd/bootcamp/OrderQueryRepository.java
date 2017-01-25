package com.thoughtworks.ddd.bootcamp;

import com.thoughtworks.domain.model.OrderView;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderQueryRepository extends CouchbaseRepository<OrderView,String> {
}
