package br.com.hub.adapter.out.persistencia;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.hub.application.port.out.FaturaRepositoryPort;
import br.com.hub.domain.model.Cliente;
import br.com.hub.domain.model.Fatura;
import br.com.hub.domain.model.ItemPedido;

/** Adaptador de saída: implementa a porta de persistência com JPA + H2. */
@Component
public class FaturaPersistenceAdapter implements FaturaRepositoryPort {

    private final FaturaJpaRepository repository;

    public FaturaPersistenceAdapter(FaturaJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Fatura salvar(Fatura fatura) {
        FaturaJpaEntity entity = repository.findById(fatura.getId()).orElseGet(FaturaJpaEntity::new);
        copiar(fatura, entity);
        return paraDominio(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Fatura> buscarPorId(UUID id) {
        return repository.findById(id).map(this::paraDominio);
    }

    private void copiar(Fatura f, FaturaJpaEntity e) {
        e.setId(f.getId());
        e.setPedidoId(f.getPedidoId());
        e.setClienteNome(f.getCliente().nome());
        e.setClienteEmail(f.getCliente().email());
        e.setClienteDocumento(f.getCliente().documento());
        e.getItens().clear();
        f.getItens().forEach(i -> e.getItens()
                .add(new ItemFaturaEmbeddable(i.descricao(), i.quantidade(), i.valorUnitario())));
        e.setValorTotal(f.getValorTotal());
        e.setStatus(f.getStatus());
        e.setChaveAcessoNotaFiscal(f.getChaveAcessoNotaFiscal());
        e.setCodigoBarrasBoleto(f.getCodigoBarrasBoleto());
        e.setHashEmail(f.getHashEmail());
        e.setCriadaEm(f.getCriadaEm());
        e.setAtualizadaEm(f.getAtualizadaEm());
    }

    private Fatura paraDominio(FaturaJpaEntity e) {
        return Fatura.restaurar(
                e.getId(),
                e.getPedidoId(),
                new Cliente(e.getClienteNome(), e.getClienteEmail(), e.getClienteDocumento()),
                e.getItens().stream()
                        .map(i -> new ItemPedido(i.getDescricao(), i.getQuantidade(), i.getValorUnitario()))
                        .toList(),
                e.getValorTotal(),
                e.getStatus(),
                e.getChaveAcessoNotaFiscal(),
                e.getCodigoBarrasBoleto(),
                e.getHashEmail(),
                e.getCriadaEm(),
                e.getAtualizadaEm());
    }
}
