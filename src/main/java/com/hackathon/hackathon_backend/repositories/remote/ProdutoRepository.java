package com.hackathon.hackathon_backend.repositories.remote;

import com.hackathon.hackathon_backend.models.remote.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Integer> {
}
