package br.com.hub.adapter.in.rest;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.hub.application.port.in.ConsultarFaturaUseCase;
import br.com.hub.application.port.in.FaturarPedidoUseCase;

/** Adaptador de entrada REST: traduz HTTP/JSON para as portas de entrada. */
@RestController
@RequestMapping("/faturas")
public class FaturaController {

    private final FaturarPedidoUseCase faturarPedido;
    private final ConsultarFaturaUseCase consultarFatura;

    public FaturaController(FaturarPedidoUseCase faturarPedido, ConsultarFaturaUseCase consultarFatura) {
        this.faturarPedido = faturarPedido;
        this.consultarFatura = consultarFatura;
    }

    @PostMapping
    public ResponseEntity<FaturaCriadaResponse> faturar(@RequestBody PedidoRequest request) {
        UUID id = faturarPedido.faturar(request.paraDominio());
        return ResponseEntity.created(URI.create("/faturas/" + id)).body(new FaturaCriadaResponse(id));
    }

    @GetMapping("/{id}")
    public FaturaResponse consultar(@PathVariable UUID id) {
        return FaturaResponse.de(consultarFatura.consultar(id));
    }
}
