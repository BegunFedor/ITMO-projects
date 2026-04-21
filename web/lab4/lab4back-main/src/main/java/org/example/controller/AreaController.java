package org.example.controller;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.example.dao.UserRepository;
import org.example.dto.ErrorResponse;
import org.example.entity.User;
import org.example.exception.UserNotFoundException;
import org.example.service.AreaService;
import org.example.tools.JwtSecurityContextInterface;

import java.util.List;
import java.util.Map;

@Path("/area")
@Stateless
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AreaController {

    @EJB
    private AreaService areaService;

    @EJB
    private UserRepository userRepository;

    private void ensurePremium(SecurityContext securityContext) {
        if (securityContext == null || !securityContext.isUserInRole("PREMIUM")) {
            throw new WebApplicationException(
                    "Только премиум-пользователи могут работать с областями",
                    Response.Status.FORBIDDEN
            );
        }
    }


    @POST
    @Path("/create")
    public Response createArea(Map<String, Object> body,
                               @Context SecurityContext securityContext) {
        ensurePremium(securityContext);

        try {
            User user = getCurrentUser(securityContext);

            String name = (String) body.get("name");
            Object schema = body.get("schema");

            if (name == null || name.isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Название области не может быть пустым"))
                        .build();
            }
            if (schema == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Поле schema обязательно"))
                        .build();
            }

            Map<String, Object> savedArea = areaService.createArea(user, name, schema);
            return Response.status(Response.Status.CREATED).entity(savedArea).build();

        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(ex.getMessage()))
                    .build();
        }
    }


    @GET
    @Path("/schema")
    public Response getAllAreas(@Context SecurityContext securityContext) {
        try {
            List<Map<String, Object>> areas = areaService.getAllAreas();
            return Response.ok(areas).build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(ex.getMessage()))
                    .build();
        }
    }


    @GET
    @Path("/schema/{id}")
    public Response getAreaById(@PathParam("id") Long id,
                                @Context SecurityContext securityContext) {
        try {
            Map<String, Object> area = areaService.getAreaById(id);
            return Response.ok(area).build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(ex.getMessage()))
                    .build();
        }
    }


    @DELETE
    @Path("/delete/{id}")
    public Response deleteAreaById(@PathParam("id") Long id,
                                   @Context SecurityContext securityContext) {
        ensurePremium(securityContext);
        try {
            User user = getCurrentUser(securityContext);
            areaService.deleteAreaForUser(id, user); // внутри проверяешь, что user – владелец
            return Response.noContent().build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(ex.getMessage()))
                    .build();
        }
    }

    private User getCurrentUser(SecurityContext securityContext) {
        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

        var principal = securityContext.getUserPrincipal();

        if (!(principal instanceof JwtSecurityContextInterface jwt)) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

        Long userId = jwt.getUserId();
        String email = jwt.getEmail();

        return userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byEmail(email));
    }
}