package com.dw.backend.doablewellbeingbackend.domain.dashboard;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UpdatePlacementsRequest(
        @NotEmpty @Valid List<PlacementUpsertRequest> placements
) {}