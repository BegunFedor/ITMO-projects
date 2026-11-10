package ru.itmo.mkd.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.itmo.mkd.dto.request.CommentDto;
import ru.itmo.mkd.dto.request.ExecutorOptionDto;
import ru.itmo.mkd.dto.request.RatingDto;
import ru.itmo.mkd.dto.request.RequestDetailsDto;
import ru.itmo.mkd.dto.request.RequestSummaryDto;
import ru.itmo.mkd.dto.request.StatusHistoryDto;
import ru.itmo.mkd.entity.Rating;
import ru.itmo.mkd.entity.Request;
import ru.itmo.mkd.entity.RequestComment;
import ru.itmo.mkd.entity.RequestStatusHistory;
import ru.itmo.mkd.procedure.ExecutorCandidate;

import java.util.List;

@Mapper
public interface RequestMapper {

    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "houseId", source = "house.id")
    @Mapping(target = "address", source = "house.address")
    @Mapping(target = "apartmentNumber", source = "apartment.number")
    @Mapping(target = "executorName", source = "executor.fullName")
    RequestSummaryDto toSummary(Request request);

    List<RequestSummaryDto> toSummaries(List<Request> requests);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "houseId", source = "house.id")
    @Mapping(target = "address", source = "house.address")
    @Mapping(target = "companyName", source = "house.company.name")
    @Mapping(target = "apartmentId", source = "apartment.id")
    @Mapping(target = "apartmentNumber", source = "apartment.number")
    @Mapping(target = "authorName", source = "author.fullName")
    @Mapping(target = "authorPhone", source = "author.phone")
    @Mapping(target = "executorId", source = "executor.id")
    @Mapping(target = "executorName", source = "executor.fullName")
    RequestDetailsDto toDetails(Request request);

    @Mapping(target = "changedByName", source = "changedBy.fullName")
    @Mapping(target = "changedByRole", source = "changedBy.role")
    StatusHistoryDto toDto(RequestStatusHistory history);

    List<StatusHistoryDto> toHistoryDtos(List<RequestStatusHistory> history);

    @Mapping(target = "authorName", source = "author.fullName")
    @Mapping(target = "authorRole", source = "author.role")
    CommentDto toDto(RequestComment comment);

    List<CommentDto> toCommentDtos(List<RequestComment> comments);

    RatingDto toDto(Rating rating);

    @Mapping(target = "id", source = "executorId")
    ExecutorOptionDto toDto(ExecutorCandidate candidate);

    List<ExecutorOptionDto> toExecutorOptions(List<ExecutorCandidate> candidates);
}
