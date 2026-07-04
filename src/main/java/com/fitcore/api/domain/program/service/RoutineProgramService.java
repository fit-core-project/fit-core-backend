package com.fitcore.api.domain.program.service;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fitcore.api.domain.program.entity.RoutineProgramCompletionEventEntity;
import com.fitcore.api.domain.program.entity.RoutineProgramEntity;
import com.fitcore.api.domain.program.entity.RoutineProgramItemEntity;
import com.fitcore.api.domain.program.entity.RoutineProgramStatus;
import com.fitcore.api.domain.program.exception.ProgramException;
import com.fitcore.api.domain.program.repository.RoutineProgramCompletionEventRepository;
import com.fitcore.api.domain.program.repository.RoutineProgramItemRepository;
import com.fitcore.api.domain.program.repository.RoutineProgramRepository;
import com.fitcore.api.domain.program.request.ProgramCreateRequest;
import com.fitcore.api.domain.program.response.ProgramDetailResponse;
import com.fitcore.api.domain.program.response.ProgramItemResponse;
import com.fitcore.api.domain.program.response.ProgramSummaryResponse;
import com.fitcore.api.domain.routine.dto.FinalRoutinePayload;
import com.fitcore.api.domain.routine.entity.RoutineFinalEntity;
import com.fitcore.api.domain.routine.repository.RoutineFinalRepository;
import com.fitcore.api.domain.workout.entity.WorkoutSessionEntity;
import com.fitcore.api.global.common.util.SecurityUtils;
import com.fitcore.api.global.error.ErrorCode;
import com.fitcore.api.global.error.exception.BusinessException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoutineProgramService {
    private final SecurityUtils securityUtils;
    private final RoutineProgramRepository programRepository;
    private final RoutineProgramItemRepository itemRepository;
    private final RoutineProgramCompletionEventRepository eventRepository;
    private final RoutineFinalRepository routineFinalRepository;

    @Transactional
    public ProgramDetailResponse createProgram(ProgramCreateRequest request) {
        String userId = securityUtils.getCurrentUserId();
        validateCreateRequest(request, userId);

        RoutineProgramEntity program;
        try {
            program = programRepository.saveAndFlush(RoutineProgramEntity.builder()
                .userId(userId)
                .name(request.getName().trim())
                .status(RoutineProgramStatus.ACTIVE)
                .currentPosition(1)
                .build());
        } catch (DataIntegrityViolationException e) {
            throw ProgramException.conflict("Active program already exists.");
        }

        Map<String, RoutineFinalEntity> routinesById = loadOwnedRoutines(request.getRoutineFinalIds(), userId);
        for (int i = 0; i < request.getRoutineFinalIds().size(); i++) {
            RoutineFinalEntity routine = routinesById.get(request.getRoutineFinalIds().get(i));
            itemRepository.save(RoutineProgramItemEntity.builder()
                .program(program)
                .position(i + 1)
                .routineFinal(routine)
                .titleSnapshot(resolveTitle(routine))
                .build());
        }

        return getProgram(program.getId());
    }

    public Optional<ProgramSummaryResponse> getActiveProgram() {
        String userId = securityUtils.getCurrentUserId();
        return programRepository.findByUserIdAndStatus(userId, RoutineProgramStatus.ACTIVE)
            .map(this::toSummaryResponse);
    }

    public ProgramDetailResponse getProgram(String programId) {
        String userId = securityUtils.getCurrentUserId();
        RoutineProgramEntity program = getOwnedProgram(programId, userId);
        return toDetailResponse(program);
    }

    public List<ProgramSummaryResponse> getPrograms() {
        String userId = securityUtils.getCurrentUserId();
        return programRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
            .map(this::toSummaryResponse)
            .toList();
    }

    @Transactional
    public ProgramDetailResponse archiveProgram(String programId) {
        String userId = securityUtils.getCurrentUserId();
        RoutineProgramEntity program = getOwnedProgram(programId, userId);
        if (program.getStatus() == RoutineProgramStatus.ARCHIVED) {
            return toDetailResponse(program);
        }
        program.archive();
        return toDetailResponse(program);
    }

    @Transactional
    public void completeCurrentItemAfterWorkout(
        String programId,
        String programItemId,
        String sourceRoutineFinalId,
        WorkoutSessionEntity savedSession,
        String userId
    ) {
        if (programId == null && programItemId == null) {
            return;
        }
        if (isBlank(programId) || isBlank(programItemId)) {
            throw ProgramException.badRequest("programId and programItemId must be provided together.");
        }
        if (eventRepository.existsByWorkoutSession_Id(savedSession.getId())) {
            return;
        }

        RoutineProgramEntity program = getOwnedProgram(programId, userId);
        if (program.getStatus() != RoutineProgramStatus.ACTIVE) {
            throw ProgramException.conflict("Only active programs can advance.");
        }

        RoutineProgramItemEntity currentItem = itemRepository
            .findByProgram_IdAndPosition(program.getId(), program.getCurrentPosition())
            .orElseThrow(() -> ProgramException.conflict("Current program item does not exist."));

        if (!currentItem.getId().equals(programItemId)) {
            throw ProgramException.conflict("Requested program item is not current.");
        }
        if (isBlank(sourceRoutineFinalId)) {
            throw ProgramException.badRequest("sourceRoutineFinalId is required for program workout saves.");
        }
        if (!currentItem.getRoutineFinal().getId().equals(sourceRoutineFinalId)) {
            throw ProgramException.conflict("Workout routine does not match current program item.");
        }

        eventRepository.save(RoutineProgramCompletionEventEntity.builder()
            .program(program)
            .programItem(currentItem)
            .workoutSession(savedSession)
            .completedPosition(currentItem.getPosition())
            .build());

        long totalItems = itemRepository.countByProgram_Id(program.getId());
        if (program.getCurrentPosition() < totalItems) {
            program.advance();
        } else {
            program.complete(LocalDateTime.now());
        }
    }

    private void validateCreateRequest(ProgramCreateRequest request, String userId) {
        List<String> routineFinalIds = request.getRoutineFinalIds();
        if (routineFinalIds == null || routineFinalIds.size() < 2 || routineFinalIds.size() > 20) {
            throw ProgramException.badRequest("routineFinalIds must contain 2 to 20 items.");
        }
        if (routineFinalIds.stream().anyMatch(this::isBlank)) {
            throw ProgramException.badRequest("routineFinalIds must not contain blank ids.");
        }
        if (new HashSet<>(routineFinalIds).size() != routineFinalIds.size()) {
            throw ProgramException.badRequest("Duplicate routineFinalIds are not allowed.");
        }
        if (programRepository.existsByUserIdAndStatus(userId, RoutineProgramStatus.ACTIVE)) {
            throw ProgramException.conflict("Active program already exists.");
        }
        loadOwnedRoutines(routineFinalIds, userId);
    }

    private Map<String, RoutineFinalEntity> loadOwnedRoutines(List<String> routineFinalIds, String userId) {
        List<RoutineFinalEntity> routines = routineFinalRepository.findAllById(routineFinalIds);
        if (routines.size() != routineFinalIds.size()) {
            throw new BusinessException(ErrorCode.ROUTINE_NOT_FOUND);
        }

        Map<String, RoutineFinalEntity> routinesById = new LinkedHashMap<>();
        for (RoutineFinalEntity routine : routines) {
            if (!routine.getUserId().equals(userId)) {
                throw new BusinessException(ErrorCode.ACCESS_DENIED);
            }
            routinesById.put(routine.getId(), routine);
        }
        return routinesById;
    }

    private RoutineProgramEntity getOwnedProgram(String programId, String userId) {
        RoutineProgramEntity program = programRepository.findById(programId)
            .orElseThrow(ProgramException::notFound);
        if (!program.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        return program;
    }

    private ProgramSummaryResponse toSummaryResponse(RoutineProgramEntity program) {
        List<RoutineProgramItemEntity> items = itemRepository.findByProgram_IdOrderByPositionAsc(program.getId());
        ProgramItemResponse nextItem = resolveNextItem(program, items);
        return ProgramSummaryResponse.builder()
            .programId(program.getId())
            .name(program.getName())
            .status(program.getStatus().name())
            .currentPosition(program.getCurrentPosition())
            .totalItems(items.size())
            .completedAt(program.getCompletedAt())
            .nextItem(nextItem)
            .build();
    }

    private ProgramDetailResponse toDetailResponse(RoutineProgramEntity program) {
        List<RoutineProgramItemEntity> items = itemRepository.findByProgram_IdOrderByPositionAsc(program.getId());
        Set<String> doneItemIds = eventRepository.findByProgram_Id(program.getId()).stream()
            .map(event -> event.getProgramItem().getId())
            .collect(java.util.stream.Collectors.toSet());

        List<ProgramItemResponse> itemResponses = items.stream()
            .map(item -> toItemResponse(item, resolveState(program, item, doneItemIds)))
            .toList();

        return ProgramDetailResponse.builder()
            .programId(program.getId())
            .name(program.getName())
            .status(program.getStatus().name())
            .currentPosition(program.getCurrentPosition())
            .totalItems(items.size())
            .completedAt(program.getCompletedAt())
            .nextItem(resolveNextItem(program, items))
            .items(itemResponses)
            .build();
    }

    private ProgramItemResponse resolveNextItem(RoutineProgramEntity program, List<RoutineProgramItemEntity> items) {
        if (program.getStatus() != RoutineProgramStatus.ACTIVE) {
            return null;
        }
        return items.stream()
            .filter(item -> item.getPosition() == program.getCurrentPosition())
            .findFirst()
            .map(item -> toItemResponse(item, "CURRENT"))
            .orElse(null);
    }

    private String resolveState(RoutineProgramEntity program, RoutineProgramItemEntity item, Set<String> doneItemIds) {
        if (program.getStatus() == RoutineProgramStatus.COMPLETED || doneItemIds.contains(item.getId())) {
            return "DONE";
        }
        if (item.getPosition() < program.getCurrentPosition()) {
            return "DONE";
        }
        if (program.getStatus() == RoutineProgramStatus.ACTIVE && item.getPosition() == program.getCurrentPosition()) {
            return "CURRENT";
        }
        return "UPCOMING";
    }

    private ProgramItemResponse toItemResponse(RoutineProgramItemEntity item, String state) {
        RoutineFinalEntity routine = item.getRoutineFinal();
        FinalRoutinePayload payload = routine.getFinalRoutinePayload();
        Integer estimatedTime = payload != null && payload.getTotalEstimatedTime() > 0
            ? payload.getTotalEstimatedTime()
            : null;
        return ProgramItemResponse.builder()
            .programItemId(item.getId())
            .position(item.getPosition())
            .routineFinalId(routine.getId())
            .title(resolveTitle(item))
            .targetSplitLabel(routine.getTargetSplitLabel())
            .estimatedTime(estimatedTime)
            .state(state)
            .build();
    }

    private String resolveTitle(RoutineProgramItemEntity item) {
        if (!isBlank(item.getTitleSnapshot())) {
            return item.getTitleSnapshot();
        }
        return resolveTitle(item.getRoutineFinal());
    }

    private String resolveTitle(RoutineFinalEntity routine) {
        FinalRoutinePayload payload = routine.getFinalRoutinePayload();
        if (payload != null && !isBlank(payload.getSummaryTitle())) {
            return payload.getSummaryTitle();
        }
        if (!isBlank(routine.getTargetSplitLabel())) {
            return routine.getTargetSplitLabel();
        }
        return "Saved Routine";
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
