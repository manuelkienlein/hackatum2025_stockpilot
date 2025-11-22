package de.tum.hack.stockpilot.repositories;

import de.tum.hack.stockpilot.entities.OrderEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OrderRepository implements PanacheRepository<OrderEntity> {
    // Panache bringt schon alle Standardmethoden mit:
    // listAll(), findById(), findByIdOptional(), persist(), deleteById(), ...
}
