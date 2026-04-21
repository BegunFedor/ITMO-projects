package org.example.controller;


import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.dto.*;
import org.example.entity.RefreshToken;
import org.example.entity.User;
import org.example.exception.*;
import org.example.service.AuthService;
import org.example.service.JWTService;
import org.example.service.RefreshTokenService;

import java.time.LocalDateTime;
import java.util.Map;

@Path("/auth")
@Stateless
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthController {

    @EJB
    private AuthService authService;

    @EJB
    private JWTService jwtService;

    @EJB
    private RefreshTokenService refreshTokenService;

    @POST
    @Path("login")
    public Response login(LoginRequest loginRequest){
        try{
            AuthResponse authResponse = authService.authUser(loginRequest.getEmail(), loginRequest.getPassword());
            return Response.ok(authResponse).build();
        }
        catch (InvalidPassword ex){
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(ex)
                    .build();
        }
        catch (UserNotFoundException ex){
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ex)
                    .build();
        }

        catch (Exception ex){
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("message", ex.getMessage()))
                    .type("application/json")
                    .build();
        }
    }

    @POST
    @Path("register")
    public Response register(RegisterRequest registerRequest){
        try{
            RegisterResponse registerResponse = authService.registerUser(registerRequest.getEmail(), registerRequest.getPassword(), registerRequest.getRole());
            return Response.ok(registerResponse).build();
        }
        catch(NameAlreadyExistsException ex){
            return Response.status(Response.Status.CONFLICT)
                    .entity(ex)
                    .build();
        }
    }


    @POST
    @Path("/logout")
    public Response logout(LogoutRequest request) {
        try{
            authService.logout(request.getRefreshToken());
            return Response.ok(new MessageResponse("Logged out successfully")).build();
        }
        catch (Exception e){
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Не удалось выйти"))
                    .build();
        }
    }

    @POST
    @Path("/refresh")
    public Response refreshTokens(RefreshRequest request) {
        try {
            RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken())
                    .orElseThrow(() -> new InvalidTokenException("Токен не найден"));

            if (refreshToken.isRevoked() || refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
                throw new InvalidTokenException("Токен просрочен");
            }

            User user = refreshToken.getUser();

            String newAccessToken = jwtService.generateAccessToken(user);
            String newRefreshToken = refreshTokenService.createRefreshToken(user).getToken();

            refreshTokenService.revokeToken(request.getRefreshToken());

            AuthResponse authResponse = AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .build();

            return Response.ok(authResponse).build();

        } catch (InvalidTokenException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("Invalid refresh token"))
                    .build();
        }
    }
}
