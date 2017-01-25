package com.thoughtworks.ddd.bootcamp;

import com.thoughtworks.domain.model.OrderView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path="/order")
public class OrderViewAPI {

    private OrderQueryRepository repository;

    public OrderViewAPI(OrderQueryRepository repository) {
        this.repository = repository;
    }

    @GetMapping(path="/{id}")
    public OrderView get(@PathVariable String id) {
        return repository.findOne(id);
    }
}