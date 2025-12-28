package com.streetfoodgo.core.port.impl.dto;

/**
 * SendSmsRequest DTO.
 */
public record SendSmsRequest(String e164, String content) {}
