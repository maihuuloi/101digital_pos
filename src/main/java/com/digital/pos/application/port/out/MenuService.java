package com.digital.pos.application.port.out;

import com.digital.pos.domain.model.MenuItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public interface MenuService {


  Set<MenuItem> getAvailableItemIds(@NotNull @Valid UUID shopId);
}
