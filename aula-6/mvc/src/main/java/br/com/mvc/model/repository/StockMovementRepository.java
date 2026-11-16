package br.com.mvc.model.repository;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.mvc.model.entity.StockMovement;

public interface StockMovementRepository extends JpaRepository<StockMovement, BigInteger> {

    List<StockMovement> findAllByOrderByCreatedAtDescIdDesc();
}
