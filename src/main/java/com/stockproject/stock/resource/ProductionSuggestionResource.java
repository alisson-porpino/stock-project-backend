package com.stockproject.stock.resource;

import com.stockproject.stock.dto.ProductionSuggestionDTO;
import com.stockproject.stock.service.ProductionSuggestionService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/api/production-suggestions")
@Produces(MediaType.APPLICATION_JSON)
public class ProductionSuggestionResource {

    @Inject
    ProductionSuggestionService suggestionService;

    @GET
    public ProductionSuggestionDTO getSuggestion() {
        return suggestionService.calculateSuggestion();
    }
}
