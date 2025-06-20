package com.teachub.api.client.course;

import com.teachub.api.dto.course.SubjectDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "course-service", path = "subjects")
public interface SubjectClient {

    @GetMapping("list")
    List<SubjectDTO> queryByIds(@RequestParam("ids") Iterable<Long> ids);
}
