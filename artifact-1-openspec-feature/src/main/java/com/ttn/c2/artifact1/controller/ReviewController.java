package com.ttn.c2.artifact1.controller;

import com.ttn.c2.artifact1.model.DiffInput;
import com.ttn.c2.artifact1.model.ReviewSummary;
import com.ttn.c2.artifact1.service.ReviewSummaryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/artifact1")
public class ReviewController {

    private final ReviewSummaryService service;

    public ReviewController(ReviewSummaryService service) {
        this.service = service;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("tasks", service.getSpecTasks());
        model.addAttribute("traceability", service.getTraceability());
        return "artifact1/index";
    }

    @GetMapping("/spec")
    public String spec(Model model) {
        model.addAttribute("tasks", service.getSpecTasks());
        model.addAttribute("standards", service.getCodingStandards());
        model.addAttribute("promptTemplate", service.getPromptTemplate());
        model.addAttribute("selfCritiquePrompt", service.getSelfCritiquePrompt());
        return "artifact1/spec";
    }

    @GetMapping("/demo")
    public String demo(Model model) {
        model.addAttribute("diffInput", new DiffInput());
        return "artifact1/demo";
    }

    @PostMapping("/generate")
    public String generate(@ModelAttribute DiffInput diffInput, Model model) {
        try {
            ReviewSummary summary = service.generate(diffInput);
            model.addAttribute("summary", summary);
            model.addAttribute("diffInput", diffInput);
            return "artifact1/result";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("diffInput", diffInput);
            return "artifact1/demo";
        }
    }

    @GetMapping("/traceability")
    public String traceability(Model model) {
        model.addAttribute("traceability", service.getTraceability());
        return "artifact1/traceability";
    }
}
