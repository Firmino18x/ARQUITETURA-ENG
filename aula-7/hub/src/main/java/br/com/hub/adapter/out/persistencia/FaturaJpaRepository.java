package br.com.hub.adapter.out.persistencia;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FaturaJpaRepository extends JpaRepository<FaturaJpaEntity, UUID> {
}
