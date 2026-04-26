package com.flacofitness.app.controller;

import com.flacofitness.app.service.GlobalSearchService;
import com.flacofitness.app.security.AccessSessionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping
public class BusquedaController {

    private final GlobalSearchService globalSearchService;
    private final AccessSessionService accessSessionService;

    public BusquedaController(GlobalSearchService globalSearchService,
                              AccessSessionService accessSessionService) {
        this.globalSearchService = globalSearchService;
        this.accessSessionService = accessSessionService;
    }

    @GetMapping("/busqueda")
    public String searchPage(@RequestParam(name = "q", required = false) String query,
                             Model model,
                             HttpSession session) {
        var profile = accessSessionService.getCurrentProfile(session);
        model.addAttribute("searchResult", filterByProfile(globalSearchService.search(query), profile));
        model.addAttribute("query", query == null ? "" : query.trim());
        return "busqueda/index";
    }

    @GetMapping("/api/busqueda/global")
    @ResponseBody
    public ResponseEntity<?> searchApi(@RequestParam(name = "q", required = false) String query,
                                       HttpSession session) {
        var profile = accessSessionService.getCurrentProfile(session);
        return ResponseEntity.ok(filterByProfile(globalSearchService.search(query), profile));
    }

    private com.flacofitness.app.model.dto.GlobalSearchResponse filterByProfile(
            com.flacofitness.app.model.dto.GlobalSearchResponse result,
            com.flacofitness.app.security.AccessProfile profile) {
        var groups = result.groups().stream()
                .map(group -> new com.flacofitness.app.model.dto.GlobalSearchGroupItem(
                        group.label(),
                        group.items().stream()
                                .filter(item -> profile.canAccess(item.url(), "GET"))
                                .toList()))
                .filter(group -> !group.items().isEmpty())
                .toList();
        int total = groups.stream().mapToInt(group -> group.items().size()).sum();
        return new com.flacofitness.app.model.dto.GlobalSearchResponse(result.query(), groups, total);
    }
}
