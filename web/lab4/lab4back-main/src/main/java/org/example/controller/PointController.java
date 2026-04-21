package org.example.controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.example.dao.PointRepository;
import org.example.dao.UserRepository;
import org.example.dto.CheckRequest;
import org.example.dto.ErrorResponse;
import org.example.entity.Point;
import org.example.entity.User;
import org.example.exception.UserNotFoundException;
import org.example.exception.ValidationException;
import org.example.service.AreaCheckService;
import org.example.tools.JwtSecurityContextInterface;

import java.util.List;

@Path("/points")
@Stateless
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PointController {

    @EJB
    private AreaCheckService areaCheckService;

    @EJB
    private UserRepository userRepository;

    @EJB
    private PointRepository pointRepository;

    @POST
    @Path("/check")
    public Response checkPoint(CheckRequest request, @Context SecurityContext securityContext){

        if (securityContext == null || securityContext.getUserPrincipal() == null){
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        var principal = securityContext.getUserPrincipal();

        if (!(principal instanceof JwtSecurityContextInterface jwt)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        try {
            Long userId = jwt.getUserId();
            String email = jwt.getEmail();

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> UserNotFoundException.byEmail(email));

            Point point = areaCheckService.checkPoint(request, user);

            return Response.ok(point).build();
        }
        catch(ValidationException ex){
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(ex.getMessage()))
                    .build();
        }
        catch (Exception ex){
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(ex.getMessage()))
                    .build();
        }
    }

    @GET
    public Response getPoints(@Context SecurityContext securityContext){

        if (securityContext == null || securityContext.getUserPrincipal() == null){
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        var principal = securityContext.getUserPrincipal();

        if (!(principal instanceof JwtSecurityContextInterface jwt)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        try {
            Long userId = jwt.getUserId();

            List<Point> points = pointRepository.findByUserId(userId);

            return Response.ok(points).build();
        } catch (Exception ex){
            ex.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(ex.getMessage()))
                    .build();
        }
    }
}