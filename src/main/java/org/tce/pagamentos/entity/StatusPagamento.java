package org.tce.pagamentos.entity;

public enum StatusPagamento {

    PENDENTE,
    AUTORIZADO,
    PROCESSANDO,
    CONCLUIDO,
    ERRO_AUTORIZACAO,
    CANCELADO
}