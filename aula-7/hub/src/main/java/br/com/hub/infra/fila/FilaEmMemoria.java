package br.com.hub.infra.fila;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;

/**
 * Broker de mensagens simulado (no lugar de RabbitMQ, SQS, Kafka...).
 * Cada fila é uma BlockingQueue e cada consumidor roda numa thread própria,
 * então quem publica não espera o processamento: a comunicação é assíncrona.
 */
@Component
public class FilaEmMemoria {

    private static final Logger log = LoggerFactory.getLogger(FilaEmMemoria.class);

    private final Map<String, BlockingQueue<Object>> filas = new ConcurrentHashMap<>();
    private final List<Thread> consumidores = new CopyOnWriteArrayList<>();

    public void publicar(String fila, Object mensagem) {
        log.info("[FILA {}] mensagem publicada: {}", fila, mensagem);
        fila(fila).add(mensagem);
    }

    public <T> void consumir(String fila, Class<T> tipo, Consumer<T> consumidor) {
        Thread thread = Thread.ofVirtual().name("consumidor-" + fila).start(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Object mensagem = fila(fila).take();
                    log.info("[FILA {}] mensagem consumida: {}", fila, mensagem);
                    consumidor.accept(tipo.cast(mensagem));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (RuntimeException e) {
                    // Num broker real a mensagem iria para retentativa ou para uma DLQ
                    log.error("[FILA {}] erro ao processar mensagem: {}", fila, e.getMessage(), e);
                }
            }
        });
        consumidores.add(thread);
    }

    private BlockingQueue<Object> fila(String nome) {
        return filas.computeIfAbsent(nome, n -> new LinkedBlockingQueue<>());
    }

    @PreDestroy
    public void encerrar() {
        consumidores.forEach(Thread::interrupt);
    }
}
