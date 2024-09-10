package org.c4marathon.assignment.user.domain;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
@Builder
@Getter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "users_id")
    private Long id;

    @Column(name = "userId", nullable = false)
    private String userId;

    @Column(name = "userPw", nullable = false)
    private String userPw;

    @Column(name = "name", nullable = false)
    private String name;

}
