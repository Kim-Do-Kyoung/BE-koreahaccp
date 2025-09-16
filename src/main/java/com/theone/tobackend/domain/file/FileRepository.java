package com.theone.tobackend.domain.file;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<File, Long>, JpaSpecificationExecutor<File> {
    List<File> findByServiceNameAndServiceId(String serviceName, String serviceId);
    Optional<File> findByExternalId(String externalId);
    void deleteByExternalId(String externalId);

    List<File> findAllByServiceName(String serviceName);
}
