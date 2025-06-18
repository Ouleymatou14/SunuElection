package com.sunelection.controller;

import com.sunelection.service.CryptoService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Expose la clé publique RSA utilisée pour chiffrer les bulletins côté client.
 * Accessible après authentification (JWT) ; si vous souhaitez la rendre publique,
 * ajustez la configuration Spring Security.
 */
@RestController
@RequestMapping("/crypto")
@CrossOrigin(origins = "*")
public class CryptoController {

    private final CryptoService cryptoService;

    public CryptoController(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    @GetMapping("/public-key")
    public Map<String, String> getPublicKey() {
        return Map.of("publicKey", cryptoService.getPublicKey());
    }
}
