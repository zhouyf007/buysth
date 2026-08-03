package com.shop.api.feign;

import com.shop.api.dto.UserDTO;
import com.shop.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service")
public interface AuthClient {

    @GetMapping("/internal/users/{userId}")
    Result<UserDTO> getUser(@PathVariable("userId") Long userId);
}

