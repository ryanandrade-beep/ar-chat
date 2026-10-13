package br.com.archat.dominio.conversa;

public enum TipoDestinatario {
    /** Pessoa, identificada pelo telefone com DDD (e DDI, opcional). */
    CONTATO,
    /** Grupo do WhatsApp, identificado pelo ID do grupo (ex.: 120363019502650977-group). */
    GRUPO
}
