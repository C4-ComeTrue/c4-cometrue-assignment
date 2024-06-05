package org.c4marathon.assignment.accounts.entity;

import jakarta.persistence.*;
import lombok.*;
import org.c4marathon.assignment.user.entity.UserEntity;
@Entity
@Table(name="accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int account;

    private String id;
    private long balance;
    private long transferLimit;
    @Enumerated(EnumType.ORDINAL)
    private AccountType accountType;
}
