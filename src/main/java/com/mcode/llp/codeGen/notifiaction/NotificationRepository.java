package com.mcode.llp.codeGen.notifiaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;


@Repository
public interface NotificationRepository extends JpaRepository<ClientDetails, Long> {
    List<ClientDetails> findByIsPaidTrueAndPaymentDateBetween(LocalDate StartDate , LocalDate EndDate);
    List<ClientDetails>  findByIsPaidFalse();


}
