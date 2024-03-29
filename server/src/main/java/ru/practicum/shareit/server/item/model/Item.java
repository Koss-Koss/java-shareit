package ru.practicum.shareit.server.item.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.server.user.model.User;

import javax.persistence.*;

@Entity
@Table(name = "items")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String name;
    String description;
    @Column(name = "is_available")
    Boolean available;
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    User owner;
    @Column(name = "request_id")
    Long requestId;
}
