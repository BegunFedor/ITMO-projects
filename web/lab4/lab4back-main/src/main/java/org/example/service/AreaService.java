package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import org.example.dao.AreaRepository;
import org.example.entity.Area;
import org.example.entity.User;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Stateless
public class AreaService {

    @EJB
    private AreaRepository areaRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> createArea(User owner, String name, Object schema) {
        String schemaJson;
        try {
            schemaJson = objectMapper.writeValueAsString(schema);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Не удалось сериализовать схему области", e);
        }

        Area area = new Area();
        area.setUser(owner);
        area.setName(name);
        area.setSchemaJson(schemaJson);
        area.setCreatedAt(LocalDateTime.now());

        Area saved = areaRepository.save(area);
        return toDto(saved);
    }

    public List<Map<String, Object>> getAllAreas() {
        List<Area> areas = areaRepository.findAll();
        return areas.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getAreaById(Long id) {
        Area area = areaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Область с id " + id + " не найдена"));
        return toDto(area);
    }

    public void deleteAreaForUser(Long id, User user) {
        Area area = areaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Область с id " + id + " не найдена"));

        if (!area.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Вы не можете удалить область другого пользователя");
        }

        areaRepository.delete(id);
    }

    public List<Map<String, Object>> getAreasForUser(User user) {
        return getAllAreas();
    }

    public Map<String, Object> getAreaForUser(Long id, User user) {
        return getAreaById(id);
    }

    private Map<String, Object> toDto(Area area) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", area.getId());
        dto.put("name", area.getName());
        dto.put("createdAt", area.getCreatedAt());
        dto.put("ownerId", area.getUser().getId());

        try {
            Object schemaObject = objectMapper.readValue(area.getSchemaJson(), Object.class);
            dto.put("schema", schemaObject);
        } catch (Exception e) {
            dto.put("schema", null);
        }

        return dto;
    }
}