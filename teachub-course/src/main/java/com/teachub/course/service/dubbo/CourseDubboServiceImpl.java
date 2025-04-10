package com.teachub.course.service.dubbo;

import com.teachub.api.client.course.CourseClient;
import com.teachub.api.dto.course.*;
import com.teachub.common.utils.BeanUtils;
import com.teachub.common.utils.CollUtils;
import com.teachub.course.domain.dto.CourseSimpleInfoListDTO;
import com.teachub.course.service.ICourseCatalogueService;
import com.teachub.course.service.ICourseService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@DubboService
public class CourseDubboServiceImpl implements CourseClient {

    @Autowired
    private ICourseService courseService;
    @Autowired
    private ICourseCatalogueService courseCatalogueService;

    @Override
    public List<SubNumAndCourseNumDTO> infoByTeacherIds(Iterable<Long> teacherIds) {
        if (teacherIds == null) {
            return new ArrayList<>();
        }
        List<Long> ids = new ArrayList<>();
        teacherIds.forEach(ids::add);
        if (CollUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        return courseService.countSubjectNumAndCourseNumOfTeacher(ids);
    }

    @Override
    public SectionInfoDTO sectionInfo(Long sectionId) {
        return courseCatalogueService.getSimpleSectionInfo(sectionId);
    }

    @Override
    public List<MediaQuoteDTO> mediaUserInfo(Iterable<Long> mediaIds) {
        List<Long> ids = new ArrayList<>();
        if(mediaIds != null) mediaIds.forEach(ids::add);
        return courseCatalogueService.countMediaUserInfo(ids);
    }

    @Override
    public CourseSearchDTO getSearchInfo(Long id) {
        CourseDTO courseDTO = courseService.getCourseDTOById(id);
        if (courseDTO == null) {
            return null;
        }
        return BeanUtils.copyBean(courseDTO, CourseSearchDTO.class);
    }

    @Override
    public List<CourseSimpleInfoDTO> getSimpleInfoList(Iterable<Long> ids) {
        CourseSimpleInfoListDTO dto = new CourseSimpleInfoListDTO();
        List<Long> idList = new ArrayList<>();
        if(ids != null) ids.forEach(idList::add);
        dto.setIds(idList);
        return courseService.getSimpleInfoList(dto);
    }

    @Override
    public CourseFullInfoDTO getCourseInfoById(Long id, boolean withCatalogue, boolean withTeachers) {
        return courseService.getInfoById(id, withCatalogue, withTeachers);
    }
}
