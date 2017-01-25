package com.thoughtworks.domain.model;

import com.couchbase.client.java.repository.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;

import java.util.List;

@Document
public class OrderView {

    @Id
    private final String id;

    private final List<Item> items;

    public OrderView(String id, List<Item> items) {

        this.id = id;
        this.items = items;
    }

    public String getId() {
        return id;
    }

    public List<Item> getItems() {
        return items;
    }

    @Override
    public String toString() {
        return "OrderView{" +
                "id='" + id + '\'' +
                ", items=" + items +
                '}';
    }
}
