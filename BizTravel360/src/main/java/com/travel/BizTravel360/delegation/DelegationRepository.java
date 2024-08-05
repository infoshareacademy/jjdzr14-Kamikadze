package com.travel.BizTravel360.delegation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Repository
public interface DelegationRepository {
    
    void createDelegation(Delegation delegation) throws IOException;
    Page<Delegation> fetchDelegationPage(Pageable pageable) throws IOException;
    List<Delegation> loadDelegationFromFile() throws IOException;
    Optional<Delegation> fetchDelegationById(Long id) throws IOException;
}
