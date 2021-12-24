package me.darkboy.snowyserver.account;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AccountRepository extends CrudRepository<SnowyAccount, Integer> {

    SnowyAccount findByEmail(String email);
    SnowyAccount findByName(String name);
    SnowyAccount findByToken(String token);
    List<SnowyAccount> findSnowyAccountsByNameContaining(String name);
}
