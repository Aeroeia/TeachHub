package com.teachub.api.client.course;

import com.teachub.api.dto.course.SubjectDTO;
import java.util.List;

public interface SubjectClient {

    List<SubjectDTO> queryByIds(Iterable<Long> ids);
}
