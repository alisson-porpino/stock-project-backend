package com.stockproject.stock.resource;

import com.stockproject.stock.dto.ProductMaterialDTO;
import com.stockproject.stock.service.ProductMaterialService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/api/products/{productId}/materials")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductMaterialResource {

    @Inject
    ProductMaterialService productMaterialService;

    @GET
    public List<ProductMaterialDTO> listByProduct(@PathParam("productId") Long productId) {
        return productMaterialService.listByProduct(productId);
    }

    @POST
    public Response addMaterial(
            @PathParam("productId") Long productId,
            @Valid ProductMaterialDTO dto) {
        ProductMaterialDTO created = productMaterialService.addMaterial(productId, dto);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{materialId}")
    public ProductMaterialDTO updateQuantity(
            @PathParam("productId") Long productId,
            @PathParam("materialId") Long materialId,
            @Valid ProductMaterialDTO dto) {
        return productMaterialService.updateQuantity(productId, materialId, dto);
    }

    @DELETE
    @Path("/{materialId}")
    public Response removeMaterial(
            @PathParam("productId") Long productId,
            @PathParam("materialId") Long materialId) {
        productMaterialService.removeMaterial(productId, materialId);
        return Response.noContent().build();
    }
}
