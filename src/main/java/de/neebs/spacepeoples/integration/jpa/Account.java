package de.neebs.spacepeoples.integration.jpa;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@Setter
@Entity
public class Account {
    @Id
    @Column(name = "account_id")
    private String accountId;
    @Column(name = "password")
    private String password;
    @Column(name = "nickname")
    private String nickname;
}
