package com.furniture.controller;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.furniture.entity.ClientAccess;
import com.furniture.service.ClientAccessService;
import com.furniture.service.ClientViewService;
import org.springframework.web.bind.annotation.*;

@RestController
public class ClientAccessController {
    private final ClientAccessService clientAccessService;
    private final ClientViewService clientViewService;
    
    public ClientAccessController(ClientAccessService clientAccessService,ClientViewService clientViewService){
        this.clientAccessService = clientAccessService;
        this.clientViewService = clientViewService;
    }

    //admin endpoints
    @PostMapping("/api/projects/{projectId}/client-access")
    public ClientAccess createAccess(@PathVariable Long projectId,@RequestParam(defaultValue="30") int validDays){
        return clientAccessService.createAccess(projectId, validDays);
    }
    @GetMapping("/api/projects/{projectId}/client-access")
    public List<ClientAccess> getAccessByProject(@PathVariable Long projectId) {
        return clientAccessService.getAccessByProject(projectId);
    }
    @PutMapping("/api/client-access/{id}/revoke")
    public ClientAccess revokeToken(@PathVariable Long id){
        return clientAccessService.revokeToken(id);
    }
    //public endpoints(for the client)
    @GetMapping("api/client-view/{token}")
    public ResponseEntity<Map<String,Object>> getClientView(@PathVariable String token){
        return clientViewService.getClientView(token).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
