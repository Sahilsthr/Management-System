package com.furniture.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.furniture.entity.ClientAccess;
import com.furniture.entity.Project;

import com.furniture.repository.ClientAccessRepository;

@Service
public class ClientViewService {
    private final ClientAccessService clientAccessService;
    private final ClientAccessRepository clientAccessRepository;
    private final SiteService siteService;
    private final PaymentService paymentService;
    private final WorkTaskService workTaskService;

    public ClientViewService(ClientAccessRepository clientAccessRepository,
            SiteService siteService,
            PaymentService paymentService,
            WorkTaskService workTaskService, ClientAccessService clientAccessService) {

        this.clientAccessRepository = clientAccessRepository;
        this.siteService = siteService;
        this.paymentService = paymentService;
        this.workTaskService = workTaskService;
        this.clientAccessService = clientAccessService;
    }

    public Optional<Map<String, Object>> getClientView(String token) {
        Optional<ClientAccess> accessOpt = clientAccessService.validateToken(token);
        if(accessOpt.isEmpty()){
            return Optional.empty();
        }
        Project project = accessOpt.get().getProject();
        Long projectId = project.getId();
        Map<String,Object> view = new LinkedHashMap<>();
        view.put("projectName", project.getProjectName());
        view.put("description", project.getDescription());
        view.put("startDate", project.getStartDate());
        view.put("expectedEndDate", project.getExpectedEndDate());
        view.put("projectStatus", project.getProjectStatus());

        return Optional.of(view);
    }
}
