package com.mcode.llp.codeGen.databases;

import com.mcode.llp.codeGen.models.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

public @Repository  interface SchemaDAO extends JpaRepository<Property, Long> {
    @Query(value = "SELECT *  FROM property WHERE entity = :entityname", nativeQuery = true)
    Set<Property> findByEntityName(@Param("entityname") String entityname);

    @Query(value = "SELECT DISTINCT entity FROM property", nativeQuery = true)
    List<String> getAllEntityNames();

}


