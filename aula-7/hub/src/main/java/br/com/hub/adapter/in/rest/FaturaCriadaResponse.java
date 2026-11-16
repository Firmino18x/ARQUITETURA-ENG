package br.com.hub.adapter.in.rest;

import java.util.UUID;

/** Resposta do POST /faturas: o ID da fatura (RF01). */
public record FaturaCriadaResponse(UUID id) {
}
