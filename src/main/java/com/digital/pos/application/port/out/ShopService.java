package com.digital.pos.application.port.out;

import com.digital.pos.domain.model.ShopConfiguration;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public interface ShopService {

  boolean existsById(@NotNull @Valid UUID shopId);

  ShopConfiguration getShopConfig(UUID shopId);
}
