package com.furniture.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.furniture.dto.ClientAccessCreateResponse;
import com.furniture.dto.ClientAccessResponse;
import com.furniture.service.ClientAccessService;
import com.furniture.service.ClientViewService;

@RestController
@RequestMapping
public class ClientAccessController {

    private final ClientAccessService clientAccessService;
    private final ClientViewService clientViewService;

    public ClientAccessController(
            ClientAccessService clientAccessService,
            ClientViewService clientViewService) {
        this.clientAccessService = clientAccessService;
        this.clientViewService = clientViewService;
    }

    @PostMapping("/api/projects/{projectId}/client-access")
    public ClientAccessCreateResponse createAccess(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "30") int validDays) {
        return clientAccessService.createAccess(projectId, validDays);
    }

    @GetMapping("/api/projects/{projectId}/client-access")
    public List<ClientAccessResponse> getAccessByProject(
            @PathVariable Long projectId) {
        return clientAccessService.getAccessByProject(projectId);
    }

    @PutMapping("/api/client-access/{id}/revoke")
    public ClientAccessResponse revokeToken(@PathVariable Long id) {
        return clientAccessService.revokeToken(id);
    }

    @GetMapping("/api/client-view/{token}")
    public ResponseEntity<Map<String, Object>> getClientView(
            @PathVariable String token) {
        var view = clientViewService.getClientView(token);
        if (view.isEmpty()) {
            return ResponseEntity.status(404)
                    .cacheControl(CacheControl.noStore())
                    .header("Referrer-Policy", "no-referrer")
                    .build();
        }

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header("Referrer-Policy", "no-referrer")
                .body(view.get());
    }
}
